package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Diary;
import com.greenroom.server.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary,Long> {
    @Modifying
    @Query("delete from Diary diary where diary.greenRoom.greenroomId=:greenroomId")
    void deleteDiaryByGreenRoom(@Param("greenroomId")Long greenroomId);
     ArrayList<Diary> findAllByGreenRoom_GreenroomIdIn(List<Long> greenroomIdList);
    ArrayList<Diary> findAllByGreenRoom_GreenroomId(Long greenRoom_greenroomId);

    @EntityGraph(attributePaths = {"greenRoom"})
    ArrayList<Diary> findAllByGreenRoomUser(User user);
}
