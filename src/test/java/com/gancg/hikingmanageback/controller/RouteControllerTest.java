package com.gancg.hikingmanageback.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用真实表结构的独立内存数据库验证接口，不修改业务数据库。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1"
})
@AutoConfigureMockMvc
@Sql("/routes-schema.sql")
@WithMockUser(authorities = {"route:list", "route:create", "route:update", "route:delete"})
class RouteControllerTest {
    private static final List<String> CHILD_TABLES = List.of("route_cost_items", "transport_cost_items",
            "route_parking_points", "traffic_profiles", "trip_feedback");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createListGetAndUpdateWithRealSchema() throws Exception {
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
        ObjectNode body = routeBody("route-b");
        createRoute(body);
        createRoute(routeBody("route-a"));

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("route-a"))
                .andExpect(jsonPath("$.data.records[1].id").value("route-b"));
        assertRouteFields("route-b", body);

        jdbcTemplate.update("UPDATE routes SET updated_at = ? WHERE id = ?", "2000-01-01T00:00:00Z", "route-b");
        body.remove("id");
        body.put("name", "更新后的路线");
        body.put("distanceKm", 15.5);
        body.put("groupTourSearchTermsJson", "[\"徒步跟团\"]");
        for (String field : List.of("latitude", "longitude", "parking", "supplies", "signal", "camping")) {
            body.putNull(field);
        }
        mockMvc.perform(put("/api/routes/route-b").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        assertRouteFields("route-b", body);
        assertThat(jdbcTemplate.queryForObject("SELECT updated_at FROM routes WHERE id = 'route-b'", String.class))
                .isNotEqualTo("2000-01-01T00:00:00Z");
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM routes WHERE id = 'route-a'", String.class))
                .isEqualTo("测试路线");
    }

    @Test
    void listRoutesPaginatesInStringIdOrder() throws Exception {
        for (String id : List.of("route-2", "route-1", "route-10")) {
            createRoute(routeBody(id));
        }
        mockMvc.perform(get("/api/routes").param("pageNum", "1").param("pageSize", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value("route-1"))
                .andExpect(jsonPath("$.data.records[1].id").value("route-10"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2));
        String response = mockMvc.perform(get("/api/routes").param("pageNum", "2").param("pageSize", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value("route-2"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode actual = objectMapper.readTree(response).get("data").get("records").get(0);
        routeBody("route-2").fields().forEachRemaining(field -> assertThat(actual.get(field.getKey()))
                .as(field.getKey()).isEqualTo(field.getValue()));
        for (String pageNum : List.of("3", "2147483647")) {
            mockMvc.perform(get("/api/routes").param("pageNum", pageNum).param("pageSize", "2"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.records").isEmpty())
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.pageNum").value(Integer.parseInt(pageNum)))
                    .andExpect(jsonPath("$.data.pageSize").value(2));
        }
    }

    @Test
    void listRoutesDefaultsToTenRecords() throws Exception {
        for (int i = 10; i >= 0; i--) {
            createRoute(routeBody(String.format("route-%02d", i)));
        }
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.records[0].id").value("route-00"))
                .andExpect(jsonPath("$.data.records[9].id").value("route-09"))
                .andExpect(jsonPath("$.data.total").value(11))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @ParameterizedTest
    @CsvSource({"pageNum,0", "pageNum,-1", "pageSize,0", "pageSize,-1",
            "pageNum,abc", "pageSize,abc", "pageNum,''", "pageSize,''"})
    void invalidPaginationReturnsBadRequest(String field, String value) throws Exception {
        mockMvc.perform(get("/api/routes").param(field, value))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void omittedDefaultFieldsUseSchemaDefaults() throws Exception {
        ObjectNode body = routeBody("defaults");
        body.remove(List.of("reviewed", "hasToilet", "hasSupplyShop", "isTraverse",
                "traverseTransferMinutes", "groupTourSearchTermsJson"));
        createRoute(body);
        mockMvc.perform(get("/api/routes/defaults"))
                .andExpect(jsonPath("$.data.reviewed").value(0))
                .andExpect(jsonPath("$.data.hasToilet").value(0))
                .andExpect(jsonPath("$.data.hasSupplyShop").value(0))
                .andExpect(jsonPath("$.data.isTraverse").value(0))
                .andExpect(jsonPath("$.data.traverseTransferMinutes").value(0))
                .andExpect(jsonPath("$.data.groupTourSearchTermsJson").value("[]"));
    }

    @Test
    void duplicateIdReturnsConflictWithoutOverwriting() throws Exception {
        createRoute(routeBody("same-id"));
        ObjectNode duplicate = routeBody("same-id").put("name", "重复路线");
        mockMvc.perform(post("/api/routes").contentType(MediaType.APPLICATION_JSON).content(duplicate.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(409));
        assertThat(jdbcTemplate.queryForObject("SELECT name FROM routes WHERE id = 'same-id'", String.class))
                .isEqualTo("测试路线");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void missingRouteReturnsNotFound(String method) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), "/api/routes/missing")
                        .contentType(MediaType.APPLICATION_JSON).content(routeBody("missing").toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
    }

    @ParameterizedTest
    @CsvSource({"name,\"\"", "id,\"\"", "distanceKm,0", "ascentM,-1", "highestAltitudeM,-1",
            "hikingMinutes,0", "durationDays,0", "latitude,91", "longitude,-181", "confidence,1.1",
            "reviewed,2", "hasToilet,-1", "hasSupplyShop,2", "isTraverse,2", "traverseTransferMinutes,-1",
            "costMinCny,-1", "costMaxCny,1", "difficulty,\"invalid\"", "sourceUrl,null", "reviewed,null"})
    void invalidFieldsReturnBadRequest(String field, String value) throws Exception {
        ObjectNode body = routeBody("invalid");
        body.set(field, objectMapper.readTree(value));
        mockMvc.perform(post("/api/routes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class)).isZero();
    }

    static Stream<Arguments> invalidJsonFields() {
        return Stream.of("bestSeasonsJson", "sceneryJson", "risksJson", "transportModesJson", "groupTourSearchTermsJson")
                .flatMap(field -> Stream.of("{", "{}", "null", "[] []").map(value -> Arguments.of(field, value)));
    }

    @ParameterizedTest
    @MethodSource("invalidJsonFields")
    void invalidJsonArrayIsRejected(String field, String value) throws Exception {
        ObjectNode body = routeBody("bad-json").put(field, value);
        mockMvc.perform(post("/api/routes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class)).isZero();
    }

    @Test
    void invalidUpdateLeavesOriginalDataUnchanged() throws Exception {
        ObjectNode original = routeBody("unchanged");
        createRoute(original);
        ObjectNode invalid = original.deepCopy().put("name", "不应写入").put("sceneryJson", "{}");
        mockMvc.perform(put("/api/routes/unchanged").contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(jsonPath("$.code").value(400));
        invalid = original.deepCopy().put("distanceKm", -1);
        mockMvc.perform(put("/api/routes/unchanged").contentType(MediaType.APPLICATION_JSON).content(invalid.toString()))
                .andExpect(jsonPath("$.code").value(400));
        assertRouteFields("unchanged", original);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void deleteRemovesOnlyTargetAndItsChildren(int foreignKeys) throws Exception {
        jdbcTemplate.execute("PRAGMA foreign_keys = " + foreignKeys);
        for (String id : List.of("delete-me", "keep-me")) {
            createRoute(routeBody(id));
            insertChildren(id);
        }
        mockMvc.perform(delete("/api/routes/delete-me"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class)).isEqualTo(1);
        assertChildrenCount("delete-me", 0);
        assertChildrenCount("keep-me", 1);
        mockMvc.perform(get("/api/routes/delete-me")).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteFailureRollsBackChildDeletions() throws Exception {
        createRoute(routeBody("rollback"));
        insertChildren("rollback");
        jdbcTemplate.execute("""
                CREATE TRIGGER prevent_route_delete BEFORE DELETE ON routes
                BEGIN SELECT RAISE(ABORT, 'test delete failure'); END
                """);
        assertThatThrownBy(() -> mockMvc.perform(delete("/api/routes/rollback")))
                .isInstanceOf(ServletException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class)).isEqualTo(1);
        assertChildrenCount("rollback", 1);
    }

    @ParameterizedTest
    @CsvSource({"GET,/api/routes", "GET,/api/routes/test", "POST,/api/routes", "PUT,/api/routes/test", "DELETE,/api/routes/test"})
    @org.springframework.security.test.context.support.WithAnonymousUser
    void anonymousAccessIsRejected(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON).content(routeBody("test").toString()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({"GET,/api/routes", "GET,/api/routes/test", "POST,/api/routes", "PUT,/api/routes/test", "DELETE,/api/routes/test"})
    @WithMockUser(authorities = "unrelated")
    void missingPermissionIsRejected(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .contentType(MediaType.APPLICATION_JSON).content(routeBody("test").toString()))
                .andExpect(status().isForbidden());
    }

    private ObjectNode routeBody(String id) throws Exception {
        ObjectNode body = (ObjectNode) objectMapper.readTree("""
                {
                  "name":"测试路线","startLocation":"起点","endLocation":"终点",
                  "latitude":30.2,"longitude":120.1,"distanceKm":10.5,"ascentM":600,
                  "highestAltitudeM":1000,"hikingMinutes":240,"difficulty":"moderate",
                  "durationDays":1,"routeType":"loop","bestSeasonsJson":"[\\"春季\\"]",
                  "sceneryJson":"[\\"山景\\"]","risksJson":"[]","transportModesJson":"[\\"self_drive\\"]",
                  "costMinCny":20.0,"costMaxCny":100.0,"parking":"停车场","supplies":"补给点",
                  "signal":"有信号","camping":"可露营","sourceUrl":"https://example.com/route",
                  "sourceName":"测试来源","collectedAt":"2026-09-07T00:00:00Z","confidence":0.8,
                  "reviewed":1,"hasToilet":1,"hasSupplyShop":1,"isTraverse":1,
                  "traverseTransferMinutes":30,"groupTourSearchTermsJson":"[]"
                }
                """);
        return body.put("id", id);
    }

    private void createRoute(ObjectNode body) throws Exception {
        mockMvc.perform(post("/api/routes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
    }

    private void assertRouteFields(String id, ObjectNode expected) throws Exception {
        String response = mockMvc.perform(get("/api/routes/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode actual = objectMapper.readTree(response).get("data");
        expected.fields().forEachRemaining(field -> assertThat(actual.get(field.getKey()))
                .as(field.getKey()).isEqualTo(field.getValue()));
        assertThat(actual.get("id").asText()).isEqualTo(id);
        assertThat(Instant.parse(actual.get("updatedAt").asText())).isNotNull();
    }

    private void insertChildren(String id) {
        jdbcTemplate.update("INSERT INTO route_cost_items(route_id,name,cost_type,billing_unit,min_cny,max_cny,source_url,updated_at) VALUES (?, 'ticket', 'ticket', 'person', 0, 10, 'source', 'now')", id);
        jdbcTemplate.update("INSERT INTO transport_cost_items(route_id,transport_mode,name,cost_type,billing_unit,min_cny,max_cny,source_url,updated_at) VALUES (?, 'self_drive', 'fuel', 'fuel', 'vehicle', 0, 10, 'source', 'now')", id);
        jdbcTemplate.update("INSERT INTO route_parking_points(route_id,name,latitude,longitude,source_url,updated_at) VALUES (?, 'parking', 30, 120, 'source', 'now')", id);
        jdbcTemplate.update("INSERT INTO traffic_profiles(route_id,base_one_way_minutes,common_bottlenecks_json,source_url,updated_at,confidence) VALUES (?, 60, '[]', 'source', 'now', 0.8)", id);
        jdbcTemplate.update("INSERT INTO trip_feedback(route_id,traveled_at,direction,actual_minutes,congestion_level,source,created_at) VALUES (?, 'now', 'outbound', 60, 'low', 'source', 'now')", id);
    }

    private void assertChildrenCount(String id, int expected) {
        for (String table : CHILD_TABLES) {
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE route_id = ?", Integer.class, id))
                    .as(table).isEqualTo(expected);
        }
    }
}
