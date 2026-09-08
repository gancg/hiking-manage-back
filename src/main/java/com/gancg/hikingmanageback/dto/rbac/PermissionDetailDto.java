package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionDetailDto {
    Long id;
    String code;
    String name;
    String resourceType;
    String resource;
    String action;
    Long parentId;
    Integer sortOrder;
    String status;
}
