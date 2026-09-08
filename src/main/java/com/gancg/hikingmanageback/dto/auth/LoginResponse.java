package com.gancg.hikingmanageback.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String token;
    String tokenType;
    long expiresAtEpochSecond;
    boolean needChangePassword;
    String passwordTip;
    UserProfile user;
}
