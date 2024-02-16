package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.PlantInformationDto;
import com.greenroom.server.api.domain.greenroom.entity.Plant;
import com.greenroom.server.api.domain.greenroom.repository.PlantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import  org.springframework.data.domain.Page;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantService {
    private final PlantRepository plantRepository;
    @Transactional(readOnly = true)
    public String getWateringTip(Long id) throws RuntimeException {
        Optional<Plant> optionalPlant = plantRepository.findById(id);
        Plant plant = optionalPlant.orElseThrow(()->new RuntimeException("해당 식물 없음"));
        return plant.getWaterCycle();
    }

    @Transactional(readOnly = true)
    public List<PlantInformationDto> getPopularPlantList(int offset){
        PageRequest pageable = PageRequest.of(0,offset, Sort.by("plantCount").descending());
        Page<Plant> pagePlantList =  plantRepository.findAll(pageable);
        ArrayList<PlantInformationDto> plantArrayList = new ArrayList<>();
        for(Plant p : pagePlantList.getContent()){
            plantArrayList.add(PlantInformationDto.from(p));
        }

        return plantArrayList;
    }
}
