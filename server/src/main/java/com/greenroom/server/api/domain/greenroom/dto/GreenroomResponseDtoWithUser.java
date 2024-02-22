package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenroomResponseDtoWithUser {
    private UserDto userInfo;
    private ArrayList<GreenroomResponseDto> greenroomTotalInfo;
}
