package com.gancg.hikingmanageback.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    @NotBlank
    private String defaultPassword;

    @NotBlank
    private String defaultPasswordTip;
}
