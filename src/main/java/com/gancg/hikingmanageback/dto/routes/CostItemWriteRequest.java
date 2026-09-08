package com.gancg.hikingmanageback.dto.routes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

/** 新增和全量更新使用的业务字段。 */
@Data
public class CostItemWriteRequest {
    /** 路线ID。 */
    @NotBlank(message = "路线ID不能为空")
    private String routeId;

    /** 费用名称。 */
    @NotBlank(message = "费用名称不能为空")
    private String name;

    /** 计费单位。 */
    @NotBlank(message = "计费单位不能为空")
    @Pattern(regexp = "person|vehicle|group", message = "计费单位仅支持 person、vehicle、group")
    private String billingUnit;

    /** 费用下限。 */
    @NotNull(message = "费用下限不能为空")
    @PositiveOrZero(message = "费用下限不能为负数")
    private Double minCny;

    /** 费用上限。 */
    @NotNull(message = "费用上限不能为空")
    @PositiveOrZero(message = "费用上限不能为负数")
    private Double maxCny;

    /** 来源链接。 */
    @NotBlank(message = "来源链接不能为空")
    private String sourceUrl;

    @JsonIgnore
    @AssertTrue(message = "费用上限不能小于费用下限")
    public boolean isCostRangeValid() {
        return minCny == null || maxCny == null || maxCny >= minCny;
    }
}
