package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GreenRoomListDto {
    private Long greenroomId;
    private Long plantId;
    private String plantName;
    private String name;
    private String shape;
    private int todoNum;



}
