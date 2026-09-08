package com.gancg.hikingmanageback.dto.routes;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 新增和全量更新使用的业务字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportCostItemWriteRequest extends CostItemWriteRequest {
    /** 交通费用类型。 */
    @NotBlank(message = "交通费用类型不能为空")
    @Pattern(regexp = "fuel|toll|train|bus|other", message = "交通费用类型仅支持 fuel、toll、train、bus、other")
    private String costType;

    /** 交通方式。 */
    @NotBlank(message = "交通方式不能为空")
    @Pattern(regexp = "self_drive|public_transit|carpool|group_tour", message = "交通方式仅支持 self_drive、public_transit、carpool、group_tour")
    private String transportMode;
}
