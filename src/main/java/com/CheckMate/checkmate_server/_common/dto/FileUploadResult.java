package com.CheckMate.checkmate_server._common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResult {
    private String originalFileName;
    private String storedFileName;
    private String fileKey;
    private String fileUrl;
    private String contentType;
    private long fileSize;
}
