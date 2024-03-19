package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.TodoLog;
import com.greenroom.server.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.beans.JavaBean;
import java.util.List;

@Repository
public interface TodoLogRepository extends JpaRepository<TodoLog,Long> {
    @Modifying
    @Query("delete from TodoLog todoLog where todoLog.todo.greenRoom.greenroomId = :greenroomId")
    void deleteTodoLogByGreenroom(@Param("greenroomId") Long greenroomId);

    List<TodoLog> findAllByTodo_GreenRoomGreenroomIdInAndTodo_Activity_ActivityIdIn(List<Long> greenroomIdList, List<Long> activityIdList);



}
