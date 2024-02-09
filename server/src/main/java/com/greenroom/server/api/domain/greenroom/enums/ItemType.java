package com.greenroom.server.api.domain.greenroom.enums;

import lombok.Getter;

@Getter
public enum ItemType {

    SHAPE("모양"),
    BACKGROUND("배경"),
    ACCESSORIES("악세사리"),
    FLOWERPOT("화분");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }
}
