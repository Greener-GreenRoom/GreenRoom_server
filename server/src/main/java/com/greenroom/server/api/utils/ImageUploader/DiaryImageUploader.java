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
public class DiaryImageUploader {

    private final S3ImageUploader s3ImageUploader;

    @Getter
    @Value("${aws.s3.path.diary}")
    private String imagePath;

    @Getter
    @Value("${aws.cdn.path.diary}")
    private String cdnImagePath;

    public String uploadDiaryImage(MultipartFile multipartFile) throws IOException {

        log.info("imagePath => {}",this.imagePath);
        log.info("cdnImagePath => {}",this.cdnImagePath);
        return s3ImageUploader.upload(multipartFile,this.imagePath,this.cdnImagePath);
    }
    public void deleteDiaryImage(String imgUrl){
        s3ImageUploader.deleteImage(imgUrl);
    }
}
