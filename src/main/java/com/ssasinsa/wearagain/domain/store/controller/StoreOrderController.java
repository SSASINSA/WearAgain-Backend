package com.ssasinsa.wearagain.domain.store.controller;

import com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreOrderCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCancelResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderListResponse;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import com.ssasinsa.wearagain.domain.store.service.StoreService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/store/orders")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs.TAG_NAME, description = com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs.TAG_DESCRIPTION)
public class StoreOrderController {

    private final StoreService storeService;

    @StoreApiDocs.CreateOrder
    @PostMapping
    public ResponseEntity<StoreOrderCreateResponse> createOrder(
            @Valid @RequestBody StoreOrderCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        StoreOrderCreateResponse response = storeService.createOrder(request, principal.userId());
        return ResponseEntity.status(201).body(response);
    }

    @StoreApiDocs.CancelOrder
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<StoreOrderCancelResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        StoreOrderCancelResponse response = storeService.cancelOrder(orderId, principal.userId());
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.GetOrders
    @GetMapping
    public ResponseEntity<StoreOrderListResponse> getOrders(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "status", required = false) StoreOrderStatus status,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        StoreOrderListResponse response = storeService.getOrders(cursor, size, status, principal.userId());
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.GetOrderDetail
    @GetMapping("/{orderId}")
    public ResponseEntity<StoreOrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        StoreOrderDetailResponse response = storeService.getOrderDetail(orderId, principal.userId());
        return ResponseEntity.ok(response);
    }
}
