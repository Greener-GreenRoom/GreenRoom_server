package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "activity")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    private String name;

    @Builder
    public Activity(Long activityId, String name) {
        this.activityId = activityId;
        this.name = name;
    }
}
