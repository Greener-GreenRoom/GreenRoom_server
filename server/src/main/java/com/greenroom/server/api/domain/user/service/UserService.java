package com.greenroom.server.api.domain.user.service;

import com.greenroom.server.api.domain.alram.entity.Alarm;
import com.greenroom.server.api.domain.alram.service.AlarmService;
import com.greenroom.server.api.domain.greenroom.dto.GradeUpDto;
import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.LevelIncreasingCause;
import com.greenroom.server.api.domain.greenroom.repository.GradeRepository;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.greenroom.repository.ItemRepository;
import com.greenroom.server.api.domain.greenroom.service.GradeService;
import com.greenroom.server.api.domain.greenroom.service.ItemService;
import com.greenroom.server.api.domain.user.dto.MyPageDto;
import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.exception.InvalidNameException;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ImageUploader.UserProfileImageUploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CustomUserDetailService userDetailService;
    private final UserRepository userRepository;
    private final GreenRoomRepository greenRoomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserProfileImageUploader userProfileImageUploader;
    private final GradeRepository gradeRepository;
    private final ItemService itemService;
    private final GradeService gradeService;
    private final AlarmService alarmService;

    public MyPageDto.MyPageMainResponseDto getMyPageInfo(String userEmail){

        User user = userDetailService.findUserByEmail(userEmail);
        Grade grade = user.getGrade();

        Optional<Grade> findGrade = gradeRepository.findFirstByLevelGreaterThanOrderByLevelAsc(grade.getLevel());

        int requiredSeed = 0;
        int nextLevelSeed = 0;
        int currentSeed = user.getTotalSeed();
        Grade nextGrade = null;

        if(findGrade.isPresent()){
            nextGrade = findGrade.get();
            requiredSeed = nextGrade.getRequiredSeed()-currentSeed;
            nextLevelSeed = nextGrade.getRequiredSeed();
        }

        return MyPageDto.MyPageMainResponseDto
                .builder()
                .name(user.getName())
                .gradeDto(new MyPageDto.GradeDto(grade,requiredSeed,nextLevelSeed,currentSeed))
                .daysFromCreated((int) ChronoUnit.DAYS.between(user.getCreateDate().toLocalDate(), LocalDate.now()))
                .build();
    }

    public MyPageDto.MyPageGradeResponseDto getMyGradeInfo(String userEmail){

        User user = userDetailService.findUserByEmail(userEmail);
        Grade grade = user.getGrade();

        Optional<Grade> findGrade = gradeRepository.findNextGradeHasItems(grade.getLevel());

        int requiredSeed = 0;
        int nextLevelSeed = 0;
        int nextLevelToGetItems = 0;
        int currentSeed = user.getTotalSeed();
        Grade nextGrade = null;
        List<MyPageDto.ItemDto> itemList = new ArrayList<>();

        if(findGrade.isPresent()){
            nextGrade = findGrade.get();
            requiredSeed = nextGrade.getRequiredSeed()-currentSeed;
            nextLevelSeed = nextGrade.getRequiredSeed();
            nextLevelToGetItems = nextGrade.getLevel();
            itemList = itemService
                    .getItemsByGrade(nextGrade).stream()
                    .map(MyPageDto.ItemDto::new)
                    .toList();
        }

        return MyPageDto.MyPageGradeResponseDto
                .builder()
                .gradeDto(new MyPageDto.GradeDto(grade,requiredSeed,nextLevelSeed,currentSeed))
                .itemDtoList(itemList)
                .nextLevelToGetItems(nextLevelToGetItems)
                .levelGroups(gradeService.getAllGradeList())
                .build();
    }

    @Transactional
    public User updateUser(UserDto.UpdateUserRequest userDto, String userEmail, MultipartFile imageFile) throws IOException {

        User user = userDetailService.findUserByEmail(userEmail);

        String profileUrl = user.getProfileUrl();

        if(!imageFile.isEmpty()){
            profileUrl = userProfileImageUploader.uploadUserProfileImage(imageFile);
        }

        return userRepository.save(
                user
                        .updateProfileUrl(profileUrl)
                        .updateUserName(userDto.getName())
        );
    }

    public boolean checkNameValid(String name){
        final String regex = "^[ㄱ-ㅎ가-힣a-zA-Z]{1,10}$";
        if(!Pattern.matches(regex,name)){
            throw new InvalidNameException("한글 또는 영문, 10자 이내로 가능해요.");
        }
        return true;
    }

    public MyPageDto.MyPageProfile getMyProfile(String userEmail){

        User user = userDetailService.findUserByEmail(userEmail);
        return MyPageDto.MyPageProfile.builder()
                .profileUrl(user.getProfileUrl())
                .name(user.getName())
                .build();
    }

    public MyPageDto.MyPageAlarm getMyAlarm(String userEmail){

        Alarm alarm = alarmService.findAlarmByUser(userDetailService.findUserByEmail(userEmail));

        return MyPageDto.MyPageAlarm.builder()
                .noticeAlarm(alarm.getNoticeAlarm())
                .waterCycleAlarm(alarm.getWaterCycleAlarm())
                .communityAlarm(alarm.getCommunityAlarm())
                .build();
    }

    public void setMyAlarm(String userEmail, MyPageDto.MyPageAlarm dto){
        alarmService.updateAlarm(userDetailService.findUserByEmail(userEmail),dto);;
    }

    @Transactional
    public Integer getUserLevel(String userEmail){
        return userDetailService.findUserByEmail(userEmail)
                .getGrade().getLevel();
    }

    @Transactional
    public GradeUpDto checkAttendance(String userEmail){

        User user = userDetailService.findUserByEmail(userEmail);
        ArrayList<GreenRoom> greenRooms = greenRoomRepository.findGreenRoomByUser(user);

        ///식물 등록을 한 적 없는 경우
        if(greenRooms.isEmpty()){
            return new GradeUpDto(user.getGrade().getLevel(),new HashMap<>(),false);
        }

        //식물을 등록한 적 있는 경우
        else{
            user.updateTotalSeed(1);
            user.updateWeeklySeed(1);

            Grade beforeGrade = user.getGrade();

            //level 조정
            applicationEventPublisher.publishEvent(user);

            Grade afterGrade = user.getGrade();

            HashMap<String,Integer> pointUp =  new HashMap<>();
            pointUp.put(LevelIncreasingCause.ATTENDANCE.toString().toLowerCase(),1);

            return new GradeUpDto(afterGrade.getLevel(),pointUp,beforeGrade!=afterGrade);
        }
    }
}
