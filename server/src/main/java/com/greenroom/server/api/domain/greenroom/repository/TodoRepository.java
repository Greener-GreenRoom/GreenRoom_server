package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo,Long>,TodoRepositoryCustom {

    Optional<ArrayList<Todo>> findTodoByGreenRoom(GreenRoom greenRoom);

    Optional<ArrayList<Todo>> findTodoByGreenRoomAndUseYn(GreenRoom greenRoom, Boolean tf);
    Optional<Todo> findTodoByGreenRoomAndActivity_ActivityId(GreenRoom greenRoom,Long activity_id);
}
