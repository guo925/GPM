USE graduation;

ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS student_no VARCHAR(50) COMMENT '学号' AFTER email,
    ADD COLUMN IF NOT EXISTS grade VARCHAR(20) COMMENT '年级' AFTER student_no;
