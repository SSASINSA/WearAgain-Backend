package com.ssasinsa.wearagain.sample.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 임시 업로드 파일 서빙용 컨트롤러.
 * Nginx 정적 매핑 적용 후 삭제 예정.
 */
@RestController
@RequestMapping("/uploads")
public class UploadSampleController {

    private final Path uploadRoot;

    public UploadSampleController(
<<<<<<< Updated upstream
            @Value("${app.upload.store-image-root:/data/uploads}") String uploadRoot
=======
            @Value("${app.upload.image-root:${app.upload.event-image-root:${app.upload.store-image-root:/data/uploads}}}") String uploadRoot
>>>>>>> Stashed changes
    ) {
        this.uploadRoot = Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String filename
    ) {
        if (!StringUtils.hasText(filename)) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = uploadRoot.resolve(filename).normalize();
        if (!filePath.startsWith(uploadRoot) || !Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
