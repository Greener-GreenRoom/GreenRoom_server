package com.greenroom.server.api.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenroom.server.api.domain.greenroom.entity.Plant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class GardeningDataService {
    private final GardeningData gd;
    private final GardeningDataRepository gdr;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public void insertPlant() throws JsonProcessingException {

        Map<String, ArrayList<String>> plantList = gd.plantList();
        JSONArray objects = gd.plantInfo(plantList);
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("db insert 중");
        for(int i=0;i<objects.length();i++) {
            Plant plant = objectMapper.readValue(objects.getJSONObject(i).toString(), Plant.class);

            gdr.save(plant);
        }
    }
    public void deleteData(){
        gdr.deleteAllInBatch();
    }
}
