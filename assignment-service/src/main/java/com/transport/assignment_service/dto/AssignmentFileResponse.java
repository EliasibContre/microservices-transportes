package com.transport.assignment_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AssignmentFileResponse(
        UUID id,
        UUID assignmentId,
        String fileName,
        String contentType,
        OffsetDateTime uploadedAt
) {
}
