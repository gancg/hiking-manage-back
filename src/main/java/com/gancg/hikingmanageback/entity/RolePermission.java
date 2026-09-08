package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role_permissions")
public class RolePermission {
    private Long roleId;
    private Long permissionId;
    private String createdAt;
}
