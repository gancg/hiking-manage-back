package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateRouteRequest extends RouteWriteRequest {
    @NotBlank(message = "路线ID不能为空")
    private String id;
}
