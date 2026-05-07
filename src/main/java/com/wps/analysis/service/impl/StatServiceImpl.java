package com.wps.analysis.service.impl;

import com.wps.analysis.config.InflateProperties;
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

/**
 * 统计服务实现：按月统计各二级公司活跃用户数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final LogDocMapper logDocMapper;
    private final CsvService csvService;
    private final InflateProperties inflateProperties;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    /** 分表名前缀 */
    private static final String TABLE_PREFIX = "log_doc_";

    /** 需要从统计中剔除的公司 */
    private static final Set<String> EXCLUDED_COMPANIES = new HashSet<>(Arrays.asList(
            "中仪公司", "通检集团", "中国汽研", "数科公司"
    ));

    /** 月份展示格式 */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月");

    /** 分表名月份格式 */
    private static final DateTimeFormatter TABLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy_MM");

    @Override
    public List<StatResult> queryStatResults(StatQuery query) {
        StatMatrix matrix = queryStatMatrix(query);
        List<StatResult> results = new ArrayList<>();

        for (String month : matrix.getMonths()) {
            Map<String, Long> monthData = matrix.getMatrix().getOrDefault(month, Collections.emptyMap());
            for (Map.Entry<String, Long> entry : monthData.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    results.add(new StatResult(month, entry.getKey(), entry.getValue()));
                }
            }
        }
        return results;
    }

    @Override
    public StatMatrix queryStatMatrix(StatQuery query) {
        List<YearMonth> months = buildMonthRange(query);
        Map<String, String> loginToCompany = csvService.getAllMappings();
        List<String> allLoginNames = new ArrayList<>();
        for (String loginName : csvService.getAllLoginNames()) {
            String company = loginToCompany.get(loginName);
            if (!EXCLUDED_COMPANIES.contains(company)) {
                allLoginNames.add(loginName);
            }
        }

        List<String> companies = new ArrayList<>();
        for (String company : csvService.getAllCompanies()) {
            if (!EXCLUDED_COMPANIES.contains(company)) {
                companies.add(company);
            }
        }

        Map<String, Integer> companyHeadcounts = new HashMap<>();
        for (Map.Entry<String, Integer> entry : csvService.getCompanyHeadcounts().entrySet()) {
            if (!EXCLUDED_COMPANIES.contains(entry.getKey())) {
                companyHeadcounts.put(entry.getKey(), entry.getValue());
            }
        }
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

            monthData = inflateMonthData(monthData, companyHeadcounts);

            dataMatrix.put(displayMonth, monthData);
        }

        matrix.setMonths(displayMonths);
        matrix.setCompanies(companies);
        matrix.setMatrix(dataMatrix);
        return matrix;
    }

    /**
     * 按月总量扩量：仅对当月原本有数据的公司进行放大，月总量尽量达到配置倍数，
     * 且每家公司不超过CSV人数上限。
     */
    private Map<String, Long> inflateMonthData(Map<String, Long> source,
                                               Map<String, Integer> companyHeadcounts) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        Map<String, Long> adjusted = new LinkedHashMap<>();
        long baseTotal = 0L;
        long maxTotal = 0L;
        double factor = Math.max(1.0d, inflateProperties.getDefaultFactor());

        for (Map.Entry<String, Long> entry : source.entrySet()) {
            String company = entry.getKey();
            long base = Math.max(0L, entry.getValue() == null ? 0L : entry.getValue());
            long cap = Math.max(0L, companyHeadcounts.getOrDefault(company, 0));
            long clippedBase = Math.min(base, cap);
            adjusted.put(company, clippedBase);
            baseTotal += clippedBase;
            if (clippedBase > 0L) {
                maxTotal += cap;
            }
        }

        if (factor <= 1.0d || baseTotal <= 0L || maxTotal <= baseTotal) {
            return adjusted;
        }

        long targetTotal = Math.min(maxTotal, Math.round(baseTotal * factor));
        long need = targetTotal - baseTotal;
        if (need <= 0L) {
            return adjusted;
        }

        while (need > 0L) {
            List<String> candidates = new ArrayList<>();
            Map<String, Long> roomMap = new HashMap<>();
            Map<String, Double> weightMap = new HashMap<>();
            double totalWeight = 0.0d;

            for (Map.Entry<String, Long> entry : adjusted.entrySet()) {
                String company = entry.getKey();
                long base = Math.max(0L, source.getOrDefault(company, 0L));
                if (base <= 0L) {
                    continue;
                }

                long current = entry.getValue();
                long cap = Math.max(0L, companyHeadcounts.getOrDefault(company, 0));
                long room = cap - current;
                if (room <= 0L) {
                    continue;
                }

                double weight = room;
                if (weight <= 0.0d) {
                    continue;
                }

                candidates.add(company);
                roomMap.put(company, room);
                weightMap.put(company, weight);
                totalWeight += weight;
            }

            if (candidates.isEmpty() || totalWeight <= 0.0d) {
                break;
            }

            long allocated = 0L;
            Map<String, Double> residueMap = new HashMap<>();

            for (String company : candidates) {
                long room = roomMap.get(company);
                double weight = weightMap.get(company);
                double expected = need * weight / totalWeight;
                long inc = Math.min(room, (long) Math.floor(expected));
                if (inc > 0L) {
                    adjusted.put(company, adjusted.get(company) + inc);
                    allocated += inc;
                }
                residueMap.put(company, expected - Math.floor(expected));
            }

            need -= allocated;
            if (need <= 0L) {
                break;
            }

            candidates.sort((a, b) -> {
                int r = Double.compare(residueMap.getOrDefault(b, 0.0d), residueMap.getOrDefault(a, 0.0d));
                if (r != 0) {
                    return r;
                }
                return Long.compare(roomMap.getOrDefault(b, 0L), roomMap.getOrDefault(a, 0L));
            });

            boolean progressed = false;
            for (String company : candidates) {
                if (need <= 0L) {
                    break;
                }

                long current = adjusted.getOrDefault(company, 0L);
                long cap = Math.max(0L, companyHeadcounts.getOrDefault(company, 0));
                if (current < cap) {
                    adjusted.put(company, current + 1L);
                    need--;
                    progressed = true;
                }
            }

            if (!progressed && allocated == 0L) {
                break;
            }
        }

        return adjusted;
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
