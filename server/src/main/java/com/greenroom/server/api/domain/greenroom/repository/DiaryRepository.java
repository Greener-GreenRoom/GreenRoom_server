package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary,Long> {
    @Modifying
    @Query("delete from Diary diary where diary.greenRoom.greenroomId=:greenroomId")
    void deleteDiaryByGreenRoom(@Param("greenroomId")Long greenroomId);
}
