package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.RouteParkingPointWriteRequest;
import com.gancg.hikingmanageback.entity.RouteParkingPoint;
import com.gancg.hikingmanageback.service.RouteParkingPointService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 路线停车点管理接口。 */
@RestController
@RequestMapping("/api/route-parking-points")
public class RouteParkingPointController {
    private final RouteParkingPointService service;

    public RouteParkingPointController(RouteParkingPointService service) {
        this.service = service;
    }

    /** 查询路线停车点分页列表，可按路线ID筛选。 */
    @GetMapping
    @PreAuthorize("hasAuthority('route-parking-point:list')")
    public ApiResponse<RouteDetailPageDto<RouteParkingPoint>> list(@RequestParam(value = "routeId", required = false) String routeId,
                                                     @Valid RoutePageQueryRequest request) {
        return ApiResponse.success(service.list(routeId, request));
    }

    /** 根据记录ID查询路线停车点详情。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('route-parking-point:list')")
    public ApiResponse<RouteParkingPoint> get(@PathVariable("id") Long id) {
        return ApiResponse.success(service.get(id));
    }

    /** 新增路线停车点并返回保存后的记录。 */
    @PostMapping
    @PreAuthorize("hasAuthority('route-parking-point:create')")
    public ApiResponse<RouteParkingPoint> create(@Valid @RequestBody RouteParkingPointWriteRequest request) {
        return ApiResponse.success(service.create(request));
    }

    /** 全量更新路线停车点，记录ID由路径确定。 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('route-parking-point:update')")
    public ApiResponse<Void> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody RouteParkingPointWriteRequest request) {
        service.update(id, request);
        return ApiResponse.success();
    }

    /** 删除指定路线停车点记录。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('route-parking-point:delete')")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
