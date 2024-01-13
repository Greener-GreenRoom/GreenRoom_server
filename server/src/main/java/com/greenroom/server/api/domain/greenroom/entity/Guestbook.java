package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "guestbook")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guestbook extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guestbookId;

    private String content;

    private Boolean confirmation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "greenroom_id")
    private GreenRoom greenRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Guestbook(String content, Boolean confirmation, GreenRoom greenRoom, User user) {
        this.content = content;
        this.confirmation = confirmation;
        this.greenRoom = greenRoom;
        this.user = user;
    }
}
