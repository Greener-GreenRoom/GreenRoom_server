package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Todo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
public class TodoInfoDto {
    private String activity;
    private Integer duration;
    private Long remainingPeriod;

    public static TodoInfoDto from(Todo todo){

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return new TodoInfoDto(todo.getActivity().getName().toString().toLowerCase(),
                todo.getDuration()!=null?todo.getDuration():null,
                todo.getNextTodoDate()==null?null: ChronoUnit.DAYS.between(today,todo.getNextTodoDate().toLocalDate()));
    }

}
