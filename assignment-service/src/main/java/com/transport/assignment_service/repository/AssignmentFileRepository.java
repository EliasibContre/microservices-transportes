package com.transport.assignment_service.repository;

import com.transport.assignment_service.entity.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, UUID> {
}
