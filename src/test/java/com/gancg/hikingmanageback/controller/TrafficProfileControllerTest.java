package com.gancg.hikingmanageback.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** 交通画像接口及数据库约束测试。 */
@WithMockUser(authorities = {"traffic-profile:list", "traffic-profile:create", "traffic-profile:update", "traffic-profile:delete"})
class TrafficProfileControllerTest extends AbstractRouteDetailControllerTest {
    @Override
    protected String permissionPrefix() {
        return "traffic-profile";
    }

    @Override
    protected String path() {
        return "/api/traffic-profiles";
    }

    @Override
    protected String table() {
        return "traffic_profiles";
    }

    @Override
    protected ObjectNode body(String routeId) throws Exception {
        ObjectNode request = (ObjectNode) objectMapper.readTree("""
                {"baseOneWayMinutes":60,"weekdayExtraMin":10,"weekdayExtraMax":20,"weekendExtraMin":20,"weekendExtraMax":40,"holidayExtraMin":30,"holidayExtraMax":60,"morningExtraMinutes":10,"eveningExtraMinutes":20,"commonBottlenecksJson":"[\\"入口路段\\"]","bestDepartureTime":"07:00","suggestedReturnTime":"16:00","sourceUrl":"https://example.com/traffic","confidence":0.8}
                """);
        return request.put("routeId", routeId);
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("routeId", "\"\""),
                Arguments.of("baseOneWayMinutes", "-1"),
                Arguments.of("baseOneWayMinutes", "null"),
                Arguments.of("confidence", "1.1"),
                Arguments.of("confidence", "-0.1"),
                Arguments.of("weekdayExtraMin", "-1"),
                Arguments.of("weekdayExtraMax", "1"),
                Arguments.of("weekendExtraMax", "1"),
                Arguments.of("holidayExtraMax", "1"),
                Arguments.of("morningExtraMinutes", "-1"),
                Arguments.of("eveningExtraMinutes", "-1"),
                Arguments.of("weekdayExtraMin", "null"));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void invalidFieldsRejectWrites(String field, String value) throws Exception {
        ObjectNode original = body("route-a");
        ObjectNode invalid = original.deepCopy();
        invalid.set(field, objectMapper.readTree(value));
        expectCreateCode(invalid, 400);
        String id = identity(create(original));
        // 更新接口不接受路线ID字段，由路径指定所属路线。
        if (!field.equals("routeId")) {
            update(id, invalid, 400);
        }
        assertFields(detail(id), original);
    }

    @Override
    protected boolean trafficProfile() {
        return true;
    }

    @Test
    void onlyOneProfilePerRouteAndPathIdentityCannotChange() throws Exception {
        ObjectNode original = body("route-a");
        create(original);
        expectCreateCode(original, 409);
        assertFields(detail("route-a"), original);
        ObjectNode update = body("route-b").put("baseOneWayMinutes", 90);
        update("route-a", update, 0);
        assertThat(detail("route-a").get("routeId").asText()).isEqualTo("route-a");
        assertThat(detail("route-a").get("baseOneWayMinutes").asInt()).isEqualTo(90);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path() + "/route-b"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(404));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{}", "null", "[] []"})
    void invalidJsonIsRejectedOnCreateAndUpdate(String json) throws Exception {
        ObjectNode original = body("route-a");
        ObjectNode invalid = original.deepCopy().put("commonBottlenecksJson", json);
        expectCreateCode(invalid, 400);
        create(original);
        update("route-a", invalid, 400);
        assertFields(detail("route-a"), original);
    }

    @Test
    void omittedExtrasDefaultToZeroAndTimesCanBeCleared() throws Exception {
        ObjectNode request = body("route-a");
        java.util.List<String> extras = java.util.List.of("weekdayExtraMin", "weekdayExtraMax",
                "weekendExtraMin", "weekendExtraMax", "holidayExtraMin", "holidayExtraMax",
                "morningExtraMinutes", "eveningExtraMinutes");
        request.remove(extras);
        JsonNode saved = create(request);
        for (String field : extras) {
            assertThat(saved.get(field).asInt()).as(field).isZero();
        }
        request.remove(java.util.List.of("bestDepartureTime", "suggestedReturnTime"));
        update("route-a", request, 0);
        assertThat(detail("route-a").get("bestDepartureTime").isNull()).isTrue();
        assertThat(detail("route-a").get("suggestedReturnTime").isNull()).isTrue();
    }
}
