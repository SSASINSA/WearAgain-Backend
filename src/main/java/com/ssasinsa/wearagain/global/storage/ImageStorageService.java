package com.ssasinsa.wearagain.global.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

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

    private final Path eventUploadRoot;
    private final Path storeUploadRoot;

    public ImageStorageService(
            @Value("${app.upload.event-image-root:/data/uploads}") String eventUploadRoot,
            @Value("${app.upload.store-image-root:/data/uploads}") String storeUploadRoot
    ) {
        this.eventUploadRoot = Paths.get(eventUploadRoot).toAbsolutePath().normalize();
        this.storeUploadRoot = Paths.get(storeUploadRoot).toAbsolutePath().normalize();
    }

    public String storeEventImage(MultipartFile file) {
        return store(file, eventUploadRoot);
    }

    public String storeStoreImage(MultipartFile file) {
        return store(file, storeUploadRoot);
    }

    public String store(MultipartFile file, Path uploadRoot) {
        validateFile(file);

        String extension = resolveExtension(file.getContentType());
        String imageName = buildImageName(extension);
        Path targetPath = uploadRoot.resolve(imageName).normalize();

        try {
            Files.createDirectories(uploadRoot);
            file.transferTo(targetPath.toFile());
        } catch (IOException exception) {
            throw new ImageStorageException(ImageStorageErrorCode.IO_ERROR, exception);
        }

        return imageName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageStorageException(ImageStorageErrorCode.INVALID_FILE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageStorageException(ImageStorageErrorCode.INVALID_FILE);
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ImageStorageException(ImageStorageErrorCode.INVALID_FILE);
        }
    }

    private String resolveExtension(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        String extension = CONTENT_TYPE_EXTENSIONS.get(normalized);
        if (extension == null) {
            throw new ImageStorageException(ImageStorageErrorCode.INVALID_FILE);
        }
        return extension;
    }

    private String buildImageName(String extension) {
        // retain simple UUID-based naming; no date or domain prefix
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }
}
