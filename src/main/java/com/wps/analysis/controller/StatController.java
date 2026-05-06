package com.wps.analysis.controller;

import com.wps.analysis.model.StatMatrix;
import com.wps.analysis.model.StatQuery;
import com.wps.analysis.model.StatResult;
import com.wps.analysis.service.CsvService;
import com.wps.analysis.service.ExportService;
import com.wps.analysis.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计与导出REST控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final ExportService exportService;
    private final CsvService csvService;

    /**
     * 查询统计结果列表
     * GET /api/stat/list?startMonth=2025-01&endMonth=2025-06
     *
     * @param startMonth 开始月份（格式：yyyy-MM）
     * @param endMonth   结束月份（格式：yyyy-MM）
     * @return 统计结果列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> queryList(
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth) {

        StatQuery query = new StatQuery();
        query.setStartMonth(startMonth);
        query.setEndMonth(endMonth);

        List<StatResult> results = statService.queryStatResults(query);

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "success");
        resp.put("data", results);
        resp.put("total", results.size());
        return ResponseEntity.ok(resp);
    }

    /**
     * 查询统计矩阵（月份×二级公司）
     * GET /api/stat/matrix?startMonth=2025-01&endMonth=2025-06
     *
     * @param startMonth 开始月份（格式：yyyy-MM）
     * @param endMonth   结束月份（格式：yyyy-MM）
     * @return 统计矩阵
     */
    @GetMapping("/matrix")
    public ResponseEntity<Map<String, Object>> queryMatrix(
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth) {

        StatQuery query = new StatQuery();
        query.setStartMonth(startMonth);
        query.setEndMonth(endMonth);

        StatMatrix matrix = statService.queryStatMatrix(query);

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "success");
        resp.put("data", matrix);
        return ResponseEntity.ok(resp);
    }

    /**
     * 导出Excel文件
     * GET /api/stat/export?startMonth=2025-01&endMonth=2025-06
     *
     * @param startMonth 开始月份（格式：yyyy-MM）
     * @param endMonth   结束月份（格式：yyyy-MM）
     * @param response   HTTP响应
     */
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth,
            HttpServletResponse response) {

        StatQuery query = new StatQuery();
        query.setStartMonth(startMonth);
        query.setEndMonth(endMonth);

        try {
            exportService.exportExcel(query, response);
        } catch (IOException e) {
            log.error("Excel导出异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写出错误响应失败", ex);
            }
        }
    }

    /**
     * 热加载CSV文件（重新加载公司表.csv映射缓存）
     * POST /api/stat/csv/reload
     *
     * @return 重载结果
     */
    @PostMapping("/csv/reload")
    public ResponseEntity<Map<String, Object>> reloadCsv() {
        log.info("收到CSV热加载请求");
        csvService.reload();

        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "CSV重新加载成功");
        resp.put("loginNameCount", csvService.getAllLoginNames().size());
        resp.put("companyCount", csvService.getAllCompanies().size());
        resp.put("companies", csvService.getAllCompanies());
        return ResponseEntity.ok(resp);
    }

    /**
     * 查询当前CSV映射状态
     * GET /api/stat/csv/status
     *
     * @return CSV映射状态
     */
    @GetMapping("/csv/status")
    public ResponseEntity<Map<String, Object>> csvStatus() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 200);
        resp.put("msg", "success");
        resp.put("loginNameCount", csvService.getAllLoginNames().size());
        resp.put("companyCount", csvService.getAllCompanies().size());
        resp.put("companies", csvService.getAllCompanies());
        return ResponseEntity.ok(resp);
    }
}
