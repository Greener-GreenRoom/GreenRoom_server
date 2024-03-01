package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenRoomListResponseDto {
    private GreenroomInfoDto greenroomInfo;
    private String shape;
    private Integer todoNum;


}
