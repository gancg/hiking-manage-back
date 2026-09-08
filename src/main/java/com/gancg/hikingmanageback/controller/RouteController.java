package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.routes.CreateRouteRequest;
import com.gancg.hikingmanageback.dto.routes.RoutePageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.UpdateRouteRequest;
import com.gancg.hikingmanageback.entity.Route;
import com.gancg.hikingmanageback.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路线管理接口。
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /** 分页查询路线，按字符串ID升序返回。 */
    @GetMapping
    @PreAuthorize("hasAuthority('route:list')")
    public ApiResponse<RoutePageDto> listRoutes(@Valid RoutePageQueryRequest request) {
        return ApiResponse.success(routeService.listRoutes(request));
    }

    /** 根据路线ID查询路线详情。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('route:list')")
    public ApiResponse<Route> getRoute(@PathVariable("id") String id) {
        return ApiResponse.success(routeService.getRoute(id));
    }

    /** 新增路线，客户端提供唯一的字符串ID。 */
    @PostMapping
    @PreAuthorize("hasAuthority('route:create')")
    public ApiResponse<Void> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        routeService.createRoute(request);
        return ApiResponse.success();
    }

    /** 全量更新路线业务字段，路线ID不可修改。 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('route:update')")
    public ApiResponse<Void> updateRoute(@PathVariable("id") String id,
                                         @Valid @RequestBody UpdateRouteRequest request) {
        routeService.updateRoute(id, request);
        return ApiResponse.success();
    }

    /** 删除路线，并在同一事务内删除费用、停车点、交通画像和出行反馈。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('route:delete')")
    public ApiResponse<Void> deleteRoute(@PathVariable("id") String id) {
        routeService.deleteRoute(id);
        return ApiResponse.success();
    }
}
