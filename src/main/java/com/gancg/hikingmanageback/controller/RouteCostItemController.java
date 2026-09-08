package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.RouteCostItemWriteRequest;
import com.gancg.hikingmanageback.entity.RouteCostItem;
import com.gancg.hikingmanageback.service.RouteCostItemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 路线费用管理接口。 */
@RestController
@RequestMapping("/api/route-cost-items")
public class RouteCostItemController {
    private final RouteCostItemService service;

    public RouteCostItemController(RouteCostItemService service) {
        this.service = service;
    }

    /** 查询路线费用分页列表，可按路线ID筛选。 */
    @GetMapping
    @PreAuthorize("hasAuthority('route-cost-item:list')")
    public ApiResponse<RouteDetailPageDto<RouteCostItem>> list(@RequestParam(value = "routeId", required = false) String routeId,
                                                     @Valid RoutePageQueryRequest request) {
        return ApiResponse.success(service.list(routeId, request));
    }

    /** 根据记录ID查询路线费用详情。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('route-cost-item:list')")
    public ApiResponse<RouteCostItem> get(@PathVariable("id") Long id) {
        return ApiResponse.success(service.get(id));
    }

    /** 新增路线费用并返回保存后的记录。 */
    @PostMapping
    @PreAuthorize("hasAuthority('route-cost-item:create')")
    public ApiResponse<RouteCostItem> create(@Valid @RequestBody RouteCostItemWriteRequest request) {
        return ApiResponse.success(service.create(request));
    }

    /** 全量更新路线费用，记录ID由路径确定。 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('route-cost-item:update')")
    public ApiResponse<Void> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody RouteCostItemWriteRequest request) {
        service.update(id, request);
        return ApiResponse.success();
    }

    /** 删除指定路线费用记录。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('route-cost-item:delete')")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
