package com.CheckMate.checkmate_server._common.service;

import com.CheckMate.checkmate_server._common.dto.FileUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileUploadService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-dir:uploads}")
    private String baseDir;

    @Value("${cloud.aws.s3.max-file-size:10485760}")
    private long maxFileSize;

    public FileUploadResult upload(MultipartFile file, String directory) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = createStoredFileName(originalFileName);
        String fileKey = createFileKey(directory, storedFileName);
        String contentType = getContentType(file);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return FileUploadResult.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileKey(fileKey)
                    .fileUrl(getFileUrl(fileKey))
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 파일을 읽을 수 없습니다.", e);
        } catch (RuntimeException e) {
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
        }
    }

    public List<FileUploadResult> uploadAll(List<MultipartFile> files, String directory) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> upload(file, directory))
                .toList();
    }

    public void delete(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        s3Client.deleteObject(request);
    }

    public void deleteAll(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }
        fileKeys.stream()
                .filter(StringUtils::hasText)
                .forEach(this::delete);
    }

    public String downloadAsUtf8Text(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return "";
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        return s3Client.getObjectAsBytes(request).asString(StandardCharsets.UTF_8);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기는 최대 " + maxFileSize + "바이트까지 업로드할 수 있습니다.");
        }
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }
    }

    private String createStoredFileName(String originalFileName) {
        String extension = getExtension(originalFileName);
        return UUID.randomUUID() + extension;
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }

    private String createFileKey(String directory, String storedFileName) {
        String normalizedBaseDir = trimSlashes(baseDir);
        String normalizedDirectory = trimSlashes(directory);

        if (StringUtils.hasText(normalizedBaseDir) && StringUtils.hasText(normalizedDirectory)) {
            return normalizedBaseDir + "/" + normalizedDirectory + "/" + storedFileName;
        }
        if (StringUtils.hasText(normalizedBaseDir)) {
            return normalizedBaseDir + "/" + storedFileName;
        }
        if (StringUtils.hasText(normalizedDirectory)) {
            return normalizedDirectory + "/" + storedFileName;
        }
        return storedFileName;
    }

    private String trimSlashes(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String getContentType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFileName = file.getOriginalFilename();

        if (isUtf8TextFile(contentType, originalFileName)) {
            return normalizeTextContentType(contentType, originalFileName);
        }
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "application/octet-stream";
    }

    private boolean isUtf8TextFile(String contentType, String fileName) {
        if (StringUtils.hasText(contentType)) {
            String lowerContentType = contentType.toLowerCase(Locale.ROOT);
            if (lowerContentType.startsWith("text/")
                    || lowerContentType.equals("application/json")
                    || lowerContentType.equals("application/xml")
                    || lowerContentType.equals("application/javascript")
                    || lowerContentType.equals("application/x-javascript")) {
                return true;
            }
        }

        String extension = getExtension(fileName == null ? "" : fileName).toLowerCase(Locale.ROOT);
        return List.of(
                ".txt", ".csv", ".tsv", ".md", ".json", ".xml", ".html", ".htm",
                ".css", ".js", ".ts", ".java", ".py", ".c", ".cpp", ".h", ".hpp",
                ".sql", ".yml", ".yaml", ".properties", ".log"
        ).contains(extension);
    }

    private String normalizeTextContentType(String contentType, String fileName) {
        String baseContentType = StringUtils.hasText(contentType)
                ? contentType.split(";", 2)[0].trim()
                : contentTypeFromExtension(fileName);
        return baseContentType + "; charset=UTF-8";
    }

    private String contentTypeFromExtension(String fileName) {
        String extension = getExtension(fileName == null ? "" : fileName).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".csv" -> "text/csv";
            case ".tsv" -> "text/tab-separated-values";
            case ".md" -> "text/markdown";
            case ".json" -> "application/json";
            case ".xml" -> "application/xml";
            case ".html", ".htm" -> "text/html";
            case ".css" -> "text/css";
            case ".js" -> "application/javascript";
            case ".yml", ".yaml" -> "application/x-yaml";
            default -> "text/plain";
        };
    }

    private String getFileUrl(String fileKey) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build())
                .toString();
    }
}
