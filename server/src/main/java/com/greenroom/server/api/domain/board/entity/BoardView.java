package com.greenroom.server.api.domain.board.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "board_view")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardView {

    @EmbeddedId
    private BoardViewPK boardViewId;
}
