package com.freelancer.file;

import com.freelancer.dto.response.FileUploadResponse;
import com.freelancer.exception.AppException;
import com.freelancer.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final FileUploadProperties props;

    private static final Set<String> ALLOWED_SUBFOLDERS = Set.of(
            "avatars", "qr-codes", "attachments", "deliverables", "payment-proof", "general"
    );

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif"
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf"
    );

    private static final Set<String> IMAGE_ONLY_SUBFOLDERS = Set.of("avatars", "qr-codes");

    @PostConstruct
    public void init() {
        try {
            Path root = Paths.get(props.getDir()).toAbsolutePath();
            Files.createDirectories(root);
            for (String sub : ALLOWED_SUBFOLDERS) {
                Files.createDirectories(root.resolve(sub));
            }
            log.info("Upload directory ready: {}", root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directories", e);
        }
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "File rỗng");
        }
        if (!isAllowedSubFolder(subFolder)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "subFolder không hợp lệ: " + subFolder);
        }

        long maxBytes = (long) props.getMaxFileSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (!isAllowedContentType(contentType, subFolder)) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file"
        );
        String ext = getExtension(originalName);
        if (ext.isBlank()) {
            ext = guessExtFromContentType(contentType);
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = Paths.get(props.getDir(), subFolder, storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save file to {}", target, e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String url = props.getBaseUrl() + "/" + subFolder + "/" + storedName;
        log.info("Uploaded {} bytes -> {}", file.getSize(), target);

        return FileUploadResponse.builder()
                .url(url)
                .fileName(originalName)
                .storedName(storedName)
                .fileSize(file.getSize())
                .contentType(contentType)
                .subFolder(subFolder)
                .build();
    }

    @Override
    public void delete(String storedName, String subFolder) {
        if (storedName.contains("/") || storedName.contains("..")) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Tên file không hợp lệ");
        }
        Path p = Paths.get(props.getDir(), subFolder, storedName);
        try {
            Files.deleteIfExists(p);
            log.info("Deleted file: {}", p);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", p, e);
        }
    }

    @Override
    public boolean isAllowedSubFolder(String subFolder) {
        return subFolder != null && ALLOWED_SUBFOLDERS.contains(subFolder);
    }

    @Override
    public boolean isAllowedContentType(String contentType, String subFolder) {
        if (contentType == null) return false;
        if (IMAGE_ONLY_SUBFOLDERS.contains(subFolder)) {
            return IMAGE_TYPES.contains(contentType);
        }
        return IMAGE_TYPES.contains(contentType) || DOCUMENT_TYPES.contains(contentType);
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i).toLowerCase() : "";
    }

    private String guessExtFromContentType(String ct) {
        if (ct == null) return ".bin";
        return switch (ct) {
            case "image/png"  -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            case "application/pdf" -> ".pdf";
            default -> ".bin";
        };
    }
}
