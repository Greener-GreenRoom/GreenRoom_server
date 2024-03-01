package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GrowthInfoDto {

    private String manageLevel;
    private String growthTemperature;
    private String lightDemand;
    private String waterCycle;
    private String humidity;
    private String fertilizer;

    public static GrowthInfoDto from(Plant plant){
        return new GrowthInfoDto(plant.getManageLevel(),plant.getGrowthTemperature(),plant.getLightDemand(),plant.getWaterCycle(),plant.getHumidity(),plant.getFertilizer());
    }
}
