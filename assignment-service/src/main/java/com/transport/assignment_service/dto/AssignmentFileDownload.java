package com.transport.assignment_service.dto;

public record AssignmentFileDownload(
        String fileName,
        String contentType,
        byte[] content
) {
}
