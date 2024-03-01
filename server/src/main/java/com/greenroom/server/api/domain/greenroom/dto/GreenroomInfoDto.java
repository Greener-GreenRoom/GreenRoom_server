package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class GreenroomInfoDto {
    private GreenroomBaseInfoDto greenroomBaseInfo;
    private PlantInformationDto plantInfo;


    public static GreenroomInfoDto from(GreenRoom greenRoom){

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long period = ChronoUnit.DAYS.between(greenRoom.getCreateDate().toLocalDate(),today) +1;

        PlantInformationDto plantInformationDto = PlantInformationDto.from(greenRoom.getPlant());
        return new GreenroomInfoDto(
                GreenroomBaseInfoDto.from(greenRoom),plantInformationDto);
    }
}
