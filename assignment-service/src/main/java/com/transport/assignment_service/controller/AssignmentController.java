package com.transport.assignment_service.controller;

import com.transport.assignment_service.dto.AssignmentFileResponse;
import com.transport.assignment_service.dto.AssignmentRequest;
import com.transport.assignment_service.dto.AssignmentResponse;
import com.transport.assignment_service.service.AssignmentFileService;
import com.transport.assignment_service.service.AssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentFileService fileService;

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(
            @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.create(request));
    }

    @PostMapping(
            value = "/{assignmentId}/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AssignmentFileResponse> upload(
            @PathVariable UUID assignmentId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.upload(assignmentId, file));
    }
}