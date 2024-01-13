package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "story")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storyId;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "greenroom_id")
    private GreenRoom greenRoom;

}
