package com.gancg.hikingmanageback.dto.rbac;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @Pattern(regexp = "active|disabled", message = "状态仅支持 active 或 disabled")
    private String status;
}
