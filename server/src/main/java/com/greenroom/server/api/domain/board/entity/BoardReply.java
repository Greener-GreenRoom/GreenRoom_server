package com.greenroom.server.api.domain.board.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "board_reply")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardReply extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardReplyId;

    private String content;

    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private BoardReply parentReply;

    @OneToMany(mappedBy = "parentReply")
    private List<BoardReply> boardReplyList;

    @Builder
    public BoardReply(String content, String imageUrl, User user, Board board, BoardReply parentReply, List<BoardReply> boardReplyList) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.user = user;
        this.board = board;
        this.parentReply = parentReply;
        this.boardReplyList = boardReplyList;
    }
}
