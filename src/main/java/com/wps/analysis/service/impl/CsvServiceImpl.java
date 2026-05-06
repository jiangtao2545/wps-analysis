package com.wps.analysis.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.wps.analysis.service.CsvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSV公司表加载服务实现
 */
@Slf4j
@Service
public class CsvServiceImpl implements CsvService {

    @Value("${app.csv-file-path:classpath:公司表.csv}")
    private String csvFilePath;

    /** loginname → 二级公司 映射缓存（线程安全） */
    private volatile Map<String, String> loginNameToCompany = new ConcurrentHashMap<>();

    /** 所有二级公司列表（保序去重） */
    private volatile List<String> companies = new ArrayList<>();

    /** company -> headcount上限（按唯一loginname计数） */
    private volatile Map<String, Integer> companyHeadcounts = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public synchronized void reload() {
        log.info("开始加载CSV公司表，路径：{}", csvFilePath);
        try {
            InputStream inputStream = resolveInputStream(csvFilePath);
            if (inputStream == null) {
                log.warn("CSV文件未找到，路径：{}", csvFilePath);
                return;
            }

            Map<String, String> newMapping = new LinkedHashMap<>();
            LinkedHashSet<String> newCompanies = new LinkedHashSet<>();

            try (CSVReader reader = new CSVReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String[] headers = reader.readNext(); // 跳过表头
                if (headers == null) {
                    log.warn("CSV文件为空：{}", csvFilePath);
                    return;
                }

                // 找loginname和company列的索引
                int loginIdx = -1, companyIdx = -1;
                for (int i = 0; i < headers.length; i++) {
                    String h = headers[i].trim().toLowerCase();
                    if ("loginname".equals(h)) loginIdx = i;
                    if ("company".equals(h)) companyIdx = i;
                }

                if (loginIdx < 0 || companyIdx < 0) {
                    log.error("CSV文件缺少必需列 loginname 或 company，表头：{}", Arrays.toString(headers));
                    return;
                }

                String[] line;
                int count = 0;
                while ((line = reader.readNext()) != null) {
                    if (line.length <= Math.max(loginIdx, companyIdx)) {
                        continue;
                    }
                    String loginName = line[loginIdx].trim();
                    String company = line[companyIdx].trim();
                    if (!loginName.isEmpty() && !company.isEmpty()) {
                        newMapping.put(loginName, company);
                        newCompanies.add(company);
                        count++;
                    }
                }

                // 原子替换缓存
                this.loginNameToCompany = new ConcurrentHashMap<>(newMapping);
                this.companies = new ArrayList<>(newCompanies);
                Map<String, Integer> newHeadcounts = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : newMapping.entrySet()) {
                    newHeadcounts.merge(entry.getValue(), 1, Integer::sum);
                }
                this.companyHeadcounts = new ConcurrentHashMap<>(newHeadcounts);
                log.info("CSV加载完成，共加载 {} 条记录，二级公司数量：{}", count, newCompanies.size());
            }

        } catch (IOException | CsvValidationException e) {
            log.error("CSV文件加载异常", e);
        }
    }

    @Override
    public String getCompanyByLoginName(String loginName) {
        if (loginName == null) return null;
        return loginNameToCompany.get(loginName);
    }

    @Override
    public Map<String, String> getAllMappings() {
        return Collections.unmodifiableMap(loginNameToCompany);
    }

    @Override
    public List<String> getAllCompanies() {
        return Collections.unmodifiableList(companies);
    }

    @Override
    public List<String> getAllLoginNames() {
        return new ArrayList<>(loginNameToCompany.keySet());
    }

    @Override
    public Map<String, Integer> getCompanyHeadcounts() {
        return Collections.unmodifiableMap(companyHeadcounts);
    }

    /**
     * 解析文件路径，支持 classpath: 前缀和普通文件路径
     */
    private InputStream resolveInputStream(String path) throws IOException {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length()).trim();
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (resource.exists()) {
                return resource.getInputStream();
            }
            log.warn("classpath资源不存在：{}", resourcePath);
            return null;
        } else {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
            log.warn("文件不存在：{}", path);
            return null;
        }
    }
}
