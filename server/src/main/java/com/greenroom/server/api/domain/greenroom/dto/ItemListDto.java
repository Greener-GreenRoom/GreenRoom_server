package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class ItemListDto {
    private String itemType;
    private ArrayList<ItemInfoDto> itemList;
}
