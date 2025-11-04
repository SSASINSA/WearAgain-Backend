package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventImageUploadResponse;
import com.ssasinsa.wearagain.domain.event.service.EventImageUploadService;
import com.ssasinsa.wearagain.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = EventApiDocs.TAG_NAME, description = EventApiDocs.TAG_DESCRIPTION)
@SecurityRequirement(name = "adminJWT")
@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;
    private final EventImageUploadService eventImageUploadService;

    @EventApiDocs.CreateEvent
    @PostMapping
    public ResponseEntity<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventCreateResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @EventApiDocs.UploadImage
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventImageUploadResponse> uploadEventImage(@RequestPart("file") MultipartFile file) {
        String imageName = eventImageUploadService.uploadImage(file);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/upload/")
                .path(imageName)
                .toUriString();
        EventImageUploadResponse response = new EventImageUploadResponse(imageName, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
