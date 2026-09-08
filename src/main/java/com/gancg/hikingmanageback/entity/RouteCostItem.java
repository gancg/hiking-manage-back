package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 路线费用数据。 */
@Data
@TableName("route_cost_items")
public class RouteCostItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeId;
    private String name;
    private String costType;
    private String billingUnit;
    private Double minCny;
    private Double maxCny;
    private String sourceUrl;
    private String updatedAt;
}
