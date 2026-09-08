package com.gancg.hikingmanageback.dto.rbac;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PermissionPageQueryRequest {
    @Min(value = 1, message = "页码必须大于等于 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数必须大于等于 1")
    private Integer pageSize = 10;
}
