package com.greenroom.server.api.domain.greenroom.enums;

import lombok.Getter;

@Getter
public enum ItemType {

    SHAPE("모양"),
    HAIR_ACCESSORY("머리핀"),
    GLASSES("안경"),
    GLASS_ACCESSORY("창문소품"),
    SHELF_ACCESSORY("선반소품");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }
}
