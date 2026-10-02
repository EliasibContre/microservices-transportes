package com.transport.assignment_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignment_files")
@Getter
@Setter
@NoArgsConstructor
public class AssignmentFile {
    @Id
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "file_name", nullable = false, columnDefinition =
            "text")
    private String fileName;

    @Column(name = "content_type", nullable = false, columnDefinition =
            "text")
    private String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;
}
