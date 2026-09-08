package com.gancg.hikingmanageback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gancg.hikingmanageback.entity.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RouteMapper extends BaseMapper<Route> {
    /** 按字符串ID升序分页查询路线。 */
    @Select("SELECT * FROM routes ORDER BY id ASC LIMIT #{pageSize} OFFSET #{offset}")
    List<Route> selectRoutePage(@Param("offset") long offset, @Param("pageSize") int pageSize);

    /** 删除路线费用明细。 */
    @Delete("DELETE FROM route_cost_items WHERE route_id = #{routeId}")
    int deleteRouteCostItems(@Param("routeId") String routeId);

    /** 删除路线交通费用明细。 */
    @Delete("DELETE FROM transport_cost_items WHERE route_id = #{routeId}")
    int deleteTransportCostItems(@Param("routeId") String routeId);

    /** 删除路线停车点。 */
    @Delete("DELETE FROM route_parking_points WHERE route_id = #{routeId}")
    int deleteParkingPoints(@Param("routeId") String routeId);

    /** 删除路线交通画像。 */
    @Delete("DELETE FROM traffic_profiles WHERE route_id = #{routeId}")
    int deleteTrafficProfile(@Param("routeId") String routeId);

    /** 删除路线出行反馈。 */
    @Delete("DELETE FROM trip_feedback WHERE route_id = #{routeId}")
    int deleteTripFeedback(@Param("routeId") String routeId);
}
