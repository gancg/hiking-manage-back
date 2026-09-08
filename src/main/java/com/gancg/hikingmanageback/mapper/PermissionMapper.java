package com.gancg.hikingmanageback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gancg.hikingmanageback.entity.Permission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    @Select({
            "SELECT DISTINCT p.code",
            "FROM permissions p",
            "INNER JOIN role_permissions rp ON rp.permission_id = p.id",
            "INNER JOIN user_roles ur ON ur.role_id = rp.role_id",
            "WHERE ur.user_id = #{userId} AND p.status = 'active'"
    })
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM permissions WHERE id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    @Select({
            "SELECT id, code, name, resource_type, resource, action, parent_id, sort_order, status, created_at, updated_at",
            "FROM permissions",
            "ORDER BY id ASC",
            "LIMIT #{pageSize} OFFSET #{offset}"
    })
    List<Permission> selectPermissionPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(1) FROM permissions")
    long countPermissions();
}
