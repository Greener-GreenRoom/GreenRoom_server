package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;


@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class PlantInformationDto {

    private Long plantId;
    private String distributionName;
    private String plantAlias;
    private String plantPictureUrl;
    private String plantExplanation;
    private String plantCategory;

    public static PlantInformationDto from(Plant plant){
        Long id = Objects.equals(plant.getPlantCategory(), "dummy")?null:plant.getPlantId();
        return  new PlantInformationDto(id,plant.getDistributionName(),plant.getPlantAlias(),plant.getPlantPictureUrl(),plant.getOtherInformation(), plant.getPlantCategory());
    }
}
