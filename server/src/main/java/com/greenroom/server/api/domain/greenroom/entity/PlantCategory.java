package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "plant_category")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlantCategory extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plantCategoryId;

    private String name;

    @OneToMany(mappedBy = "plantCategory")
    private List<Plant> plants;
}
