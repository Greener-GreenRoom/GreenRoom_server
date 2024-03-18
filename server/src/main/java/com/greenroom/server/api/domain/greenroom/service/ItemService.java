package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.dto.ItemInfoDto;
import com.greenroom.server.api.domain.greenroom.dto.ItemListDto;
import com.greenroom.server.api.domain.greenroom.dto.ItemListResponseDto;
import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.domain.greenroom.entity.Item;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.greenroom.repository.ItemRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    public ItemListResponseDto getItemList(String userEmail){
        User user =  userRepository.findByEmail(userEmail).orElseThrow(()->new UsernameNotFoundException("해당 user를 찾을 수 없음."));
        Grade userGrade = user.getGrade();

        Map<ItemType,List<Item>> itemList = itemRepository.findAll().stream().collect(Collectors.groupingBy(Item::getItemType));

        ArrayList<ItemListDto> itemListDtoArrayList = new ArrayList<>();

        for(ItemType itemType: itemList.keySet().stream().toList()){
            ArrayList<ItemInfoDto> itemInfoDtoArrayList = new ArrayList<>(itemList.get(itemType).stream().map(i -> new ItemInfoDto(i.getItemId(), i.getItemName(), i.getItemType().name().toLowerCase(), i.getImageUrl(), i.getGrade().getLevel(), i.getGrade().getLevel() <= userGrade.getLevel())).toList());
            itemListDtoArrayList.add(new ItemListDto(itemType.name().toLowerCase(),itemInfoDtoArrayList));
        }

        return new ItemListResponseDto(new ArrayList<>(List.of(Arrays.toString(ItemType.values()).toLowerCase())),itemListDtoArrayList);
    }
}
