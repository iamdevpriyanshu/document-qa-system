package com.devpriyanshu.documentqa.web.dto;

import com.devpriyanshu.documentqa.domain.IngestionStatus;
import com.devpriyanshu.documentqa.domain.StoredDocument;

import java.time.Instant;

public record DocumentSummaryDto(
        Long id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        IngestionStatus status,
        String failureReason,
        Instant createdAt) {

    public static DocumentSummaryDto from(StoredDocument e) {
        return new DocumentSummaryDto(
                e.getId(),
                e.getOriginalFilename(),
                e.getContentType(),
                e.getSizeBytes(),
                e.getStatus(),
                e.getFailureReason(),
                e.getCreatedAt());
    }
}
