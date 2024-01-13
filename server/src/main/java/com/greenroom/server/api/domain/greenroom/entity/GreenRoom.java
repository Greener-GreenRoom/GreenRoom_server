package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "greenroom")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GreenRoom extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long greenroomId;

    private String name;

    private String pictureUrl;

    private String memo;

    private Boolean isVisible;

    @Enumerated(EnumType.STRING)
    private GreenRoomStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id",nullable = false,updatable = false)
    private Plant plant;

    @Builder
    public GreenRoom(String name, String pictureUrl,Boolean isVisible, GreenRoomStatus status, User user, Plant plant) {
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.memo = "";
        this.isVisible = Boolean.TRUE;
        this.status = GreenRoomStatus.ENABLED;
        this.user = user;
        this.plant = plant;
    }
}
