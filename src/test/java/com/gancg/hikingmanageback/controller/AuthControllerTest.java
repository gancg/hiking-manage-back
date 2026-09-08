package com.gancg.hikingmanageback.controller;

import com.gancg.hikingmanageback.dto.auth.LoginResponse;
import com.gancg.hikingmanageback.dto.auth.UserProfile;
import com.gancg.hikingmanageback.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void loginShouldReturnDefaultPasswordHintFields() throws Exception {
        when(authService.login(any())).thenReturn(
                LoginResponse.builder()
                        .token("token")
                        .tokenType("Bearer")
                        .expiresAtEpochSecond(1899999999L)
                        .needChangePassword(true)
                        .passwordTip("当前密码为默认密码，请尽快修改密码")
                        .user(UserProfile.builder()
                                .id(1L)
                                .username("admin")
                                .displayName("管理员")
                                .status("active")
                                .roles(List.of("super_admin"))
                                .permissions(List.of("rbac:user:list"))
                                .build())
                        .build()
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.needChangePassword").value(true))
                .andExpect(jsonPath("$.data.passwordTip").value("当前密码为默认密码，请尽快修改密码"));
    }

    @Test
    void changePasswordSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .principal(() -> "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "oldPassword": "123456",
                                  "newPassword": "abc123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"));

        verify(authService).changePassword(org.mockito.ArgumentMatchers.eq("admin"), any());
    }
}
