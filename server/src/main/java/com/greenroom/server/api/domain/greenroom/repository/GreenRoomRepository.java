package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface GreenRoomRepository extends JpaRepository<GreenRoom,Long> {
   ArrayList<GreenRoom> findGreenRoomByUser(User user);

}
