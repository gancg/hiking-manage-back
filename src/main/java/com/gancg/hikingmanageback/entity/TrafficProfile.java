package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 交通画像数据。 */
@Data
@TableName("traffic_profiles")
public class TrafficProfile {
    @TableId(type = IdType.INPUT)
    private String routeId;
    private Integer baseOneWayMinutes;
    private Integer weekdayExtraMin;
    private Integer weekdayExtraMax;
    private Integer weekendExtraMin;
    private Integer weekendExtraMax;
    private Integer holidayExtraMin;
    private Integer holidayExtraMax;
    private Integer morningExtraMinutes;
    private Integer eveningExtraMinutes;
    private String commonBottlenecksJson;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String bestDepartureTime;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String suggestedReturnTime;
    private String sourceUrl;
    private String updatedAt;
    private Double confidence;
}
