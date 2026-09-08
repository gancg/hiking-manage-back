package com.gancg.hikingmanageback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.routes.RouteDetailPageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.TrafficProfileWriteRequest;
import com.gancg.hikingmanageback.dto.routes.CreateTrafficProfileRequest;
import com.gancg.hikingmanageback.entity.TrafficProfile;
import com.gancg.hikingmanageback.mapper.TrafficProfileMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TrafficProfileService {
    private final TrafficProfileMapper mapper;
    private final RouteDetailSupport support;

    public TrafficProfileService(TrafficProfileMapper mapper, RouteDetailSupport support) {
        this.mapper = mapper;
        this.support = support;
    }

    public RouteDetailPageDto<TrafficProfile> list(String routeId, RoutePageQueryRequest request) {
        LambdaQueryWrapper<TrafficProfile> query = new LambdaQueryWrapper<>();
        if (routeId != null) {
            support.requireRoute(routeId);
            query.eq(TrafficProfile::getRouteId, routeId);
        }
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        long total = mapper.selectCount(query);
        List<TrafficProfile> records = mapper.selectList(query.orderByAsc(TrafficProfile::getRouteId)
                .last("LIMIT " + pageSize + " OFFSET " + offset));
        return RouteDetailPageDto.<TrafficProfile>builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public TrafficProfile get(String routeId) {
        TrafficProfile entity = mapper.selectById(routeId);
        if (entity == null) {
            throw new BizException(404, "交通画像不存在");
        }
        return entity;
    }

    @Transactional
    public TrafficProfile create(CreateTrafficProfileRequest request) {
        support.requireRoute(request.getRouteId());
        if (mapper.selectById(request.getRouteId()) != null) {
            throw new BizException(409, "该路线的交通画像已存在");
        }
        support.validateJsonArray(request.getCommonBottlenecksJson(), "常见拥堵点");
        TrafficProfile entity = new TrafficProfile();
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        try {
            mapper.insert(entity);
        } catch (DuplicateKeyException exception) {
            throw new BizException(409, "该路线的交通画像已存在");
        }
        return entity;
    }

    @Transactional
    public void update(String routeId, TrafficProfileWriteRequest request) {
        TrafficProfile entity = get(routeId);
        support.requireRoute(routeId);
        support.validateJsonArray(request.getCommonBottlenecksJson(), "常见拥堵点");
        BeanUtils.copyProperties(request, entity);
        entity.setUpdatedAt(Instant.now().toString());
        mapper.updateById(entity);
    }

    @Transactional
    public void delete(String routeId) {
        get(routeId);
        mapper.deleteById(routeId);
    }
}
