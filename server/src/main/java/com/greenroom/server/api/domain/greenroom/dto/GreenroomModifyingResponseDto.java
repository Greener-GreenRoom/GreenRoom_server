package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GreenroomModifyingResponseDto {
    private Long greenroomId;
    private String greenroomName;
    private String imgUrl;
    private GreenRoomStatus status;

    public static GreenroomModifyingResponseDto from(GreenRoom greenRoom){
        return new GreenroomModifyingResponseDto(greenRoom.getGreenroomId(),greenRoom.getName(),greenRoom.getPictureUrl(),greenRoom.getStatus());
    }
}
