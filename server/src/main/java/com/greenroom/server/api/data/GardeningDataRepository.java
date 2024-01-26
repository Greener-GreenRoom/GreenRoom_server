package com.greenroom.server.api.data;

import com.greenroom.server.api.domain.greenroom.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GardeningDataRepository extends JpaRepository<Plant, Long> {

}
