package com.flex.interpre.global.module.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.flex.interpre.global.module.s3.exception.S3UploaderExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3DocumentUploader {
    private final AmazonS3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "PDF");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB


    // 단일 PDF 문서 업로드
    public String uploadDocument(MultipartFile multipartFile, String dirName) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw S3UploaderExceptions.DOCUMENT_NOT_FOUND.toException();
        }

        validateFile(multipartFile);
        return uploadToS3(multipartFile, dirName); // 업로드된 파일의 S3 URL
    }

    // 파일 삭제
    public void deleteDocument(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("삭제할 파일 URL이 비어있습니다.");
            return;
        }

        try {
            AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);
            s3Client.deleteObject(s3Uri.getBucket(), s3Uri.getKey());
            log.info("S3 문서 삭제 성공: {}", fileUrl);
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 S3 URL 형식입니다: {}", fileUrl);
            throw S3UploaderExceptions.INVALID_FILE_URL.toException();
        } catch (Exception e) {
            log.error("S3 문서 삭제 중 오류가 발생했습니다.", e);
            throw S3UploaderExceptions.FILE_DELETE_FAILED.toException();
        }
    }

    // 파일 존재 여부 확인
    public boolean isDocumentExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        try {
            AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);
            boolean exists = s3Client.doesObjectExist(s3Uri.getBucket(), s3Uri.getKey());
            log.info("S3 문서 존재 여부 확인: {} - {}", fileUrl, exists);
            return exists;
        } catch (Exception e) {
            log.error("파일 존재 여부 확인 중 오류 발생", e);
            return false;
        }
    }


    /* 내부 메서드 */

    // S3에 파일 업로드
    private String uploadToS3(MultipartFile multipartFile, String dirName) {
        String fileName = generateFileName(multipartFile.getOriginalFilename());
        String key = dirName + "/" + fileName;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentDisposition("inline"); // 브라우저에서 바로 볼 수 있도록 설정

        // S3 업로드
        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(
                    new PutObjectRequest(bucket, key, inputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
            log.info("S3 문서 업로드 성공: {}", key);
        } catch (IOException e) {
            log.error("S3 파일 업로드 중 IO 에러 발생", e);
            throw S3UploaderExceptions.FILE_UPLOAD_FAILED.toException();
        }

        return s3Client.getUrl(bucket, key).toString();
    }

    // 파일 유효성 검증
    private void validateFile(MultipartFile file) {
        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw S3UploaderExceptions.FILE_SIZE_EXCEEDED.toException();
        }

        // 파일 확장자 검증
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw S3UploaderExceptions.UNSUPPORTED_FILE_TYPE.toException();
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw S3UploaderExceptions.INVALID_PDF_FILE.toException();
        }
    }

     // 고유 파일명 생성 (형식: UUID_원본파일명.pdf)
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String sanitizedFilename = originalFilename
                .replaceAll("[^a-zA-Z0-9가-힣._-]", "_"); // 특수문자 제거
        return UUID.randomUUID().toString() + "_" + sanitizedFilename;
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            throw S3UploaderExceptions.INVALID_FILE_NAME.toException();
        }
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null || extension.isEmpty()) {
            throw S3UploaderExceptions.FILE_EXTENSION_NOT_FOUND.toException();
        }
        return extension;
    }
}