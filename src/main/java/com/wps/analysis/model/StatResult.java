package com.wps.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条统计结果：某月某二级公司的活跃用户数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatResult {

    /** 统计月份（格式：yyyy年M月，如 2025年1月） */
    private String month;

    /** 二级公司名称 */
    private String company;

    /** 去重活跃用户数 */
    private Long userCount;
}
