package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenroomResponseDto {
    private GreenroomInfoDto greenroomInfo;
    private ArrayList<GreenroomItemDto> greenroomItem;
    private ArrayList<GreenroomTodoDto> greenroomTodo;

}
