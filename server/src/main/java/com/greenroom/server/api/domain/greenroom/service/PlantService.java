package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.PlantDetailInfoResponseDto;
import com.greenroom.server.api.domain.greenroom.dto.PlantInformationDto;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.entity.Plant;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.greenroom.repository.PlantRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final GreenRoomRepository greenRoomRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public String getWateringTip(Long id) throws IllegalArgumentException {
        Optional<Plant> optionalPlant = plantRepository.findById(id);
        Plant plant = optionalPlant.orElseThrow(()->new IllegalArgumentException("해당 식물 없음"));
        return plant.getWaterCycle();
    }

    @Transactional(readOnly = true)
    public List<PlantInformationDto> getPlantList(Integer offset,String sort){

        List<Plant> plantList ;

        //많이 키우는 순으로 가져오기
        if(Objects.equals(sort, "popular")){
            plantList = plantRepository.findAll(Sort.by(Sort.Order.desc("plantCount")));
        }
        //조건 없음
        else{
           plantList = plantRepository.findAll();
        }

        if(offset==null){

            //더미 데이터 제외하고 반환
            return  plantList.stream().filter(p-> !Objects.equals(p.getPlantCategory(), "dummy")).map(PlantInformationDto::from).toList();
        }

        if(plantList.isEmpty()){
            return null;
        }

        offset = offset>plantList.size()?plantList.size():offset;
        return  plantList.stream().filter(p-> !Objects.equals(p.getPlantCategory(), "dummy")).map(PlantInformationDto::from).toList().subList(0,offset);

    }

    @Transactional(readOnly = true)
    public PlantDetailInfoResponseDto getPlantInfo(Long plantId){
        Plant plant = plantRepository.findById(plantId).orElseThrow(()->new IllegalArgumentException("해당 식물 없음."));
        return PlantDetailInfoResponseDto.from(plant);
    }
}
