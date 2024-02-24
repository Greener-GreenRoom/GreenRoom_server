package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.OneAdornmentCreationDto;
import com.greenroom.server.api.domain.greenroom.entity.Adornment;
import com.greenroom.server.api.domain.greenroom.entity.Item;
import com.greenroom.server.api.domain.greenroom.repository.AdornmentRepository;
import com.greenroom.server.api.domain.greenroom.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdornmentService {
    private final AdornmentRepository adornmentRepository;
    private final ItemRepository  itemRepository;

    public void createOneAdornment(OneAdornmentCreationDto oneAdornmentCreationDto) throws IllegalArgumentException{

        Item item = itemRepository.findItemByItemName(oneAdornmentCreationDto.getItemName()).orElseThrow(()->new IllegalArgumentException("해당 item 없음."));

        Adornment adornment = Adornment.builder()
                .item(item)
                .greenRoom(oneAdornmentCreationDto.getGreenRoom()).build();
        adornmentRepository.save(adornment);
    }
}
