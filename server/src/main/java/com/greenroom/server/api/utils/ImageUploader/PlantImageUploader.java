package com.greenroom.server.api.utils.ImageUploader;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

@RequiredArgsConstructor
@Slf4j
@Component
public class PlantImageUploader{

    private final S3ImageUploader s3ImageUploader;

    @Getter
    @Value("${aws.s3.path.plant}")
    private String imagePath;

    @Getter
    @Value("${aws.cdn.path.plant}")
    private String cdnImagePath;

    public String uploadPlantImage(MultipartFile multipartFile) throws IOException {

        log.info("imagePath => {}",this.imagePath);
        log.info("cdnImagePath => {}",this.cdnImagePath);
        return s3ImageUploader.upload(multipartFile,this.imagePath,this.cdnImagePath);
    }
}
