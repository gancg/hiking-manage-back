package com.gancg.hikingmanageback.dto.rbac;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "显示名不能为空")
    private String displayName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String mobile;

    @NotEmpty(message = "角色不能为空")
    private List<Long> roleIds;
}
