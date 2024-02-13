package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenroomResponseDto {
    private GreenroomInfoDto greenroom_info;
    private HashMap<String,String> greenroom_item;
    private HashMap<String, LocalDate> greenroom_todo;

}
