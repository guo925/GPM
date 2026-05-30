-- GPMS 单体架构冷热数据分离、归档与异步消息改造脚本
-- 执行前请先备份 graduation 数据库。本脚本不删除原始业务表，原表继续作为兼容层和迁移来源。
USE graduation;

-- ==================== 课题 current/history ====================
CREATE TABLE IF NOT EXISTS topic_current LIKE topic;
INSERT IGNORE INTO topic_current SELECT * FROM topic;

CREATE TABLE IF NOT EXISTS topic_history LIKE topic;
ALTER TABLE topic_history
    ADD COLUMN IF NOT EXISTS archive_time DATETIME NULL COMMENT '归档时间',
    ADD COLUMN IF NOT EXISTS archive_batch_id BIGINT NULL COMMENT '归档批次ID',
    ADD COLUMN IF NOT EXISTS archive_operator BIGINT NULL COMMENT '归档操作人';
CREATE INDEX idx_topic_current_batch ON topic_current(batch_id);
CREATE INDEX idx_topic_history_archive_batch ON topic_history(archive_batch_id);

-- ==================== 选题 current/history ====================
CREATE TABLE IF NOT EXISTS selection_current LIKE selection_record;
INSERT IGNORE INTO selection_current SELECT * FROM selection_record;

CREATE TABLE IF NOT EXISTS selection_history LIKE selection_record;
ALTER TABLE selection_history
    ADD COLUMN IF NOT EXISTS archive_time DATETIME NULL COMMENT '归档时间',
    ADD COLUMN IF NOT EXISTS archive_batch_id BIGINT NULL COMMENT '归档批次ID',
    ADD COLUMN IF NOT EXISTS archive_operator BIGINT NULL COMMENT '归档操作人';
CREATE INDEX idx_selection_current_batch_student ON selection_current(batch_id, student_id);
CREATE INDEX idx_selection_history_archive_batch ON selection_history(archive_batch_id);

-- ==================== 成绩 current/history ====================
CREATE TABLE IF NOT EXISTS score_current LIKE score_sheet;
INSERT IGNORE INTO score_current SELECT * FROM score_sheet;

CREATE TABLE IF NOT EXISTS score_history LIKE score_sheet;
ALTER TABLE score_history
    ADD COLUMN IF NOT EXISTS archive_time DATETIME NULL COMMENT '归档时间',
    ADD COLUMN IF NOT EXISTS archive_batch_id BIGINT NULL COMMENT '归档批次ID',
    ADD COLUMN IF NOT EXISTS archive_operator BIGINT NULL COMMENT '归档操作人';
CREATE INDEX idx_score_current_batch ON score_current(batch_id);
CREATE INDEX idx_score_history_archive_batch ON score_history(archive_batch_id);

-- ==================== 周记 current/history ====================
-- 原系统周记实体为 guidance_record，按要求建立 weekly_log_current/history 作为冷热分离表。
CREATE TABLE IF NOT EXISTS weekly_log_current LIKE guidance_record;
INSERT IGNORE INTO weekly_log_current SELECT gr.*
FROM guidance_record gr
INNER JOIN student_topic st ON st.id = gr.student_topic_id;

CREATE TABLE IF NOT EXISTS weekly_log_history LIKE guidance_record;
ALTER TABLE weekly_log_history
    ADD COLUMN IF NOT EXISTS archive_time DATETIME NULL COMMENT '归档时间',
    ADD COLUMN IF NOT EXISTS archive_batch_id BIGINT NULL COMMENT '归档批次ID',
    ADD COLUMN IF NOT EXISTS archive_operator BIGINT NULL COMMENT '归档操作人';
CREATE INDEX idx_weekly_log_current_student_topic ON weekly_log_current(student_topic_id);
CREATE INDEX idx_weekly_log_history_archive_batch ON weekly_log_history(archive_batch_id);

-- ==================== 归档幂等日志 ====================
CREATE TABLE IF NOT EXISTS archive_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '归档批次ID',
    operator_id BIGINT NULL COMMENT '操作人',
    topic_count INT DEFAULT 0 COMMENT '课题归档数量',
    selection_count INT DEFAULT 0 COMMENT '选题归档数量',
    score_count INT DEFAULT 0 COMMENT '成绩归档数量',
    weekly_log_count INT DEFAULT 0 COMMENT '周记归档数量',
    status VARCHAR(20) NOT NULL COMMENT '状态 SUCCESS/FAILED',
    message VARCHAR(500) NULL COMMENT '归档说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_archive_log_batch_success (batch_id, status)
) COMMENT '冷热数据归档日志';
