package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.TransportCostItemWriteRequest;
import com.gancg.hikingmanageback.entity.TransportCostItem;
import com.gancg.hikingmanageback.service.TransportCostItemService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** 交通费用管理接口。 */
@RestController
@RequestMapping("/api/transport-cost-items")
public class TransportCostItemController {
    private final TransportCostItemService service;

    public TransportCostItemController(TransportCostItemService service) {
        this.service = service;
    }

    /** 查询交通费用分页列表，可按路线ID筛选。 */
    @GetMapping
    @PreAuthorize("hasAuthority('transport-cost-item:list')")
    public ApiResponse<RouteDetailPageDto<TransportCostItem>> list(@RequestParam(value = "routeId", required = false) String routeId,
                                                     @Valid RoutePageQueryRequest request) {
        return ApiResponse.success(service.list(routeId, request));
    }

    /** 根据记录ID查询交通费用详情。 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('transport-cost-item:list')")
    public ApiResponse<TransportCostItem> get(@PathVariable("id") Long id) {
        return ApiResponse.success(service.get(id));
    }

    /** 新增交通费用并返回保存后的记录。 */
    @PostMapping
    @PreAuthorize("hasAuthority('transport-cost-item:create')")
    public ApiResponse<TransportCostItem> create(@Valid @RequestBody TransportCostItemWriteRequest request) {
        return ApiResponse.success(service.create(request));
    }

    /** 全量更新交通费用，记录ID由路径确定。 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('transport-cost-item:update')")
    public ApiResponse<Void> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody TransportCostItemWriteRequest request) {
        service.update(id, request);
        return ApiResponse.success();
    }

    /** 删除指定交通费用记录。 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('transport-cost-item:delete')")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
