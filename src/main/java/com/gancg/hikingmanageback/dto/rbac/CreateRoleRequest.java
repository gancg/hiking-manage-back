package com.gancg.hikingmanageback.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "角色编码不能为空")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "角色状态不能为空")
    @Pattern(regexp = "active|disabled", message = "状态仅支持 active 或 disabled")
    private String status;

    private List<Long> permissionIds;
}
