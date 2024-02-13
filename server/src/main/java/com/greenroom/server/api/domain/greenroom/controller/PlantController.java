package com.greenroom.server.api.domain.greenroom.controller;


import com.greenroom.server.api.domain.greenroom.service.PlantService;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PlantController {
    private final PlantService plantService;

    @GetMapping("/plant/lists")
    public ResponseEntity<ApiResponse> getPopularPlantList(@RequestParam(value = "offset" ,defaultValue = "8") int offset) {
        return ResponseEntity.ok(ApiResponse.success(plantService.getPopularPlantList(offset)));
    }

    @GetMapping("/plant/{id}/watering-tip")
    public ResponseEntity<ApiResponse> getWateringTip(@PathVariable("id") Long id){
        ApiResponse response = ApiResponse.success();
       try {
           String watering_tip = plantService.getWateringTip(id);
           response = ApiResponse.success(watering_tip);
       }
       catch (RuntimeException e){
           response = ApiResponse.failed(ResponseCodeEnum.RESULT_NOT_FOUND,e.getMessage());
       }
       return ResponseEntity.ok(response);
    }

}
