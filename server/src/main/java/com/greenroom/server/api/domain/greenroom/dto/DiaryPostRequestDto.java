package com.greenroom.server.api.domain.greenroom.dto;

import jakarta.persistence.Access;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryPostRequestDto {
    private Long greenroomId;
    private String date;
    private String title;
    private String content;
}
