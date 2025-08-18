-- Standalone index creation script for psychology module (MySQL compatible)
-- Run once after tables are created

-- Unique index on assessment task number
ALTER TABLE lvye_assessment_task
  ADD UNIQUE INDEX uk_lvye_assessment_task_no (task_no);

-- Assessment participant indexes
ALTER TABLE lvye_assessment_participant
  ADD INDEX idx_psy_participant_task (task_id),
  ADD INDEX idx_psy_participant_student (student_profile_id);

-- Assessment answer indexes
ALTER TABLE lvye_assessment_answer
  ADD INDEX idx_psy_answer_participant (participant_id);

-- Assessment result indexes
ALTER TABLE lvye_assessment_result
  ADD INDEX idx_psy_result_participant (participant_id);

-- Student timeline indexes (skipped)

-- Crisis intervention indexes (skipped)

-- Unique index on dept ext
ALTER TABLE lvye_dept_ext
  ADD UNIQUE INDEX uk_psy_dept_ext_dept (dept_id);

-- Helpful additional indexes
ALTER TABLE lvye_assessment_user_task
  ADD INDEX idx_psy_user_task_user (user_id);

ALTER TABLE lvye_assessment_dept_task
  ADD INDEX idx_psy_dept_task_dept (dept_id);

-- Consultation record indexes (skipped)


