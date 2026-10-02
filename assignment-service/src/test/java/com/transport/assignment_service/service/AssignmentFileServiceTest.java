package com.transport.assignment_service.service;

import com.transport.assignment_service.dto.AssignmentFileResponse;
import com.transport.assignment_service.entity.AssignmentFile;
import com.transport.assignment_service.exception.ApplicationException;
import com.transport.assignment_service.mapper.AssignmentMapper;
import com.transport.assignment_service.repository.AssignmentFileRepository;
import com.transport.assignment_service.repository.AssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentFileServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentFileRepository fileRepository;

    @Mock
    private AssignmentMapper assignmentMapper;

    @InjectMocks
    private AssignmentFileService fileService;

    @ParameterizedTest
    @MethodSource("validFiles")
    void storesSupportedFiles(String name, String contentType, byte[] content) {
        UUID assignmentId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", name, contentType, content);
        when(assignmentRepository.existsById(assignmentId)).thenReturn(true);
        when(fileRepository.save(any(AssignmentFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentMapper.toFileResponse(any(AssignmentFile.class)))
                .thenAnswer(invocation -> {
                    AssignmentFile saved = invocation.getArgument(0);
                    return new AssignmentFileResponse(saved.getId(), saved.getAssignmentId(),
                            saved.getFileName(), saved.getContentType(), saved.getUploadedAt());
                });

        AssignmentFileResponse response = fileService.upload(assignmentId, file);

        assertNotNull(response.id());
        assertNotNull(response.uploadedAt());
        assertEquals(assignmentId, response.assignmentId());
        assertEquals(name, response.fileName());
        assertEquals(contentType, response.contentType());
        ArgumentCaptor<AssignmentFile> saved = ArgumentCaptor.forClass(AssignmentFile.class);
        verify(fileRepository).save(saved.capture());
        assertArrayEquals(content, saved.getValue().getContent());
    }

    @ParameterizedTest
    @MethodSource("invalidFiles")
    void rejectsInvalidFormats(String name, String contentType, byte[] content) {
        UUID assignmentId = UUID.randomUUID();
        when(assignmentRepository.existsById(assignmentId)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", name, contentType, content);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> fileService.upload(assignmentId, file));

        assertEquals(ApplicationException.Reason.INVALID_FILE, exception.reason());
        verify(fileRepository, never()).save(any(AssignmentFile.class));
    }

    @Test
    void rejectsFileForMissingAssignment() {
        UUID assignmentId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "receipt.pdf",
                "application/pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> fileService.upload(assignmentId, file));

        assertEquals(ApplicationException.Reason.RESOURCE_NOT_FOUND, exception.reason());
        verifyNoInteractions(fileRepository, assignmentMapper);
    }

    @Test
    void rejectsEmptyFile() {
        UUID assignmentId = UUID.randomUUID();
        when(assignmentRepository.existsById(assignmentId)).thenReturn(true);
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf",
                "application/pdf", new byte[0]);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> fileService.upload(assignmentId, file));

        assertEquals(ApplicationException.Reason.INVALID_FILE, exception.reason());
        verifyNoInteractions(fileRepository, assignmentMapper);
    }

    private static Stream<Arguments> validFiles() {
        return Stream.of(
                Arguments.of("receipt.pdf", "application/pdf",
                        "%PDF-1.7\nbody".getBytes(StandardCharsets.US_ASCII)),
                Arguments.of("photo.png", "image/png",
                        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
                Arguments.of("photo.jpg", "image/jpeg",
                        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
                Arguments.of("photo.jpeg", "image/jpeg",
                        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})
        );
    }

    private static Stream<Arguments> invalidFiles() {
        return Stream.of(
                Arguments.of("fake.pdf", "application/pdf",
                        "not a pdf".getBytes(StandardCharsets.US_ASCII)),
                Arguments.of("photo.txt", "image/png",
                        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
                Arguments.of("file.txt", "text/plain",
                        "text".getBytes(StandardCharsets.US_ASCII))
        );
    }
}
