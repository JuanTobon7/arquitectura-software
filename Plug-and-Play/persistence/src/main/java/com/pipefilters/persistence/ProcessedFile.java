package com.pipefilters.persistence;

import java.time.LocalDateTime;

public record ProcessedFile(
        String fileName,
        String sha256,
        String ubication,
        long sizeBytes,
        String filterOrigin,
        LocalDateTime processedAt
) {
}