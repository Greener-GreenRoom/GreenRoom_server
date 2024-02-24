package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade,Long> {

    Optional<Grade> findDistinctFirstByRequiredSeedLessThanEqualOrderByRequiredSeedDesc(int requiredSeed);

}