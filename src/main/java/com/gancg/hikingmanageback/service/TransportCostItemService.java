package com.gancg.hikingmanageback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.TransportCostItemWriteRequest;
import com.gancg.hikingmanageback.entity.TransportCostItem;
import com.gancg.hikingmanageback.mapper.TransportCostItemMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransportCostItemService {
    private final TransportCostItemMapper mapper;
    private final RouteDetailSupport support;

    public TransportCostItemService(TransportCostItemMapper mapper, RouteDetailSupport support) {
        this.mapper = mapper;
        this.support = support;
    }

    public RouteDetailPageDto<TransportCostItem> list(String routeId, RoutePageQueryRequest request) {
        LambdaQueryWrapper<TransportCostItem> query = new LambdaQueryWrapper<>();
        if (routeId != null) {
            support.requireRoute(routeId);
            query.eq(TransportCostItem::getRouteId, routeId);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        long total = mapper.selectCount(query);
        List<TransportCostItem> records = mapper.selectList(query.orderByAsc(TransportCostItem::getId)
                .last("LIMIT " + pageSize + " OFFSET " + offset));
        return RouteDetailPageDto.<TransportCostItem>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public TransportCostItem get(Long id) {
        TransportCostItem entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "交通费用不存在");
        }
        return entity;
    }

    @Transactional
    public TransportCostItem create(TransportCostItemWriteRequest request) {
        support.requireRoute(request.getRouteId());
        TransportCostItem entity = new TransportCostItem();
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        mapper.insert(entity);
        return entity;
    }

    @Transactional
    public void update(Long id, TransportCostItemWriteRequest request) {
        TransportCostItem entity = get(id);
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
