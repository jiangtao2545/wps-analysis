package com.wps.analysis.service.impl;

import com.wps.analysis.mapper.LogDocMapper;
import com.wps.analysis.model.StatMatrix;
import com.wps.analysis.model.StatQuery;
import com.wps.analysis.model.StatResult;
import com.wps.analysis.service.CsvService;
import com.wps.analysis.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现：按月统计各二级公司活跃用户数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final LogDocMapper logDocMapper;
    private final CsvService csvService;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    /** 分表名前缀 */
    private static final String TABLE_PREFIX = "log_doc_";

    /** 月份展示格式 */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月");

    /** 分表名月份格式 */
    private static final DateTimeFormatter TABLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy_MM");

    @Override
    public List<StatResult> queryStatResults(StatQuery query) {
        List<YearMonth> months = buildMonthRange(query);
        List<String> allLoginNames = csvService.getAllLoginNames();
        Map<String, String> loginToCompany = csvService.getAllMappings();
        String dbName = extractDbName();

        if (allLoginNames.isEmpty()) {
            log.warn("CSV映射为空，无法进行统计");
            return Collections.emptyList();
        }

        List<StatResult> results = new ArrayList<>();

        for (YearMonth ym : months) {
            String tableName = TABLE_PREFIX + ym.format(TABLE_FORMATTER);
            String displayMonth = ym.format(DISPLAY_FORMATTER);

            // 检查分表是否存在
            if (!tableExists(tableName, dbName)) {
                log.debug("分表不存在，跳过：{}", tableName);
                continue;
            }

            // 查询该月各login_name对应的活跃用户数
            List<LogDocMapper.LoginNameCount> counts =
                    logDocMapper.countActiveByLoginNames(tableName, allLoginNames);

            // 按二级公司汇总
            Map<String, Long> companyCount = new HashMap<>();
            for (LogDocMapper.LoginNameCount lnc : counts) {
                String company = loginToCompany.get(lnc.getLoginName());
                if (company != null) {
                    companyCount.merge(company, lnc.getCount(), Long::sum);
                }
            }

            // 转换为StatResult列表
            for (Map.Entry<String, Long> entry : companyCount.entrySet()) {
                results.add(new StatResult(displayMonth, entry.getKey(), entry.getValue()));
            }
        }

        return results;
    }

    @Override
    public StatMatrix queryStatMatrix(StatQuery query) {
        List<YearMonth> months = buildMonthRange(query);
        List<String> allLoginNames = csvService.getAllLoginNames();
        Map<String, String> loginToCompany = csvService.getAllMappings();
        List<String> companies = csvService.getAllCompanies();
        String dbName = extractDbName();

        StatMatrix matrix = new StatMatrix();
        List<String> displayMonths = new ArrayList<>();
        Map<String, Map<String, Long>> dataMatrix = new LinkedHashMap<>();

        for (YearMonth ym : months) {
            String tableName = TABLE_PREFIX + ym.format(TABLE_FORMATTER);
            String displayMonth = ym.format(DISPLAY_FORMATTER);
            displayMonths.add(displayMonth);

            // 初始化该月所有公司数据为0
            Map<String, Long> monthData = new LinkedHashMap<>();
            for (String company : companies) {
                monthData.put(company, 0L);
            }

            // 检查分表是否存在，不存在则全部填0
            if (!tableExists(tableName, dbName)) {
                log.debug("分表不存在，月份 {} 全部填0：{}", displayMonth, tableName);
                dataMatrix.put(displayMonth, monthData);
                continue;
            }

            if (!allLoginNames.isEmpty()) {
                // 查询该月各login_name对应的活跃用户数
                List<LogDocMapper.LoginNameCount> counts =
                        logDocMapper.countActiveByLoginNames(tableName, allLoginNames);

                // 按二级公司汇总
                for (LogDocMapper.LoginNameCount lnc : counts) {
                    String company = loginToCompany.get(lnc.getLoginName());
                    if (company != null && monthData.containsKey(company)) {
                        monthData.merge(company, lnc.getCount(), Long::sum);
                    }
                }
            }

            dataMatrix.put(displayMonth, monthData);
        }

        matrix.setMonths(displayMonths);
        matrix.setCompanies(companies);
        matrix.setMatrix(dataMatrix);
        return matrix;
    }

    /**
     * 生成起止月份之间的所有自然月列表（正序）
     */
    private List<YearMonth> buildMonthRange(StatQuery query) {
        YearMonth start = query.getStartYearMonth();
        YearMonth end = query.getEndYearMonth();

        if (start == null || end == null) {
            // 默认查询近12个月
            end = YearMonth.now();
            start = end.minusMonths(11);
        }

        if (start.isAfter(end)) {
            YearMonth tmp = start;
            start = end;
            end = tmp;
        }

        List<YearMonth> months = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }
        return months;
    }

    /**
     * 检查分表是否存在
     */
    private boolean tableExists(String tableName, String dbName) {
        try {
            int count = logDocMapper.checkTableExists(tableName, dbName);
            return count > 0;
        } catch (Exception e) {
            log.warn("检查分表存在性异常：{}", tableName, e);
            return false;
        }
    }

    /**
     * 从datasource URL中提取数据库名
     * 格式：jdbc:mysql://host:port/dbName?params
     */
    private String extractDbName() {
        if (datasourceUrl == null || datasourceUrl.isEmpty()) return "";
        try {
            // 去掉 jdbc: 前缀，解析为 URI
            String uriStr = datasourceUrl.startsWith("jdbc:") ? datasourceUrl.substring(5) : datasourceUrl;
            // 取 ? 前的路径部分，提取最后一个 / 后的内容
            String withoutParams = uriStr.contains("?") ? uriStr.substring(0, uriStr.indexOf('?')) : uriStr;
            int lastSlash = withoutParams.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < withoutParams.length() - 1) {
                return withoutParams.substring(lastSlash + 1);
            }
        } catch (Exception e) {
            log.warn("解析数据库名失败，URL：{}", datasourceUrl);
        }
        return "";
    }
}
