package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "story_like")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryLike {

    @EmbeddedId
    private StoryLikePK storyLikeId;
}
