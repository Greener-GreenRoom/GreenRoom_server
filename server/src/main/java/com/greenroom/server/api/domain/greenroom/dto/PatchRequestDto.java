package com.greenroom.server.api.domain.greenroom.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter@RequiredArgsConstructor
public class PatchRequestDto {
    private String op;
    private String object;
    private String value;
}
