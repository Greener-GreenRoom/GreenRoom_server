package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.enums.LevelIncreasingCause;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class GradeUpDto {
    private Integer level; // user의 변화된 level
    private Map<String,Integer> increasingPoint;  //포인트
    private Boolean isLevelUpdated; //레벨 update 되었는지 여부
    }

