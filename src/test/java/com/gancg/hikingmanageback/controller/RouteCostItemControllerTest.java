package com.gancg.hikingmanageback.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** 路线费用接口及数据库约束测试。 */
@WithMockUser(authorities = {"route-cost-item:list", "route-cost-item:create", "route-cost-item:update", "route-cost-item:delete"})
class RouteCostItemControllerTest extends AbstractRouteDetailControllerTest {
    @Override
    protected String permissionPrefix() {
        return "route-cost-item";
    }

    @Override
    protected String path() {
        return "/api/route-cost-items";
    }

    @Override
    protected String table() {
        return "route_cost_items";
    }

    @Override
    protected ObjectNode body(String routeId) throws Exception {
        ObjectNode request = (ObjectNode) objectMapper.readTree("""
                {"name":"测试费用","costType":"ticket","billingUnit":"person","minCny":10.5,"maxCny":30.5,"sourceUrl":"https://example.com/cost"}
                """);
        return request.put("routeId", routeId);
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("name", "\"\""),
                Arguments.of("routeId", "\"\""),
                Arguments.of("costType", "\"fuel\""),
                Arguments.of("billingUnit", "\"invalid\""),
                Arguments.of("minCny", "-1"),
                Arguments.of("maxCny", "1"),
                Arguments.of("minCny", "null"));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void invalidFieldsRejectWrites(String field, String value) throws Exception {
        ObjectNode original = body("route-a");
        ObjectNode invalid = original.deepCopy();
        invalid.set(field, objectMapper.readTree(value));
        expectCreateCode(invalid, 400);
        String id = identity(create(original));
        update(id, invalid, 400);
        assertFields(detail(id), original);
    }

    @Test
    void updateRejectsMissingParentWithoutChangingRecord() throws Exception {
        ObjectNode original = body("route-a");
        String id = identity(create(original));
        update(id, original.deepCopy().put("routeId", "missing"), 404);
        assertFields(detail(id), original);
    }
}
