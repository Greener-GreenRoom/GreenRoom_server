package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Getter
@RequiredArgsConstructor
public class PlantInformationDto {

    private Long plantId;
    private String distributionName;
    private String plantPictureUrl;

    public PlantInformationDto(Long plantId, String distributionName, String plantPictureUrl) {
        this.plantId = plantId;
        this.distributionName = distributionName;
        this.plantPictureUrl = plantPictureUrl;
    }

    public static PlantInformationDto from(Plant plant){

        return  new PlantInformationDto(plant.getPlantId(),plant.getDistributionName(),plant.getPlantPictureUrl());
    }
}
