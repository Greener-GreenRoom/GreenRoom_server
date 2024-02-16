package com.greenroom.server.api.domain.greenroom.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GreenroomRegistrationDto {
    private Long plantId;
    private String name;
    private int lastWatering; //마지막으로 물 준 시기
    private Integer wateringDuration; //물 주는 시기
    private String shape; //user가 선택한 식물 모양
}

