package com.greenroom.server.api.domain.board.entity;

import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SuggestionLikePK implements Serializable {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion_id",nullable = false)
    private Suggestion suggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
}

