package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserKeywordScope;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserResponse;
import com.ssasinsa.wearagain.domain.user.exception.AdminManagementErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.AdminManagementException;
import com.ssasinsa.wearagain.domain.user.repository.AdminManagedUserSpecifications;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminManagedUserServiceImpl implements AdminManagedUserService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminManagedUserListResponse getAdminUsers(
            AdminStatus status,
            String keyword,
            AdminManagedUserKeywordScope keywordScope,
            int page,
            int size,
            String sortBy
    ) {
        validatePageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
        Specification<AdminUser> specification = Specification.allOf(
                AdminManagedUserSpecifications.statusEquals(status),
                AdminManagedUserSpecifications.keywordMatches(keyword, keywordScope)
        );
        Page<AdminUser> result = adminUserRepository.findAll(specification, pageable);
        List<AdminManagedUserResponse> content = result.getContent()
                .stream()
                .map(AdminManagedUserResponse::from)
                .toList();
        return AdminManagedUserListResponse.of(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public void softDeleteAdminUser(Long adminUserId, Long actorAdminId) {
        if (adminUserId == null || actorAdminId == null) {
            throw new AdminManagementException(AdminManagementErrorCode.INVALID_REQUEST);
        }
        if (adminUserId.equals(actorAdminId)) {
            throw new AdminManagementException(AdminManagementErrorCode.DELETE_FORBIDDEN);
        }

        AdminUser target = adminUserRepository.findById(adminUserId)
                .orElseThrow(() -> new AdminManagementException(AdminManagementErrorCode.ADMIN_USER_NOT_FOUND));

        if (target.getRole() == AdminRole.SUPER_ADMIN) {
            throw new AdminManagementException(AdminManagementErrorCode.DELETE_FORBIDDEN);
        }

        if (target.getStatus() == AdminStatus.INACTIVE) {
            return;
        }

        target.markInactive();
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new AdminManagementException(AdminManagementErrorCode.INVALID_REQUEST);
        }
    }

    private Sort resolveSort(String sortBy) {
        String normalized = StringUtils.hasText(sortBy) ? sortBy.trim().toUpperCase() : "CREATED_DESC";
        return switch (normalized) {
            case "CREATED_ASC" -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case "NAME_ASC" -> Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
            case "NAME_DESC" -> Sort.by(Sort.Order.desc("name"), Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        };
    }
}
