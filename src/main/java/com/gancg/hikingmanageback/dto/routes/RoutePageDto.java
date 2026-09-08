package com.gancg.hikingmanageback.dto.routes;

import com.gancg.hikingmanageback.entity.Route;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/** 路线分页查询结果。 */
@Value
@Builder
public class RoutePageDto {
    List<Route> records;
    long total;
    int pageNum;
    int pageSize;
}
