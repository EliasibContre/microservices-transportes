package com.transport.assignment_service.mapper;


import com.transport.assignment_service.dto.AssignmentFileResponse;
import com.transport.assignment_service.dto.AssignmentRequest;
import com.transport.assignment_service.dto.AssignmentResponse;
import com.transport.assignment_service.entity.Assignment;
import com.transport.assignment_service.entity.AssignmentFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    Assignment toEntity(AssignmentRequest request);

    AssignmentResponse toResponse(Assignment assignment);

    AssignmentFileResponse toFileResponse(AssignmentFile file);

}
