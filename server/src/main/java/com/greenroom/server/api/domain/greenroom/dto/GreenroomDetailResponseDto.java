package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public class GreenroomDetailResponseDto {
    private GreenroomInfoDto greenroomInfo;
    private ArrayList <GreenroomItemDto> greenroomItem;
    private ArrayList<ManagementCycleDto> managementCycle;
    private GrowthInfoDto growthInfo;

}
