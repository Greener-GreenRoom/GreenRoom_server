package com.greenroom.server.api.domain.user.repository;

import com.greenroom.server.api.domain.greenroom.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade,Long> {
}
