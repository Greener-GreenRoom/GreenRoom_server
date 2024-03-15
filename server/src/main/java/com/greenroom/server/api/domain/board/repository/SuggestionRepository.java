package com.greenroom.server.api.domain.board.repository;

import com.greenroom.server.api.domain.board.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion,Long> {
    Optional<Suggestion> findSuggestionByPlantName(String plantName);
}
