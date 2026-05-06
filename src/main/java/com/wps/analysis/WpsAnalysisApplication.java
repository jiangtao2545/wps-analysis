package com.wps.analysis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WPS文档操作日志统计分析应用启动类
 */
@SpringBootApplication
@MapperScan("com.wps.analysis.mapper")
public class WpsAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(WpsAnalysisApplication.class, args);
    }
}
