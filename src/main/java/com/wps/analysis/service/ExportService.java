package com.wps.analysis.service;

import com.wps.analysis.model.StatQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Excel导出服务
 */
public interface ExportService {

    /**
     * 导出统计矩阵到Excel（流式写出到Response）
     *
     * @param query    查询参数（起止月份）
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportExcel(StatQuery query, HttpServletResponse response) throws IOException;
}
