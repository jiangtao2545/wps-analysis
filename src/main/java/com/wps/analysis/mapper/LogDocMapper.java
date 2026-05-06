package com.wps.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wps.analysis.entity.LogDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档操作日志Mapper（支持动态分表）
 */
@Mapper
public interface LogDocMapper extends BaseMapper<LogDoc> {

    /**
     * 查询指定分表中当月所有活跃用户ID（去重）
     *
     * @param tableName 分表名（如 log_doc_2025_06）
     * @param loginNames 在CSV中有映射关系的login_name列表（通过用户表关联）
     * @return 活跃用户login_name列表（去重）
     */
    List<String> queryActiveLoginNames(@Param("tableName") String tableName,
                                       @Param("loginNames") List<String> loginNames);

    /**
     * 按二级公司批量统计指定分表的活跃用户数
     * 返回：login_name -> count 映射数据
     *
     * @param tableName  分表名
     * @param loginNames 参与统计的login_name列表
     * @return 每个login_name对应的活跃用户数（已去重）
     */
    List<LoginNameCount> countActiveByLoginNames(@Param("tableName") String tableName,
                                                  @Param("loginNames") List<String> loginNames);

    /**
     * 检查分表是否存在
     *
     * @param tableName 分表名
     * @param dbName    数据库名
     * @return 表存在数量（0或1）
     */
    int checkTableExists(@Param("tableName") String tableName,
                         @Param("dbName") String dbName);

    /**
     * 内部结果类：login_name与对应活跃用户数
     */
    class LoginNameCount {
        private String loginName;
        private Long count;

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }
}
