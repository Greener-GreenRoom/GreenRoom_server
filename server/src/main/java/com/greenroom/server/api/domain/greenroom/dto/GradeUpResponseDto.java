package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GradeUpResponseDto {
    private Long greenroomId;
    private int level; // point update 이후 user의 level
    private int increasingPoint; //update된 point
    private boolean isLevelUpdated; //레벨 update 되었는지 여부
}
