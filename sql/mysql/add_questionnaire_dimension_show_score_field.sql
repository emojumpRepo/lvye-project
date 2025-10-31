-- ----------------------------
-- 为问卷维度表添加 show_score 字段
-- 用于标识该维度的得分是否在前端展示
-- Author: MinGoo
-- Date: 2025-10-31
-- ----------------------------

-- 添加 show_score 字段
ALTER TABLE `lvye_questionnaire_dimension`
ADD COLUMN `show_score` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示得分（0：否，1：是）'
AFTER `participate_ranking`;

-- 验证字段添加成功
-- SELECT * FROM `lvye_questionnaire_dimension` LIMIT 1;
