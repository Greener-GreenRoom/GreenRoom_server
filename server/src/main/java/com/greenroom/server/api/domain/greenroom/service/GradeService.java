package com.greenroom.server.api.domain.greenroom.service;

import com.greenroom.server.api.domain.greenroom.entity.Grade;
import com.greenroom.server.api.domain.greenroom.repository.GradeRepository;
import com.greenroom.server.api.domain.user.entity.User;
import com.greenroom.server.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GradeService {
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    ///point update 이후 호출 -> 특정 level의 요구 포인트 달성 시 level up.
    public void checkingLevelUp(User user){

        int currentSeed = user.getTotalSeed();

        //user가 가지고 있는 현재 씨앗 개수보다 적거나 같은 씨앗을 요구하는 레벨 중에서, 가장 높은 레벨을 가져오기-> 즉 user의 update된 씨앗개수에 해당하는 레벨 반환
        Grade grade = gradeRepository.findDistinctFirstByRequiredSeedLessThanEqualOrderByRequiredSeedDesc(currentSeed).orElse(user.getGrade());

        if (!grade.equals(user.getGrade())){
            user.updateGrade(grade);
        }
    }


}
