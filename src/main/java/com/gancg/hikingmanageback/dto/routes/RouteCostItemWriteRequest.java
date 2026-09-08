package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 新增和全量更新使用的业务字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RouteCostItemWriteRequest extends CostItemWriteRequest {
    /** 路线费用类型。 */
    @NotBlank(message = "路线费用类型不能为空")
    @Pattern(regexp = "ticket|shuttle|waste|parking|other", message = "路线费用类型仅支持 ticket、shuttle、waste、parking、other")
    private String costType;
}
