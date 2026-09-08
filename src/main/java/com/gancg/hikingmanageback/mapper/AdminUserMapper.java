package com.gancg.hikingmanageback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gancg.hikingmanageback.entity.AdminUser;
import com.gancg.hikingmanageback.model.RbacUserRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    @Select("SELECT * FROM admin_users WHERE username = #{username} LIMIT 1")
    AdminUser selectByUsername(@Param("username") String username);

    @Select({
            "SELECT",
            "  u.id,",
            "  u.username,",
            "  u.display_name AS displayName,",
            "  u.email,",
            "  u.mobile,",
            "  u.status,",
            "  COALESCE(group_concat(r.code), '') AS roleCodesCsv",
            "FROM admin_users u",
            "LEFT JOIN user_roles ur ON ur.user_id = u.id",
            "LEFT JOIN roles r ON r.id = ur.role_id",
            "GROUP BY u.id",
            "ORDER BY u.id DESC"
    })
    List<RbacUserRow> selectUserRows();

    @Select({
            "SELECT",
            "  u.id,",
            "  u.username,",
            "  u.display_name AS displayName,",
            "  u.email,",
            "  u.mobile,",
            "  u.status,",
            "  COALESCE(group_concat(r.code), '') AS roleCodesCsv",
            "FROM admin_users u",
            "LEFT JOIN user_roles ur ON ur.user_id = u.id",
            "LEFT JOIN roles r ON r.id = ur.role_id",
            "WHERE u.id = #{userId}",
            "GROUP BY u.id"
    })
    RbacUserRow selectUserRowById(@Param("userId") Long userId);
}
