package com.wps.analysis.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

/**
 * 统计查询参数
 */
@Data
public class StatQuery {

    /**
     * 开始月份（格式：yyyy-MM，如 2025-01）
     */
    private String startMonth;

    /**
     * 结束月份（格式：yyyy-MM，如 2025-12）
     */
    private String endMonth;

    /**
     * 解析开始月份为 YearMonth
     */
    public YearMonth getStartYearMonth() {
        return startMonth != null ? YearMonth.parse(startMonth) : null;
    }

    /**
     * 解析结束月份为 YearMonth
     */
    public YearMonth getEndYearMonth() {
        return endMonth != null ? YearMonth.parse(endMonth) : null;
    }
}
