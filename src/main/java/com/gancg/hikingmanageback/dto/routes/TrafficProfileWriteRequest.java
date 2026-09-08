package com.gancg.hikingmanageback.dto.routes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

/** 新增和全量更新使用的业务字段。 */
@Data
public class TrafficProfileWriteRequest {
    /** 基础单程耗时。 */
    @NotNull(message = "基础单程耗时不能为空")
    @PositiveOrZero(message = "基础单程耗时不能为负数")
    private Integer baseOneWayMinutes;

    /** 工作日额外耗时下限。 */
    @NotNull(message = "工作日额外耗时下限不能为空")
    @PositiveOrZero(message = "工作日额外耗时下限不能为负数")
    private Integer weekdayExtraMin = 0;

    /** 工作日额外耗时上限。 */
    @NotNull(message = "工作日额外耗时上限不能为空")
    @PositiveOrZero(message = "工作日额外耗时上限不能为负数")
    private Integer weekdayExtraMax = 0;

    /** 周末额外耗时下限。 */
    @NotNull(message = "周末额外耗时下限不能为空")
    @PositiveOrZero(message = "周末额外耗时下限不能为负数")
    private Integer weekendExtraMin = 0;

    /** 周末额外耗时上限。 */
    @NotNull(message = "周末额外耗时上限不能为空")
    @PositiveOrZero(message = "周末额外耗时上限不能为负数")
    private Integer weekendExtraMax = 0;

    /** 节假日额外耗时下限。 */
    @NotNull(message = "节假日额外耗时下限不能为空")
    @PositiveOrZero(message = "节假日额外耗时下限不能为负数")
    private Integer holidayExtraMin = 0;

    /** 节假日额外耗时上限。 */
    @NotNull(message = "节假日额外耗时上限不能为空")
    @PositiveOrZero(message = "节假日额外耗时上限不能为负数")
    private Integer holidayExtraMax = 0;

    /** 早间额外耗时。 */
    @NotNull(message = "早间额外耗时不能为空")
    @PositiveOrZero(message = "早间额外耗时不能为负数")
    private Integer morningExtraMinutes = 0;

    /** 晚间额外耗时。 */
    @NotNull(message = "晚间额外耗时不能为空")
    @PositiveOrZero(message = "晚间额外耗时不能为负数")
    private Integer eveningExtraMinutes = 0;

    /** 常见拥堵点JSON。 */
    @NotBlank(message = "常见拥堵点JSON不能为空")
    private String commonBottlenecksJson;

    /** 最佳出发时间。 */
    private String bestDepartureTime;

    /** 建议返程时间。 */
    private String suggestedReturnTime;

    /** 来源链接。 */
    @NotBlank(message = "来源链接不能为空")
    private String sourceUrl;

    /** 置信度。 */
    @NotNull(message = "置信度不能为空")
    @DecimalMin(value = "0", message = "置信度不能小于0")
    @DecimalMax(value = "1", message = "置信度不能大于1")
    private Double confidence;

    @JsonIgnore
    @AssertTrue(message = "工作日额外耗时上限不能小于下限")
    public boolean isWeekdayRangeValid() {
        return weekdayExtraMin == null || weekdayExtraMax == null || weekdayExtraMax >= weekdayExtraMin;
    }

    @JsonIgnore
    @AssertTrue(message = "周末额外耗时上限不能小于下限")
    public boolean isWeekendRangeValid() {
        return weekendExtraMin == null || weekendExtraMax == null || weekendExtraMax >= weekendExtraMin;
    }

    @JsonIgnore
    @AssertTrue(message = "节假日额外耗时上限不能小于下限")
    public boolean isHolidayRangeValid() {
        return holidayExtraMin == null || holidayExtraMax == null || holidayExtraMax >= holidayExtraMin;
    }
}
