package com.wps.analysis.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统计矩阵结果：月份 × 二级公司 的交叉统计表
 */
@Data
public class StatMatrix {

    /** 所有月份列表（按时间正序，格式：yyyy年MM月） */
    private List<String> months;

    /** 所有二级公司列表（按CSV中顺序去重） */
    private List<String> companies;

    /**
     * 数据矩阵：key = "月份" → map（二级公司 → 用户数）
     * 例：{"2025年1月" -> {"华北分公司"->10, "华南分公司"->5}}
     */
    private Map<String, Map<String, Long>> matrix;
}
