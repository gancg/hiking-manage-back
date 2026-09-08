package com.gancg.hikingmanageback.dto.routes;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** 路线附属信息分页查询结果。 */
@Value
@Builder
public class RouteDetailPageDto<T> {
    List<T> records;
    long total;
    int pageNum;
    int pageSize;
}
