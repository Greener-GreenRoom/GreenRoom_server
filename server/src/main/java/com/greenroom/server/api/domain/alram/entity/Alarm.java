package com.greenroom.server.api.domain.alram.entity;

import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "ALARM")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean waterCycleAlarm;

    private Boolean noticeAlarm;

    private Boolean communityAlarm;

    private String eventToken;

    @Builder
    public Alarm(User user){
        this.waterCycleAlarm = Boolean.FALSE;
        this.noticeAlarm = Boolean.FALSE;
        this.communityAlarm = Boolean.FALSE;
        this.user = user;
    }

    public Alarm updateAlarmSet(Boolean waterCycleAlarm,Boolean noticeAlarm,Boolean communityAlarm){
        this.waterCycleAlarm = waterCycleAlarm;
        this.noticeAlarm = noticeAlarm;
        this.communityAlarm = communityAlarm;
        return this;
    }
}
