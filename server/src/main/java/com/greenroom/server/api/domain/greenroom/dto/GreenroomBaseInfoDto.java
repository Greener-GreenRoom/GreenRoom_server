package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@AllArgsConstructor
public class GreenroomBaseInfoDto {
    private Long greenroomId;
    private String name;
    private Long period;
    private String memo;
    private String imgUrl;
    private Boolean status;

    public static GreenroomBaseInfoDto from(GreenRoom greenRoom){
        LocalDateTime today = LocalDateTime.now();
        long period = ChronoUnit.DAYS.between(greenRoom.getCreateDate(),today) +1;

        return  new GreenroomBaseInfoDto(greenRoom.getGreenroomId(),greenRoom.getName(),period,greenRoom.getMemo(),greenRoom.getPictureUrl(), greenRoom.getStatus() == GreenRoomStatus.ENABLED);
    }

}
