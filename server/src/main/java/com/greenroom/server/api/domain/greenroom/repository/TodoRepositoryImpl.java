package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.QTodo;
import com.greenroom.server.api.domain.greenroom.entity.Todo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
@RequiredArgsConstructor

public class TodoRepositoryImpl implements TodoRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    QTodo todo = QTodo.todo;
    @Override
    public Optional<ArrayList<Todo>> findByGreenroomAndActivity(Long greenroom_id, ArrayList<Long> todo_list) {
        return Optional.ofNullable((ArrayList<Todo>) jpaQueryFactory
                .selectFrom(todo)
                .where(todo.greenRoom.greenroomId.eq(greenroom_id))
                .where(activityEqOr(todo_list))
                .fetch());
    }
    private BooleanExpression activityEq(Long activity_id){
        if (activity_id!=null){ return todo.activity.activityId.eq(activity_id);}
        return null; }

    private BooleanBuilder activityEqOr(ArrayList<Long> todo_list){
        return new BooleanBuilder().or(activityEq(todo_list.get(0)))
                .or(activityEq(todo_list.get(1)))
                .or(activityEq(todo_list.get(2)))
                .or(activityEq(todo_list.get(3)))
                .or(activityEq(todo_list.get(4)))
                .or(activityEq(todo_list.get(5)));
    }
}
