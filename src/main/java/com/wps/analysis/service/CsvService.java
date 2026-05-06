package com.wps.analysis.service;

import java.util.List;
import java.util.Map;

/**
 * CSV公司表加载服务
 * 负责加载和维护 loginname → 二级公司 的映射缓存
 */
public interface CsvService {

    /**
     * 加载（或重新加载）CSV文件，刷新缓存
     */
    void reload();

    /**
     * 根据login_name获取对应的二级公司名称
     *
     * @param loginName 用户登录名
     * @return 二级公司名称，若无映射返回null
     */
    String getCompanyByLoginName(String loginName);

    /**
     * 获取所有 loginname → company 的映射（不可变视图）
     *
     * @return 映射Map
     */
    Map<String, String> getAllMappings();

    /**
     * 获取所有去重的二级公司名称列表（保持CSV中顺序）
     *
     * @return 二级公司名称列表
     */
    List<String> getAllCompanies();

    /**
     * 获取所有已知的loginName列表
     *
     * @return loginName列表
     */
    List<String> getAllLoginNames();
}
