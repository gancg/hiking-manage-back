package com.gancg.hikingmanageback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.rbac.CreateUserRequest;
import com.gancg.hikingmanageback.dto.rbac.CreateRoleRequest;
import com.gancg.hikingmanageback.dto.rbac.PermissionDto;
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
import com.gancg.hikingmanageback.entity.AdminUser;
import com.gancg.hikingmanageback.entity.Permission;
import com.gancg.hikingmanageback.entity.Role;
import com.gancg.hikingmanageback.entity.RolePermission;
import com.gancg.hikingmanageback.entity.UserRole;
import com.gancg.hikingmanageback.mapper.AdminUserMapper;
import com.gancg.hikingmanageback.mapper.PermissionMapper;
import com.gancg.hikingmanageback.mapper.RoleMapper;
import com.gancg.hikingmanageback.mapper.RolePermissionMapper;
import com.gancg.hikingmanageback.mapper.UserRoleMapper;
import com.gancg.hikingmanageback.model.RbacUserRow;
import com.gancg.hikingmanageback.security.AuthProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacService {
    private final AdminUserMapper adminUserMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    public RbacService(
            AdminUserMapper adminUserMapper,
            RoleMapper roleMapper,
            PermissionMapper permissionMapper,
            RolePermissionMapper rolePermissionMapper,
            UserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder,
            AuthProperties authProperties
    ) {
        this.adminUserMapper = adminUserMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    public List<RbacUserDto> listUsers() {
        return adminUserMapper.selectUserRows().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public RbacUserDto getUser(Long userId) {
        RbacUserRow userRow = adminUserMapper.selectUserRowById(userId);
        if (userRow == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserDto(userRow);
    }

    @Transactional
    public void createUser(CreateUserRequest request) {
        if (adminUserMapper.selectByUsername(request.getUsername()) != null) {
            throw new BizException(409, "用户名已存在");
        }
        ensureActiveRoles(request.getRoleIds());

        String now = Instant.now().toString();
        String rawPassword = StringUtils.hasText(request.getPassword()) ? request.getPassword() : authProperties.getDefaultPassword();
        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setStatus("active");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        adminUserMapper.insert(user);

        for (Long roleId : request.getRoleIds()) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(now);
            userRoleMapper.insert(userRole);
        }
    }

    @Transactional
    public void updateUser(Long userId, UpdateUserRequest request) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        ensureActiveRoles(request.getRoleIds());

        String now = Instant.now().toString();
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setUpdatedAt(now);
        adminUserMapper.updateById(user);

        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : request.getRoleIds()) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(now);
            userRoleMapper.insert(userRole);
        }
    }

    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setStatus(request.getStatus());
        user.setUpdatedAt(Instant.now().toString());
        adminUserMapper.updateById(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        userRoleMapper.deleteByUserId(userId);
        adminUserMapper.deleteById(userId);
    }

    public void resetUserPassword(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(authProperties.getDefaultPassword()));
        user.setUpdatedAt(Instant.now().toString());
        adminUserMapper.updateById(user);
    }

    public List<RoleDto> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getId))
                .stream()
                .map(role -> RoleDto.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .name(role.getName())
                        .status(role.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public RoleDetailDto getRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }
        return RoleDetailDto.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .status(role.getStatus())
                .permissionIds(rolePermissionMapper.selectPermissionIdsByRoleId(role.getId()))
                .build();
    }

    @Transactional
    public void createRole(CreateRoleRequest request) {
        Role existingRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, request.getCode())
                .last("LIMIT 1"));
        if (existingRole != null) {
            throw new BizException(409, "角色编码已存在");
        }
        List<Long> permissionIds = normalizePermissionIds(request.getPermissionIds());
        ensureActivePermissions(permissionIds);

        String now = Instant.now().toString();
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setIsSystem(0);
        role.setStatus(request.getStatus());
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        roleMapper.insert(role);

        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedAt(now);
            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Transactional
    public void updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }

        Role sameCodeRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, request.getCode())
                .last("LIMIT 1"));
        if (sameCodeRole != null && !sameCodeRole.getId().equals(roleId)) {
            throw new BizException(409, "角色编码已存在");
        }

        List<Long> permissionIds = normalizePermissionIds(request.getPermissionIds());
        ensureActivePermissions(permissionIds);

        String now = Instant.now().toString();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setUpdatedAt(now);
        roleMapper.updateById(role);

        rolePermissionMapper.deleteByRoleId(roleId);
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedAt(now);
            rolePermissionMapper.insert(rolePermission);
        }
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(404, "角色不存在");
        }
        long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BizException(400, "角色已关联用户，无法删除");
        }
        rolePermissionMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    public PermissionPageDto listPermissions(PermissionPageQueryRequest request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        List<PermissionDto> records = permissionMapper.selectPermissionPage(offset, pageSize)
                .stream()
                .map(permission -> PermissionDto.builder()
                        .id(permission.getId())
                        .code(permission.getCode())
                        .name(permission.getName())
                        .resource(permission.getResource())
                        .action(permission.getAction())
                        .status(permission.getStatus())
                        .build())
                .collect(Collectors.toList());
        long total = permissionMapper.countPermissions();
        return PermissionPageDto.builder()
                .records(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    public PermissionDetailDto getPermission(Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BizException(404, "权限不存在");
        }
        return toPermissionDetailDto(permission);
    }

    @Transactional
    public void createPermission(CreatePermissionRequest request) {
        Permission existingPermission = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getCode, request.getCode())
                .last("LIMIT 1"));
        if (existingPermission != null) {
            throw new BizException(409, "权限编码已存在");
        }
        String now = Instant.now().toString();
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setResourceType(request.getResourceType());
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setParentId(request.getParentId());
        permission.setSortOrder(defaultSortOrder(request.getSortOrder()));
        permission.setStatus(request.getStatus());
        permission.setCreatedAt(now);
        permission.setUpdatedAt(now);
        permissionMapper.insert(permission);
    }

    @Transactional
    public void updatePermission(Long permissionId, UpdatePermissionRequest request) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BizException(404, "权限不存在");
        }
        Permission sameCodePermission = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getCode, request.getCode())
                .last("LIMIT 1"));
        if (sameCodePermission != null && !sameCodePermission.getId().equals(permissionId)) {
            throw new BizException(409, "权限编码已存在");
        }
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setResourceType(request.getResourceType());
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setParentId(request.getParentId());
        permission.setSortOrder(defaultSortOrder(request.getSortOrder()));
        permission.setStatus(request.getStatus());
        permission.setUpdatedAt(Instant.now().toString());
        permissionMapper.updateById(permission);
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BizException(404, "权限不存在");
        }
        long roleBindCount = rolePermissionMapper.countByPermissionId(permissionId);
        if (roleBindCount > 0) {
            throw new BizException(400, "权限已关联角色，无法删除");
        }
        permissionMapper.deleteByPermissionId(permissionId);
    }

    private RbacUserDto toUserDto(RbacUserRow row) {
        List<String> roleCodes = Arrays.stream(row.getRoleCodesCsv().split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return RbacUserDto.builder()
                .id(row.getId())
                .username(row.getUsername())
                .displayName(row.getDisplayName())
                .email(row.getEmail())
                .mobile(row.getMobile())
                .status(row.getStatus())
                .roleCodes(roleCodes)
                .build();
    }

    private void ensureActiveRoles(List<Long> roleIds) {
        long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<Role>()
                .in(Role::getId, roleIds)
                .eq(Role::getStatus, "active"));
        if (roleCount != roleIds.size()) {
            throw new BizException(400, "角色不存在或已禁用");
        }
    }

    private void ensureActivePermissions(List<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return;
        }
        long permissionCount = permissionMapper.selectCount(new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, permissionIds)
                .eq(Permission::getStatus, "active"));
        if (permissionCount != permissionIds.size()) {
            throw new BizException(400, "权限不存在或已禁用");
        }
    }

    private List<Long> normalizePermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> uniquePermissionIds = new LinkedHashSet<>(permissionIds);
        return List.copyOf(uniquePermissionIds);
    }

    private PermissionDetailDto toPermissionDetailDto(Permission permission) {
        return PermissionDetailDto.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .resourceType(permission.getResourceType())
                .resource(permission.getResource())
                .action(permission.getAction())
                .parentId(permission.getParentId())
                .sortOrder(permission.getSortOrder())
                .status(permission.getStatus())
                .build();
    }

    private int defaultSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
