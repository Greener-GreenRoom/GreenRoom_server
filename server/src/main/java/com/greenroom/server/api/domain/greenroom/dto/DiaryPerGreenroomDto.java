package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryPerGreenroomDto {
    private Long greenroomId;
    private String greenroomName;
    private List<DiaryInfoDto> diaryList;
}
