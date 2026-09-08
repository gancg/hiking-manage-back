package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleDto {
    Long id;
    String code;
    String name;
    String status;
}
