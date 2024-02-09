package com.greenroom.server.api.domain.greenroom.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenroom.server.api.domain.greenroom.service.GardeningDataService;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/plant")
public class GardeningDataController {
    private final GardeningDataService gardeningDataService;

    @PostMapping("/gardening-data")
    public ResponseEntity<ApiResponse> insertData(){
        ApiResponse response = ApiResponse.success();
        try{
            gardeningDataService.insertPlant();
        }catch (JsonProcessingException e){
            response = ApiResponse.failed(ResponseCodeEnum.FAIL_DATA_PARSE);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/gardening-data")
    public ResponseEntity<ApiResponse> deleteData(){
        gardeningDataService.deleteData();
        return ResponseEntity.ok(ApiResponse.success());
    }
}
