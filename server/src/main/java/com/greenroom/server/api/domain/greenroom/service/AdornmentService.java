package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.AdornmentRequestDto;
import com.greenroom.server.api.domain.greenroom.dto.OneAdornmentCreationDto;
import com.greenroom.server.api.domain.greenroom.entity.Adornment;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.entity.Item;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.greenroom.repository.AdornmentRepository;
import com.greenroom.server.api.domain.greenroom.repository.GreenRoomRepository;
import com.greenroom.server.api.domain.greenroom.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdornmentService {
    private final AdornmentRepository adornmentRepository;
    private final ItemRepository  itemRepository;
    private final GreenRoomRepository greenRoomRepository;

    public void createOneAdornment(OneAdornmentCreationDto oneAdornmentCreationDto) throws IllegalArgumentException{

        Item item = itemRepository.findItemByItemName(oneAdornmentCreationDto.getItemName()).orElseThrow(()->new IllegalArgumentException("해당 item 없음."));

        Adornment adornment = Adornment.builder()
                .item(item)
                .greenRoom(oneAdornmentCreationDto.getGreenRoom()).build();
        adornmentRepository.save(adornment);
    }


    public void adornGreenroom(Long greenoomId,AdornmentRequestDto adornmentRequestDto) {

        ArrayList<Adornment> adornments = adornmentRepository.findAdornmentByGreenRoom_GreenroomId(greenoomId);
        GreenRoom greenRoom = greenRoomRepository.findById(greenoomId).orElseThrow(()->new IllegalArgumentException("해당 그린룸 없음."));

        HashMap<ItemType,String> itemAndType = new HashMap<>();
        itemAndType.put(ItemType.SHAPE,adornmentRequestDto.getShape());
        itemAndType.put(ItemType.GLASSES,adornmentRequestDto.getGlasses());
        itemAndType.put(ItemType.HAIR_ACCESSORY,adornmentRequestDto.getHairAccessory());
        itemAndType.put(ItemType.BACKGROUND_WINDOW,adornmentRequestDto.getBackgroundWindow());
        itemAndType.put(ItemType.BACKGROUND_SHELF,adornmentRequestDto.getBackgroundShelf());
        ArrayList<ItemType> itemTypes = new ArrayList<>(List.of(ItemType.values()));


        //이미 등록을 해 놓은 아이템
        for(Adornment adornment : adornments) {
            //아이템을 등록해놓은 상태에서 아이템을 삭제하는 경우
            if (Objects.equals(itemAndType.get(adornment.getItem().getItemType()), "UNUSED")) {
                adornmentRepository.delete(adornment);
            }
            //아이템을 등록해놓은 상태에서 아이템을 다른 아이템으로 변경 또는 그대로 하는 경우
            else {
                Item item = itemRepository.findItemByItemName(itemAndType.get(adornment.getItem().getItemType())).orElseThrow(() -> new IllegalArgumentException("해당 item을 찾을 수 없음."));
                adornment.updateItem(item);
            }

            itemTypes.remove(adornment.getItem().getItemType());
        }

        for(ItemType itemType : itemTypes){

            //아이템을 등록하지 않은 상황에서 새롭게 아이템을 등록하는 경우
            if(!Objects.equals(itemAndType.get(itemType), "UNUSED")){
                Item item = itemRepository.findItemByItemName(itemAndType.get(itemType)).orElseThrow(()-> new IllegalArgumentException("해당 item 없음."));
                Adornment adornment = Adornment.builder()
                        .item(item)
                        .greenRoom(greenRoom).build();
                adornmentRepository.save(adornment);
            }
        }

    }

}
