package com.greenroom.server.api.domain.greenroom.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Getter
@RequiredArgsConstructor
public class TodoRequestDto {

    private Long greenroomId ;
    private ArrayList<Long> activityList ;

}
