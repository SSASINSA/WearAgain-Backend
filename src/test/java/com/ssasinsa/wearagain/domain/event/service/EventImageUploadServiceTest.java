package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class EventImageUploadServiceTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    @TempDir
    Path tempDir;

    @Test
    void should_upload_image_when_request_is_valid() throws IOException {
        EventImageUploadService service = new EventImageUploadService(tempDir.toString());
        byte[] content = "fake-image-data".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                content
        );

        String imageName = service.uploadImage(multipartFile);

        String today = LocalDate.now().format(DATE_FORMATTER);
        assertThat(imageName).startsWith("events/" + today + "/");

        Path storedFile = tempDir.resolve("events").resolve(today)
                .resolve(imageName.substring(imageName.lastIndexOf('/') + 1));
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).containsExactly(content);
    }

    @Test
    void should_fail_when_file_size_exceeds_limit() {
        EventImageUploadService service = new EventImageUploadService(tempDir.toString());
        byte[] large = new byte[(int) (5 * 1024 * 1024L + 1)];
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                large
        );

        assertThatThrownBy(() -> service.uploadImage(multipartFile))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_IMAGE_INFORMATION);
    }

    @Test
    void should_fail_when_content_type_is_not_supported() {
        EventImageUploadService service = new EventImageUploadService(tempDir.toString());
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "text.txt",
                "text/plain",
                "text".getBytes()
        );

        assertThatThrownBy(() -> service.uploadImage(multipartFile))
                .isInstanceOf(EventException.class)
                .hasFieldOrPropertyWithValue("errorCode", EventErrorCode.INVALID_IMAGE_INFORMATION);
    }
}
