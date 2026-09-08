package com.gancg.hikingmanageback.security;

import com.gancg.hikingmanageback.entity.AdminUser;
import com.gancg.hikingmanageback.mapper.AdminUserMapper;
import com.gancg.hikingmanageback.mapper.PermissionMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminUserMapper adminUserMapper;
    private final PermissionMapper permissionMapper;

    public AdminUserDetailsService(AdminUserMapper adminUserMapper, PermissionMapper permissionMapper) {
        this.adminUserMapper = adminUserMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());
        return new AdminPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), user.getStatus(), permissions);
    }
}
