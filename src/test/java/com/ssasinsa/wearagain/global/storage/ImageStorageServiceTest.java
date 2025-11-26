package com.ssasinsa.wearagain.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void should_store_image_when_request_is_valid() throws IOException {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        byte[] content = "fake-image-data".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                content
        );

        String imageName = service.store(multipartFile);

        assertThat(imageName).endsWith(".jpg");
        Path storedFile = tempDir.resolve(imageName);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).containsExactly(content);
    }

    @Test
    void should_fail_when_file_size_exceeds_limit() {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        byte[] large = new byte[(int) (5 * 1024 * 1024L + 1)];
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                large
        );

        assertThatThrownBy(() -> service.store(multipartFile))
                .isInstanceOf(ImageStorageException.class)
                .hasFieldOrPropertyWithValue("errorCode", ImageStorageErrorCode.INVALID_FILE);
    }

    @Test
    void should_fail_when_content_type_is_not_supported() {
        ImageStorageService service = new ImageStorageService(tempDir.toString());
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "text.txt",
                "text/plain",
                "text".getBytes()
        );

        assertThatThrownBy(() -> service.store(multipartFile))
                .isInstanceOf(ImageStorageException.class)
                .hasFieldOrPropertyWithValue("errorCode", ImageStorageErrorCode.INVALID_FILE);
    }
}
