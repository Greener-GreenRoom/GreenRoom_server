package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.entity.Adornment;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.repository.AdornmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class AdornmentService {
    private final AdornmentRepository adornmentRepository;

    public HashMap<String,String> parseToAdornmentDto(GreenRoom greenroom){

        ArrayList<Adornment> adornments=adornmentRepository.findAdornmentByGreenRoom(greenroom).orElseThrow(()->new RuntimeException("해당 greenroom에 대한 꾸미기 정보를 찾을 수 없음"));

        HashMap<String,String> items = new HashMap<>();
        items.put("shape",null);
        items.put("hair_accessory",null);
        items.put("glasses",null);
        items.put("glass_accessory",null);
        items.put("shelf_accessory",null);

        for(Adornment adornment :adornments){
            items.replace(adornment.getItem().getItemType().name().toLowerCase(),adornment.getItem().getItemName());
        }
        return items;

    }
}
