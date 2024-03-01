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
import java.util.Optional;

@Repository
public interface GreenRoomRepository extends JpaRepository<GreenRoom,Long> {

   ArrayList<GreenRoom> findGreenRoomByUser(User user);

   @EntityGraph(attributePaths = {"plant"})
   ArrayList<GreenRoom> findGreenRoomByUserAndStatus(User user, GreenRoomStatus status,Sort sort);

   @EntityGraph(attributePaths = {"plant"})
   ArrayList<GreenRoom> findGreenRoomByUserAndStatusIn(User user, ArrayList<GreenRoomStatus> status,Sort sort);

   ArrayList<GreenRoom> findGreenRoomByNameAndUser(String name, User user);
}
