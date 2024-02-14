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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ImageUploader {

    @Getter
    @Value("${aws.s3.path.root}")
    private String root;

    @Getter
    @Value("${aws.cdn.path.root}")
    private String cdnRoot;

    @Getter
    @Value("${temp.filePath}")
    private String tempFilePath;

    @Getter
    @Value("${aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    /*
    public List<String> upload(List<MultipartFile> multipartFile, String dirName, String cdnImagePath) throws IOException {
        List<File> convertedFiles = new ArrayList<>();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        for (MultipartFile file : multipartFile) {
            convertedFiles.add(convert(file,timestamp+"_"+file.getOriginalFilename()));
        }
        List<String> urls = new ArrayList<>();
        for (File convertedFile : convertedFiles) {
            String fileName = dirName+"/"+convertedFile.getName();
            urls.add(putS3(convertedFile, fileName));
            removeNewFile(convertedFile);
        }
        return urls.stream()
                .map(url -> toCdnPath(url, dirName, this.cdnRoot + cdnImagePath))
                .collect(Collectors.toList());
    }
    */

    public String toCdnPath(String path,String dirName,String cdnPath){
        return path.replace(this.root+dirName,cdnPath);
    }

    public String upload(MultipartFile multipartFile,String dirName,String cdnImagePath) throws IOException {

        File convertedFile = convert(multipartFile, UUID.randomUUID() + "_" + multipartFile.getOriginalFilename());
        String fileName;
        String url;

        try{
            fileName = dirName+"/"+convertedFile.getName();
            url = putS3(convertedFile, fileName);
        } finally {
            removeNewFile(convertedFile);
        }
        return toCdnPath(url,dirName, this.cdnRoot + cdnImagePath);
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client
                .putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("S3Uploader - File delete success");
            return;
        }
        log.info("S3Uploader - File delete fail");
    }

    private File convert(MultipartFile file, String userNo) throws IOException {

        File convertFile = new File(tempFilePath+userNo);
        log.info(convertFile.getAbsolutePath());
        try {
            if (convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                catch (FileNotFoundException e){throw new RuntimeException(String.format("파일 변환이 실패했습니다. 파일 이름: %s", file.getName()));}

            }
            return convertFile;
        }
        catch(IOException e){
            throw new RuntimeException(String.format("파일 변환이 실패했습니다. 파일 이름: %s", file.getName()));
        }
    }

//
//    public void deleteImage(String path) throws IOException {
//        log.info("S3 delete image Path => {}",path);
//        amazonS3Client.deleteObject(bucket, path);
//    }
}
