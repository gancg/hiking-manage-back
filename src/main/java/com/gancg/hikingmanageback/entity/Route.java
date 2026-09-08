package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("routes")
public class Route {
    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
    private String startLocation;
    private String endLocation;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Double latitude;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Double longitude;
    private Double distanceKm;
    private Integer ascentM;
    private Integer highestAltitudeM;
    private Integer hikingMinutes;
    private String difficulty;
    private Integer durationDays;
    private String routeType;
    private String bestSeasonsJson;
    private String sceneryJson;
    private String risksJson;
    private String transportModesJson;
    private Double costMinCny;
    private Double costMaxCny;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String parking;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String supplies;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String signal;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String camping;
    private String sourceUrl;
    private String sourceName;
    private String collectedAt;
    private String updatedAt;
    private Double confidence;
    private Integer reviewed;
    private Integer hasToilet;
    private Integer hasSupplyShop;
    private Integer isTraverse;
    private Integer traverseTransferMinutes;
    private String groupTourSearchTermsJson;
}
