package com.wps.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wps.analysis.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据operator_id列表批量查询login_name
     * 关联链：日志表operator_id → 用户表userid → 用户表login_name
     *
     * @param operatorIds operator_id列表（来自日志表）
     * @return 对应的login_name列表（去重）
     */
    List<String> selectLoginNamesByUserIds(@Param("userIds") List<Long> userIds);
}
