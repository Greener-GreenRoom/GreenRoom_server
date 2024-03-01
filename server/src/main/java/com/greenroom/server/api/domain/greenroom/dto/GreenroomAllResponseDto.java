package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.user.dto.UserBaseInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenroomAllResponseDto {
    private UserBaseInfoDto userInfo;
    private ArrayList<GreenroomResponseDto> greenroomTotalInfo;
}
