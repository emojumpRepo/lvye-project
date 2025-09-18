-- 更新危机事件处理过程表结构
-- 执行时间：2025-01-17
-- 作者：芋道源码

-- 添加新字段到lvye_crisis_event_process表
ALTER TABLE lvye_crisis_event_process 
ADD COLUMN reason VARCHAR(500) COMMENT '操作原因' AFTER content,
ADD COLUMN related_user_id BIGINT(20) COMMENT '涉及的用户ID（如新负责人ID）' AFTER reason,
ADD COLUMN original_user_id BIGINT(20) COMMENT '原用户ID（如原负责人ID）' AFTER related_user_id;

-- 为新字段添加索引
ALTER TABLE lvye_crisis_event_process
ADD INDEX idx_related_user_id (related_user_id),
ADD INDEX idx_original_user_id (original_user_id);

-- 更新现有数据（可选）
-- 如果需要迁移现有的content字段中的数据，可以根据实际情况编写迁移脚本