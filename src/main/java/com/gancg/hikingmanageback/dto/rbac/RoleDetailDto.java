package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RoleDetailDto {
    Long id;
    String code;
    String name;
    String description;
    String status;
    List<Long> permissionIds;
}
