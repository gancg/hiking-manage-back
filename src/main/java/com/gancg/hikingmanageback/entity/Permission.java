package com.gancg.hikingmanageback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("permissions")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String resourceType;
    private String resource;
    private String action;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private String createdAt;
    private String updatedAt;
}
