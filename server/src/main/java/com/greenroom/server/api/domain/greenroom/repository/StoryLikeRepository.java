package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryLikeRepository extends JpaRepository<StoryLike,Long> {
    @Modifying
    @Query("delete from StoryLike storyLike where storyLike.storyLikeId.story.greenRoom.greenroomId = :greenroomId")
    void deleteStoryLikeByGreenroom(@Param("greenroomId")Long greenroomId);
}
