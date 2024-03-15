package com.greenroom.server.api.domain.board.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.greenroom.entity.Plant;
import com.greenroom.server.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "suggestion")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suggestion extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suggestionId;

    private Boolean isRegistered;

    private String plantName;

    private String plantImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Suggestion(User user,String plantName,String plantImageUrl) {
        this.isRegistered = Boolean.FALSE;
        this.user = user;
        this.plantImageUrl = plantImageUrl;
        this.plantName = plantName;
    }

    public static Suggestion createSuggestion(User user,String plantName,String plantImageUrl){
        return Suggestion.builder()
                .plantName(plantName)
                .plantImageUrl(plantImageUrl)
                .user(user)
                .build();
    }
}
