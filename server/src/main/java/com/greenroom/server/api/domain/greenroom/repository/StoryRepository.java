package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;

public interface StoryRepository extends JpaRepository<Story,Long> {

    @Modifying
    @Query("delete from Story story where story.greenRoom.greenroomId=:greenroomId")
    void deleteStoryByGreenRoom(@Param("greenroomId")Long greenroomId);

}
