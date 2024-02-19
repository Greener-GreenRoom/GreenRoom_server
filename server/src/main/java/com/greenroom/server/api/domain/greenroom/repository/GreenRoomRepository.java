package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.GreenRoomStatus;
import com.greenroom.server.api.domain.user.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;

@Repository
public interface GreenRoomRepository extends JpaRepository<GreenRoom,Long> {
   @EntityGraph(attributePaths = {"plant"})
   ArrayList<GreenRoom> findGreenRoomByUser(User user,Sort sort);

   @EntityGraph(attributePaths = {"plant"})
   ArrayList<GreenRoom> findGreenRoomByUserAndStatusIn(User user, ArrayList<GreenRoomStatus> status,Sort sort);
}
