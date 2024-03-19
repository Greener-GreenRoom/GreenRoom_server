package com.greenroom.server.api.domain.greenroom.dto;

import com.greenroom.server.api.domain.greenroom.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DiaryInfoDto {
    private Long diaryId;
    private String title;
    private String content;
    private String imgUrl;
    private LocalDate date;

    public static DiaryInfoDto from(Diary diary){
        return new DiaryInfoDto(diary.getDiaryId(),diary.getTitle(),diary.getContent(),diary.getDiaryPictureUrl(),diary.getDate());
    }
}
