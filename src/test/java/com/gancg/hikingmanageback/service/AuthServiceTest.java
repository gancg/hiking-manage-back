package com.gancg.hikingmanageback.service;

import com.gancg.hikingmanageback.dto.auth.LoginRequest;
import com.gancg.hikingmanageback.dto.auth.LoginResponse;
import com.gancg.hikingmanageback.dto.auth.ChangePasswordRequest;
import com.gancg.hikingmanageback.common.BizException;
import com.gancg.hikingmanageback.entity.AdminUser;
import com.gancg.hikingmanageback.mapper.AdminUserMapper;
import com.gancg.hikingmanageback.mapper.PermissionMapper;
import com.gancg.hikingmanageback.mapper.RoleMapper;
import com.gancg.hikingmanageback.security.AuthProperties;
import com.gancg.hikingmanageback.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PermissionMapper permissionMapper;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private AuthProperties authProperties;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void loginShouldRequirePasswordChangeWhenUsingDefaultPassword() {
        AuthService authService = new AuthService(
                adminUserMapper,
                roleMapper,
                permissionMapper,
                passwordEncoder,
                jwtTokenService,
                authProperties
        );

        when(authProperties.getDefaultPassword()).thenReturn("123456");
        when(authProperties.getDefaultPasswordTip()).thenReturn("当前密码为默认密码，请尽快修改密码");
        when(jwtTokenService.createToken(1L, "alice")).thenReturn("token");
        when(jwtTokenService.expiresAtEpochSecond()).thenReturn(1899999999L);
        when(roleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("super_admin"));
        when(permissionMapper.selectPermissionCodesByUserId(1L)).thenReturn(List.of("rbac:user:list"));

        AdminUser user = new AdminUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setDisplayName("Alice");
        user.setStatus("active");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        when(adminUserMapper.selectByUsername("alice")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("123456");

        LoginResponse response = authService.login(request);

        assertTrue(response.isNeedChangePassword());
        assertTrue("当前密码为默认密码，请尽快修改密码".equals(response.getPasswordTip()));
    }

    @Test
    void loginShouldNotRequirePasswordChangeWhenUsingCustomPassword() {
        AuthService authService = new AuthService(
                adminUserMapper,
                roleMapper,
                permissionMapper,
                passwordEncoder,
                jwtTokenService,
                authProperties
        );

        when(authProperties.getDefaultPassword()).thenReturn("123456");
        when(jwtTokenService.createToken(2L, "bob")).thenReturn("token");
        when(jwtTokenService.expiresAtEpochSecond()).thenReturn(1899999999L);
        when(roleMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("editor"));
        when(permissionMapper.selectPermissionCodesByUserId(2L)).thenReturn(List.of("rbac:role:list"));

        AdminUser user = new AdminUser();
        user.setId(2L);
        user.setUsername("bob");
        user.setDisplayName("Bob");
        user.setStatus("active");
        user.setPasswordHash(passwordEncoder.encode("abc123456"));
        when(adminUserMapper.selectByUsername("bob")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("bob");
        request.setPassword("abc123456");

        LoginResponse response = authService.login(request);

        assertFalse(response.isNeedChangePassword());
        assertNull(response.getPasswordTip());
    }

    @Test
    void changePasswordSuccess() {
        AuthService authService = new AuthService(
                adminUserMapper,
                roleMapper,
                permissionMapper,
                passwordEncoder,
                jwtTokenService,
                authProperties
        );
        AdminUser user = new AdminUser();
        user.setId(3L);
        user.setUsername("tom");
        user.setStatus("active");
        user.setPasswordHash(passwordEncoder.encode("old123456"));
        when(adminUserMapper.selectById(3L)).thenReturn(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old123456");
        request.setNewPassword("new123456");

        authService.changePassword(3L, request);

        verify(adminUserMapper).updateById(argThat((AdminUser updated) ->
                passwordEncoder.matches("new123456", updated.getPasswordHash())
        ));
    }

    @Test
    void changePasswordFailsWhenOldPasswordWrong() {
        AuthService authService = new AuthService(
                adminUserMapper,
                roleMapper,
                permissionMapper,
                passwordEncoder,
                jwtTokenService,
                authProperties
        );
        AdminUser user = new AdminUser();
        user.setId(4L);
        user.setUsername("jerry");
        user.setStatus("active");
        user.setPasswordHash(passwordEncoder.encode("old123456"));
        when(adminUserMapper.selectById(4L)).thenReturn(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("new123456");

        BizException exception = assertThrows(BizException.class, () -> authService.changePassword(4L, request));
        assertTrue(exception.getCode() == 400);
    }
}
