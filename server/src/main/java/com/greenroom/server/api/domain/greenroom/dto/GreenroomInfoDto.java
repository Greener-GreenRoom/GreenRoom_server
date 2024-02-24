package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class GreenroomInfoDto {

    private PlantInformationDto plantInfo;
    private Long greenroomId;
    private String greenroomName;
    private String imageUrl;
    private int period;
    private String memo;

    public static GreenroomInfoDto from(GreenRoom greenRoom){

        LocalDateTime today = LocalDateTime.now();
        long period = ChronoUnit.DAYS.between(greenRoom.getCreateDate(),today) +1;

        PlantInformationDto plantInformationDto = PlantInformationDto.from(greenRoom.getPlant());
        return new GreenroomInfoDto(
                plantInformationDto,
                greenRoom.getGreenroomId(),
                greenRoom.getName(),
                greenRoom.getPictureUrl(),
                (int) period,
                greenRoom.getMemo());
    }
}
