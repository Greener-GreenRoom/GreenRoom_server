package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemInfoDto {

    private Long itemId;
    private String itemName;
    private String itemType;
    private String imgUrl;
    private Integer level;
    private Boolean userAvailable;
}
