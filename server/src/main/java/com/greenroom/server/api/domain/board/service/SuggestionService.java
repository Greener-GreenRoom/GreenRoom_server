package com.greenroom.server.api.domain.board.service;

import com.greenroom.server.api.domain.board.dto.SuggestionRequest;
import com.greenroom.server.api.domain.board.entity.Suggestion;
import com.greenroom.server.api.domain.board.exception.AlreadyExistPlant;
import com.greenroom.server.api.domain.board.repository.SuggestionRepository;
import com.greenroom.server.api.domain.greenroom.repository.PlantRepository;
import com.greenroom.server.api.enums.ResponseCodeEnum;
import com.greenroom.server.api.security.service.CustomUserDetailService;
import com.greenroom.server.api.utils.ImageUploader.SuggestionImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final PlantRepository plantRepository;
    private final CustomUserDetailService userService;
    private final SuggestionImageUploader suggestionImageUploader;

    public void createSuggestion(User userDetails, SuggestionRequest dto, MultipartFile plantImageFile) throws IOException {

        userService.getUser(userDetails.getUsername());

        if(plantRepository.findByDistributionName(dto.getPlantName()).isPresent()){
            throw new AlreadyExistPlant(ResponseCodeEnum.ALREADY_EXIST,"이미 등록된 식물 입니다.");
        }else if(suggestionRepository.findSuggestionByPlantName(dto.getPlantName()).isPresent()){
            throw new AlreadyExistPlant(ResponseCodeEnum.ALREADY_EXIST,"이미 등록된 건의 입니다.");
        }

        String plantImageUrl = suggestionImageUploader.uploadSuggestionPlantImage(plantImageFile);
        Suggestion suggestion = Suggestion.createSuggestion(userService.getUser(userDetails.getUsername()), dto.getPlantName(), plantImageUrl);
        suggestionRepository.save(suggestion);
    }
}
