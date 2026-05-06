package com.wps.analysis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文档操作日志实体（分表，表名动态设置）
 * 实际表名：log_doc_yyyy_MM
 */
@Data
@TableName("log_doc")
public class LogDoc {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 企业ID */
    private Long compId;

    /** 操作人ID（关联用户表userid） */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作人邮箱 */
    private String operatorEmail;

    /** 操作时间（Unix毫秒时间戳） */
    private Long operationTime;

    /** 操作类型 */
    private String operationType;

    /** 文件名 */
    private String fileName;

    /** 扩展ID */
    private Long extId;

    /** 部门ID路径 */
    private String deptIdPath;

    /** 分组ID */
    private Long groupId;

    /** IP地址 */
    private String ipAddr;

    /** 元数据 */
    private String metaData;

    /** 平台类型 */
    private String platformType;
}
