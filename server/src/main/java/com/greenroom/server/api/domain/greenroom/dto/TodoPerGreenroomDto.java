package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TodoPerGreenroomDto {
    private Long greenroomId;
    private String greenroomName;
    private List<TodoLogInfoDto> todoCompleted;
    private List<TodoInfoDto> todoHaveToDo;
    private List<DiaryInfoDto> diaryList;
}
