package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 新增和全量更新使用的业务字段。 */
@Data
public class RouteParkingPointWriteRequest {
    /** 路线ID。 */
    @NotBlank(message = "路线ID不能为空")
    private String routeId;

    /** 停车点名称。 */
    @NotBlank(message = "停车点名称不能为空")
    private String name;

    /** 纬度。 */
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度不能小于-90")
    @DecimalMax(value = "90", message = "纬度不能大于90")
    private Double latitude;

    /** 经度。 */
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180", message = "经度不能小于-180")
    @DecimalMax(value = "180", message = "经度不能大于180")
    private Double longitude;

    /** 备注。 */
    private String note;

    /** 推荐标记。 */
    @NotNull(message = "推荐标记不能为空")
    @Min(value = 0, message = "推荐标记只能为0或1")
    @Max(value = 1, message = "推荐标记只能为0或1")
    private Integer isRecommended = 0;

    /** 审核标记。 */
    @NotNull(message = "审核标记不能为空")
    @Min(value = 0, message = "审核标记只能为0或1")
    @Max(value = 1, message = "审核标记只能为0或1")
    private Integer isReviewed = 0;

    /** 来源链接。 */
    @NotBlank(message = "来源链接不能为空")
    private String sourceUrl;
}
