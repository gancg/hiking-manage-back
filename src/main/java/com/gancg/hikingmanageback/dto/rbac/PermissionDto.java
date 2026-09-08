package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PermissionDto {
    Long id;
    String code;
    String name;
    String resource;
    String action;
    String status;
}
