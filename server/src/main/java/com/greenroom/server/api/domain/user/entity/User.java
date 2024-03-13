package com.greenroom.server.api.domain.user.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.domain.user.enums.AlarmConfirm;
import com.greenroom.server.api.domain.user.enums.Provider;
import com.greenroom.server.api.domain.user.enums.UserStatus;
import com.greenroom.server.api.domain.user.enums.converter.ProviderConverter;
import com.greenroom.server.api.security.dto.GoogleOAuthAttribute;
import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.enums.Role;
import com.greenroom.server.api.security.dto.TokenDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "`USERS`")
@Entity
@Getter
//@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    private String email;

    private String password;

    private int totalSeed;

    private int weeklySeed;

    private String profileUrl;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime accessTokenExpirationTime;

    private LocalDateTime refreshTokenExpirationTime;

    @Enumerated(EnumType.STRING)
    public Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Convert(converter = ProviderConverter.class)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    private AlarmConfirm alarmConfirm;

    private String withdrawalReason;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    public User(String name,String email,String password,String profileUrl,Grade grade,Role role,Provider provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileUrl = profileUrl;
        this.totalSeed = 0;
        this.weeklySeed = 0;
        this.grade = grade;
        this.role = role;
        this.provider = provider;
        this.alarmConfirm = AlarmConfirm.Y;
        this.status = UserStatus.IN_ACTION;
        this.withdrawalReason = "";
    }

    public User setRefreshToken(String refreshToken,LocalDateTime refreshTokenExpirationTime){
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        return this;
    }

    public User setAccessToken(String accessToken,LocalDateTime accessTokenExpirationTime){
        this.accessToken = accessToken;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        return this;
    }

    public User invalidateAllTokens(){
        this.refreshToken = null;
        this.accessToken = null;
        this.refreshTokenExpirationTime = null;
        this.accessTokenExpirationTime = null;
        return this;
    }

    public static User createUser(UserDto userDto,Grade grade){
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .profileUrl(userDto.getProfileUrl())
                .grade(grade)
                .role(Role.GENERAL)
                .provider(userDto.getProvider())
                .build();
    }
    public User setDefaultPasswordOnOAuth2User(String password){
        this.password = password;
        return this;
    }
    public User updateUserName(String name){
        this.name = name;
        return this;
    }

    public User updateProfileUrl(String profileUrl){
        this.profileUrl = profileUrl;
        return this;
    }

    public void withdrawalUser(String withdrawalReason){
        this.withdrawalReason = withdrawalReason;
        this.status = UserStatus.DELETE_PENDING;
    }

    public void updateTotalSeed(int plusSeed){
        this.totalSeed +=plusSeed;
    }
    public void updateWeeklySeed(int plusSeed){
        this.weeklySeed +=plusSeed;
    }
    public void updateGrade(Grade grade) {
        this.grade = grade;
    }
}
