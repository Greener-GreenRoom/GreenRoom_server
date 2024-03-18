package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class ItemListResponseDto {
    private ArrayList<String> itemTypeList;
    private ArrayList<ItemListDto> itemList;
}
