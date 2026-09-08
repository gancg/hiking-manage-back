package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.rbac.PermissionDto;
import com.gancg.hikingmanageback.dto.rbac.PermissionDetailDto;
import com.gancg.hikingmanageback.dto.rbac.PermissionPageDto;
import com.gancg.hikingmanageback.dto.rbac.PermissionPageQueryRequest;
import com.gancg.hikingmanageback.dto.rbac.RoleDetailDto;
import com.gancg.hikingmanageback.dto.rbac.UpdateUserRequest;
import com.gancg.hikingmanageback.service.RbacService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RbacControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RbacService rbacService;

    @Test
    @WithMockUser(authorities = "rbac:user:update")
    void updateUserSuccess() throws Exception {
        mockMvc.perform(put("/api/rbac/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "新名字",
                                  "email": "new@example.com",
                                  "mobile": "13800000000",
                                  "roleIds": [1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));

        verify(rbacService).updateUser(ArgumentMatchers.eq(1L), ArgumentMatchers.any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(authorities = "rbac:user:update")
    void updateUserFailsWhenUserNotFound() throws Exception {
        doThrow(new BizException(404, "用户不存在"))
                .when(rbacService)
                .updateUser(ArgumentMatchers.eq(99L), ArgumentMatchers.any(UpdateUserRequest.class));

        mockMvc.perform(put("/api/rbac/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "新名字",
                                  "email": "new@example.com",
                                  "mobile": "13800000000",
                                  "roleIds": [1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(authorities = "rbac:user:update")
    void updateUserFailsWhenRoleInvalid() throws Exception {
        doThrow(new BizException(400, "角色不存在或已禁用"))
                .when(rbacService)
                .updateUser(ArgumentMatchers.eq(1L), ArgumentMatchers.any(UpdateUserRequest.class));

        mockMvc.perform(put("/api/rbac/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "新名字",
                                  "email": "new@example.com",
                                  "mobile": "13800000000",
                                  "roleIds": [999]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色不存在或已禁用"));
    }

    @Test
    @WithMockUser(authorities = "rbac:user:list")
    void getUserSuccess() throws Exception {
        when(rbacService.getUser(1L)).thenReturn(com.gancg.hikingmanageback.dto.rbac.RbacUserDto.builder()
                .id(1L)
                .username("admin")
                .displayName("管理员")
                .email("admin@test.com")
                .mobile("13800000000")
                .status("active")
                .roleCodes(java.util.List.of("super_admin"))
                .build());

        mockMvc.perform(get("/api/rbac/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @WithMockUser(authorities = "rbac:user:delete")
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(delete("/api/rbac/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));

        verify(rbacService).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = "rbac:user:delete")
    void deleteUserFailsWhenUserNotFound() throws Exception {
        doThrow(new BizException(404, "用户不存在")).when(rbacService).deleteUser(99L);

        mockMvc.perform(delete("/api/rbac/users/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(authorities = "rbac:user:reset-password")
    void resetUserPasswordSuccess() throws Exception {
        mockMvc.perform(post("/api/rbac/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));

        verify(rbacService).resetUserPassword(1L);
    }

    @Test
    @WithMockUser(authorities = "rbac:user:reset-password")
    void resetUserPasswordFailsWhenUserNotFound() throws Exception {
        doThrow(new BizException(404, "用户不存在")).when(rbacService).resetUserPassword(99L);

        mockMvc.perform(post("/api/rbac/users/99/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("用户不存在"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:list")
    void getRoleSuccess() throws Exception {
        when(rbacService.getRole(1L)).thenReturn(RoleDetailDto.builder()
                .id(1L)
                .code("editor")
                .name("编辑")
                .description("内容编辑角色")
                .status("active")
                .permissionIds(java.util.List.of(1L, 2L))
                .build());

        mockMvc.perform(get("/api/rbac/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("editor"))
                .andExpect(jsonPath("$.data.permissionIds[0]").value(1));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:create")
    void createRoleSuccess() throws Exception {
        mockMvc.perform(post("/api/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "editor",
                                  "name": "编辑",
                                  "description": "内容编辑角色",
                                  "status": "active",
                                  "permissionIds": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:create")
    void createRoleFailsWhenCodeExists() throws Exception {
        doThrow(new BizException(409, "角色编码已存在")).when(rbacService).createRole(ArgumentMatchers.any());

        mockMvc.perform(post("/api/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "editor",
                                  "name": "编辑",
                                  "description": "内容编辑角色",
                                  "status": "active",
                                  "permissionIds": [1, 2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("角色编码已存在"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:update")
    void updateRoleSuccess() throws Exception {
        mockMvc.perform(put("/api/rbac/roles/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "editor",
                                  "name": "编辑2",
                                  "description": "内容编辑角色2",
                                  "status": "active",
                                  "permissionIds": [1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:update")
    void updateRoleFailsWhenRoleNotFound() throws Exception {
        doThrow(new BizException(404, "角色不存在")).when(rbacService).updateRole(ArgumentMatchers.eq(99L), ArgumentMatchers.any());

        mockMvc.perform(put("/api/rbac/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "editor",
                                  "name": "编辑2",
                                  "description": "内容编辑角色2",
                                  "status": "active",
                                  "permissionIds": [1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:delete")
    void deleteRoleSuccess() throws Exception {
        mockMvc.perform(delete("/api/rbac/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(authorities = "rbac:role:delete")
    void deleteRoleFailsWhenRoleHasUsers() throws Exception {
        doThrow(new BizException(400, "角色已关联用户，无法删除")).when(rbacService).deleteRole(2L);

        mockMvc.perform(delete("/api/rbac/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色已关联用户，无法删除"));
    }

    @Test
    @WithMockUser(authorities = "rbac:permission:list")
    void listPermissionsPageSuccess() throws Exception {
        when(rbacService.listPermissions(ArgumentMatchers.any(PermissionPageQueryRequest.class)))
                .thenReturn(PermissionPageDto.builder()
                        .records(List.of(PermissionDto.builder()
                                .id(2L)
                                .code("rbac:user:update")
                                .name("用户更新")
                                .resource("/api/rbac/users/{id}")
                                .action("update")
                                .status("active")
                                .build()))
                        .total(12)
                        .pageNum(2)
                        .pageSize(1)
                        .build());

        mockMvc.perform(get("/api/rbac/permissions")
                        .param("pageNum", "2")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(1))
                .andExpect(jsonPath("$.data.records[0].code").value("rbac:user:update"));

        verify(rbacService).listPermissions(ArgumentMatchers.argThat((PermissionPageQueryRequest query) ->
                query.getPageNum().equals(2) && query.getPageSize().equals(1)));
    }

    @Test
    @WithMockUser(authorities = "rbac:permission:list")
    void getPermissionSuccess() throws Exception {
        when(rbacService.getPermission(1L)).thenReturn(PermissionDetailDto.builder()
                .id(1L)
                .code("rbac:user:list")
                .name("用户列表")
                .resourceType("api")
                .resource("/api/rbac/users")
                .action("list")
                .parentId(null)
                .sortOrder(0)
                .status("active")
                .build());

        mockMvc.perform(get("/api/rbac/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.code").value("rbac:user:list"));
    }

    @Test
    @WithMockUser(authorities = "rbac:permission:create")
    void createPermissionSuccess() throws Exception {
        mockMvc.perform(post("/api/rbac/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "rbac:demo:create",
                                  "name": "示例新增",
                                  "resourceType": "api",
                                  "resource": "/api/demo",
                                  "action": "create",
                                  "parentId": null,
                                  "sortOrder": 0,
                                  "status": "active"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "rbac:permission:update")
    void updatePermissionSuccess() throws Exception {
        mockMvc.perform(put("/api/rbac/permissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "rbac:demo:update",
                                  "name": "示例修改",
                                  "resourceType": "api",
                                  "resource": "/api/demo",
                                  "action": "update",
                                  "parentId": null,
                                  "sortOrder": 1,
                                  "status": "active"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "rbac:permission:delete")
    void deletePermissionFailsWhenBoundToRole() throws Exception {
        doThrow(new BizException(400, "权限已关联角色，无法删除")).when(rbacService).deletePermission(1L);

        mockMvc.perform(delete("/api/rbac/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("权限已关联角色，无法删除"));
    }
}
