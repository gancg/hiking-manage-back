package com.gancg.hikingmanageback.dto.auth;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserProfile {
    Long id;
    String username;
    String displayName;
    String status;
    List<String> roles;
    List<String> permissions;
}
