package com.wps.analysis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体
 */
@Data
@TableName("`user`")
public class User {

    /** 企业用户ID（主键） */
    @TableId("comp_uid")
    private Long compUid;

    /** 成员真实ID（关联日志表operator_id） */
    private Long userid;

    /** 企业ID */
    private Long compId;

    /** 用户名称 */
    private String userName;

    /** 登录名（关联CSV loginname列） */
    private String loginName;

    /** 账号 */
    private String account;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 用户状态 */
    private String status;

    /** 角色ID */
    private Long roleid;

    /** 默认部门ID */
    private Long defDeptId;

    /** 头像 */
    private String avatar;

    /** 是否隐藏 */
    private Integer isHide;

    /** 员工编号 */
    private String employeeId;

    /** 就职单位 */
    private String employer;

    /** 创建时间 */
    private Integer ctime;

    /** 修改时间 */
    private Integer mtime;
}
