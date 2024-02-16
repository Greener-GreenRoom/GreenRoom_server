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
    private GreenroomInfoDto greenroomInfo;
    private HashMap<String,String> greenroomItem;
    private HashMap<String, LocalDate> greenroomTodo;

}
