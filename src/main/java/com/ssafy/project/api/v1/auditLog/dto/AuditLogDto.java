package com.ssafy.project.api.v1.auditLog.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long logId;
    private Long userId;
    private String event;
    private String meta;
    private LocalDateTime createdAt;
}
