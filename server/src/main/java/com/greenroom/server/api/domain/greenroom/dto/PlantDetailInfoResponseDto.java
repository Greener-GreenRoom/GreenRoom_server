package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class PlantDetailInfoResponseDto {
    private PlantInformationDto plantInfo;
    private GrowthInfoDto growthInfo;

    public static PlantDetailInfoResponseDto from(Plant plant){
        return new PlantDetailInfoResponseDto(PlantInformationDto.from(plant),GrowthInfoDto.from(plant));
    }
}
