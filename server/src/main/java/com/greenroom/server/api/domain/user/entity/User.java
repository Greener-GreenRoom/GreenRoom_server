package com.greenroom.server.api.domain.user.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import com.greenroom.server.api.domain.greenroom.entity.Grade;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
@Getter
@ToString
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @Builder
    public User(String name,String email,String password,String profileUrl,Grade grade) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileUrl = profileUrl;
        this.totalSeed = 0;
        this.weeklySeed = 0;
        this.grade = grade;
    }
}
