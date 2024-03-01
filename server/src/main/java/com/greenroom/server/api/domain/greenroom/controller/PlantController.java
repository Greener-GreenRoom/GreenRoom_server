package com.greenroom.server.api.domain.greenroom.controller;


import com.greenroom.server.api.domain.greenroom.service.PlantService;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PlantController {
    private final PlantService plantService;

    //모든 식물 받아오기
    @GetMapping("/plants")
    public ResponseEntity<ApiResponse> getPlantList(@RequestParam(value = "offset" ,required = false) Integer offset, @RequestParam(value = "sort",required = false)String sort) {
        return ResponseEntity.ok(ApiResponse.success(plantService.getPlantList(offset,sort)));
    }

    //식물 물주기 팁 받아오기
    @GetMapping("/plants/{id}/watering-tip")
    public ResponseEntity<ApiResponse> getWateringTip(@PathVariable("id") Long id){
        ApiResponse response = ApiResponse.success();

        String watering_tip = plantService.getWateringTip(id);

       return ResponseEntity.ok(ApiResponse.success(watering_tip));
    }

    //특정 식물 상세 정보 받아오기
    @GetMapping("/plants/{id}")
    public ResponseEntity<ApiResponse> getPlantInfo(@PathVariable(value = "id")Long plantId){
        return ResponseEntity.ok(ApiResponse.success(plantService.getPlantInfo(plantId)));

    }
}
