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

/** 路线停车点接口及数据库约束测试。 */
@WithMockUser(authorities = {"route-parking-point:list", "route-parking-point:create", "route-parking-point:update", "route-parking-point:delete"})
class RouteParkingPointControllerTest extends AbstractRouteDetailControllerTest {
    @Override
    protected String permissionPrefix() {
        return "route-parking-point";
    }

    @Override
    protected String path() {
        return "/api/route-parking-points";
    }

    @Override
    protected String table() {
        return "route_parking_points";
    }

    @Override
    protected ObjectNode body(String routeId) throws Exception {
        ObjectNode request = (ObjectNode) objectMapper.readTree("""
                {"name":"测试停车点","latitude":30.2,"longitude":120.1,"note":"入口附近","isRecommended":1,"isReviewed":1,"sourceUrl":"https://example.com/parking"}
                """);
        return request.put("routeId", routeId);
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("name", "\"\""),
                Arguments.of("routeId", "\"\""),
                Arguments.of("latitude", "91"),
                Arguments.of("longitude", "-181"),
                Arguments.of("latitude", "null"),
                Arguments.of("isRecommended", "2"),
                Arguments.of("isReviewed", "-1"),
                Arguments.of("isRecommended", "null"));
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

    @Test
    void duplicateNameIsRejectedOnCreateRenameAndRouteChange() throws Exception {
        ObjectNode original = body("route-a");
        String firstId = identity(create(original));
        expectCreateCode(original, 409);
        ObjectNode other = body("route-a").put("name", "另一停车点");
        String otherId = identity(create(other));
        update(otherId, original, 409);
        assertFields(detail(otherId), other);
        String differentRouteId = identity(create(body("route-b")));
        update(differentRouteId, original, 409);
        assertThat(detail(differentRouteId).get("routeId").asText()).isEqualTo("route-b");
        update(firstId, original, 0);
    }

    @Test
    void omittedFlagsDefaultToZeroAndNoteCanBeCleared() throws Exception {
        ObjectNode request = body("route-a");
        request.remove(java.util.List.of("isRecommended", "isReviewed"));
        JsonNode saved = create(request);
        assertThat(saved.get("isRecommended").asInt()).isZero();
        assertThat(saved.get("isReviewed").asInt()).isZero();
        request.remove("note");
        update(identity(saved), request, 0);
        assertThat(detail(identity(saved)).get("note").isNull()).isTrue();
    }
}
