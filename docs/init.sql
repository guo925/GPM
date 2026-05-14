-- GPMS 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS graduation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE graduation;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status INT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    college_id BIGINT COMMENT '所属学院ID',
    major_id BIGINT COMMENT '所属专业ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除'
) COMMENT '系统用户';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    status INT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '系统角色';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限标识',
    group_name VARCHAR(50) COMMENT '权限分组',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '系统权限';

-- 4. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID'
) COMMENT '用户角色关联';

-- 5. 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID'
) COMMENT '角色权限关联';

-- 6. 学院表
CREATE TABLE IF NOT EXISTS college (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '学院代码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '学院';

-- 7. 专业表
CREATE TABLE IF NOT EXISTS major (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '专业代码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '专业';

-- 8. 批次表
CREATE TABLE IF NOT EXISTS batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '批次名称',
    grade VARCHAR(20) NOT NULL COMMENT '年级',
    college_id BIGINT NOT NULL COMMENT '学院ID',
    major_id BIGINT NOT NULL COMMENT '专业ID',
    current_stage VARCHAR(50) DEFAULT 'topic_selection' COMMENT '当前阶段',
    config TEXT COMMENT '时间节点配置JSON',
    max_student_per_teacher INT DEFAULT 5 COMMENT '每导师最多带学生数',
    selection_mode VARCHAR(30) DEFAULT 'voluntary' COMMENT '双选模式',
    student_max_choices INT DEFAULT 3 COMMENT '学生可选志愿数',
    allow_teacher_reject INT DEFAULT 1 COMMENT '是否允许导师拒绝',
    reject_strategy VARCHAR(30) DEFAULT 'pool' COMMENT '被拒后策略',
    status INT DEFAULT 1 COMMENT '状态 1进行中 0已结束',
    created_by BIGINT COMMENT '创建者用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '毕设批次';

-- 9. 课题表
CREATE TABLE IF NOT EXISTS topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '所属批次ID',
    title VARCHAR(200) NOT NULL COMMENT '题目名称',
    description TEXT COMMENT '题目描述',
    source VARCHAR(30) DEFAULT 'preset' COMMENT '来源: preset/student_propose',
    creator_id BIGINT NOT NULL COMMENT '创建人用户ID',
    max_capacity INT DEFAULT 1 COMMENT '可容纳学生数',
    current_count INT DEFAULT 0 COMMENT '已选人数',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    review_comment VARCHAR(500) COMMENT '审核意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '课题';

-- 10. 选题记录表
CREATE TABLE IF NOT EXISTS selection_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    topic_id BIGINT NOT NULL COMMENT '题目ID',
    priority TINYINT COMMENT '志愿序号',
    teacher_action VARCHAR(20) COMMENT '教师操作: approve/reject',
    teacher_comment VARCHAR(500) COMMENT '教师意见',
    is_selected TINYINT DEFAULT 0 COMMENT '是否最终选中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '学生操作时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '教师操作时间'
) COMMENT '选题记录';

-- 11. 学生选题关系表
CREATE TABLE IF NOT EXISTS student_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    topic_id BIGINT NOT NULL COMMENT '题目ID',
    advisor_id BIGINT NOT NULL COMMENT '指导教师ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active/transferred/deferred/extended',
    allocation_time DATETIME COMMENT '分配时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '学生选题关系';

-- 12. 流程实例表（状态机）
CREATE TABLE IF NOT EXISTS process_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_topic_id BIGINT NOT NULL COMMENT '学生选题ID',
    stage VARCHAR(50) NOT NULL COMMENT '阶段',
    status VARCHAR(20) DEFAULT 'not_started' COMMENT '状态',
    submitter_id BIGINT COMMENT '提交人',
    submitted_at DATETIME COMMENT '提交时间',
    file_path VARCHAR(500) COMMENT '附件路径',
    content TEXT COMMENT '文本内容',
    reviewer_id BIGINT COMMENT '审核人',
    reviewed_at DATETIME COMMENT '审核时间',
    review_comment VARCHAR(500) COMMENT '审核意见',
    version INT DEFAULT 1 COMMENT '版本号',
    is_editable TINYINT DEFAULT 0 COMMENT '通过后是否仍可修改',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '流程实例';

-- 13. 指导记录表
CREATE TABLE IF NOT EXISTS guidance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_topic_id BIGINT NOT NULL,
    week_number INT COMMENT '第几周',
    content TEXT COMMENT '学生填写内容',
    file_path VARCHAR(500),
    status VARCHAR(20) DEFAULT 'draft',
    advisor_comment VARCHAR(500) COMMENT '导师评语',
    reviewed_at DATETIME COMMENT '批阅时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '指导记录';

-- 14. 答辩批次表
CREATE TABLE IF NOT EXISTS defense_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '所属毕设批次',
    type VARCHAR(30) NOT NULL COMMENT '答辩类型',
    name VARCHAR(200) NOT NULL COMMENT '名称',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    location_template VARCHAR(500) COMMENT '地点模版',
    is_supplementary TINYINT DEFAULT 0 COMMENT '是否补答辩',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '答辩批次';

-- 15. 答辩组表
CREATE TABLE IF NOT EXISTS defense_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    defense_batch_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '组名',
    leader_id BIGINT NOT NULL COMMENT '组长ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '答辩组';

-- 16. 答辩组成员表
CREATE TABLE IF NOT EXISTS defense_group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'member' COMMENT 'leader/member',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '答辩组成员';

-- 17. 答辩安排表
CREATE TABLE IF NOT EXISTS defense_arrangement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    defense_batch_id BIGINT COMMENT '答辩批次ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    group_id BIGINT NOT NULL COMMENT '答辩组ID',
    defense_time DATETIME COMMENT '答辩时间',
    location VARCHAR(200) COMMENT '地点',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '答辩安排';

-- 18. 答辩结果表
CREATE TABLE IF NOT EXISTS defense_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    arrangement_id BIGINT NOT NULL COMMENT '答辩安排ID',
    score_items TEXT COMMENT '评分项JSON',
    total_score DECIMAL(5,2) COMMENT '总分',
    decision VARCHAR(30) COMMENT '答辩意见',
    comment VARCHAR(500) COMMENT '评语',
    recorded_by BIGINT COMMENT '录入人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '答辩结果';

-- 19. 成绩单表
CREATE TABLE IF NOT EXISTS score_sheet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_topic_id BIGINT NOT NULL,
    batch_id BIGINT COMMENT '批次ID',
    final_score DECIMAL(5,2) COMMENT '综合总分',
    grade_level VARCHAR(10) COMMENT '等级',
    status VARCHAR(20) DEFAULT 'draft',
    reviewed_by BIGINT COMMENT '审核人',
    review_comment VARCHAR(500),
    submitted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '成绩单';

-- 20. 成绩明细表
CREATE TABLE IF NOT EXISTS score_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sheet_id BIGINT NOT NULL,
    type VARCHAR(20) COMMENT '评分类型: advisor/reviewer/defense',
    score DECIMAL(5,2) COMMENT '分数',
    weight DECIMAL(4,2) COMMENT '权重',
    comment VARCHAR(500),
    reviewer_id BIGINT COMMENT '评分人',
    is_blind TINYINT DEFAULT 0 COMMENT '是否盲审',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '成绩明细';

-- 21. 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL COMMENT '接收人',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(30) COMMENT '通知类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    read_at DATETIME COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '通知';

-- 22. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '操作人',
    action VARCHAR(50) COMMENT '操作',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志';

-- 23. 审核日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_instance_id BIGINT COMMENT '流程实例ID',
    target_type VARCHAR(50) COMMENT '审核对象类型',
    target_id BIGINT COMMENT '审核对象ID',
    auditor_id BIGINT COMMENT '审核人',
    action VARCHAR(30) COMMENT '操作: approve/reject',
    comment VARCHAR(500) COMMENT '审核意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '审核日志';

-- ==================== 种子数据 ====================

-- 默认管理员 (密码: admin123, BCrypt加密)
-- BCrypt: admin123
INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES
('admin', '$2b$10$AhwQzb94gjFuxtzbyvKZ1.I4w6e4wktsWgj8QhiQWTYfBYRvXT9Qi', '系统管理员', '13800000000', 'admin@gpms.com', 1);

-- 权限数据
INSERT INTO sys_permission (permission_name, permission_code, group_name) VALUES
('用户新增', 'system:user:add', '用户管理'),
('用户修改', 'system:user:update', '用户管理'),
('用户删除', 'system:user:delete', '用户管理'),
('用户查询', 'system:user:query', '用户管理'),
('用户分页', 'system:user:page', '用户管理'),
('用户状态', 'system:user:status', '用户管理'),
('重置密码', 'system:user:reset-password', '用户管理'),
('角色查询', 'system:user:role:query', '用户管理'),
('角色分配', 'system:user:role:assign', '用户管理'),
('角色新增', 'system:role:add', '角色管理'),
('角色修改', 'system:role:update', '角色管理'),
('角色删除', 'system:role:delete', '角色管理'),
('角色分页', 'system:role:page', '角色管理'),
('角色列表', 'system:role:list', '角色管理'),
('角色查询', 'system:role:query', '角色管理'),
('权限列表', 'system:permission:list', '权限管理'),
('权限新增', 'system:permission:add', '权限管理'),
('权限修改', 'system:permission:update', '权限管理'),
('权限删除', 'system:permission:delete', '权限管理'),
('学院列表', 'system:college:list', '学院管理'),
('学院新增', 'system:college:add', '学院管理'),
('学院修改', 'system:college:update', '学院管理'),
('学院删除', 'system:college:delete', '学院管理'),
('专业列表', 'system:major:list', '专业管理'),
('专业新增', 'system:major:add', '专业管理'),
('专业修改', 'system:major:update', '专业管理'),
('专业删除', 'system:major:delete', '专业管理'),
('批次分页', 'batch:page', '批次管理'),
('批次查询', 'batch:query', '批次管理'),
('批次新增', 'batch:add', '批次管理'),
('批次修改', 'batch:update', '批次管理'),
('批次删除', 'batch:delete', '批次管理'),
('批次阶段推进', 'batch:stage', '批次管理'),
('课题分页', 'topic:page', '课题管理'),
('课题新增', 'topic:add', '课题管理'),
('课题修改', 'topic:update', '课题管理'),
('课题删除', 'topic:delete', '课题管理'),
('课题审核', 'topic:review', '课题管理'),
('志愿提交', 'selection:submit', '选题管理'),
('志愿查看', 'selection:my', '选题管理'),
('志愿审核', 'selection:review', '选题管理'),
('自动分配', 'selection:allocate', '选题管理'),
('选题结果', 'student-topic:page', '选题管理'),
('流程提交', 'process:submit', '流程管理'),
('流程审核', 'process:review', '流程管理'),
('流程查询', 'process:query', '流程管理'),
('周记提交', 'guidance:submit', '指导记录'),
('周记批阅', 'guidance:review', '指导记录'),
('周记查询', 'guidance:query', '指导记录'),
('答辩批次新增', 'defense:batch:add', '答辩管理'),
('答辩批次删除', 'defense:batch:delete', '答辩管理'),
('答辩组新增', 'defense:group:add', '答辩管理'),
('答辩组删除', 'defense:group:delete', '答辩管理'),
('答辩安排', 'defense:arrange', '答辩管理'),
('答辩结果', 'defense:result', '答辩管理'),
('成绩计算', 'score:calculate', '成绩管理'),
('成绩提交', 'score:submit', '成绩管理'),
('成绩审核', 'score:review', '成绩管理'),
('操作日志', 'log:page', '日志管理'),
('审核日志', 'audit-log:page', '日志管理'),
('成绩导出', 'export:score', '导出管理'),
('论文查重', 'plagiarism:check', '查重管理');

-- 超级管理员角色
INSERT INTO sys_role (role_name, role_code, status) VALUES
('超级管理员', 'SUPER_ADMIN', 1);

-- 给超级管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 给admin用户分配超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
