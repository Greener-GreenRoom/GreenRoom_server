package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Adornment;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import com.greenroom.server.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public interface AdornmentRepository extends JpaRepository<Adornment,Long> {

    @EntityGraph(attributePaths = {"item"})
    ArrayList<Adornment> findAdornmentByGreenRoom_User(User greenRoomUser);

    @EntityGraph(attributePaths = {"item"})
    ArrayList<Adornment> findAdornmentByGreenRoom_UserAndItem_ItemType(User greenRoomUser, ItemType temType);

    @EntityGraph(attributePaths = {"item"})
    ArrayList<Adornment> findAdornmentByGreenRoom_GreenroomId(Long greenroomId);

    @Modifying
    @Query("delete from Adornment adornment where adornment.greenRoom.greenroomId=:greenroomId")
    void deleteAdornmentByGreenRoom(@Param("greenroomId")Long greenroomId);

}
