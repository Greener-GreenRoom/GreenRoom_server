package com.greenroom.server.api.utils.ImageUploader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class SuggestionImageUploader {

    private final S3ImageUploader s3ImageUploader;

    @Getter
    @Value("${aws.s3.path.suggestion}")
    private String imagePath;

    @Getter
    @Value("${aws.cdn.path.suggestion}")
    private String cdnImagePath;

    public String uploadSuggestionPlantImage(MultipartFile multipartFile) throws IOException {

        log.info("imagePath => {}",this.imagePath);
        log.info("cdnImagePath => {}",this.cdnImagePath);
        return s3ImageUploader.upload(multipartFile,this.imagePath,this.cdnImagePath);
    }

    public void deleteSuggestionPlantImage(String imgUrl){
        s3ImageUploader.deleteImage(imgUrl);
    }
}
