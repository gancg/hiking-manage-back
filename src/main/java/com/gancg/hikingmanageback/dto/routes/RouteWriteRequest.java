package com.gancg.hikingmanageback.dto.routes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 路线新增和全量更新共用的业务字段。
 */
@Data
public class RouteWriteRequest {
    @NotBlank(message = "路线名称不能为空")
    private String name;
    @NotBlank(message = "起点不能为空")
    private String startLocation;
    @NotBlank(message = "终点不能为空")
    private String endLocation;
    @DecimalMin(value = "-90", message = "纬度不能小于-90")
    @DecimalMax(value = "90", message = "纬度不能大于90")
    private Double latitude;
    @DecimalMin(value = "-180", message = "经度不能小于-180")
    @DecimalMax(value = "180", message = "经度不能大于180")
    private Double longitude;
    @NotNull(message = "距离不能为空")
    @Positive(message = "距离必须大于0")
    private Double distanceKm;
    @NotNull(message = "爬升不能为空")
    @PositiveOrZero(message = "爬升不能为负数")
    private Integer ascentM;
    @NotNull(message = "最高海拔不能为空")
    @PositiveOrZero(message = "最高海拔不能为负数")
    private Integer highestAltitudeM;
    @NotNull(message = "徒步时长不能为空")
    @Positive(message = "徒步时长必须大于0")
    private Integer hikingMinutes;
    @NotBlank(message = "难度不能为空")
    @Pattern(regexp = "easy|moderate|hard|expert", message = "难度仅支持 easy、moderate、hard、expert")
    private String difficulty;
    @NotNull(message = "天数不能为空")
    @Positive(message = "天数必须大于0")
    private Integer durationDays;
    @NotBlank(message = "路线类型不能为空")
    private String routeType;
    @NotBlank(message = "适宜季节JSON不能为空")
    private String bestSeasonsJson;
    @NotBlank(message = "景观JSON不能为空")
    private String sceneryJson;
    @NotBlank(message = "风险JSON不能为空")
    private String risksJson;
    @NotBlank(message = "交通方式JSON不能为空")
    private String transportModesJson;
    @NotNull(message = "费用下限不能为空")
    @PositiveOrZero(message = "费用下限不能为负数")
    private Double costMinCny;
    @NotNull(message = "费用上限不能为空")
    @PositiveOrZero(message = "费用上限不能为负数")
    private Double costMaxCny;
    private String parking;
    private String supplies;
    private String signal;
    private String camping;
    @NotBlank(message = "来源链接不能为空")
    private String sourceUrl;
    @NotBlank(message = "来源名称不能为空")
    private String sourceName;
    @NotBlank(message = "采集时间不能为空")
    private String collectedAt;
    @NotNull(message = "置信度不能为空")
    @DecimalMin(value = "0", message = "置信度不能小于0")
    @DecimalMax(value = "1", message = "置信度不能大于1")
    private Double confidence;
    @NotNull(message = "审核标记不能为空")
    @Min(value = 0, message = "审核标记只能为0或1")
    @Max(value = 1, message = "审核标记只能为0或1")
    private Integer reviewed = 0;
    @NotNull(message = "厕所标记不能为空")
    @Min(value = 0, message = "厕所标记只能为0或1")
    @Max(value = 1, message = "厕所标记只能为0或1")
    private Integer hasToilet = 0;
    @NotNull(message = "补给店标记不能为空")
    @Min(value = 0, message = "补给店标记只能为0或1")
    @Max(value = 1, message = "补给店标记只能为0或1")
    private Integer hasSupplyShop = 0;
    @NotNull(message = "穿越标记不能为空")
    @Min(value = 0, message = "穿越标记只能为0或1")
    @Max(value = 1, message = "穿越标记只能为0或1")
    private Integer isTraverse = 0;
    @NotNull(message = "穿越接驳时长不能为空")
    @PositiveOrZero(message = "穿越接驳时长不能为负数")
    private Integer traverseTransferMinutes = 0;
    @NotBlank(message = "跟团搜索词JSON不能为空")
    private String groupTourSearchTermsJson = "[]";

    @JsonIgnore
    @AssertTrue(message = "费用上限不能小于费用下限")
    public boolean isCostRangeValid() {
        return costMinCny == null || costMaxCny == null || costMaxCny >= costMinCny;
    }
}
