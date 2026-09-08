package com.gancg.hikingmanageback.dto.rbac;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RbacUserDto {
    Long id;
    String username;
    String displayName;
    String email;
    String mobile;
    String status;
    List<String> roleCodes;
}
