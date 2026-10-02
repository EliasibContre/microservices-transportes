package com.transport.assignment_service.service;
import com.transport.assignment_service.dto.AssignmentFileResponse;
import com.transport.assignment_service.entity.AssignmentFile;
import com.transport.assignment_service.mapper.AssignmentMapper;
import com.transport.assignment_service.repository.AssignmentFileRepository;
import com.transport.assignment_service.repository.AssignmentRepository;
import com.transport.assignment_service.exception.ApplicationException;
import com.transport.assignment_service.exception.ApplicationException.Reason;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentFileService {

    private static final Logger log =
            LoggerFactory.getLogger(AssignmentFileService.class);

    private static final byte[] PDF_HEADER =
            "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] PNG_HEADER = {
            (byte) 0x89, 0x50, 0x4E, 0x47,
            0x0D, 0x0A, 0x1A, 0x0A
    };

    private static final byte[] JPEG_HEADER = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF
    };

    private final AssignmentRepository assignmentRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final AssignmentMapper assignmentMapper;

    @Transactional
    public AssignmentFileResponse upload(UUID assignmentId, MultipartFile
            file) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ApplicationException(
                    Reason.RESOURCE_NOT_FOUND,
                    "No existe la asignación con ID " + assignmentId);
        }

        if (file == null || file.isEmpty()) {
            throw new ApplicationException(
                    Reason.INVALID_FILE,
                    "Debes enviar un archivo no vacío");
        }

        String originalName = file.getOriginalFilename();
        String fileName = originalName == null ? ""
                : originalName.replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        if (fileName.isBlank()) {
            throw new ApplicationException(
                    Reason.INVALID_FILE,
                    "El archivo debe tener un nombre válido");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el archivo",
                    ex);
        }

        String contentType = file.getContentType();

        if (!validFormat(contentType, fileName, content)) {
            throw new ApplicationException(
                    Reason.INVALID_FILE,
                    "Solo se aceptan archivos PDF, PNG, JPG o JPEG válidos");
        }

        AssignmentFile assignmentFile = new AssignmentFile();
        assignmentFile.setId(UUID.randomUUID());
        assignmentFile.setAssignmentId(assignmentId);
        assignmentFile.setFileName(fileName);
        assignmentFile.setContentType(contentType);
        assignmentFile.setContent(content);
        assignmentFile.setUploadedAt(OffsetDateTime.now(ZoneOffset.UTC));

        AssignmentFile saved =
                assignmentFileRepository.save(assignmentFile);

        log.atInfo()
                .addKeyValue("event", "assignment_file_uploaded")
                .addKeyValue("assignmentId",
                        saved.getAssignmentId().toString())
                .addKeyValue("fileId", saved.getId().toString())
                .addKeyValue("contentType", saved.getContentType())
                .addKeyValue("sizeBytes", saved.getContent().length)
                .log("Archivo agregado a la asignación");

        return assignmentMapper.toFileResponse(saved);
    }

    private boolean validFormat(String contentType, String fileName,
                                byte[] content) {
        if (contentType == null) {
            return false;
        }

        String name = fileName.toLowerCase(Locale.ROOT);

        return switch (contentType) {
            case "application/pdf" ->
                    name.endsWith(".pdf") && startsWith(content,
                            PDF_HEADER);
            case "image/png" ->
                    name.endsWith(".png") && startsWith(content,
                            PNG_HEADER);
            case "image/jpeg" ->
                    (name.endsWith(".jpg") || name.endsWith(".jpeg"))
                            && startsWith(content, JPEG_HEADER);
            default -> false;
        };
    }

    private boolean startsWith(byte[] content, byte[] header) {
        return content.length >= header.length
                && Arrays.equals(content, 0, header.length,
                header, 0, header.length);
    }
}