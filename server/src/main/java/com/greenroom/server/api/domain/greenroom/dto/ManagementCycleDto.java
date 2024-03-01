package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Todo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
@Slf4j
public class ManagementCycleDto {
    private String activity;
    private LocalDate nextTodoDate;
    private LocalDate lastUpdateDate;
    private Integer duration;
    private Long remainingPeriod;
    private Boolean status;

    public static ManagementCycleDto from(Todo todo){

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return new ManagementCycleDto(
                todo.getActivity().getName().toString().toLowerCase(),
                todo.getNextTodoDate()!=null?todo.getNextTodoDate().toLocalDate():null,
                todo.getLastUpdateDate()!=null?todo.getLastUpdateDate().toLocalDate():null,
                todo.getDuration()!=null?todo.getDuration():null,
                todo.getNextTodoDate()==null?null:ChronoUnit.DAYS.between(today,todo.getNextTodoDate().toLocalDate()),
                todo.getUseYn());
    }
}
