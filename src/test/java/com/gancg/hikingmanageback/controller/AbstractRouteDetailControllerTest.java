package com.gancg.hikingmanageback.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 使用真实表结构验证附属信息接口，所有数据仅写入独立内存数据库。 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.hikari.maximum-pool-size=1",
        "logging.level.com.gancg.hikingmanageback.filter.ApiAccessLogFilter=OFF"
})
@AutoConfigureMockMvc
@Sql("/routes-schema.sql")
abstract class AbstractRouteDetailControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected abstract String permissionPrefix();
    protected abstract String path();
    protected abstract String table();
    protected abstract ObjectNode body(String routeId) throws Exception;

    protected boolean trafficProfile() {
        return false;
    }

    @BeforeEach
    void insertParentRoutes() {
        for (String id : new String[]{"route-a", "route-b"}) {
            insertParentRoute(id);
        }
    }

    private void insertParentRoute(String id) {
        jdbcTemplate.update("""
                    INSERT INTO routes(id,name,start_location,end_location,distance_km,ascent_m,
                      highest_altitude_m,hiking_minutes,difficulty,duration_days,route_type,
                      best_seasons_json,scenery_json,risks_json,transport_modes_json,cost_min_cny,
                      cost_max_cny,source_url,source_name,collected_at,updated_at,confidence)
                    VALUES (?, '测试路线', '起点', '终点', 10, 500, 1000, 240, 'moderate', 1,
                      'loop', '[]', '[]', '[]', '[]', 0, 100, 'source', '测试来源', 'now', 'now', 0.8)
                    """, id);
    }

    @Test
    void crudPersistsAllFieldsAndFiltersByRoute() throws Exception {
        mockMvc.perform(get(path())).andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
        ObjectNode original = body("route-a");
        JsonNode first = create(original);
        String id = identity(first);
        assertFields(first, original);
        assertFields(detail(id), original);
        create(body("route-b"));
        mockMvc.perform(get(path())).andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2));
        mockMvc.perform(get(path()).param("routeId", "route-a"))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].routeId").value("route-a"));

        jdbcTemplate.update("UPDATE " + table() + " SET updated_at = ? WHERE " + keyColumn() + " = ?",
                "2000-01-01T00:00:00Z", id);
        ObjectNode updated = original.deepCopy().put("sourceUrl", "https://example.com/updated");
        if (trafficProfile()) {
            updated.remove("routeId");
            updated.putNull("bestDepartureTime");
            updated.putNull("suggestedReturnTime");
            updated.put("commonBottlenecksJson", "[\"更新拥堵点\"]");
        } else {
            updated.put("routeId", "route-b");
            updated.put("name", "更新后的名称");
            if (updated.has("note")) {
                updated.putNull("note");
            }
        }
        update(id, updated, 0);
        JsonNode saved = detail(id);
        assertFields(saved, updated);
        assertThat(identity(saved)).isEqualTo(id);
        assertThat(saved.get("updatedAt").asText()).isNotEqualTo("2000-01-01T00:00:00Z");
        mockMvc.perform(delete(path() + "/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get(path() + "/" + id)).andExpect(jsonPath("$.code").value(404));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table(), Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class)).isEqualTo(2);
    }

    /** 验证默认条数、跨页排序、字段完整性和超大页码。 */
    @Test
    void listPaginatesWithDefaultsAndStableOrder() throws Exception {
        List<JsonNode> expected = new ArrayList<>();
        for (int i = 10; i >= 0; i--) {
            String routeId = "page-route-" + i;
            insertParentRoute(routeId);
            expected.add(create(body(routeId)));
        }
        if (trafficProfile()) {
            expected.sort(Comparator.comparing(this::identity));
        }
        JsonNode firstPage = page(get(path()), 11, 1, 10);
        assertThat(firstPage.size()).isEqualTo(10);
        for (int i = 0; i < 10; i++) {
            assertThat(firstPage.get(i)).isEqualTo(expected.get(i));
        }
        JsonNode lastPage = page(get(path()).param("pageNum", "2"), 11, 2, 10);
        assertThat(lastPage.size()).isEqualTo(1);
        assertThat(lastPage.get(0)).isEqualTo(expected.get(10));
        JsonNode customPage = page(get(path()).param("pageNum", "2").param("pageSize", "3"), 11, 2, 3);
        assertThat(customPage.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(customPage.get(i)).isEqualTo(expected.get(i + 3));
        }
        assertThat(page(get(path()).param("pageNum", "5").param("pageSize", "3"), 11, 5, 3).isEmpty()).isTrue();
        assertThat(page(get(path()).param("pageNum", "2147483647").param("pageSize", "2"),
                11, Integer.MAX_VALUE, 2).isEmpty()).isTrue();
    }

    /** 验证先按路线筛选再分页，总数不包含其他路线。 */
    @Test
    void filteredPageCountsOnlyMatchingRecords() throws Exception {
        assertThat(page(get(path()).param("routeId", "route-a"), 0, 1, 10).isEmpty()).isTrue();
        create(body("route-b"));
        int total = trafficProfile() ? 1 : 3;
        List<JsonNode> expected = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            ObjectNode request = body("route-a");
            if (!trafficProfile()) {
                request.put("name", "分页测试记录" + i);
            }
            expected.add(create(request));
        }
        for (int i = 0; i < total; i++) {
            JsonNode records = page(get(path()).param("routeId", "route-a")
                    .param("pageNum", String.valueOf(i + 1)).param("pageSize", "1"), total, i + 1, 1);
            assertThat(records.size()).isEqualTo(1);
            assertThat(records.get(0)).isEqualTo(expected.get(i));
        }
        assertThat(page(get(path()).param("routeId", "route-a").param("pageNum", String.valueOf(total + 1))
                .param("pageSize", "1"), total, total + 1, 1).isEmpty()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"pageNum,0", "pageNum,-1", "pageSize,0", "pageSize,-1",
            "pageNum,abc", "pageSize,abc", "pageNum,''", "pageSize,''"})
    void invalidPaginationIsRejected(String field, String value) throws Exception {
        mockMvc.perform(get(path()).param(field, value))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
    }

    private JsonNode page(MockHttpServletRequestBuilder request, int total, int pageNum, int pageSize) throws Exception {
        String response = mockMvc.perform(request)
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(total))
                .andExpect(jsonPath("$.data.pageNum").value(pageNum))
                .andExpect(jsonPath("$.data.pageSize").value(pageSize))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("data").get("records");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void missingRecordReturnsNotFound(String method) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path() + "/" + (trafficProfile() ? "missing" : "99999"))
                        .contentType(MediaType.APPLICATION_JSON).content(body("route-a").toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void missingParentIsRejectedEvenWithoutForeignKeys() throws Exception {
        jdbcTemplate.execute("PRAGMA foreign_keys = OFF");
        expectCreateCode(body("missing"), 404);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table(), Integer.class)).isZero();
    }

    @Test
    void invalidRouteFilterIsRejected() throws Exception {
        mockMvc.perform(get(path()).param("routeId", "missing")).andExpect(jsonPath("$.code").value(404));
        mockMvc.perform(get(path()).param("routeId", " ")).andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void missingRequiredFieldRejectsCreateAndUpdate() throws Exception {
        ObjectNode original = body("route-a");
        ObjectNode invalid = original.deepCopy();
        invalid.remove("sourceUrl");
        expectCreateCode(invalid, 400);
        String id = identity(create(original));
        update(id, invalid, 400);
        assertFields(detail(id), original);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "DETAIL", "POST", "PUT", "DELETE"})
    @WithAnonymousUser
    void anonymousAccessIsRejected(String operation) throws Exception {
        mockMvc.perform(operation(operation)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "DETAIL", "POST", "PUT", "DELETE"})
    @WithMockUser(authorities = "unrelated")
    void missingPermissionIsRejected(String operation) throws Exception {
        mockMvc.perform(operation(operation)).andExpect(status().isForbidden());
    }

    /** 原路线权限不再授予附属模块操作权限。 */
    @ParameterizedTest
    @ValueSource(strings = {"GET", "DETAIL", "POST", "PUT", "DELETE"})
    @WithMockUser(authorities = {"route:list", "route:create", "route:update", "route:delete"})
    void routePermissionsDoNotGrantModuleAccess(String operation) throws Exception {
        mockMvc.perform(operation(operation)).andExpect(status().isForbidden());
    }

    /** 查询权限不能用于新增、修改或删除。 */
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void listPermissionCannotWrite(String operation) throws Exception {
        mockMvc.perform(operation(operation).with(user("reader").authorities(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(permissionPrefix() + ":list"))))
                .andExpect(status().isForbidden());
    }

    /** 其他模块的操作权限不能用于当前模块。 */
    @ParameterizedTest
    @ValueSource(strings = {"GET", "DETAIL", "POST", "PUT", "DELETE"})
    void otherModulePermissionsDoNotGrantAccess(String operation) throws Exception {
        String otherPrefix = permissionPrefix().equals("route-cost-item") ? "transport-cost-item" : "route-cost-item";
        mockMvc.perform(operation(operation).with(user("other").authorities(
                        java.util.stream.Stream.of("list", "create", "update", "delete")
                                .map(action -> new org.springframework.security.core.authority.SimpleGrantedAuthority(otherPrefix + ":" + action))
                                .toArray(org.springframework.security.core.GrantedAuthority[]::new))))
                .andExpect(status().isForbidden());
    }

    protected JsonNode create(ObjectNode requestBody) throws Exception {
        String response = mockMvc.perform(post(path()).contentType(MediaType.APPLICATION_JSON).content(requestBody.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("data");
    }

    protected void expectCreateCode(ObjectNode requestBody, int code) throws Exception {
        mockMvc.perform(post(path()).contentType(MediaType.APPLICATION_JSON).content(requestBody.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(code));
    }

    protected void update(String id, ObjectNode requestBody, int code) throws Exception {
        mockMvc.perform(put(path() + "/" + id).contentType(MediaType.APPLICATION_JSON).content(requestBody.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(code));
    }

    protected JsonNode detail(String id) throws Exception {
        String response = mockMvc.perform(get(path() + "/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("data");
    }

    protected String identity(JsonNode data) {
        return data.get(trafficProfile() ? "routeId" : "id").asText();
    }

    protected void assertFields(JsonNode actual, ObjectNode expected) {
        expected.fields().forEachRemaining(field -> assertThat(actual.get(field.getKey()))
                .as(field.getKey()).isEqualTo(field.getValue()));
        assertThat(Instant.parse(actual.get("updatedAt").asText())).isNotNull();
    }

    private String keyColumn() {
        return trafficProfile() ? "route_id" : "id";
    }

    private MockHttpServletRequestBuilder operation(String operation) throws Exception {
        String method = operation.equals("DETAIL") ? "GET" : operation;
        String url = path() + (operation.equals("GET") || operation.equals("POST") ? "" : "/" + (trafficProfile() ? "route-a" : "1"));
        return request(HttpMethod.valueOf(method), url).contentType(MediaType.APPLICATION_JSON).content(body("route-a").toString());
    }
}
