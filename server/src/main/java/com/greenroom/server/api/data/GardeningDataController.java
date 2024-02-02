package com.greenroom.server.api.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greenroom.server.api.utils.ResponseWithData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class GardeningDataController {
    private final GardeningDataService gds;

    @PostMapping("/gardening-data")
    private ResponseEntity<ResponseWithData> insertData() throws JsonProcessingException {
        gds.insertPlant();

        return ResponseEntity.ok(ResponseWithData.success());
    }
    @DeleteMapping("/gardening-data")
    private ResponseEntity<ResponseWithData> deleteData(){
        gds.deleteData();
        return ResponseEntity.ok(ResponseWithData.success());
    }
}
