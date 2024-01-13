package com.greenroom.server.api.domain.board.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "board")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    private String title;

    private String content;

    private int weeklyView;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "board",cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
    private List<BoardImage> boardImages;

    @Builder
    public Board(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.weeklyView = 0;
        this.user = user;
    }
}
