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
    private Long greenroom_id;
    private String greenroom_name;
    private String image_url;
    private int period;
    private String memo;

    public GreenroomInfoDto(Long greenroomId, String name, String pictureUrl, int period, String memo) {
        this.greenroom_id = greenroomId;
        this.greenroom_name= name;
        this.image_url = pictureUrl;
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
