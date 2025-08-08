-- Psychology module core tables (H2/MySQL compatible subset)

CREATE TABLE IF NOT EXISTS psy_student_profile (
    id                BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    member_user_id    BIGINT,
    student_no        VARCHAR(64) NOT NULL,
    name              VARCHAR(64),
    sex               TINYINT,
    mobile            VARCHAR(32),
    grade_dept_id     BIGINT,
    class_dept_id     BIGINT,
    graduation_status INT,
    psychological_status INT,
    risk_level        INT,
    remark            VARCHAR(512),
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_psy_assessment_task_no ON psy_assessment_task(task_no);
CREATE INDEX IF NOT EXISTS idx_psy_participant_task ON psy_assessment_participant(task_id);
CREATE INDEX IF NOT EXISTS idx_psy_participant_student ON psy_assessment_participant(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_psy_answer_participant ON psy_assessment_answer(participant_id);
CREATE INDEX IF NOT EXISTS idx_psy_result_participant ON psy_assessment_result(participant_id);
CREATE INDEX IF NOT EXISTS idx_psy_timeline_student ON psy_student_timeline(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_psy_timeline_event ON psy_student_timeline(event_type);
CREATE INDEX IF NOT EXISTS idx_psy_crisis_student ON psy_crisis_intervention(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_psy_crisis_source ON psy_crisis_intervention(source_type);
CREATE INDEX IF NOT EXISTS idx_psy_crisis_reporter_source ON psy_crisis_intervention(reporter_user_id, source_type);
CREATE UNIQUE INDEX IF NOT EXISTS uk_psy_dept_ext_dept ON psy_dept_ext(dept_id);

CREATE TABLE IF NOT EXISTS psy_assessment_task (
    id                BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    task_no           VARCHAR(64) NOT NULL,
    name              VARCHAR(128) NOT NULL,
    scale_code        VARCHAR(64) NOT NULL,
    target_audience   INT NOT NULL,
    status            INT NOT NULL,
    publish_user_id   BIGINT,
    deadline          TIMESTAMP,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_assessment_participant (
    id                   BIGINT PRIMARY KEY,
    tenant_id            BIGINT,
    task_id              BIGINT NOT NULL,
    student_profile_id   BIGINT NOT NULL,
    is_parent            BIT,
    completion_status    INT NOT NULL,
    start_time           TIMESTAMP,
    submit_time          TIMESTAMP,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_assessment_answer (
    id                BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    participant_id    BIGINT NOT NULL,
    question_index    INT NOT NULL,
    answer            VARCHAR(256),
    score             INT,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_assessment_result (
    id                BIGINT PRIMARY KEY,
    tenant_id         BIGINT,
    participant_id    BIGINT NOT NULL,
    dimension_code    VARCHAR(64) NOT NULL,
    score             INT,
    risk_level        INT,
    suggestion        VARCHAR(1024),
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_student_timeline (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT,
    student_profile_id  BIGINT NOT NULL,
    event_type          INT NOT NULL,
    title               VARCHAR(128),
    content             VARCHAR(2048),
    biz_id              BIGINT,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

-- Removed: psy_quick_report folded into crisis_intervention via sourceType/reporterUserId/reportedAt/urgencyLevel

CREATE TABLE IF NOT EXISTS psy_consultation_record (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT,
    student_profile_id  BIGINT NOT NULL,
    counselor_user_id   BIGINT,
    type                INT,
    method              INT,
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    duration_minutes    INT,
    content             VARCHAR(2048),
    suggestion          VARCHAR(2048),
    status              INT,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_crisis_intervention (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT,
    student_profile_id  BIGINT NOT NULL,
    title               VARCHAR(128),
    description         VARCHAR(2048),
    risk_level          INT,
    status              INT NOT NULL,
    handler_user_id     BIGINT,
    source_type         INT NOT NULL,
    reporter_user_id    BIGINT,
    reported_at         TIMESTAMP,
    urgency_level       INT,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);

CREATE TABLE IF NOT EXISTS psy_dept_ext (
    id                  BIGINT PRIMARY KEY,
    tenant_id           BIGINT,
    dept_id             BIGINT,
    dept_type           INT,
    grade_no            INT,
    class_no            INT,
    head_teacher_user_id BIGINT,
    creator           VARCHAR(64),
    updater           VARCHAR(64),
    create_time       TIMESTAMP,
    update_time       TIMESTAMP,
    deleted           BIT
);


