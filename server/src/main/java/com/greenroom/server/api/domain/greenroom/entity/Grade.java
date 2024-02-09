package com.greenroom.server.api.domain.greenroom.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "grade")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;

    private String description;

    private String gradeImageUrl;

    private int requiredSeed;

    @Builder
    public Grade(String description,String gradeImageUrl,int requiredSeed){
        this.description =description;
        this.gradeImageUrl = gradeImageUrl;
        this.requiredSeed = requiredSeed;
    }
}
