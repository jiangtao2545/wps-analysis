package com.wps.analysis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 扩量配置：按月总量统一放大倍数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.inflate")
public class InflateProperties {

    /** 默认扩量倍数 */
    private double defaultFactor = 3.0d;
}

