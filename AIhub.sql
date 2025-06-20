-- -----------------------------------------------------
-- AI教学实训智能体 - 数据库表结构
-- -----------------------------------------------------
-- 版本: 2.2 (增加练习提交记录表)
-- -----------------------------------------------------

-- 删除已存在的视图和表，以便于重新创建
DROP VIEW IF EXISTS `v_chapter_tree`;
DROP VIEW IF EXISTS `v_learning_stats`;
DROP TABLE IF EXISTS `exercise_submissions`;
DROP TABLE IF EXISTS `answers`;
DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `assessment_questions`;
DROP TABLE IF EXISTS `questions`;
DROP TABLE IF EXISTS `assessments`;
DROP TABLE IF EXISTS `resources`;
DROP TABLE IF EXISTS `class_members`;
DROP TABLE IF EXISTS `classes`;
DROP TABLE IF EXISTS `enrollments`;
DROP TABLE IF EXISTS `learning_progress`;
DROP TABLE IF EXISTS `learning_records`;
DROP TABLE IF EXISTS `chapter_comments`;
DROP TABLE IF EXISTS `chapter_exercises`;
DROP TABLE IF EXISTS `exercises`;
DROP TABLE IF EXISTS `sync_logs`;
DROP TABLE IF EXISTS `chapters`;
DROP TABLE IF EXISTS `course_chapters`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `chat_messages`;
DROP TABLE IF EXISTS `knowledge_documents`;
DROP TABLE IF EXISTS `system_logs`;
DROP TABLE IF EXISTS `users`;


-- -----------------------------------------------------
-- 核心表: 用户 (users)
-- -----------------------------------------------------
CREATE TABLE `users` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名, 用于登录',
  `email` VARCHAR(100) NOT NULL COMMENT '电子邮箱, 用于登录和通知',
  `user_code` VARCHAR(50) NULL COMMENT '学号或教师编号, 具有唯一性',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '加密后的密码哈希值',
  `full_name` VARCHAR(100) NULL COMMENT '用户真实姓名',
  `avatar` VARCHAR(255) NULL COMMENT '用户头像的URL链接',
  `role` ENUM('student', 'teacher', 'admin') NOT NULL COMMENT '用户角色: student, teacher, or admin',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户账户创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '用户信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志 (0: 未删除, 1: 已删除)',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC),
  UNIQUE INDEX `user_code_UNIQUE` (`user_code` ASC)
)
ENGINE = InnoDB
COMMENT = '存储所有系统用户，包括学生、教师和管理员';


-- =====================================================
-- 简化的混合方案数据库设计
-- 核心思路：Markdown为主，数据库提供索引和扩展功能
-- =====================================================

-- -----------------------------------------------------
-- 核心表: 课程 (courses)
-- -----------------------------------------------------
CREATE TABLE `courses` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `title` VARCHAR(255) NOT NULL COMMENT '课程名称',
  `description` TEXT NULL COMMENT '课程描述',
  `teacher_id` INT NOT NULL COMMENT '教师ID',
  `file_path` VARCHAR(500) NOT NULL COMMENT 'Markdown文件路径',
  `file_hash` VARCHAR(64) NULL COMMENT '文件内容哈希，用于检测变化',
  `file_updated_at` TIMESTAMP NULL COMMENT '文件最后修改时间',
  `parse_status` ENUM('pending', 'success', 'failed') DEFAULT 'pending' COMMENT '解析状态',
  `parse_error` TEXT NULL COMMENT '解析错误信息',
  `parsed_at` TIMESTAMP NULL COMMENT '最后解析时间',
  `chapter_count` INT DEFAULT 0 COMMENT '章节数量',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_teacher` (`teacher_id`),
  INDEX `idx_parse_status` (`parse_status`),
  INDEX `idx_file_hash` (`file_hash`)
) ENGINE = InnoDB COMMENT = '课程基本信息和Markdown文件元数据';

-- -----------------------------------------------------
-- 解析缓存表: 章节信息 (chapters)
-- -----------------------------------------------------
CREATE TABLE `chapters` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `course_id` INT NOT NULL COMMENT '课程ID',
  `parent_id` INT NULL COMMENT '父章节ID, 引用本表的id字段',
  `chapter_key` VARCHAR(100) NOT NULL COMMENT '章节标识（如: 1, 1.1, 1.2.1）',
  `level` INT NOT NULL COMMENT '层级深度 1,2,3...',
  `title` VARCHAR(255) NOT NULL COMMENT '章节标题',
  `content` MEDIUMTEXT NULL COMMENT '章节内容',
  `sort_order` INT NOT NULL COMMENT '排序序号',
  `line_start` INT NULL COMMENT '起始行号',
  `line_end` INT NULL COMMENT '结束行号',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_chapter` (`course_id`, `chapter_key`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_level_sort` (`course_id`, `level`, `sort_order`),
  CONSTRAINT `fk_chapters_course`
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chapters_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `chapters` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB COMMENT = '章节解析缓存，用于快速查询和导航';

-- -----------------------------------------------------
-- 新增核心表: 学期 (semesters)
-- -----------------------------------------------------
CREATE TABLE `semesters` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '学期唯一ID',
  `name` VARCHAR(100) NOT NULL COMMENT '学期名称, 用于显示 (如: 2024-2025学年第一学期)',
  `start_date` DATE NOT NULL COMMENT '学期开始日期',
  `end_date` DATE NOT NULL COMMENT '学期结束日期',
  `status` ENUM('current', 'past', 'future') NOT NULL DEFAULT 'future' COMMENT '学期状态 (current: 当前, past: 已结束, future: 未开始)',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC)
)
ENGINE = InnoDB
COMMENT = '统一管理所有学期信息';
ALTER TABLE semesters INSERT `deleted` TINYINT(1) DEFAULT 0;

CREATE TABLE `classes` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '班级唯一ID',
  `name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  
  -- 核心关联字段
  `teacher_id` INT NOT NULL COMMENT '外键, 关联到users表的教师ID',
  `course_id` INT NULL COMMENT '外键, 关联到courses表的课程ID (可选)',
  `semester_id` INT NOT NULL COMMENT '外键, 关联到semesters表的学期ID', -- 新增的核心外键
  
  `class_code` VARCHAR(8) NOT NULL COMMENT '班级口令/邀请码',
  `status` ENUM('pending', 'active', 'finished', 'archived') NOT NULL DEFAULT 'pending' COMMENT '班级状态 (pending: 未开课, active: 进行中, finished: 已结课, archived: 已归档)',
  
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NULL DEFAULT 0,
  
  PRIMARY KEY (`id`),
  UNIQUE INDEX `class_code_UNIQUE` (`class_code` ASC),
  INDEX `fk_classes_teacher_id_idx` (`teacher_id` ASC),
  INDEX `fk_classes_course_id_idx` (`course_id` ASC),
  INDEX `fk_classes_semester_id_idx` (`semester_id` ASC), -- 为外键创建索引

  CONSTRAINT `fk_classes_teacher_id`
    FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_classes_course_id`
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_classes_semester_id`
    FOREIGN KEY (`semester_id`) REFERENCES `semesters` (`id`) ON DELETE RESTRICT -- 关键约束
)
ENGINE = InnoDB
COMMENT = '与学期关联的班级表';

-- 班级成员表不变
CREATE TABLE `class_members` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `class_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_cm_class_id_idx` (`class_id` ASC),
  INDEX `fk_cm_student_id_idx` (`student_id` ASC),
  UNIQUE INDEX `class_student_unique` (`class_id` ASC, `student_id` ASC),
  CONSTRAINT `fk_cm_class_id`
    FOREIGN KEY (`class_id`) REFERENCES `classes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cm_student_id`
    FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- 教学表: 教学资源 (resources)
-- -----------------------------------------------------
CREATE TABLE `resources` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '资源唯一ID',
  `title` VARCHAR(255) NOT NULL COMMENT '资源标题',
  `type` ENUM('pdf', 'video', 'docx', 'pptx', 'other') NOT NULL COMMENT '资源文件类型',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件在服务器上的存储路径',
  `file_size` INT NULL COMMENT '文件大小 (Bytes)',
  `uploader_id` INT NOT NULL COMMENT '外键, 关联到users表的上传者ID',
  `course_id` INT NULL COMMENT '外键, 关联到courses表的课程ID (可选)',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '资源上传时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_resources_uploader_id_idx` (`uploader_id` ASC),
  INDEX `fk_resources_course_id_idx` (`course_id` ASC),
  CONSTRAINT `fk_resources_uploader_id`
    FOREIGN KEY (`uploader_id`)
    REFERENCES `users` (`id`)
    ON DELETE NO ACTION,
  CONSTRAINT `fk_resources_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE SET NULL
)
ENGINE = InnoDB
COMMENT = '存储上传的教学资源，如课件、视频、文档等';


-- -----------------------------------------------------
-- 功能扩展表: 章节练习 (chapter_exercises)
-- -----------------------------------------------------
CREATE TABLE `chapter_exercises` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '练习唯一ID',
  `course_id` INT NOT NULL COMMENT '课程ID',
  `chapter_id` VARCHAR(100) NOT NULL COMMENT '关联的章节ID',
  `title` VARCHAR(255) NOT NULL COMMENT '练习标题',
  `description` TEXT NULL COMMENT '练习描述',
  `exercise_type` ENUM('choice', 'fill_blank', 'code', 'essay') NOT NULL COMMENT '练习类型',
  `content` JSON NULL COMMENT '练习内容（JSON格式，根据类型存储不同结构）',
  `answer` JSON NULL COMMENT '参考答案',
  `difficulty` ENUM('easy', 'medium', 'hard') DEFAULT 'medium' COMMENT '难度级别',
  `sort_order` INT DEFAULT 0 COMMENT '在章节中的排序',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `idx_exercises_chapter` (`course_id`, `chapter_id`),
  INDEX `idx_exercises_type` (`exercise_type`),
  INDEX `idx_exercises_difficulty` (`difficulty`),
  CONSTRAINT `fk_exercises_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = '章节练习表，为特定章节创建练习题';

-- -----------------------------------------------------
-- 功能扩展表: 练习提交记录 (exercise_submissions)
-- -----------------------------------------------------
CREATE TABLE `exercise_submissions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '提交记录唯一ID',
  `exercise_id` INT NOT NULL COMMENT '外键, 关联到chapter_exercises表的练习ID',
  `user_id` INT NOT NULL COMMENT '外键, 关联到users表的学生ID',
  `submitted_answer` JSON NOT NULL COMMENT '学生提交的答案 (JSON格式)',
  `is_correct` TINYINT(1) NULL COMMENT '答案是否正确 (1: 正确, 0: 错误, NULL: 待批改)',
  `score` DECIMAL(5, 2) NULL COMMENT '得分',
  `feedback` TEXT NULL COMMENT '教师或系统的反馈',
  `submission_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_submission_exercise_id` (`exercise_id`),
  INDEX `idx_submission_user_id` (`user_id`),
  UNIQUE KEY `uk_submission_user_exercise` (`user_id`, `exercise_id`) COMMENT '确保同一用户对同一练习只能提交一次 (如果业务需要可多次，则移除此唯一约束)',
  CONSTRAINT `fk_submission_exercise_id`
    FOREIGN KEY (`exercise_id`)
    REFERENCES `chapter_exercises` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_submission_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '记录学生对章节练习的提交答案和批改情况';

-- -----------------------------------------------------
-- 功能扩展表: 学习任务 (learning_tasks)
-- -----------------------------------------------------
CREATE TABLE `learning_tasks` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '任务唯一ID',
  `class_id` INT NOT NULL COMMENT '外键, 关联到classes表的班级ID',
  `creator_id` INT NOT NULL COMMENT '外键, 关联到users表的创建者（教师）ID',
  `title` VARCHAR(255) NOT NULL COMMENT '任务标题',
  `description` TEXT NULL COMMENT '任务详细描述',
  `due_date` TIMESTAMP NOT NULL COMMENT '任务截止日期',
  `status` ENUM('draft', 'published', 'closed') NOT NULL DEFAULT 'draft' COMMENT '任务状态 (draft: 草稿, published: 已发布, closed: 已关闭)',
  `related_chapter_id` INT NULL COMMENT '关联的章节ID, 外键关联到chapters表',
  `related_resource_id` INT NULL COMMENT '关联的资源ID, 外键关联到resources表',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_task_class_id` (`class_id`),
  INDEX `idx_task_creator_id` (`creator_id`),
  INDEX `idx_task_related_chapter_id` (`related_chapter_id`),
  INDEX `idx_task_related_resource_id` (`related_resource_id`),
  CONSTRAINT `fk_task_class_id`
    FOREIGN KEY (`class_id`)
    REFERENCES `classes` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_task_creator_id`
    FOREIGN KEY (`creator_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_task_related_chapter_id`
    FOREIGN KEY (`related_chapter_id`)
    REFERENCES `chapters` (`id`)
    ON DELETE SET NULL
)
ENGINE = InnoDB
COMMENT = '教师发布的学习任务，可关联章节、资源等，有截止日期';

-- -----------------------------------------------------
-- 功能扩展表: 学习任务提交记录 (task_submissions)
-- -----------------------------------------------------
CREATE TABLE `task_submissions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '提交记录唯一ID',
  `task_id` INT NOT NULL COMMENT '外键, 关联到learning_tasks表的任务ID',
  `student_id` INT NOT NULL COMMENT '外键, 关联到users表的学生ID',
  `status` ENUM('pending', 'completed', 'overdue') NOT NULL DEFAULT 'pending' COMMENT '提交状态 (pending: 待完成, completed: 已完成, overdue: 已逾期)',
  `completed_at` TIMESTAMP NULL COMMENT '学生标记完成的时间',
  `content` TEXT NULL COMMENT '学生提交的简短内容或笔记',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_submiss
-- ----ion_student_task` (`student_id`, `task_id`),
  INDEX `idx_submission_task_id` (`task_id`),
  CONSTRAINT `fk_submission_task_id`
    FOREIGN KEY (`task_id`)
    REFERENCES `learning_tasks` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_submission_student_id`
    FOREIGN KEY (`student_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '记录学生对学习任务的完成情况';
-------------------------------------------------
-- 功能扩展表: 学习进度 (learning_progress)
-- -----------------------------------------------------
CREATE TABLE `learning_progress` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '进度记录唯一ID',
  `user_id` INT NOT NULL COMMENT '学习者用户ID',
  `course_id` INT NOT NULL COMMENT '课程ID',
  `chapter_key` VARCHAR(100) NOT NULL COMMENT '章节标识键',
  `status` ENUM('not_started', 'in_progress', 'completed') DEFAULT 'not_started' COMMENT '学习状态',
  `progress_percentage` DECIMAL(5,2) DEFAULT 0.00 COMMENT '学习进度百分比',
  `reading_time_seconds` INT DEFAULT 0 COMMENT '阅读时长（秒）',
  `last_read_position` INT DEFAULT 0 COMMENT '最后阅读位置（字符偏移量）',
  `first_visit_time` TIMESTAMP NULL COMMENT '首次访问时间',
  `last_visit_time` TIMESTAMP NULL COMMENT '最后访问时间',
  `completion_time` TIMESTAMP NULL COMMENT '完成时间',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_progress_user_chapter` (`user_id`, `course_id`, `chapter_key`),
  INDEX `idx_progress_course` (`chapter_key`),
  INDEX `idx_progress_status` (`status`),
  CONSTRAINT `fk_progress_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_progress_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = '学习进度跟踪表，记录用户对每个章节的学习情况';

-- -----------------------------------------------------
-- 扩展功能表: 章节评论 (chapter_comments)
-- -----------------------------------------------------
CREATE TABLE `chapter_comments` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `course_id` INT NOT NULL,
  `chapter_key` VARCHAR(100) NOT NULL COMMENT '章节标识',
  `user_id` INT NOT NULL,
  `content` TEXT NOT NULL COMMENT '评论内容',
  `parent_id` INT NULL COMMENT '父评论ID',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_chapter` (`course_id`, `chapter_key`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_parent` (`parent_id`),
  CONSTRAINT `fk_comments_course`
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = '章节评论';


-- -----------------------------------------------------
-- AI与系统表: 知识库文档 (knowledge_documents)
-- -----------------------------------------------------
CREATE TABLE `knowledge_documents` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '文档唯一ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(255) NOT NULL COMMENT '文件在服务器上的存储路径',
  `status` ENUM('pending', 'processing', 'processed', 'failed') NULL DEFAULT 'pending' COMMENT '文档处理状态: 待处理, 处理中, 已处理, 处理失败',
  `chunk_count` INT NULL COMMENT '向量化后生成的文本块数量',
  `uploaded_by` INT NOT NULL COMMENT '外键, 关联到users表的上传者ID',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '文档上传时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_kd_uploaded_by_idx` (`uploaded_by` ASC),
  CONSTRAINT `fk_kd_uploaded_by`
    FOREIGN KEY (`uploaded_by`)
    REFERENCES `users` (`id`)
    ON DELETE NO ACTION
)
ENGINE = InnoDB
COMMENT = '用于管理AI知识库的源文档';


-- -----------------------------------------------------
-- 辅助表: 文件同步日志 (sync_logs)
-- -----------------------------------------------------
CREATE TABLE `sync_logs` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `course_id` INT NOT NULL,
  `operation` ENUM('parse', 'update', 'delete') NOT NULL,
  `status` ENUM('success', 'failed') NOT NULL,
  `message` TEXT NULL COMMENT '操作信息或错误信息',
  `duration_ms` INT DEFAULT 0 COMMENT '处理耗时',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_course` (`course_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created` (`create_time`),
  CONSTRAINT `fk_logs_course`
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = '文件同步操作日志';


-- -----------------------------------------------------
-- AI与系统表: 系统日志 (system_logs)
-- -----------------------------------------------------
CREATE TABLE `system_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
  `level` ENUM('info', 'warning', 'error', 'debug') NOT NULL COMMENT '日志级别',
  `message` TEXT NOT NULL COMMENT '日志详细信息',
  `context` JSON NULL COMMENT '相关的上下文信息 (如请求ID, 用户ID等)',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志记录时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB
COMMENT = '记录系统运行状态、错误、警告等信息';


-- -----------------------------------------------------
-- AI与系统表: AI助教聊天记录 (chat_messages)
-- -----------------------------------------------------
CREATE TABLE `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息唯一ID',
  `session_id` VARCHAR(100) NOT NULL COMMENT '用于标识一次完整的对话会话',
  `user_id` INT NOT NULL COMMENT '外键, 关联到users表的用户ID',
  `sender` ENUM('user', 'ai') NOT NULL COMMENT '消息发送方 (用户或AI)',
  `content` TEXT NOT NULL COMMENT '消息的具体内容',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_chat_user_id_idx` (`user_id` ASC),
  INDEX `session_id_idx` (`session_id` ASC),
  CONSTRAINT `fk_chat_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '存储学生与AI学习助手的聊天记录';

-- =====================================================
-- 实用视图
-- =====================================================

-- 课程章节树形视图
CREATE VIEW `v_chapter_tree` AS
SELECT 
    c.id,
    c.course_id,
    c.chapter_key,
    c.parent_id,
    c.level,
    c.title,
    c.sort_order,
    course.title as course_title
FROM chapters c
JOIN courses course ON c.course_id = course.id
WHERE course.deleted = 0
ORDER BY c.course_id, c.level, c.sort_order;

-- 学习进度统计视图
CREATE VIEW `v_learning_stats` AS
SELECT 
    lp.user_id,
    lp.course_id,
    COUNT(*) as total_chapters,
    SUM(CASE WHEN lp.status = 'completed' THEN 1 ELSE 0 END) as completed_count,
    ROUND(AVG(lp.progress_percentage), 2) as avg_progress,
    SUM(lp.reading_time_seconds) as total_read_time,
    MAX(lp.last_visit_time) as last_read_at
FROM learning_progress lp
JOIN courses c ON lp.course_id = c.id
WHERE c.deleted = 0
GROUP BY lp.user_id, lp.course_id; 