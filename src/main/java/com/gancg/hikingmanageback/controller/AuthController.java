package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.common.ApiResponse;
import com.gancg.hikingmanageback.dto.auth.ChangePasswordRequest;
import com.gancg.hikingmanageback.dto.auth.LoginRequest;
import com.gancg.hikingmanageback.dto.auth.LoginResponse;
import com.gancg.hikingmanageback.dto.auth.UserProfile;
import com.gancg.hikingmanageback.security.AdminPrincipal;
import com.gancg.hikingmanageback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * 认证相关接口。
 *
 * @author gancg
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录接口。
     *
     * @param request 登录请求参数，包含用户名和密码
     * @return 登录成功后的用户信息和访问令牌
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * 用户退出登录接口。
     *
     * @param principal 当前登录用户信息
     * @return 退出登录结果
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AdminPrincipal principal) {
        authService.logout(principal.getUserId());
        return ApiResponse.success();
    }

    /**
     * 获取当前登录用户详情接口。
     *
     * @param principal 当前登录用户信息
     * @return 当前用户的基础信息和权限信息
     */
    @GetMapping("/me")
    public ApiResponse<UserProfile> currentUser(@AuthenticationPrincipal AdminPrincipal principal) {
        return ApiResponse.success(authService.currentUser(principal.getUserId()));
    }

    /**
     * 修改当前用户密码接口。
     *
     * @param principal 当前登录用户身份信息
     * @param request 修改密码请求，包含旧密码和新密码
     * @return 修改密码结果
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getName(), request);
        return ApiResponse.success();
    }
}
