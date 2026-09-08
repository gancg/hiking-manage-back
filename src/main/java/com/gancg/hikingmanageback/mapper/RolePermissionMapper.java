package com.gancg.hikingmanageback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gancg.hikingmanageback.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT permission_id FROM role_permissions WHERE role_id = #{roleId} ORDER BY permission_id ASC")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT COUNT(1) FROM role_permissions WHERE permission_id = #{permissionId}")
    long countByPermissionId(@Param("permissionId") Long permissionId);

    @Delete("DELETE FROM role_permissions WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
