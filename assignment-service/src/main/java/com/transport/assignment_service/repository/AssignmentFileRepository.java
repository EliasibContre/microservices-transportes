package com.transport.assignment_service.repository;

import com.transport.assignment_service.entity.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, UUID> {
    Optional<AssignmentFile> findByIdAndAssignmentId(UUID id, UUID assignmentId);
}
