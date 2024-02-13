package com.greenroom.server.api.domain.greenroom.repository;

import com.greenroom.server.api.domain.greenroom.entity.Adornment;
import com.greenroom.server.api.domain.greenroom.entity.GreenRoom;
import com.greenroom.server.api.domain.greenroom.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface AdornmentRepository extends JpaRepository<Adornment,Long> {
    Optional<ArrayList<Adornment>> findAdornmentByGreenRoom(GreenRoom greenRoom);
    Optional<Adornment> findAdornmentByGreenRoomAndItem_ItemType(GreenRoom greenRoom, ItemType item_itemType);
}
