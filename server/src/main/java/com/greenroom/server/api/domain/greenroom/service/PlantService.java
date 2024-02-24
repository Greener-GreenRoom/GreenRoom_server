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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import  org.springframework.data.domain.Page;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlantService {
    private final PlantRepository plantRepository;
    @Transactional(readOnly = true)
    public String getWateringTip(Long id) throws IllegalArgumentException {
        Optional<Plant> optionalPlant = plantRepository.findById(id);
        Plant plant = optionalPlant.orElseThrow(()->new IllegalArgumentException("해당 식물 없음"));
        return plant.getWaterCycle();
    }

    @Transactional(readOnly = true)
    public List<PlantInformationDto> getPopularPlantList(int offset){

        ///많이 키우는 순서대로 받아온 뒤, 더미데이터가 아닌 것 중에 offset 만큼 가져오기
        List<Plant> plantList = plantRepository.findAll(Sort.by(Sort.Order.desc("plantCount")));
        return  plantList.stream().filter(p-> !Objects.equals(p.getPlantCategory(), "dummy")).map(PlantInformationDto::from).toList().subList(0,offset);

    }

    @Transactional(readOnly = true)
    public List<PlantInformationDto> getAllPlantList(){
        List<Plant> plants = plantRepository.findAll();

        //더미데이터가 아닌것만 반환하기
        return plants.stream().filter(p-> !Objects.equals(p.getPlantCategory(), "dummy")).map(PlantInformationDto::from).toList();

    }
}
