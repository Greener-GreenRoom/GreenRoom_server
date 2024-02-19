package com.greenroom.server.api.domain.greenroom.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class GreenroomRegistrationDto {
    private Long plantId;

    private String name;

   private String lastWatering; //마지막으로 물 준 날짜

    private Integer wateringDuration; //물 주는 시기

    private String shape; //user가 선택한 식물 모양
}

