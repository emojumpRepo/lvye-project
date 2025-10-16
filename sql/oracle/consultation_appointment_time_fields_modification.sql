-- ====================================================
-- Oracle - 咨询预约表字段修改脚本
-- 将 appointment_time 字段拆分为 appointment_start_time 和 appointment_end_time
-- ====================================================

-- 1. 添加新字段
ALTER TABLE lvye_consultation_appointment 
ADD (
  appointment_start_time DATE,
  appointment_end_time DATE
);

-- 2. 添加字段注释
COMMENT ON COLUMN lvye_consultation_appointment.appointment_start_time IS '预约咨询的开始时间';
COMMENT ON COLUMN lvye_consultation_appointment.appointment_end_time IS '预约咨询的结束时间';

-- 3. 数据迁移：将原有的 appointment_time 数据迁移到新字段
-- 先确认要更新的记录数量
-- SELECT COUNT(*) FROM lvye_consultation_appointment WHERE appointment_time IS NOT NULL AND appointment_start_time IS NULL;

-- 安全的数据迁移（只更新新字段为NULL的记录）
UPDATE lvye_consultation_appointment 
SET appointment_start_time = appointment_time,
    appointment_end_time = appointment_time + (NVL(duration_minutes, 60) / (24 * 60))
WHERE appointment_time IS NOT NULL 
  AND appointment_start_time IS NULL 
  AND appointment_end_time IS NULL;

-- 4. 提交数据迁移
COMMIT;

-- 5. 设置新字段为非空（在数据迁移完成后）
ALTER TABLE lvye_consultation_appointment 
MODIFY (
  appointment_start_time DATE NOT NULL,
  appointment_end_time DATE NOT NULL
);

-- 6. 添加新的索引
CREATE INDEX idx_appointment_start_time ON lvye_consultation_appointment (appointment_start_time);
CREATE INDEX idx_appointment_end_time ON lvye_consultation_appointment (appointment_end_time);

-- 7. 删除旧字段和旧索引
DROP INDEX idx_appointment_time;
ALTER TABLE lvye_consultation_appointment DROP COLUMN appointment_time;
