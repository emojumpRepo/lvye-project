-- ----------------------------
-- 问卷表添加测评维度字段
-- 添加测评维度字典配置
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 为 lvye_questionnaire 表添加测评维度字段
ALTER TABLE `lvye_questionnaire` 
ADD COLUMN `assessment_dimension` VARCHAR(50) COMMENT '测评维度（字典：questionnaire_assessment_dimension）' AFTER `target_audience`;

-- 2. 添加索引优化查询
CREATE INDEX `idx_assessment_dimension` ON `lvye_questionnaire`(`assessment_dimension`);

-- 3. 添加测评维度字典类型
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) 
VALUES (NULL, '问卷测评维度', 'questionnaire_assessment_dimension', 0, '问卷测评维度分类', 'admin', NOW(), 'admin', NOW(), 0, NULL);

-- 获取刚插入的字典类型ID（用于后续插入字典数据）
SET @dict_type_id = LAST_INSERT_ID();

-- 4. 添加测评维度字典数据
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(NULL, 1, '情绪状态', 'emotional_state', 'questionnaire_assessment_dimension', 0, 'primary', '', '评估学生的情绪状态和情感健康', 'admin', NOW(), 'admin', NOW(), 0),
(NULL, 2, '压力水平', 'stress_level', 'questionnaire_assessment_dimension', 0, 'warning', '', '评估学生的压力水平和应对能力', 'admin', NOW(), 'admin', NOW(), 0),
(NULL, 3, '社交适应', 'social_adaptation', 'questionnaire_assessment_dimension', 0, 'success', '', '评估学生的社交能力和人际关系适应性', 'admin', NOW(), 'admin', NOW(), 0),
(NULL, 4, '学习适应', 'learning_adaptation', 'questionnaire_assessment_dimension', 0, 'info', '', '评估学生的学习适应能力和学业表现', 'admin', NOW(), 'admin', NOW(), 0),
(NULL, 5, '自我认知', 'self_awareness', 'questionnaire_assessment_dimension', 0, 'default', '', '评估学生的自我认知和自我评价能力', 'admin', NOW(), 'admin', NOW(), 0);

-- 5. 验证字典配置
SELECT 
    dt.name as dict_type_name,
    dt.type as dict_type_code,
    dd.label as dict_label,
    dd.value as dict_value,
    dd.remark as dict_remark
FROM system_dict_type dt
LEFT JOIN system_dict_data dd ON dt.type = dd.dict_type
WHERE dt.type = 'questionnaire_assessment_dimension'
ORDER BY dd.sort;

-- 6. 显示表结构变更结果
DESCRIBE lvye_questionnaire;

SET FOREIGN_KEY_CHECKS = 1;

-- 使用说明：
-- 1. 测评维度字段使用 questionnaire_assessment_dimension 字典
-- 2. 字典值说明：
--    - emotional_state: 情绪状态 - 评估学生的情绪状态和情感健康
--    - stress_level: 压力水平 - 评估学生的压力水平和应对能力  
--    - social_adaptation: 社交适应 - 评估学生的社交能力和人际关系适应性
--    - learning_adaptation: 学习适应 - 评估学生的学习适应能力和学业表现
--    - self_awareness: 自我认知 - 评估学生的自我认知和自我评价能力
-- 3. 新增问卷时，需要选择对应的测评维度
-- 4. 可以根据测评维度对问卷进行分类和筛选
