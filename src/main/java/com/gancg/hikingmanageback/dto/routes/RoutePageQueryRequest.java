package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 路线分页查询参数。 */
@Data
public class RoutePageQueryRequest {
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于等于 1")
    private Integer pageNum = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数必须大于等于 1")
    private Integer pageSize = 10;
}
