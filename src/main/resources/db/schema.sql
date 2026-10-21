-- ============================================================
-- 医疗 AI 问答系统 数据库初始化脚本（MySQL 8）
-- 执行：mysql -u root -p < db/schema.sql
-- 说明：健康档案、报告内容等敏感字段由应用层加密后写入，列类型为 TEXT/BLOB。
-- ============================================================

CREATE DATABASE IF NOT EXISTS medical_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE medical_ai;

-- 用户
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    nickname      VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    password      VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt 密文',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    register_type VARCHAR(20)  NOT NULL DEFAULT 'PASSWORD' COMMENT 'PASSWORD/SMS/WECHAT',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

-- 健康档案（敏感字段加密存储）
CREATE TABLE IF NOT EXISTS user_profile (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    user_id            BIGINT      NOT NULL,
    gender             VARCHAR(8)  DEFAULT NULL,
    birth_date         DATE        DEFAULT NULL,
    height             VARCHAR(16) DEFAULT NULL,
    weight             VARCHAR(16) DEFAULT NULL,
    allergy_history    TEXT        DEFAULT NULL COMMENT '过敏史(加密)',
    past_history       TEXT        DEFAULT NULL COMMENT '既往病史(加密)',
    medication_history TEXT        DEFAULT NULL COMMENT '长期用药(加密)',
    family_history     TEXT        DEFAULT NULL COMMENT '家族史(加密)',
    create_time        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '健康档案';

-- 对话会话
CREATE TABLE IF NOT EXISTS chat_session (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(128) NOT NULL DEFAULT '新会话',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对话会话';

-- 对话消息
CREATE TABLE IF NOT EXISTS chat_message (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    session_id  BIGINT      NOT NULL,
    role        VARCHAR(16) NOT NULL COMMENT 'user/assistant',
    content     TEXT        NOT NULL,
    refs        JSON        DEFAULT NULL COMMENT '参考来源数组',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_session (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '对话消息';

-- 分诊记录
CREATE TABLE IF NOT EXISTS triage_record (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      DEFAULT NULL,
    symptom_input  TEXT        NOT NULL COMMENT '症状输入',
    departments    JSON        DEFAULT NULL COMMENT '推荐科室',
    urgency_level  VARCHAR(16) DEFAULT NULL COMMENT '立即/尽快/先观察',
    red_flag       TINYINT     NOT NULL DEFAULT 0 COMMENT '红旗 1是 0否',
    result_summary TEXT        DEFAULT NULL,
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分诊记录';

-- 用药咨询记录
CREATE TABLE IF NOT EXISTS med_consult_record (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   DEFAULT NULL,
    query       TEXT     NOT NULL,
    result      TEXT     DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用药咨询记录';

-- 报告记录（OCR/解读结果加密存储）
CREATE TABLE IF NOT EXISTS report_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    file_url       VARCHAR(255) NOT NULL,
    ocr_result     TEXT         DEFAULT NULL COMMENT 'OCR结果(加密)',
    interpretation TEXT         DEFAULT NULL COMMENT '解读结果(加密)',
    abnormal_flags JSON         DEFAULT NULL COMMENT '异常指标标注',
    critical_flag  TINYINT      NOT NULL DEFAULT 0 COMMENT '危急值 1是 0否',
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '报告记录';

-- 反馈
CREATE TABLE IF NOT EXISTS feedback (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      DEFAULT NULL,
    message_id  BIGINT      DEFAULT NULL,
    type        VARCHAR(16) NOT NULL COMMENT 'HELPFUL/NOT_HELPFUL/CORRECTION',
    content     TEXT        DEFAULT NULL,
    status      VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSED',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '反馈';

-- 知识库条目
CREATE TABLE IF NOT EXISTS knowledge_entry (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255) NOT NULL,
    content     MEDIUMTEXT   NOT NULL,
    source      VARCHAR(255) DEFAULT NULL,
    category    VARCHAR(64)  DEFAULT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PUBLISHED/OFFLINE',
    version     INT          NOT NULL DEFAULT 1,
    create_by   BIGINT       DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '知识库条目';

-- 用户私有知识库文档（上传文件 → 向量化入库）
CREATE TABLE IF NOT EXISTS user_document (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_type   VARCHAR(16)  NOT NULL COMMENT 'txt/md/pdf/docx',
    status      VARCHAR(16)  NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/READY/FAILED',
    chunk_count INT          NOT NULL DEFAULT 0,
    error_msg   VARCHAR(500) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户私有知识库文档';

-- 管理员
CREATE TABLE IF NOT EXISTS admin_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    role        VARCHAR(32)  NOT NULL DEFAULT 'OPERATOR' COMMENT 'SUPER_ADMIN/OPERATOR/AUDITOR',
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员';

-- 审核留痕
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    operator_id BIGINT      DEFAULT NULL,
    target_type VARCHAR(32) DEFAULT NULL,
    target_id   BIGINT      DEFAULT NULL,
    action      VARCHAR(64) DEFAULT NULL,
    detail      TEXT        DEFAULT NULL,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operator (operator_id),
    KEY idx_target (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审核留痕';

-- 药品药典（结构化，供用药咨询查询，避免模型编造用法）
CREATE TABLE IF NOT EXISTS drug (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    generic_name       VARCHAR(64)  NOT NULL COMMENT '通用名',
    brand_names        VARCHAR(255) DEFAULT NULL COMMENT '商品名，逗号分隔',
    indications        VARCHAR(500) DEFAULT NULL COMMENT '适应症',
    dosage             VARCHAR(500) DEFAULT NULL COMMENT '用法用量',
    contraindications  VARCHAR(500) DEFAULT NULL COMMENT '禁忌',
    adverse_reactions  VARCHAR(500) DEFAULT NULL COMMENT '不良反应',
    storage            VARCHAR(255) DEFAULT NULL COMMENT '贮藏',
    special_population VARCHAR(500) DEFAULT NULL COMMENT '特殊人群警示',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted            TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_generic (generic_name),
    KEY idx_brand (brand_names)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '药品药典';

-- 药物相互作用（两两组合）
CREATE TABLE IF NOT EXISTS drug_interaction (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    drug_a      VARCHAR(64)  NOT NULL,
    drug_b      VARCHAR(64)  NOT NULL,
    risk_level  VARCHAR(8)   NOT NULL COMMENT '高/中/低',
    description VARCHAR(500) NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pair (drug_a, drug_b)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '药物相互作用';

-- 检验指标值对照表（报告解读规则引擎，结构化数据，不依赖模型）
CREATE TABLE IF NOT EXISTS lab_reference (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    item_name     VARCHAR(64)   NOT NULL COMMENT '指标名',
    alias         VARCHAR(255)  DEFAULT NULL COMMENT '别名，逗号分隔，用于 OCR 文本匹配',
    unit          VARCHAR(32)   DEFAULT NULL COMMENT '单位',
    ref_low       DECIMAL(10,2) DEFAULT NULL COMMENT '参考下限',
    ref_high      DECIMAL(10,2) DEFAULT NULL COMMENT '参考上限',
    critical_low  DECIMAL(10,2) DEFAULT NULL COMMENT '危急值下限(可空，单边危急)',
    critical_high DECIMAL(10,2) DEFAULT NULL COMMENT '危急值上限(可空，单边危急)',
    meaning       VARCHAR(500)  DEFAULT NULL COMMENT '指标含义',
    high_reason   VARCHAR(500)  DEFAULT NULL COMMENT '偏高常见原因',
    low_reason    VARCHAR(500)  DEFAULT NULL COMMENT '偏低常见原因',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item (item_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '检验指标值对照表';
