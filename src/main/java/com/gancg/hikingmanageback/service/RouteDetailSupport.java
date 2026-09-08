package com.gancg.hikingmanageback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.mapper.RouteMapper;
import org.springframework.stereotype.Component;

/** 路线附属信息共用的关联及JSON校验。 */
@Component
public class RouteDetailSupport {
    private final RouteMapper routeMapper;
    private final ObjectMapper objectMapper;

    public RouteDetailSupport(RouteMapper routeMapper, ObjectMapper objectMapper) {
        this.routeMapper = routeMapper;
        this.objectMapper = objectMapper;
    }

    public void requireRoute(String routeId) {
        if (routeId == null || routeId.isBlank()) {
            throw new BizException(400, "路线ID不能为空");
        }
        if (routeMapper.selectById(routeId) == null) {
            throw new BizException(404, "路线不存在");
        }
    }

    public void validateJsonArray(String value, String fieldName) {
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
