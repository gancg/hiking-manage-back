package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 路线停车点数据。 */
@Data
@TableName("route_parking_points")
public class RouteParkingPoint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeId;
    private String name;
    private Double latitude;
    private Double longitude;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String note;
    private Integer isRecommended;
    private Integer isReviewed;
    private String sourceUrl;
    private String updatedAt;
}
