package com.gancg.hikingmanageback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.routes.CreateRouteRequest;
import com.gancg.hikingmanageback.dto.routes.RoutePageDto;
import com.gancg.hikingmanageback.dto.routes.RoutePageQueryRequest;
import com.gancg.hikingmanageback.dto.routes.UpdateRouteRequest;
import com.gancg.hikingmanageback.dto.routes.RouteWriteRequest;
import com.gancg.hikingmanageback.entity.Route;
import com.gancg.hikingmanageback.mapper.RouteMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RouteService {
    private final RouteMapper routeMapper;
    private final ObjectMapper objectMapper;

    public RouteService(RouteMapper routeMapper, ObjectMapper objectMapper) {
        this.routeMapper = routeMapper;
        this.objectMapper = objectMapper;
    }

    public RoutePageDto listRoutes(RoutePageQueryRequest request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        long offset = (long) (pageNum - 1) * pageSize;
        List<Route> records = routeMapper.selectRoutePage(offset, pageSize);
        long total = routeMapper.selectCount(null);
        return RoutePageDto.builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public Route getRoute(String routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BizException(404, "路线不存在");
        }
        return route;
    }

    @Transactional
    public void createRoute(CreateRouteRequest request) {
        ensureUniqueId(request.getId());
        validateJsonFields(request);
        Route route = new Route();
        BeanUtils.copyProperties(request, route);
        route.setUpdatedAt(Instant.now().toString());
        try {
            routeMapper.insert(route);
        } catch (DuplicateKeyException exception) {
            throw new BizException(409, "路线ID已存在");
        }
    }

    @Transactional
    public void updateRoute(String routeId, UpdateRouteRequest request) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BizException(404, "路线不存在");
        }
        validateJsonFields(request);
        BeanUtils.copyProperties(request, route);
        route.setUpdatedAt(Instant.now().toString());
        routeMapper.updateById(route);
    }

    @Transactional
    public void deleteRoute(String routeId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BizException(404, "路线不存在");
        }
        // 在同一事务内先删除关联数据，保证未启用外键的连接也不会留下孤立记录。
        routeMapper.deleteRouteCostItems(routeId);
        routeMapper.deleteTransportCostItems(routeId);
        routeMapper.deleteParkingPoints(routeId);
        routeMapper.deleteTrafficProfile(routeId);
        routeMapper.deleteTripFeedback(routeId);
        routeMapper.deleteById(routeId);
    }

    private void ensureUniqueId(String routeId) {
        if (routeMapper.selectById(routeId) != null) {
            throw new BizException(409, "路线ID已存在");
        }
    }

    private void validateJsonFields(RouteWriteRequest request) {
        validateJsonArray(request.getBestSeasonsJson(), "适宜季节");
        validateJsonArray(request.getSceneryJson(), "景观");
        validateJsonArray(request.getRisksJson(), "风险");
        validateJsonArray(request.getTransportModesJson(), "交通方式");
        validateJsonArray(request.getGroupTourSearchTermsJson(), "跟团搜索词");
    }

    private void validateJsonArray(String value, String fieldName) {
        try {
            JsonNode node = objectMapper.reader()
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(value);
            if (node == null || !node.isArray()) {
                throw new BizException(400, fieldName + "必须为JSON数组文本");
            }
        } catch (JsonProcessingException exception) {
            throw new BizException(400, fieldName + "必须为JSON数组文本");
        }
    }
}
