package com.gancg.hikingmanageback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.RouteCostItemWriteRequest;
import com.gancg.hikingmanageback.entity.RouteCostItem;
import com.gancg.hikingmanageback.mapper.RouteCostItemMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RouteCostItemService {
    private final RouteCostItemMapper mapper;
    private final RouteDetailSupport support;

    public RouteCostItemService(RouteCostItemMapper mapper, RouteDetailSupport support) {
        this.mapper = mapper;
        this.support = support;
    }

    public RouteDetailPageDto<RouteCostItem> list(String routeId, RoutePageQueryRequest request) {
        LambdaQueryWrapper<RouteCostItem> query = new LambdaQueryWrapper<>();
        if (routeId != null) {
            support.requireRoute(routeId);
            query.eq(RouteCostItem::getRouteId, routeId);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        long total = mapper.selectCount(query);
        List<RouteCostItem> records = mapper.selectList(query.orderByAsc(RouteCostItem::getId)
                .last("LIMIT " + pageSize + " OFFSET " + offset));
        return RouteDetailPageDto.<RouteCostItem>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public RouteCostItem get(Long id) {
        RouteCostItem entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "路线费用不存在");
        }
        return entity;
    }

    @Transactional
    public RouteCostItem create(RouteCostItemWriteRequest request) {
        support.requireRoute(request.getRouteId());
        RouteCostItem entity = new RouteCostItem();
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        mapper.insert(entity);
        return entity;
    }

    @Transactional
    public void update(Long id, RouteCostItemWriteRequest request) {
        RouteCostItem entity = get(id);
        support.requireRoute(request.getRouteId());
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        mapper.updateById(entity);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        mapper.deleteById(id);
    }
}
