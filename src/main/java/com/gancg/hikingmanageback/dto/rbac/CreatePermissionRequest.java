package com.gancg.hikingmanageback.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "权限编码不能为空")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    private String name;

    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    @NotBlank(message = "资源标识不能为空")
    private String resource;

    @NotBlank(message = "动作不能为空")
    private String action;

    private Long parentId;

    private Integer sortOrder;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "active|disabled", message = "状态仅支持 active 或 disabled")
    private String status;
}
