package com.greenroom.server.api.domain.greenroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GreenroomRegisterResponseDto {
    private Long greenroomId;
    private GradeUpDto levelUp;
}
