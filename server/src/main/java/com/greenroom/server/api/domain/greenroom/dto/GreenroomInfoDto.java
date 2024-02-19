package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class GreenroomInfoDto {
    private Long plantId;
    private String plantName;
    private Long greenroomId;
    private String greenroomName;
    private String imageUrl;
    private int period;
    private String memo;

    public GreenroomInfoDto(Long plantId,String plantName,Long greenroomId, String name, String pictureUrl, int period, String memo) {
        this.plantId = plantId;
        this.plantName = plantName;
        this.greenroomId = greenroomId;
        this.greenroomName= name;
        this.imageUrl = pictureUrl;
        this.period = period;
        this.memo = memo;


    }


    public static GreenroomInfoDto from(GreenRoom greenRoom){
        LocalDateTime today = LocalDateTime.now();
        long period = ChronoUnit.DAYS.between(greenRoom.getCreateDate(),today) +1;

        String imageUrl = greenRoom.getPictureUrl();
        Long plantId = greenRoom.getPlant()== null? null:greenRoom.getPlant().getPlantId();

        String plantName = greenRoom.getPlant()==null?null:greenRoom.getPlant().getDistributionName();
        return new GreenroomInfoDto(
                plantId,plantName,
                greenRoom.getGreenroomId(),
                greenRoom.getName(),
                imageUrl,
                (int) period,
                greenRoom.getMemo());
    }
}
