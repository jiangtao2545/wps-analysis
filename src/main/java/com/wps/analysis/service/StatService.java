package com.wps.analysis.service;

import com.wps.analysis.model.StatMatrix;
import com.wps.analysis.model.StatQuery;
import com.wps.analysis.model.StatResult;

import java.util.List;

/**
 * 统计服务：按月统计各二级公司的活跃用户数
 */
public interface StatService {

    /**
     * 查询统计结果列表
     *
     * @param query 查询参数（起止月份）
     * @return 统计结果列表
     */
    List<StatResult> queryStatResults(StatQuery query);

    /**
     * 查询统计矩阵（月份×二级公司的交叉统计表）
     *
     * @param query 查询参数（起止月份）
     * @return 统计矩阵
     */
    StatMatrix queryStatMatrix(StatQuery query);
}
