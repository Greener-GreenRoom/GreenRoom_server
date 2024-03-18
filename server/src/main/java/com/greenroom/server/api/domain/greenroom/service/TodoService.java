package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.*;
import com.greenroom.server.api.domain.greenroom.entity.*;
import com.greenroom.server.api.domain.greenroom.enums.ActivityName;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.greenroom.enums.LevelIncreasingCause;
import com.greenroom.server.api.domain.greenroom.repository.*;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import com.greenroom.server.api.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoLogRepository todoLogRepository;
    private final GreenRoomRepository greenRoomRepository;
    private final ActivityRepository activityRepository;
    private final DiaryRepository diaryRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public GradeUpDto completeTodo(Long greenroomId, ArrayList<Long> activityList, String userEmail) throws UsernameNotFoundException{

        //요청을 보낸 activity에 해당하는 todo가져오기
        List<Todo> todoList = todoRepository.findAllByGreenRoom_GreenroomIdAndActivity_ActivityIdIn(greenroomId,activityList).stream().filter(t->t.getUseYn()&&t.getNextTodoDate()!=null).toList();

        User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));

        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        //완료된 미션 개수
        int activityNum=0;

        for(Todo todo : todoList){
            if(todo.getNextTodoDate()!=null){ //주기 등록을 마친 경우에만
                activityNum+=1;
                ///todo 날짜 update
                todo.updateNextTodoDate(today.plusDays(todo.getDuration()));
                todo.updateLastUpdateDate(today);

                //todo Log 생성
                TodoLog todoLog = TodoLog.builder().todo(todo).build();
                todoLogRepository.save(todoLog);
            }
        }

        //씨앗 개수 update. 미션 하나 당 2점씩 추가.
        user.updateTotalSeed(activityNum*3);
        user.updateWeeklySeed(activityNum*3);

        Grade beforeGrade = user.getGrade();
        //level 조정
        applicationEventPublisher.publishEvent(user);
        Grade afterGrade = user.getGrade();

        HashMap<String,Integer> pointUp = new HashMap<>();
        pointUp.put(LevelIncreasingCause.TODO_COMPLETION.toString().toLowerCase(),activityNum*3);

        return new GradeUpDto(user.getGrade().getLevel(),pointUp,!beforeGrade.equals(afterGrade));
    }

    public void createTodo(TodoCreationDto todoCreationDto){

        GreenRoom greenRoom = greenRoomRepository.findById(todoCreationDto.getGreenRoomId()).orElseThrow(()->new IllegalArgumentException("해당 그린룸을 찾을 수 없음."));
        Activity activity = activityRepository.findById(todoCreationDto.getActivityId()).orElseThrow(()->new IllegalArgumentException("해당 activity를 찾을 수 없음."));

        LocalDateTime nextTodoDate ;
        if(todoCreationDto.getFirstStartDate()!=null&&todoCreationDto.getDuration()!=null){
            nextTodoDate = todoCreationDto.getFirstStartDate().plusDays(todoCreationDto.getDuration());
        }
        else{nextTodoDate=null;}

        //todo 생성
        Todo todo = Todo
                .builder()
                .greenRoom(greenRoom)
                .useYn(true)
                .firstStartDate(todoCreationDto.getFirstStartDate())
                .lastUpdateDate(todoCreationDto.getFirstStartDate())
                .nextTodoDate(nextTodoDate)
                .duration(todoCreationDto.getDuration())
                .activity(activity)
                .build();
        todoRepository.save(todo);
    }

    //활성화된 주기 가져오기
    public List<ManagementCycleDto> getActiveTodoCycle(Long greenroomId){
        //현재 활성화된 주기만 가져오기
        ArrayList<Todo> todoArrayList = todoRepository.findTodoByGreenRoom_GreenroomIdAndUseYn(greenroomId,true);
        return todoArrayList.stream().map(ManagementCycleDto::from).toList();
    }


    //주기 변경하기
    public ManagementCycleDto modifyTodoCycle(Long greenroomId, TodoModifyingRequestDto todoModifyingRequestDto){

        if(todoModifyingRequestDto.getDuration()==null||todoModifyingRequestDto.getLastUpdateDate()==null||todoModifyingRequestDto.getActivityId()==null){
            throw new IllegalArgumentException("요청 값에 null을 허용하지 않음");
        }

        Todo todo = todoRepository.findByGreenRoomGreenroomIdAndActivity_ActivityId(greenroomId, todoModifyingRequestDto.getActivityId()).orElseThrow(()->new IllegalArgumentException("해당 그린룸의 todo를 찾을 수 없음."));

        //1. 주기를 변경한다
        todo.updateDuration(todoModifyingRequestDto.getDuration());

        //2. 마지막 수행 날짜를 변경한다
        LocalDateTime lastUpdateDate = LocalDate.parse(todoModifyingRequestDto.getLastUpdateDate()).atStartOfDay();
        todo.updateLastUpdateDate(lastUpdateDate);

        //3. 다음 수행 날짜를 변경한다
        todo.updateNextTodoDate(todo.getLastUpdateDate().plusDays(todo.getDuration()));

        return ManagementCycleDto.from(todo);
    }

    //주기 비활성화/활성화
    public void modifyTodoState(Long greenroomId, HashMap<String,String> patchRequest){


        ArrayList<Long> activityIdList = new ArrayList<>(patchRequest.keySet().stream().map(Long::valueOf).toList());

        //수정을 요구한 모든 activity에 대해서 수정 작업 진행
        for(Long activityId : activityIdList){

            String action = patchRequest.get(String.valueOf(activityId));

            Todo todo =  todoRepository.findByGreenRoomGreenroomIdAndActivity_ActivityId(greenroomId,activityId).orElse(null);

            //해당 할 일에 대해서 주기 활성화를 요청한 경우
            if(Objects.equals(action, "activate")){

                //처음으로 활성화 하는 경우.
                if(todo==null){
                    //주기는 설정하지 않은 채 todo 생성.
                    createTodo(new TodoCreationDto(greenroomId,null,null,activityId));
                }
                //비활성화 했던 것을 다시 활성화 하는 경우 ->  상태만 update
                else{todo.updateUseYn(true);}

            }

            // 할 일에 대해서 주기 비활성화를 요청한 경우
            else if (Objects.equals(action, "deactivate")) {
                //비활성화 할 주기가 있으면 비활성화. 비활성화 할 주기가 없으면 pass
                if(todo!=null){todo.updateUseYn(false);}
            }

        }

    }

    public Map<LocalDate,List<Todo>> getTodoFromPastToPresent(List<Long> greenRoomIdList,List<Long> activityIdList,Integer year, Integer month){

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Predicate<Todo> isNextTodoDateExist = t->t.getNextTodoDate()!=null;


        Predicate<Todo> isPassedOrPresent = t->t.getNextTodoDate().getYear()< year || (t.getNextTodoDate().getYear()==year && t.getNextTodoDate().getMonthValue()<=month);

        Map<LocalDate,List<Todo>> todoWithDate = todoRepository.findAllByGreenRoom_GreenroomIdInAndActivity_ActivityIdIn(greenRoomIdList,activityIdList).stream().filter(isNextTodoDateExist.and(isPassedOrPresent)).collect(Collectors.groupingBy(t->t.getNextTodoDate().toLocalDate()));

        List<LocalDate> todoDateList = !todoWithDate.keySet().isEmpty()? new ArrayList<>(todoWithDate.keySet().stream().toList()): new ArrayList<>();

        if(!todoDateList.isEmpty()){todoDateList.sort(Collections.reverseOrder());}

        for(LocalDate dateTemp : todoDateList){
            if(dateTemp.isBefore(today)){
                if(todoWithDate.get(today)!=null){
                    todoWithDate.get(today).addAll(todoWithDate.get(dateTemp));
                }
                else{
                    todoWithDate.put(today,todoWithDate.get(dateTemp));
                }
                todoWithDate.remove(dateTemp);
            }
        }

        Map<LocalDate,List<Todo>> todoWithDate2 = new HashMap<>();
        for(LocalDate dateTemp: todoWithDate.keySet().stream().toList()){
            for(Todo todo:todoWithDate.get(dateTemp)) {

                if (dateTemp.getMonthValue() == month) {
                    if (todoWithDate2.containsKey(dateTemp)) {
                        todoWithDate2.get(dateTemp).add(todo);
                    } else {
                        List<Todo> todoTempList = new ArrayList<>();
                        todoTempList.add(todo);
                        todoWithDate2.put(dateTemp, todoTempList);
                    }
                }

                boolean increasing = true;
                LocalDate futureDate = dateTemp;

                while (increasing) {
                    futureDate = futureDate.plusDays(todo.getDuration());

                    if (futureDate.getMonthValue() > month) {
                        increasing = false;
                    } else if (futureDate.getMonthValue() == month) {
                        Todo todoTemp = Todo.builder().nextTodoDate(futureDate.atStartOfDay()).activity(todo.getActivity()).greenRoom(todo.getGreenRoom()).duration(todo.getDuration()).useYn(todo.getUseYn()).lastUpdateDate(todo.getLastUpdateDate()).build();
                        if (todoWithDate2.containsKey(futureDate)) {
                            todoWithDate2.get(futureDate).add(todoTemp);
                        } else {
                            List<Todo> todoTempList = new ArrayList<>();
                            todoTempList.add(todoTemp);
                            todoWithDate2.put(futureDate, todoTempList);
                        }
                    }
                }
            }
        }
        return todoWithDate2;
    }


    public ArrayList<TodoPerMonthResponseDto> getTodoPerMonth(String userEmail,Integer year,Integer month,String sort,Long greenroomId,Long activityId){

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<Long> greenRoomIdList = new ArrayList<>();
        List<Long> activityIdList = new ArrayList<>();
        if (greenroomId!=null){
            greenRoomIdList.add(greenroomId);}
        else{
            User user = userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));
            greenRoomIdList.addAll(greenRoomRepository.findGreenRoomByUser(user).stream().filter(g->g.getStatus()== GreenRoomStatus.ENABLED).map(GreenRoom::getGreenroomId).toList());}

        if(activityId!=null){
            activityIdList.add(activityId);
        }
        else{
            activityIdList.addAll(activityRepository.findAll().stream().map(Activity::getActivityId).toList());
        }

        Map<LocalDate,List<TodoLog>> todoLogWithDate = todoLogRepository.findAllByTodo_GreenRoomGreenroomIdInAndTodo_Activity_ActivityIdIn(greenRoomIdList,activityIdList).stream().filter(tl->tl.getCreateDate().getYear()==year&&tl.getCreateDate().getMonthValue()==month).collect(Collectors.groupingBy(tl2->tl2.getCreateDate().toLocalDate()));
        Map<LocalDate,List<Diary>> diaryWithDate = diaryRepository.findAllByGreenRoom_GreenroomIdIn(greenRoomIdList).stream().filter(d->d.getDate().getYear()==year&&d.getDate().getMonthValue()==month).collect(Collectors.groupingBy(Diary::getDate));

        ArrayList<TodoPerMonthResponseDto> todoPerMonthResponseDtoList = new ArrayList<>();



        //과거 달을 조회 요청한 경우
        if(year<today.getYear() || (year==today.getYear()&&month<today.getMonthValue())) {


            List<LocalDate> todoLogdateList =  new ArrayList<>(todoLogWithDate.keySet().stream().toList());
            List<LocalDate> diarydateList =  new ArrayList<>(diaryWithDate.keySet().stream().toList());

            todoLogdateList.addAll(diarydateList);

            List<LocalDate> dateList = new ArrayList<>(todoLogdateList.stream().distinct().toList());
            if(Objects.equals(sort, "desc") &&!dateList.isEmpty()){dateList.sort(Collections.reverseOrder());}
            else if(Objects.equals(sort, "asc" )&&!dateList.isEmpty()) {Collections.sort(dateList);}
            else if(!dateList.isEmpty()){Collections.sort(dateList);}

            for(LocalDate date:dateList){

                ArrayList<TodoPerGreenroomDto> todoPerGreenroomDtoList = new ArrayList<>();

                Map<GreenRoom,List<TodoLog>> todoLogWithGreenroom= new HashMap<>();
                Map<GreenRoom,List<Diary>> diaryWithGreenroom = new HashMap<>();

                if(todoLogWithDate.get(date)!=null){todoLogWithGreenroom = todoLogWithDate.get(date).stream().collect(Collectors.groupingBy(tl->tl.getTodo().getGreenRoom()));}
                if(diaryWithDate.get(date)!=null){diaryWithGreenroom = diaryWithDate.get(date).stream().collect(Collectors.groupingBy(Diary::getGreenRoom));}


                ArrayList<TodoLogInfoDto> todoLogInfoDtoList = new ArrayList<>();
                ArrayList<DiaryInfoDto> diaryInfoDtoList = new ArrayList<>();

                List<GreenRoom> greenRooms = new ArrayList<>(todoLogWithGreenroom.keySet().stream().toList());
                greenRooms.addAll(new ArrayList<>(diaryWithGreenroom.keySet().stream().toList()));
                greenRooms = new ArrayList<>(greenRooms.stream().distinct().toList());

                greenRooms.sort(Collections.reverseOrder(Comparator.comparingLong(GreenRoom::getGreenroomId)));


                for(GreenRoom greenRoom:greenRooms){
                    if(todoLogWithGreenroom.get(greenRoom)!=null){
                        todoLogInfoDtoList.addAll(todoLogWithGreenroom.get(greenRoom).stream().map(TodoLogInfoDto::from).toList());
                    }
                    if(diaryWithGreenroom.get(greenRoom)!=null){
                        diaryInfoDtoList.addAll(diaryWithGreenroom.get(greenRoom).stream().map(DiaryInfoDto::from).toList());
                    }
                    todoPerGreenroomDtoList.add(new TodoPerGreenroomDto(greenRoom.getGreenroomId(),greenRoom.getName(),todoLogInfoDtoList,null,diaryInfoDtoList));
                }


                List<String> activityDoneList =  new ArrayList<>();

                for(TodoPerGreenroomDto todoPerGreenroomDto:todoPerGreenroomDtoList){
                    for(TodoLogInfoDto todoLogInfoDto:  todoPerGreenroomDto.getTodoCompleted()){
                        activityDoneList.add(todoLogInfoDto.getActivityName());
                    }
                }

                todoPerMonthResponseDtoList.add(new TodoPerMonthResponseDto(year,month,date.getDayOfMonth(),date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA),activityDoneList.stream().distinct().toList(),null,todoPerGreenroomDtoList));

            }
            return todoPerMonthResponseDtoList;
        }

        //현재 달을 요청한 경우
        else if(today.getYear()==year&&today.getMonthValue()==month){

            Map<LocalDate,List<Todo>> todoWithDate = getTodoFromPastToPresent(greenRoomIdList,activityIdList,year,month);


            List<LocalDate> todoDateList =  new ArrayList<>(todoWithDate.keySet().stream().toList());
            List<LocalDate> todoLogDateList =  new ArrayList<>(new ArrayList<>(todoLogWithDate.keySet().stream().toList()));
            List<LocalDate> diaryDateList =  new ArrayList<>(diaryWithDate.keySet().stream().toList());

            todoDateList.addAll(todoLogDateList);
            todoDateList.addAll(diaryDateList);

            List<LocalDate> dateList = new ArrayList<>(todoDateList.stream().distinct().toList());
            if(Objects.equals(sort, "desc") &&!dateList.isEmpty()){dateList.sort(Collections.reverseOrder());}
            else if(Objects.equals(sort, "asc" )&&!dateList.isEmpty()) {Collections.sort(dateList);}
            else if(!dateList.isEmpty()){Collections.sort(dateList);}

            for(LocalDate date : dateList) {

                ArrayList<TodoPerGreenroomDto> todoPerGreenroomDtoList = new ArrayList<>();

                Map<GreenRoom, List<Todo>> todoWithGreenroom = new HashMap<>();
                Map<GreenRoom, List<TodoLog>> todoLogWithGreenroom = new HashMap<>();
                Map<GreenRoom, List<Diary>> diaryWithGreenroom = new HashMap<>();


                if (todoWithDate.get(date) != null) {todoWithGreenroom = todoWithDate.get(date).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));}
                if(todoLogWithDate.get(date)!=null){ todoLogWithGreenroom = todoLogWithDate.get(date).stream().collect(Collectors.groupingBy(tl->tl.getTodo().getGreenRoom()));}
                if(diaryWithDate.get(date)!=null) {diaryWithGreenroom = diaryWithDate.get(date).stream().collect(Collectors.groupingBy(Diary::getGreenRoom));}

                List<GreenRoom> tempGreenroomList =  new ArrayList<>(todoWithGreenroom.keySet().stream().toList());
                tempGreenroomList.addAll(todoLogWithGreenroom.keySet().stream().toList());
                tempGreenroomList.addAll(diaryWithGreenroom.keySet().stream().toList());

                ArrayList<GreenRoom> greenRooms = new ArrayList<>(tempGreenroomList.stream().distinct().toList());
                greenRooms.sort(Collections.reverseOrder(Comparator.comparingLong(GreenRoom::getGreenroomId)));


                for (GreenRoom greenRoom : greenRooms) {

                    List<TodoLogInfoDto> todoLogInfoDtoList = new ArrayList<>();
                    List<TodoInfoDto> todoInfoDtoList = new ArrayList<>();
                    List<DiaryInfoDto> diaryInfoDtoList = new ArrayList<>();

                    if(todoWithGreenroom.get(greenRoom)!=null){
                        todoInfoDtoList.addAll(new ArrayList<>(todoWithGreenroom.get(greenRoom).stream().map(TodoInfoDto::from).toList()));
                    }
                    if(todoLogWithGreenroom.get(greenRoom)!=null){
                        todoLogInfoDtoList.addAll(new ArrayList<>(todoLogWithGreenroom.get(greenRoom).stream().map(TodoLogInfoDto::from).toList()));
                    }
                    if(diaryWithGreenroom.get(greenRoom)!=null){
                        diaryInfoDtoList.addAll(new ArrayList<>(diaryWithGreenroom.get(greenRoom).stream().map(DiaryInfoDto::from).toList()));
                    }

                    todoPerGreenroomDtoList.add(new TodoPerGreenroomDto(greenRoom.getGreenroomId(), greenRoom.getName(), todoLogInfoDtoList, todoInfoDtoList, diaryInfoDtoList));
                }


                List<String> activityHaveToDoList = new ArrayList<>();
                List<String> activityCompleted = new ArrayList<>();

                for (TodoPerGreenroomDto todoPerGreenroomDto : todoPerGreenroomDtoList) {
                    for (TodoInfoDto todoInfoDto : todoPerGreenroomDto.getTodoHaveToDo()) {
                        activityHaveToDoList.add(todoInfoDto.getActivity());
                    }
                    for(TodoLogInfoDto todoLogInfoDto: todoPerGreenroomDto.getTodoCompleted()){
                        activityCompleted.add(todoLogInfoDto.getActivityName());
                    }
                }


                todoPerMonthResponseDtoList.add(new TodoPerMonthResponseDto(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA), activityCompleted.stream().distinct().toList(), activityHaveToDoList.stream().distinct().toList(), todoPerGreenroomDtoList));
            }
            return todoPerMonthResponseDtoList;
        }

        //미래 날짜 조회시   /// 지금 달로부터 한달 뒤 주기 조회 가능.(주기 표시 가능)
        else{
            Map<LocalDate,List<Todo>> todoWithDate = getTodoFromPastToPresent(greenRoomIdList,activityIdList,year,month);

            List<LocalDate> dateList;

            dateList = !todoWithDate.keySet().isEmpty()? new ArrayList<>(todoWithDate.keySet().stream().toList()) : new ArrayList<>();

            if(Objects.equals(sort, "desc") &&!dateList.isEmpty()){dateList.sort(Collections.reverseOrder());}
            else if(Objects.equals(sort, "asc" )&&!dateList.isEmpty()) {Collections.sort(dateList);}
            else if(!dateList.isEmpty()){Collections.sort(dateList);}

            for(LocalDate date:dateList){
                Map<GreenRoom,List<Todo>> todoWithGreenroom =  todoWithDate.get(date).stream().collect(Collectors.groupingBy(Todo::getGreenRoom));


                ArrayList<TodoPerGreenroomDto> todoPerGreenroomDtoArrayList = new ArrayList<>();

                ArrayList<GreenRoom> greenRooms =  new ArrayList<>(todoWithGreenroom.keySet().stream().toList());
                greenRooms.sort(Collections.reverseOrder(Comparator.comparingLong(GreenRoom::getGreenroomId)));



                for(GreenRoom greenRoom: greenRooms){
                    ArrayList<TodoInfoDto> todoInfoDtoArrayList = new ArrayList<>(todoWithGreenroom.get(greenRoom).stream().map(TodoInfoDto::from).toList());
                    todoPerGreenroomDtoArrayList.add(new TodoPerGreenroomDto(greenRoom.getGreenroomId(),greenRoom.getName(),null,todoInfoDtoArrayList,null));
                }

                List<String> activityHaveToDoList =  new ArrayList<>();

                for(TodoPerGreenroomDto todoPerGreenroomDto:todoPerGreenroomDtoArrayList){
                    for(TodoInfoDto todoInfoDto:  todoPerGreenroomDto.getTodoHaveToDo()){
                        activityHaveToDoList.add(todoInfoDto.getActivity());
                    }
                }
                todoPerMonthResponseDtoList.add(new TodoPerMonthResponseDto(date.getYear(),date.getMonthValue(),date.getDayOfMonth(),date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA),null,activityHaveToDoList.stream().distinct().toList(),todoPerGreenroomDtoArrayList));

            }
        }
        return todoPerMonthResponseDtoList;
    }

}
