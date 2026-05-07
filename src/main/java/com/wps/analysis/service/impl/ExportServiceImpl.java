package com.wps.analysis.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.wps.analysis.model.StatMatrix;
import com.wps.analysis.model.StatQuery;
import com.wps.analysis.service.ExportService;
import com.wps.analysis.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Excel导出服务实现（EasyExcel动态表头、流式写出）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final StatService statService;

    private static final DateTimeFormatter FILE_TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public void exportExcel(StatQuery query, HttpServletResponse response) throws IOException {
        StatMatrix matrix = statService.queryStatMatrix(query);
        List<String> months = matrix.getMonths();
        List<String> companies = matrix.getCompanies();
        Map<String, Map<String, Long>> dataMatrix = matrix.getMatrix();

        // 生成文件名
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
        String fileName = "二级公司月度用户统计_" + timestamp + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                .replace("+", "%20");

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 构建动态表头（第一行：月份 + 所有二级公司）
        List<List<String>> head = buildHead(companies);

        // 构建数据行
        List<List<Object>> dataRows = buildDataRows(months, companies, dataMatrix);

        // 样式设置
        HorizontalCellStyleStrategy styleStrategy = buildStyleStrategy();

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                .head(head)
                .registerWriteHandler(styleStrategy)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("月度用户统计").build();
            excelWriter.write(dataRows, writeSheet);
        }

        log.info("Excel导出完成，文件：{}，月份数：{}，公司数：{}", fileName, months.size(), companies.size());
    }

    /**
     * 构建动态表头（单行表头）
     * 第一列：月份；中间列：各二级公司名称；最后一列：总数
     */
    private List<List<String>> buildHead(List<String> companies) {
        List<List<String>> head = new ArrayList<>();
        head.add(Collections.singletonList("月份"));
        for (String company : companies) {
            head.add(Collections.singletonList(company));
        }
        head.add(Collections.singletonList("总数"));
        return head;
    }

    /**
     * 构建数据行
     * 每行：月份显示值 + 各公司用户数（无数据填0）+ 行总数
     */
    private List<List<Object>> buildDataRows(List<String> months,
                                              List<String> companies,
                                              Map<String, Map<String, Long>> dataMatrix) {
        List<List<Object>> rows = new ArrayList<>();
        for (String month : months) {
            List<Object> row = new ArrayList<>();
            row.add(month);
            Map<String, Long> companyData = dataMatrix.getOrDefault(month, Collections.emptyMap());
            long total = 0L;
            for (String company : companies) {
                long value = companyData.getOrDefault(company, 0L);
                row.add(value);
                total += value;
            }
            row.add(total);
            rows.add(row);
        }
        return rows;
    }

    /**
     * 构建单元格样式策略：
     * - 表头：加粗、灰色背景、完整边框、居中
     * - 数据：完整边框
     */
    private HorizontalCellStyleStrategy buildStyleStrategy() {
        // 表头样式
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        setBorderStyle(headStyle, BorderStyle.THIN);

        WriteFont headFont = new WriteFont();
        headFont.setBold(true);
        headFont.setFontHeightInPoints((short) 11);
        headStyle.setWriteFont(headFont);

        // 数据行样式
        WriteCellStyle contentStyle = new WriteCellStyle();
        setBorderStyle(contentStyle, BorderStyle.THIN);
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 设置四周边框样式
     */
    private void setBorderStyle(WriteCellStyle style, BorderStyle borderStyle) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
    }
}
