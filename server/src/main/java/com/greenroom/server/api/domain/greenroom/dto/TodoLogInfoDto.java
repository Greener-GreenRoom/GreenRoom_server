package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.TodoLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TodoLogInfoDto {
    private String activityName;
    private LocalDate completeDate;

    public static TodoLogInfoDto from(TodoLog todoLog){
        return new TodoLogInfoDto(todoLog.getTodo().getActivity().getName().toString().toLowerCase(),todoLog.getCreateDate().toLocalDate());
    }
}
