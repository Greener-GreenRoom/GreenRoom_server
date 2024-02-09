package com.greenroom.server.api.domain.board.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "sugguestion_like")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuggestionLike {

    @EmbeddedId
    private SuggestionLikePK suggestionLikeId;
}

