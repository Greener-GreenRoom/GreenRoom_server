package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.entity.Todo;
import com.greenroom.server.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Long> {


    @EntityGraph(attributePaths = {"activity"})
    ArrayList<Todo> findTodoByGreenRoom_UserAndUseYn(User greenRoom_user, Boolean useYn);

    ArrayList<Todo> findTodoByGreenRoom_User(User greenRoom_user);

    @EntityGraph(attributePaths = {"activity"})
    ArrayList<Todo> findTodoByGreenRoom_GreenroomIdAndUseYn(Long greenRoom_greenroomId, Boolean useYn);


    ArrayList<Todo> findAllByGreenRoom_GreenroomIdAndActivity_ActivityIdIn(Long greenroomId, ArrayList<Long> activityIdList);

    Optional<Todo> findByGreenRoomGreenroomIdAndActivity_ActivityId(Long greenRoom_greenroomId, Long activity_activityId);

    @Modifying
    @Query("delete from Todo todo where todo.greenRoom.greenroomId = :greenroomId")
    void deleteTodoByGreenroom(@Param("greenroomId")Long greenroomId);
}
