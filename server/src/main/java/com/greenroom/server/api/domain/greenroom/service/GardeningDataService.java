package com.greenroom.server.api.domain.greenroom.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenroom.server.api.domain.greenroom.entity.Plant;
import com.greenroom.server.api.domain.greenroom.repository.PlantRepository;
import com.greenroom.server.api.domain.greenroom.utils.GardeningDataUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;


@RequiredArgsConstructor
@Service
@Slf4j
public class GardeningDataService {
    private final GardeningDataUtil gardeningDataUtil;
    private final PlantRepository plantRepository;

    @Transactional
    public void insertPlant() throws JsonProcessingException {

        Map<String, ArrayList<String>> plantList = gardeningDataUtil.plantList();
        JSONArray objects = gardeningDataUtil.plantInfo(plantList);
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("db insert 중");
        for(int i=0;i<objects.length();i++) {
            Plant plant = objectMapper.readValue(objects.getJSONObject(i).toString(), Plant.class);
            plantRepository.save(plant);
        }
    }

    @Transactional
    public void deleteData(){
        plantRepository.deleteAllInBatch();
    }
}
