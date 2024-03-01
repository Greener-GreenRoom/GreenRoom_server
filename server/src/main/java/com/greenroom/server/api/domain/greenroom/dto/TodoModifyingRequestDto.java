package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TodoModifyingRequestDto {
    private Long activityId;
    private String lastUpdateDate;
    private Integer duration;
}
