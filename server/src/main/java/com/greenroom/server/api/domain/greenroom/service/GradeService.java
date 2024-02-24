package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.domain.greenroom.repository.GradeRepository;
import com.greenroom.server.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {
    private final GradeRepository gradeRepository;

    ///point update 이후 호출 -> 특정 level의 요구 포인트 달성 시 level up.
    public void checkingLevelUp(User user){

        int currentSeed = user.getTotalSeed();

        Grade grade = gradeRepository.findDistinctFirstByRequiredSeedLessThanEqualOrderByRequiredSeedDesc(currentSeed).orElse(user.getGrade());

        if (!grade.equals(user.getGrade())){
            user.updateGrade(grade);
        }
    }


}
