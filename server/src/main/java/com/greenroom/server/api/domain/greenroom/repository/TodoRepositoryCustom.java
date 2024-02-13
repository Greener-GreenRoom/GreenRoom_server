package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface TodoRepositoryCustom {
    Optional<ArrayList<Todo>> findByGreenroomAndActivity(Long greenroom_id, ArrayList<Long>todo_list);
}
