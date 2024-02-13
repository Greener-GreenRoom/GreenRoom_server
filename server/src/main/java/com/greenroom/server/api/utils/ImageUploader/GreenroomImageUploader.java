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
public class GreenroomImageUploader {

    private final S3ImageUploader s3ImageUploader;

    @Getter
    @Value("${aws.s3.path.greenroom}")
    private String imagePath;

    @Getter
    @Value("${aws.cdn.path.greenroom}")
    private String cdnImagePath;

    public String uploadGreenroomImage(MultipartFile multipartFile) throws IOException {

        log.info("imagePath => {}",this.imagePath);
        log.info("cdnImagePath => {}",this.cdnImagePath);
        return s3ImageUploader.upload(multipartFile,this.imagePath,this.cdnImagePath);
    }
}
