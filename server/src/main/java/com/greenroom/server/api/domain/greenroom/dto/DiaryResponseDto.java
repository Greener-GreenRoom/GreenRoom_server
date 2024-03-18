package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryResponseDto {
    private Integer year;
    private Integer month;
    private Integer date;
    private String day;
    private List<DiaryPerGreenroomDto> diaryListPerGreenroom;
}
