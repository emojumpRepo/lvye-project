-- Drop optional modules: student timeline, crisis intervention, consultation record
-- Run this after tables were created if you want to exclude these modules

-- Dropping tables will implicitly drop their indexes as well
DROP TABLE IF EXISTS lvye_student_timeline;
DROP TABLE IF EXISTS lvye_crisis_intervention;
DROP TABLE IF EXISTS lvye_consultation_record;


