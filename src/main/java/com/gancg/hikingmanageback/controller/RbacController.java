package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.rbac.CreateRoleRequest;
import com.gancg.hikingmanageback.dto.rbac.CreateUserRequest;
import com.gancg.hikingmanageback.dto.rbac.CreatePermissionRequest;
import com.gancg.hikingmanageback.dto.rbac.PermissionDetailDto;
import com.gancg.hikingmanageback.dto.rbac.PermissionPageDto;
import com.gancg.hikingmanageback.dto.rbac.PermissionPageQueryRequest;
import com.gancg.hikingmanageback.dto.rbac.RbacUserDto;
import com.gancg.hikingmanageback.dto.rbac.RoleDetailDto;
import com.gancg.hikingmanageback.dto.rbac.RoleDto;
import com.gancg.hikingmanageback.dto.rbac.UpdatePermissionRequest;
import com.gancg.hikingmanageback.dto.rbac.UpdateRoleRequest;
import com.gancg.hikingmanageback.dto.rbac.UpdateUserRequest;
import com.gancg.hikingmanageback.dto.rbac.UpdateUserStatusRequest;
import com.gancg.hikingmanageback.service.RbacService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RBAC 权限管理接口。
 *
 * 提供用户、角色、权限的增删改查及状态管理能力。
 */
@RestController
@RequestMapping("/api/rbac")
public class RbacController {
    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * 获取全部用户列表接口。
     *
     * @return 用户列表数据
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('rbac:user:list')")
    public ApiResponse<List<RbacUserDto>> listUsers() {
        return ApiResponse.success(rbacService.listUsers());
    }

    /**
     * 根据用户ID查询用户详情接口。
     *
     * @param id 用户ID
     * @return 用户详情数据
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('rbac:user:list')")
    public ApiResponse<RbacUserDto> getUser(@PathVariable("id") Long id) {
        return ApiResponse.success(rbacService.getUser(id));
    }

    /**
     * 创建用户接口。
     *
     * @param request 新增用户请求参数
     * @return 创建结果
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('rbac:user:create')")
    public ApiResponse<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        rbacService.createUser(request);
        return ApiResponse.success();
    }

    /**
     * 更新用户状态接口。
     *
     * @param id 用户ID
     * @param request 用户状态更新参数
     * @return 更新结果
     */
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('rbac:user:update')")
    public ApiResponse<Void> updateUserStatus(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        rbacService.updateUserStatus(id, request);
        return ApiResponse.success();
    }

    /**
     * 更新用户信息接口。
     *
     * @param id 用户ID
     * @param request 更新用户请求参数
     * @return 更新结果
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('rbac:user:update')")
    public ApiResponse<Void> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRequest request) {
        rbacService.updateUser(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除用户接口。
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('rbac:user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
        rbacService.deleteUser(id);
        return ApiResponse.success();
    }

    /**
     * 重置用户密码接口。
     *
     * @param id 用户ID
     * @return 重置密码结果
     */
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('rbac:user:reset-password')")
    public ApiResponse<Void> resetUserPassword(@PathVariable("id") Long id) {
        rbacService.resetUserPassword(id);
        return ApiResponse.success();
    }

    /**
     * 获取全部角色列表接口。
     *
     * @return 角色列表数据
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('rbac:role:list')")
    public ApiResponse<List<RoleDto>> listRoles() {
        return ApiResponse.success(rbacService.listRoles());
    }

    /**
     * 根据角色ID获取角色详情接口。
     *
     * @param id 角色ID
     * @return 角色详情数据
     */
    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('rbac:role:list')")
    public ApiResponse<RoleDetailDto> getRole(@PathVariable("id") Long id) {
        return ApiResponse.success(rbacService.getRole(id));
    }

    /**
     * 创建角色接口。
     *
     * @param request 新增角色请求参数
     * @return 创建结果
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('rbac:role:create')")
    public ApiResponse<Void> createRole(@Valid @RequestBody CreateRoleRequest request) {
        rbacService.createRole(request);
        return ApiResponse.success();
    }

    /**
     * 更新角色信息接口。
     *
     * @param id 角色ID
     * @param request 更新角色请求参数
     * @return 更新结果
     */
    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('rbac:role:update')")
    public ApiResponse<Void> updateRole(@PathVariable("id") Long id, @Valid @RequestBody UpdateRoleRequest request) {
        rbacService.updateRole(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除角色接口。
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('rbac:role:delete')")
    public ApiResponse<Void> deleteRole(@PathVariable("id") Long id) {
        rbacService.deleteRole(id);
        return ApiResponse.success();
    }

    /**
     * 获取权限分页列表接口。
     *
     * @param request 分页查询参数
     * @return 权限分页列表数据
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('rbac:permission:list')")
    public ApiResponse<PermissionPageDto> listPermissions(@Valid PermissionPageQueryRequest request) {
        return ApiResponse.success(rbacService.listPermissions(request));
    }

    /**
     * 根据权限ID查询权限详情接口。
     *
     * @param id 权限ID
     * @return 权限详情数据
     */
    @GetMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('rbac:permission:list')")
    public ApiResponse<PermissionDetailDto> getPermission(@PathVariable("id") Long id) {
        return ApiResponse.success(rbacService.getPermission(id));
    }

    /**
     * 创建权限接口。
     *
     * @param request 新增权限请求参数
     * @return 创建结果
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('rbac:permission:create')")
    public ApiResponse<Void> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        rbacService.createPermission(request);
        return ApiResponse.success();
    }

    /**
     * 更新权限信息接口。
     *
     * @param id 权限ID
     * @param request 更新权限请求参数
     * @return 更新结果
     */
    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('rbac:permission:update')")
    public ApiResponse<Void> updatePermission(@PathVariable("id") Long id, @Valid @RequestBody UpdatePermissionRequest request) {
        rbacService.updatePermission(id, request);
        return ApiResponse.success();
    }

    /**
     * 删除权限接口。
     *
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('rbac:permission:delete')")
    public ApiResponse<Void> deletePermission(@PathVariable("id") Long id) {
        rbacService.deletePermission(id);
        return ApiResponse.success();
    }
}
