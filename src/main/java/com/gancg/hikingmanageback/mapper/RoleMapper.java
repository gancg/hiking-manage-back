package com.gancg.hikingmanageback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gancg.hikingmanageback.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select({
            "SELECT DISTINCT r.code",
            "FROM roles r",
            "INNER JOIN user_roles ur ON ur.role_id = r.id",
            "WHERE ur.user_id = #{userId} AND r.status = 'active'"
    })
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
