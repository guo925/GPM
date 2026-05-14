package com.gjx.gpms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GpMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GpMsApplication.class, args);
    }

    @Bean
    CommandLineRunner fixAdminPassword(JdbcTemplate jdbcTemplate) {
        return args -> {
            // 自动创建系统表
            try { jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user (id BIGINT AUTO_INCREMENT PRIMARY KEY,username VARCHAR(50) NOT NULL UNIQUE,password VARCHAR(255) NOT NULL,real_name VARCHAR(50),phone VARCHAR(20),email VARCHAR(100),status INT DEFAULT 1,college_id BIGINT,major_id BIGINT,create_time DATETIME DEFAULT CURRENT_TIMESTAMP,is_deleted INT DEFAULT 0)"); } catch (Exception ignored) {}

            // 自动补齐缺失的列
            try { jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN college_id BIGINT COMMENT '所属学院ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN major_id BIGINT COMMENT '所属专业ID'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE sys_permission ADD COLUMN group_name VARCHAR(50) COMMENT '权限分组'"); } catch (Exception ignored) {}

            // 自动创建缺失的业务表
            String[] tables = {
                "CREATE TABLE IF NOT EXISTS college (id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(100) NOT NULL,code VARCHAR(50) NOT NULL UNIQUE,sort_order INT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS major (id BIGINT AUTO_INCREMENT PRIMARY KEY,college_id BIGINT NOT NULL,name VARCHAR(100) NOT NULL,code VARCHAR(50) NOT NULL UNIQUE,sort_order INT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS batch (id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(200) NOT NULL,grade VARCHAR(20) NOT NULL,college_id BIGINT NOT NULL,major_id BIGINT NOT NULL,current_stage VARCHAR(50) DEFAULT 'topic_selection',config TEXT,max_student_per_teacher INT DEFAULT 5,selection_mode VARCHAR(30) DEFAULT 'voluntary',student_max_choices INT DEFAULT 3,allow_teacher_reject INT DEFAULT 1,reject_strategy VARCHAR(30) DEFAULT 'pool',status INT DEFAULT 1,created_by BIGINT,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS topic (id BIGINT AUTO_INCREMENT PRIMARY KEY,batch_id BIGINT NOT NULL,title VARCHAR(200) NOT NULL,description TEXT,source VARCHAR(30) DEFAULT 'preset',creator_id BIGINT NOT NULL,max_capacity INT DEFAULT 1,current_count INT DEFAULT 0,status VARCHAR(20) DEFAULT 'pending',review_comment VARCHAR(500),created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS selection_record (id BIGINT AUTO_INCREMENT PRIMARY KEY,batch_id BIGINT NOT NULL,student_id BIGINT NOT NULL,topic_id BIGINT NOT NULL,priority TINYINT,teacher_action VARCHAR(20),teacher_comment VARCHAR(500),is_selected TINYINT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS student_topic (id BIGINT AUTO_INCREMENT PRIMARY KEY,batch_id BIGINT NOT NULL,student_id BIGINT NOT NULL,topic_id BIGINT NOT NULL,advisor_id BIGINT NOT NULL,status VARCHAR(20) DEFAULT 'active',allocation_time DATETIME,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS process_instance (id BIGINT AUTO_INCREMENT PRIMARY KEY,student_topic_id BIGINT NOT NULL,stage VARCHAR(50) NOT NULL,status VARCHAR(20) DEFAULT 'not_started',submitter_id BIGINT,submitted_at DATETIME,file_path VARCHAR(500),content TEXT,reviewer_id BIGINT,reviewed_at DATETIME,review_comment VARCHAR(500),version INT DEFAULT 1,is_editable TINYINT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS guidance_record (id BIGINT AUTO_INCREMENT PRIMARY KEY,student_topic_id BIGINT NOT NULL,week_number INT,content TEXT,file_path VARCHAR(500),status VARCHAR(20) DEFAULT 'draft',advisor_comment VARCHAR(500),reviewed_at DATETIME,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS defense_batch (id BIGINT AUTO_INCREMENT PRIMARY KEY,batch_id BIGINT NOT NULL,type VARCHAR(30) NOT NULL,name VARCHAR(200) NOT NULL,start_time DATETIME,end_time DATETIME,location_template VARCHAR(500),is_supplementary TINYINT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS defense_group (id BIGINT AUTO_INCREMENT PRIMARY KEY,defense_batch_id BIGINT NOT NULL,name VARCHAR(100) NOT NULL,leader_id BIGINT NOT NULL,created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS defense_group_member (id BIGINT AUTO_INCREMENT PRIMARY KEY,group_id BIGINT NOT NULL,teacher_id BIGINT NOT NULL,role VARCHAR(20) DEFAULT 'member',created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS defense_arrangement (id BIGINT AUTO_INCREMENT PRIMARY KEY,defense_batch_id BIGINT,student_id BIGINT NOT NULL,group_id BIGINT NOT NULL,defense_time DATETIME,location VARCHAR(200),created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS defense_result (id BIGINT AUTO_INCREMENT PRIMARY KEY,arrangement_id BIGINT NOT NULL,score_items TEXT,total_score DECIMAL(5,2),decision VARCHAR(30),comment VARCHAR(500),recorded_by BIGINT,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS score_sheet (id BIGINT AUTO_INCREMENT PRIMARY KEY,student_topic_id BIGINT NOT NULL,batch_id BIGINT,final_score DECIMAL(5,2),grade_level VARCHAR(10),status VARCHAR(20) DEFAULT 'draft',reviewed_by BIGINT,review_comment VARCHAR(500),submitted_at DATETIME,created_at DATETIME DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS score_detail (id BIGINT AUTO_INCREMENT PRIMARY KEY,sheet_id BIGINT NOT NULL,type VARCHAR(20),score DECIMAL(5,2),weight DECIMAL(4,2),comment VARCHAR(500),reviewer_id BIGINT,is_blind TINYINT DEFAULT 0,created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS notification (id BIGINT AUTO_INCREMENT PRIMARY KEY,recipient_id BIGINT NOT NULL,title VARCHAR(200) NOT NULL,content TEXT,type VARCHAR(30),is_read TINYINT DEFAULT 0,read_at DATETIME,created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS operation_log (id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT,action VARCHAR(50),target_type VARCHAR(50),target_id BIGINT,old_value TEXT,new_value TEXT,ip_address VARCHAR(50),user_agent VARCHAR(500),remark VARCHAR(500),created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS audit_log (id BIGINT AUTO_INCREMENT PRIMARY KEY,process_instance_id BIGINT,target_type VARCHAR(50),target_id BIGINT,auditor_id BIGINT,action VARCHAR(30),comment VARCHAR(500),created_at DATETIME DEFAULT CURRENT_TIMESTAMP)"
            };
            for (String sql : tables) {
                try { jdbcTemplate.execute(sql); } catch (Exception ignored) {}
            }

            // 统一密码哈希
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode("admin123");

            // ==================== 种子数据 ====================

            // 创建系统表（如果不存在）
            try { jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role (id BIGINT AUTO_INCREMENT PRIMARY KEY,role_name VARCHAR(50) NOT NULL,role_code VARCHAR(50) NOT NULL UNIQUE,status INT DEFAULT 1,create_time DATETIME DEFAULT CURRENT_TIMESTAMP)"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_permission (id BIGINT AUTO_INCREMENT PRIMARY KEY,permission_name VARCHAR(100) NOT NULL,permission_code VARCHAR(100) NOT NULL UNIQUE,group_name VARCHAR(50),create_time DATETIME DEFAULT CURRENT_TIMESTAMP)"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_role (id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT NOT NULL,role_id BIGINT NOT NULL)"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role_permission (id BIGINT AUTO_INCREMENT PRIMARY KEY,role_id BIGINT NOT NULL,permission_id BIGINT NOT NULL)"); } catch (Exception ignored) {}

            // 种子：权限数据
            try {
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户新增','system:user:add','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户修改','system:user:update','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户删除','system:user:delete','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户查询','system:user:query','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户分页','system:user:page','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('用户状态','system:user:status','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('重置密码','system:user:reset-password','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色查询','system:user:role:query','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色分配','system:user:role:assign','用户管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色新增','system:role:add','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色修改','system:role:update','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色删除','system:role:delete','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色分页','system:role:page','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色列表','system:role:list','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('角色查询','system:role:query','角色管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('权限列表','system:permission:list','权限管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('权限新增','system:permission:add','权限管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('权限修改','system:permission:update','权限管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('权限删除','system:permission:delete','权限管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('学院列表','system:college:list','学院管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('学院新增','system:college:add','学院管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('学院修改','system:college:update','学院管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('学院删除','system:college:delete','学院管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('专业列表','system:major:list','专业管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('专业新增','system:major:add','专业管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('专业修改','system:major:update','专业管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('专业删除','system:major:delete','专业管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次分页','batch:page','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次查询','batch:query','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次新增','batch:add','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次修改','batch:update','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次删除','batch:delete','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('批次阶段推进','batch:stage','批次管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('课题分页','topic:page','课题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('课题新增','topic:add','课题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('课题修改','topic:update','课题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('课题删除','topic:delete','课题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('课题审核','topic:review','课题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('志愿提交','selection:submit','选题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('志愿查看','selection:my','选题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('志愿审核','selection:review','选题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('自动分配','selection:allocate','选题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('选题结果','student-topic:page','选题管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('流程提交','process:submit','流程管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('流程审核','process:review','流程管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('流程查询','process:query','流程管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('周记提交','guidance:submit','指导记录')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('周记批阅','guidance:review','指导记录')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('周记查询','guidance:query','指导记录')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩批次新增','defense:batch:add','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩批次删除','defense:batch:delete','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩组新增','defense:group:add','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩组删除','defense:group:delete','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩安排','defense:arrange','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('答辩结果','defense:result','答辩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('成绩计算','score:calculate','成绩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('成绩提交','score:submit','成绩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('成绩审核','score:review','成绩管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('操作日志','log:page','日志管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('审核日志','audit-log:page','日志管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('成绩导出','export:score','导出管理')");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_permission (permission_name, permission_code, group_name) VALUES ('论文查重','plagiarism:check','查重管理')");
            } catch (Exception ignored) {}

            // 种子数据：创建角色
            try {
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (1, '超级管理员', 'SUPER_ADMIN', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (2, '校级管理员', 'UNIVERSITY_ADMIN', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (3, '院级管理员', 'COLLEGE_ADMIN', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (4, '年级管理员', 'GRADE_ADMIN', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (5, '专业管理员', 'MAJOR_ADMIN', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (6, '教师', 'TEACHER', 1)");
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role (id, role_name, role_code, status) VALUES (7, '学生', 'STUDENT', 1)");
            } catch (Exception ignored) {}

            // 清理非种子用户（之前可能因 bug 产生了重复/多余用户）
            try {
                jdbcTemplate.execute("DELETE FROM sys_user_role WHERE user_id IN (SELECT id FROM sys_user WHERE username NOT IN ('admin','u_admin','c_admin','g_admin','m_admin','teacher1','student1'))");
                jdbcTemplate.execute("DELETE FROM sys_user WHERE username NOT IN ('admin','u_admin','c_admin','g_admin','m_admin','teacher1','student1')");
            } catch (Exception ignored) {}

            // 种子数据：逐个创建用户（每个独立 try-catch，互不影响）
            String[][] seedUsers = {
                {"admin", "系统管理员", "13800000000", "admin@gpms.com"},
                {"u_admin", "校级管理员", "13800000001", "u_admin@gpms.com"},
                {"c_admin", "院级管理员", "13800000002", "c_admin@gpms.com"},
                {"g_admin", "年级管理员", "13800000003", "g_admin@gpms.com"},
                {"m_admin", "专业管理员", "13800000004", "m_admin@gpms.com"},
                {"teacher1", "张老师", "13800000005", "teacher1@gpms.com"},
                {"student1", "李同学", "13800000006", "student1@gpms.com"}
            };
            for (String[] u : seedUsers) {
                try {
                    int updated = jdbcTemplate.update(
                        "UPDATE sys_user SET password = ?, real_name = ?, phone = ?, email = ?, status = 1 WHERE username = ?",
                        hash, u[1], u[2], u[3], u[0]
                    );
                    if (updated == 0) {
                        jdbcTemplate.update(
                            "INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES (?, ?, ?, ?, ?, 1)",
                            u[0], hash, u[1], u[2], u[3]
                        );
                        System.out.println("创建用户：" + u[0]);
                    }
                } catch (Exception e) {
                    System.out.println("用户 " + u[0] + " 操作失败：" + e.getMessage());
                }
            }

            // 种子数据：添加唯一约束防止重复
            try { jdbcTemplate.execute("ALTER TABLE sys_user_role ADD UNIQUE INDEX idx_user_role (user_id, role_id)"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE sys_role_permission ADD UNIQUE INDEX idx_role_perm (role_id, permission_id)"); } catch (Exception ignored) {}

            // 种子数据：分配角色（基于用户名查询用户ID）
            String[][] userRoleMapping = {
                {"admin", "1"}, {"u_admin", "2"}, {"c_admin", "3"},
                {"g_admin", "4"}, {"m_admin", "5"}, {"teacher1", "6"}, {"student1", "7"}
            };
            for (String[] ur : userRoleMapping) {
                try {
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO sys_user_role (user_id, role_id) " +
                        "SELECT u.id, ? FROM sys_user u WHERE u.username = ?",
                        Integer.parseInt(ur[1]), ur[0]
                    );
                } catch (Exception e) {
                    System.out.println("分配角色失败 " + ur[0] + "：" + e.getMessage());
                }
            }

            // 种子数据：给超级管理员分配所有权限（兼容已有数据）
            try { jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 1, id FROM sys_permission"); } catch (Exception ignored) {}

            // 种子数据：给各角色分配权限
            try {
                // 校级管理员：学院+专业+用户+批次+课题+选题管理
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 2, id FROM sys_permission WHERE permission_code IN ('system:college:list','system:college:add','system:college:update','system:college:delete','system:major:list','system:major:add','system:major:update','system:major:delete','system:user:page','system:user:query','system:user:add','system:user:update','system:user:delete','system:user:status','system:user:reset-password','system:user:role:query','batch:page','batch:query','batch:add','batch:update','batch:delete','batch:stage','topic:page','topic:query','topic:review')");

                // 院级管理员：专业+用户+批次+课题管理（限本院）
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 3, id FROM sys_permission WHERE permission_code IN ('system:college:list','system:major:list','system:major:add','system:major:update','system:user:page','system:user:query','system:user:add','system:user:update','system:user:status','batch:page','batch:query','batch:add','batch:update','batch:delete','topic:page','topic:query','topic:review')");

                // 年级管理员：批次+课题+选题管理（需要学院/专业列表权限用于新建批次表单）
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 4, id FROM sys_permission WHERE permission_code IN ('batch:page','batch:query','batch:add','batch:update','batch:delete','batch:stage','topic:page','topic:query','topic:review','student-topic:page','selection:review','system:college:list','system:major:list')");

                // 专业管理员：批次+课题+监控（选题/流程/成绩查看）
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 5, id FROM sys_permission WHERE permission_code IN ('batch:page','batch:query','batch:add','batch:update','batch:delete','topic:page','topic:query','topic:review','system:college:list','system:major:list','student-topic:page','selection:review','process:query','system:user:page')");

                // 教师：课题+选题审核+指导记录+流程审核+成绩评定+批次查看
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 6, id FROM sys_permission WHERE permission_code IN ('topic:page','topic:query','topic:add','topic:update','batch:page','batch:query','selection:review','selection:my','guidance:submit','guidance:review','guidance:query','process:review','process:query','score:calculate','score:submit','student-topic:page','defense:result')");

                // 学生：课题浏览+选题+流程提交+指导记录查看+批次查看
                jdbcTemplate.execute("INSERT IGNORE INTO sys_role_permission (role_id, permission_id) SELECT 7, id FROM sys_permission WHERE permission_code IN ('topic:page','topic:query','batch:page','batch:query','selection:submit','selection:my','process:submit','process:query','guidance:query','student-topic:page')");
            } catch (Exception ignored) {}

            System.out.println("种子数据已初始化：7个角色 + 7个用户 + 权限已分配");
        };
    }
}
