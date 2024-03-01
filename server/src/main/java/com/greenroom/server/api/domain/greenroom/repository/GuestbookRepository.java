package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuestbookRepository extends JpaRepository<Guestbook,Long> {

    @Modifying
    @Query("delete from Guestbook guestbook where guestbook.greenRoom.greenroomId=:greenroomId")
    void deleteGuestbookByGreenRoom(@Param("greenroomId")Long greenroomId);
}
