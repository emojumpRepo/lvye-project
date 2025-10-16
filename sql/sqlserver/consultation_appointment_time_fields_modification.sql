-- ====================================================
-- SQL Server - 咨询预约表字段修改脚本
-- 将 appointment_time 字段拆分为 appointment_start_time 和 appointment_end_time
-- ====================================================

-- 1. 添加新字段
ALTER TABLE [lvye_consultation_appointment] 
ADD [appointment_start_time] DATETIME2,
    [appointment_end_time] DATETIME2
GO

-- 2. 数据迁移：将原有的 appointment_time 数据迁移到新字段
-- 先确认要更新的记录数量
-- SELECT COUNT(*) FROM [lvye_consultation_appointment] WHERE [appointment_time] IS NOT NULL AND [appointment_start_time] IS NULL
-- GO

-- 安全的数据迁移（只更新新字段为NULL的记录）
UPDATE [lvye_consultation_appointment] 
SET [appointment_start_time] = [appointment_time],
    [appointment_end_time] = DATEADD(MINUTE, ISNULL([duration_minutes], 60), [appointment_time])
WHERE [appointment_time] IS NOT NULL 
  AND [appointment_start_time] IS NULL 
  AND [appointment_end_time] IS NULL
GO

-- 3. 设置新字段为非空（在数据迁移完成后）
ALTER TABLE [lvye_consultation_appointment] 
ALTER COLUMN [appointment_start_time] DATETIME2 NOT NULL
GO

ALTER TABLE [lvye_consultation_appointment] 
ALTER COLUMN [appointment_end_time] DATETIME2 NOT NULL
GO

-- 4. 添加字段注释
EXEC sp_addextendedproperty 
    'MS_Description', N'预约咨询的开始时间',
    'SCHEMA', N'dbo',
    'TABLE', N'lvye_consultation_appointment',
    'COLUMN', N'appointment_start_time'
GO

EXEC sp_addextendedproperty 
    'MS_Description', N'预约咨询的结束时间',
    'SCHEMA', N'dbo',
    'TABLE', N'lvye_consultation_appointment', 
    'COLUMN', N'appointment_end_time'
GO

-- 5. 添加新的索引
CREATE INDEX idx_appointment_start_time ON [lvye_consultation_appointment] ([appointment_start_time])
GO

CREATE INDEX idx_appointment_end_time ON [lvye_consultation_appointment] ([appointment_end_time])
GO

-- 6. 删除旧字段和旧索引
DROP INDEX [idx_appointment_time] ON [lvye_consultation_appointment]
GO

ALTER TABLE [lvye_consultation_appointment] DROP COLUMN [appointment_time]
GO
