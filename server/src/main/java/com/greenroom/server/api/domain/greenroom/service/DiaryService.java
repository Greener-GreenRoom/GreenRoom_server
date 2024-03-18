package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.entity.Diary;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.entity.Todo;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.greenroom.repository.DiaryRepository;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.utils.ImageUploader.DiaryImageUploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final GreenRoomRepository greenRoomRepository;
    private final DiaryImageUploader diaryImageUploader;
    private final UserRepository userRepository;

    public void postDiary(DiaryPostRequestDto diaryPostRequestDto, MultipartFile imgFile) throws IOException {

        GreenRoom greenRoom =  greenRoomRepository.findById(diaryPostRequestDto.getGreenroomId()).orElseThrow(()-> new IllegalArgumentException("해당 그린룸을 찾을 수 없음."));

        String imgUrl = null;

        if(imgFile!=null){
            try{
                imgUrl = diaryImageUploader.uploadDiaryImage(imgFile);
            }
            catch (IOException e){
                throw new IOException("다이어리 이미지 업로드 실패");
            }
        }


        LocalDate date = LocalDate.parse(diaryPostRequestDto.getDate());

        Diary diary =  Diary.builder()
                .title(diaryPostRequestDto.getTitle())
                .content(diaryPostRequestDto.getContent())
                .date(date)
                .diaryPictureUrl(imgUrl)
                .greenRoom(greenRoom).build();

        diaryRepository.save(diary);

    }

    public void deleteDiary(Long diaryId){
        diaryRepository.deleteById(diaryId);
    }


    public ArrayList<DiaryResponseDto> getDiaryList(String userEmail,Integer year, Integer month, Long greenroomId,String sort){

        Predicate<Diary> yearCheck = d->d.getDate().getYear()==year;
        Predicate<Diary> monthCheck = d->d.getDate().getMonthValue() == month;

        List<Long> greenRoomIdList = new ArrayList<>();
        if (greenroomId!=null){
            greenRoomIdList.add(greenroomId);}
        else{

            //user가 현재 키우고 있는 그린룸 리스트 받아오기.
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));
            greenRoomIdList.addAll(greenRoomRepository.findGreenRoomByUser(user).stream().filter(g->g.getStatus()== GreenRoomStatus.ENABLED).map(GreenRoom::getGreenroomId).toList());
        }

        Map<LocalDate,List<Diary>> diaries = diaryRepository.findAllByGreenRoom_GreenroomIdIn(greenRoomIdList).stream().filter(yearCheck.and(monthCheck)).collect(Collectors.groupingBy(Diary::getDate));

        List<LocalDate> dateList = new ArrayList<>(diaries.keySet().stream().toList());

        //parameter를 안넘겼거나 내림차순으로 지정한 경우 -> 최신순으로 보여주기
        if(sort==null || sort.equals("desc")){
            dateList.sort(Collections.reverseOrder());
        }
        //오래된 순을 보여주기
        else if (sort.equals("asc")){
            Collections.sort(dateList);
        }

        ArrayList<DiaryResponseDto> diaryResponseDtoArrayList = new ArrayList<>();

        for(LocalDate localDate:dateList){

            Map<GreenRoom,List<Diary>> diaryPerGreenroom  =  diaries.get(localDate).stream().collect(Collectors.groupingBy(Diary::getGreenRoom));
            List<DiaryPerGreenroomDto> diaryPerGreenroomDtos = new ArrayList<>();

            ArrayList<GreenRoom> greenRooms =  new ArrayList<>(diaryPerGreenroom.keySet().stream().toList());
            greenRooms.sort(Collections.reverseOrder(Comparator.comparingLong(GreenRoom::getGreenroomId)));

            for(GreenRoom greenRoom: greenRooms){
                List<DiaryInfoDto> diaryInfoDtoList =  diaryPerGreenroom.get(greenRoom).stream().map(d->new DiaryInfoDto(d.getDiaryId(),d.getTitle(),d.getContent(),d.getDiaryPictureUrl(),d.getDate())).toList();
                diaryPerGreenroomDtos.add(new DiaryPerGreenroomDto(greenRoom.getGreenroomId(),greenRoom.getName(),diaryInfoDtoList));
            }
            DiaryResponseDto diaryResponseDto = new DiaryResponseDto(year,month,localDate.getDayOfMonth(),localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA),diaryPerGreenroomDtos);
            diaryResponseDtoArrayList.add(diaryResponseDto);
        }
        return diaryResponseDtoArrayList;
    }

}
