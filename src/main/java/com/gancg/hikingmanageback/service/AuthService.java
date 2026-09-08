package com.gancg.hikingmanageback.service;

import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.dto.auth.ChangePasswordRequest;
import com.gancg.hikingmanageback.dto.auth.LoginRequest;
import com.gancg.hikingmanageback.dto.auth.LoginResponse;
import com.gancg.hikingmanageback.dto.auth.UserProfile;
import com.gancg.hikingmanageback.entity.AdminUser;
import com.gancg.hikingmanageback.mapper.AdminUserMapper;
import com.gancg.hikingmanageback.mapper.PermissionMapper;
import com.gancg.hikingmanageback.mapper.RoleMapper;
import com.gancg.hikingmanageback.security.AuthProperties;
import com.gancg.hikingmanageback.security.JwtTokenService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
@Slf4j
public class AuthService {
    private final AdminUserMapper adminUserMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthProperties authProperties;

    public AuthService(
            AdminUserMapper adminUserMapper,
            RoleMapper roleMapper,
            PermissionMapper permissionMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            AuthProperties authProperties
    ) {
        this.adminUserMapper = adminUserMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authProperties = authProperties;
    }

    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (!"active".equals(user.getStatus())) {
            throw new BizException(403, "账号已禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }
        boolean needChangePassword = passwordEncoder.matches(authProperties.getDefaultPassword(), user.getPasswordHash());
        return buildLoginResponse(user, needChangePassword);
    }

    public void logout(Long userId) {
        log.info("用户登出: userId={}", userId);
    }

    public UserProfile currentUser(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        return UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .status(user.getStatus())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        changePasswordInternal(user, request);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        AdminUser user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        changePasswordInternal(user, request);
    }

    private void changePasswordInternal(AdminUser user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException(400, "旧密码错误");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BizException(400, "新密码不能与旧密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now().toString());
        adminUserMapper.updateById(user);
    }

    private LoginResponse buildLoginResponse(AdminUser user, boolean needChangePassword) {
        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        String token = jwtTokenService.createToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAtEpochSecond(jwtTokenService.expiresAtEpochSecond())
                .needChangePassword(needChangePassword)
                .passwordTip(needChangePassword ? authProperties.getDefaultPasswordTip() : null)
                .user(UserProfile.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .status(user.getStatus())
                        .roles(roleCodes)
                        .permissions(permissions)
                        .build())
                .build();
    }
}
