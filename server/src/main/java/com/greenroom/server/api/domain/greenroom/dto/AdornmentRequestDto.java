package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdornmentRequestDto {
    private String shape;
    private String glasses;
    private String hairAccessory;
    private String backgroundWindow;
    private String backgroundShelf;
}