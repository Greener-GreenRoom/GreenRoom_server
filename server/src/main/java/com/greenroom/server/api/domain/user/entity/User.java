package com.greenroom.server.api.domain.user.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.security.dto.GoogleOAuthAttribute;
import com.greenroom.server.api.domain.user.dto.UserDto;
import com.greenroom.server.api.domain.user.enums.Role;
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

    @Builder
    public User(String name,String email,String password,String profileUrl,Grade grade,Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileUrl = profileUrl;
        this.totalSeed = 0;
        this.weeklySeed = 0;
        this.grade = grade;
        this.role = role;
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

    public static User createUser(UserDto userDto,Grade grade){
        return User.builder()
                .name(userDto.getName())
                .password(userDto.getPassword())
                .email(userDto.getEmail())
                .grade(grade)
                .role(Role.GENERAL)
                .build();
    }
    public static User createUser(GoogleOAuthAttribute attribute,Grade grade){
        return User.builder()
                .name(attribute.getName())
                .email(attribute.getEmail())
                .grade(grade)
                .role(Role.GENERAL)
                .build();
    }

    public User setDefaultPasswordOnOAuth2User(String password){
        this.password = password;
        return this;
    }

    public User updateUser(GoogleOAuthAttribute attribute){
        this.name = attribute.getName();
        return this;
    }
}
