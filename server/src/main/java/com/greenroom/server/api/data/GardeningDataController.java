package com.greenroom.server.api.data;

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
    private ResponseEntity<Void> insertData(){
        gds.insertPlant();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    @DeleteMapping("/gardening-data")
    private ResponseEntity<Void> deleteData(){
        gds.deleteData();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
