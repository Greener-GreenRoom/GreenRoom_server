package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
public class GreenroomInfoDto {
    private Long greenroomId;
    private String greenroomName;
    private String imageUrl;
    private int period;
    private String memo;

    public GreenroomInfoDto(Long greenroomId, String name, String pictureUrl, int period, String memo) {
        this.greenroomId = greenroomId;
        this.greenroomName= name;
        this.imageUrl = pictureUrl;
        this.period = period;
        this.memo = memo;

    }


    public static GreenroomInfoDto from(GreenRoom greenRoom){
        LocalDateTime today = LocalDateTime.now();
        long period = ChronoUnit.DAYS.between(greenRoom.getCreateDate(),today) +1;
        return new GreenroomInfoDto(
                greenRoom.getGreenroomId(),
                greenRoom.getName(),
                greenRoom.getPictureUrl(),
                (int) period,
                greenRoom.getMemo());
    }
}
