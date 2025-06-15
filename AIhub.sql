-- -----------------------------------------------------
-- AI教学实训智能体 - 数据库表结构
-- -----------------------------------------------------
-- 版本: 1.1
-- 更新: 增加了班级口令、考核类型、项目提交与评分详情字段
-- -----------------------------------------------------

-- 删除已存在的表，以便于重新创建
DROP TABLE IF EXISTS `answers`;
DROP TABLE IF EXISTS `submissions`;
DROP TABLE IF EXISTS `assessment_questions`;
DROP TABLE IF EXISTS `questions`;
DROP TABLE IF EXISTS `assessments`;
DROP TABLE IF EXISTS `resources`;
DROP TABLE IF EXISTS `class_members`;
DROP TABLE IF EXISTS `classes`;
DROP TABLE IF EXISTS `enrollments`;
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


-- -----------------------------------------------------
-- 核心表: 课程 (courses)
-- -----------------------------------------------------
CREATE TABLE `courses` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '课程唯一ID',
  `title` VARCHAR(255) NOT NULL COMMENT '课程名称',
  `description` TEXT NULL COMMENT '课程详细描述',
  `teacher_id` INT NOT NULL COMMENT '外键, 关联到users表的教师ID',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '课程创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_courses_teacher_id_idx` (`teacher_id` ASC),
  CONSTRAINT `fk_courses_teacher_id`
    FOREIGN KEY (`teacher_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '存储教师创建的课程信息';


-- -----------------------------------------------------
-- 核心表: 选课记录 (enrollments)
-- -----------------------------------------------------
CREATE TABLE `enrollments` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '选课记录唯一ID',
  `student_id` INT NOT NULL COMMENT '外键, 关联到users表的学生ID',
  `course_id` INT NOT NULL COMMENT '外键, 关联到courses表的课程ID',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_enrollments_student_id_idx` (`student_id` ASC),
  INDEX `fk_enrollments_course_id_idx` (`course_id` ASC),
  UNIQUE INDEX `student_course_unique` (`student_id` ASC, `course_id` ASC) COMMENT '确保学生和课程的组合是唯一的',
  CONSTRAINT `fk_enrollments_student_id`
    FOREIGN KEY (`student_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_enrollments_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '记录学生选课情况，是学生和课程的多对多关系链接表';


-- -----------------------------------------------------
-- 核心表: 班级 (classes)
-- -----------------------------------------------------
CREATE TABLE `classes` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '班级唯一ID',
  `name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `teacher_id` INT NOT NULL COMMENT '外键, 关联到users表的教师ID',
  `course_id` INT NULL COMMENT '外键, 关联到courses表的课程ID (可选)',
  `class_code` VARCHAR(8) NOT NULL COMMENT '班级口令/邀请码, 用于学生加入班级',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '班级创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `class_code_UNIQUE` (`class_code` ASC),
  INDEX `fk_classes_teacher_id_idx` (`teacher_id` ASC),
  INDEX `fk_classes_course_id_idx` (`course_id` ASC),
  CONSTRAINT `fk_classes_teacher_id`
    FOREIGN KEY (`teacher_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_classes_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE SET NULL
)
ENGINE = InnoDB
COMMENT = '教师可以创建班级来管理学生';


-- -----------------------------------------------------
-- 核心表: 班级成员 (class_members)
-- -----------------------------------------------------
CREATE TABLE `class_members` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '班级成员记录ID',
  `class_id` INT NOT NULL COMMENT '外键, 关联到classes表的班级ID',
  `student_id` INT NOT NULL COMMENT '外键, 关联到users表的学生ID',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_cm_class_id_idx` (`class_id` ASC),
  INDEX `fk_cm_student_id_idx` (`student_id` ASC),
  UNIQUE INDEX `class_student_unique` (`class_id` ASC, `student_id` ASC) COMMENT '确保学生在同一班级中不重复',
  CONSTRAINT `fk_cm_class_id`
    FOREIGN KEY (`class_id`)
    REFERENCES `classes` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_cm_student_id`
    FOREIGN KEY (`student_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '班级和学生的多对多关系链接表';


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
-- 教学表: 考核 (assessments)
-- -----------------------------------------------------
CREATE TABLE `assessments` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '考核唯一ID',
  `title` VARCHAR(255) NOT NULL COMMENT '考核标题 (如: 期中测试)',
  `course_id` INT NOT NULL COMMENT '外键, 关联到courses表的所属课程ID',
  `creator_id` INT NOT NULL COMMENT '外键, 关联到users表的创建者(教师)ID',
  `type` ENUM('exam', 'assignment', 'project') NOT NULL COMMENT '考核类型: 考试, 作业, 项目',
  `due_date` TIMESTAMP NULL COMMENT '考核截止日期',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '考核创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_assessments_course_id_idx` (`course_id` ASC),
  INDEX `fk_assessments_creator_id_idx` (`creator_id` ASC),
  CONSTRAINT `fk_assessments_course_id`
    FOREIGN KEY (`course_id`)
    REFERENCES `courses` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_assessments_creator_id`
    FOREIGN KEY (`creator_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '定义一次考核（如测验、作业、实训项目）';


-- -----------------------------------------------------
-- 教学表: 题库 (questions)
-- -----------------------------------------------------
CREATE TABLE `questions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '题目唯一ID',
  `content` TEXT NOT NULL COMMENT '题目内容 (题干)',
  `type` ENUM('single_choice', 'multi_choice', 'true_false', 'fill_blank', 'short_answer') NOT NULL COMMENT '题型',
  `options` JSON NULL COMMENT '存储选择题选项, 格式为 {"A": "...", "B": "..."}',
  `correct_answer` TEXT NOT NULL COMMENT '题目的正确答案',
  `explanation` TEXT NULL COMMENT '答案的详细解析',
  `creator_id` INT NOT NULL COMMENT '外键, 关联到users表的题目创建者ID',
  `knowledge_point` VARCHAR(255) NULL COMMENT '题目关联的知识点',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '题目创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_questions_creator_id_idx` (`creator_id` ASC),
  CONSTRAINT `fk_questions_creator_id`
    FOREIGN KEY (`creator_id`)
    REFERENCES `users` (`id`)
    ON DELETE NO ACTION
)
ENGINE = InnoDB
COMMENT = '存储所有考核的题目，形成题库';


-- -----------------------------------------------------
-- 教学表: 考核-题目关联 (assessment_questions)
-- -----------------------------------------------------
CREATE TABLE `assessment_questions` (
  `assessment_id` INT NOT NULL COMMENT '外键, 关联到assessments表的考核ID',
  `question_id` INT NOT NULL COMMENT '外键, 关联到questions表的题目ID',
  `points` INT NULL DEFAULT 10 COMMENT '该题目在本次考核中的分值',
  PRIMARY KEY (`assessment_id`, `question_id`),
  INDEX `fk_aq_question_id_idx` (`question_id` ASC),
  CONSTRAINT `fk_aq_assessment_id`
    FOREIGN KEY (`assessment_id`)
    REFERENCES `assessments` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_aq_question_id`
    FOREIGN KEY (`question_id`)
    REFERENCES `questions` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '考核与题目的多对多关系链接表, 主要用于考试和作业';


-- -----------------------------------------------------
-- 教学表: 学生提交记录 (submissions)
-- -----------------------------------------------------
CREATE TABLE `submissions` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '提交记录的唯一ID',
  `assessment_id` INT NOT NULL COMMENT '外键, 关联到assessments表的考核ID',
  `student_id` INT NOT NULL COMMENT '外键, 关联到users表的学生ID',
  `status` ENUM('in_progress', 'submitted', 'graded') NULL DEFAULT 'in_progress' COMMENT '提交状态: 进行中, 已提交, 已评分',
  `score` DECIMAL(5,2) NULL COMMENT '本次提交的总得分',
  `submission_content` TEXT NULL COMMENT '用于存储提交内容, 如项目代码仓库链接、文件路径或文本',
  `feedback` TEXT NULL COMMENT '教师对本次提交的总体评语',
  `grading_details` JSON NULL COMMENT '用于存储评分细则, 如 {"功能完整性": "28/30", "代码质量": "22/25"}',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '信息最后更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_submissions_assessment_id_idx` (`assessment_id` ASC),
  INDEX `fk_submissions_student_id_idx` (`student_id` ASC),
  UNIQUE INDEX `assessment_student_unique` (`assessment_id` ASC, `student_id` ASC) COMMENT '确保学生对同一考核只能提交一次',
  CONSTRAINT `fk_submissions_assessment_id`
    FOREIGN KEY (`assessment_id`)
    REFERENCES `assessments` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_submissions_student_id`
    FOREIGN KEY (`student_id`)
    REFERENCES `users` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '记录学生提交的每一次考核';


-- -----------------------------------------------------
-- 教学表: 学生答案 (answers)
-- -----------------------------------------------------
CREATE TABLE `answers` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '答案记录的唯一ID',
  `submission_id` INT NOT NULL COMMENT '外键, 关联到submissions表的提交ID',
  `question_id` INT NOT NULL COMMENT '外键, 关联到questions表的题目ID',
  `student_answer` TEXT NULL COMMENT '学生提交的答案内容',
  `is_correct` BOOLEAN NULL COMMENT '该答案是否正确 (用于客观题自动批改)',
  `create_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  INDEX `fk_answers_submission_id_idx` (`submission_id` ASC),
  INDEX `fk_answers_question_id_idx` (`question_id` ASC),
  CONSTRAINT `fk_answers_submission_id`
    FOREIGN KEY (`submission_id`)
    REFERENCES `submissions` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_answers_question_id`
    FOREIGN KEY (`question_id`)
    REFERENCES `questions` (`id`)
    ON DELETE CASCADE
)
ENGINE = InnoDB
COMMENT = '存储学生对具体题目的答案(主要用于考试/作业)';


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