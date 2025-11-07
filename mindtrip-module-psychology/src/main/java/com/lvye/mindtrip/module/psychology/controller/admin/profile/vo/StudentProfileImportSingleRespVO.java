package com.lvye.mindtrip.module.psychology.controller.admin.profile.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个学生档案导入结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileImportSingleRespVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 学生档案主键 ID（失败为 null）
     */
    private Long id;
}


