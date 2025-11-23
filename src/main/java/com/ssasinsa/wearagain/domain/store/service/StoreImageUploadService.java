package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StoreImageUploadService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final Path uploadRoot;

    public StoreImageUploadService(
            @Value("${app.upload.store-image-root:/data/uploads}") String uploadRoot
    ) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    public String uploadImage(MultipartFile file) {
        validateFile(file);

        String contentType = file.getContentType();
        String extension = resolveExtension(contentType);
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String imageName = buildImageName(datePath, extension);
        Path targetDirectory = uploadRoot.resolve("store").resolve(datePath);
        String storedFileName = imageName.substring(imageName.lastIndexOf('/') + 1);
        Path targetPath = targetDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetPath.toFile());
        } catch (IOException exception) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_UPLOAD_FAILED, exception);
        }

        return imageName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
        }
    }

    private String resolveExtension(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        String extension = CONTENT_TYPE_EXTENSIONS.get(normalized);
        if (extension == null) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
        }
        return extension;
    }

    private String buildImageName(String datePath, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "store/" + datePath + "/" + uuid + "." + extension;
    }
}
