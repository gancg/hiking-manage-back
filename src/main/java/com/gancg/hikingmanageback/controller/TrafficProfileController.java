package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.TrafficProfileWriteRequest;
import com.gancg.hikingmanageback.dto.routes.CreateTrafficProfileRequest;
import com.gancg.hikingmanageback.entity.TrafficProfile;
import com.gancg.hikingmanageback.service.TrafficProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 交通画像管理接口。 */
@RestController
@RequestMapping("/api/traffic-profiles")
public class TrafficProfileController {
    private final TrafficProfileService service;

    public TrafficProfileController(TrafficProfileService service) {
        this.service = service;
    }

    /** 查询交通画像分页列表，可按路线ID筛选。 */
    @GetMapping
    @PreAuthorize("hasAuthority('traffic-profile:list')")
    public ApiResponse<RouteDetailPageDto<TrafficProfile>> list(@RequestParam(value = "routeId", required = false) String routeId,
                                                     @Valid RoutePageQueryRequest request) {
        return ApiResponse.success(service.list(routeId, request));
    }

    /** 根据路线ID查询交通画像详情。 */
    @GetMapping("/{routeId}")
    @PreAuthorize("hasAuthority('traffic-profile:list')")
    public ApiResponse<TrafficProfile> get(@PathVariable("routeId") String routeId) {
        return ApiResponse.success(service.get(routeId));
    }

    /** 新增交通画像并返回保存后的记录。 */
    @PostMapping
    @PreAuthorize("hasAuthority('traffic-profile:create')")
    public ApiResponse<TrafficProfile> create(@Valid @RequestBody CreateTrafficProfileRequest request) {
        return ApiResponse.success(service.create(request));
    }

    /** 全量更新交通画像，路线ID由路径确定。 */
    @PutMapping("/{routeId}")
    @PreAuthorize("hasAuthority('traffic-profile:update')")
    public ApiResponse<Void> update(@PathVariable("routeId") String routeId,
                                    @Valid @RequestBody TrafficProfileWriteRequest request) {
        service.update(routeId, request);
        return ApiResponse.success();
    }

    /** 删除指定交通画像记录。 */
    @DeleteMapping("/{routeId}")
    @PreAuthorize("hasAuthority('traffic-profile:delete')")
    public ApiResponse<Void> delete(@PathVariable("routeId") String routeId) {
        service.delete(routeId);
        return ApiResponse.success();
    }
}
