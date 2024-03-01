package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GradeUpDto {
    private Integer level; // point update 이후 user의 level
    private Integer increasingPoint; //update된 point
    private Boolean isLevelUpdated; //레벨 update 되었는지 여부
}
