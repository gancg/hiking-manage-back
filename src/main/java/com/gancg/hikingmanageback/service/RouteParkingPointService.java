package com.gancg.hikingmanageback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.RouteParkingPointWriteRequest;
import com.gancg.hikingmanageback.entity.RouteParkingPoint;
import com.gancg.hikingmanageback.mapper.RouteParkingPointMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RouteParkingPointService {
    private final RouteParkingPointMapper mapper;
    private final RouteDetailSupport support;

    public RouteParkingPointService(RouteParkingPointMapper mapper, RouteDetailSupport support) {
        this.mapper = mapper;
        this.support = support;
    }

    public RouteDetailPageDto<RouteParkingPoint> list(String routeId, RoutePageQueryRequest request) {
        LambdaQueryWrapper<RouteParkingPoint> query = new LambdaQueryWrapper<>();
        if (routeId != null) {
            support.requireRoute(routeId);
            query.eq(RouteParkingPoint::getRouteId, routeId);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        long total = mapper.selectCount(query);
        List<RouteParkingPoint> records = mapper.selectList(query.orderByAsc(RouteParkingPoint::getId)
                .last("LIMIT " + pageSize + " OFFSET " + offset));
        return RouteDetailPageDto.<RouteParkingPoint>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public RouteParkingPoint get(Long id) {
        RouteParkingPoint entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(404, "路线停车点不存在");
        }
        return entity;
    }

    @Transactional
    public RouteParkingPoint create(RouteParkingPointWriteRequest request) {
        support.requireRoute(request.getRouteId());
        ensureUniqueName(request.getRouteId(), request.getName(), null);
        RouteParkingPoint entity = new RouteParkingPoint();
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        try {
            mapper.insert(entity);
        } catch (DuplicateKeyException exception) {
            throw new BizException(409, "同一路线下停车点名称已存在");
        }
        return entity;
    }

    @Transactional
    public void update(Long id, RouteParkingPointWriteRequest request) {
        RouteParkingPoint entity = get(id);
        support.requireRoute(request.getRouteId());
        ensureUniqueName(request.getRouteId(), request.getName(), id);
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        try {
            mapper.updateById(entity);
        } catch (DuplicateKeyException exception) {
            throw new BizException(409, "同一路线下停车点名称已存在");
        }
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        mapper.deleteById(id);
    }

    private void ensureUniqueName(String routeId, String name, Long currentId) {
        LambdaQueryWrapper<RouteParkingPoint> query = new LambdaQueryWrapper<RouteParkingPoint>()
                .eq(RouteParkingPoint::getRouteId, routeId).eq(RouteParkingPoint::getName, name);
        if (currentId != null) {
            query.ne(RouteParkingPoint::getId, currentId);
        }
        if (mapper.selectCount(query) > 0) {
            throw new BizException(409, "同一路线下停车点名称已存在");
        }
    }
}
