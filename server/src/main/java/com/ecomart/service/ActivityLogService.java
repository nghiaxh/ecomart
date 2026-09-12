package com.ecomart.service;

import com.ecomart.common.Mapper;
import com.ecomart.common.SecurityUtils;
import com.ecomart.domain.entity.ActivityLog;
import com.ecomart.domain.entity.User;
import com.ecomart.dto.response.ActivityLogResponse;
import com.ecomart.dto.response.PageResponse;
import com.ecomart.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityLogService {

    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String TOGGLE_USER_ACTIVE = "TOGGLE_USER_ACTIVE";
    public static final String CREATE_PRODUCT = "CREATE_PRODUCT";
    public static final String UPDATE_PRODUCT = "UPDATE_PRODUCT";
    public static final String DELETE_PRODUCT = "DELETE_PRODUCT";
    public static final String TOGGLE_PRODUCT_ACTIVE = "TOGGLE_PRODUCT_ACTIVE";
    public static final String CREATE_CATEGORY = "CREATE_CATEGORY";
    public static final String UPDATE_CATEGORY = "UPDATE_CATEGORY";
    public static final String DELETE_CATEGORY = "DELETE_CATEGORY";
    public static final String UPDATE_ORDER_STATUS = "UPDATE_ORDER_STATUS";
    public static final String CONFIRM_PAYMENT = "CONFIRM_PAYMENT";
    public static final String TOGGLE_REVIEW_VISIBILITY = "TOGGLE_REVIEW_VISIBILITY";

    public static final String TYPE_USER = "USER";
    public static final String TYPE_PRODUCT = "PRODUCT";
    public static final String TYPE_CATEGORY = "CATEGORY";
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_REVIEW = "REVIEW";

    private final ActivityLogRepository activityLogRepository;
    private final SecurityUtils securityUtils;

    public ActivityLogService(ActivityLogRepository activityLogRepository, SecurityUtils securityUtils) {
        this.activityLogRepository = activityLogRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public void record(String action, String entityType, Long entityId, String entityName, String detail) {
        User actor = securityUtils.currentUserOrNull();
        if (actor == null) {
            return;
        }
        ActivityLog log = new ActivityLog();
        log.setUserId(actor.getId());
        log.setUsername(actor.getUsername());
        log.setRole(actor.getRole() == null ? null : actor.getRole().name());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityName(entityName);
        log.setDetail(detail);
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> list(int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> logs;
        if (search != null && !search.isBlank()) {
            String q = search.trim();
            logs = activityLogRepository.findByUsernameContainingIgnoreCaseOrActionContainingIgnoreCaseOrDetailContainingIgnoreCase(
                    q, q, q, pageable);
        } else {
            logs = activityLogRepository.findAll(pageable);
        }
        List<ActivityLogResponse> content = logs.getContent().stream().map(Mapper::toActivityLog).toList();
        return new PageResponse<>(content, logs.getNumber(), logs.getSize(),
                logs.getTotalElements(), logs.getTotalPages());
    }
}