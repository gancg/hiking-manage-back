package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 新增交通画像，路线ID不可重复。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateTrafficProfileRequest extends TrafficProfileWriteRequest {
    /** 路线ID。 */
    @NotBlank(message = "路线ID不能为空")
    private String routeId;
}
