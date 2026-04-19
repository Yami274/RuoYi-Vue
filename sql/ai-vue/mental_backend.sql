-- 心理助手后端服务建表脚本
-- 执行库：ry-vue

DROP TABLE IF EXISTS mh_chat_session;
CREATE TABLE mh_chat_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID(sys_user.user_id)',
  session_title VARCHAR(200) NOT NULL COMMENT '会话标题',
  last_message VARCHAR(1000) DEFAULT NULL COMMENT '最后一条消息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (id),
  KEY idx_mh_chat_session_user (user_id),
  KEY idx_mh_chat_session_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理咨询会话';

DROP TABLE IF EXISTS mh_chat_message;
CREATE TABLE mh_chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role VARCHAR(20) NOT NULL COMMENT '消息角色(user/assistant)',
  content TEXT NOT NULL COMMENT '消息内容',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (id),
  KEY idx_mh_chat_message_session (session_id),
  KEY idx_mh_chat_message_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理咨询消息';

DROP TABLE IF EXISTS mh_chat_emotion;
CREATE TABLE mh_chat_emotion (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  dominant_emotion VARCHAR(64) NOT NULL COMMENT '主导情绪',
  mood_score INT NOT NULL COMMENT '情绪分值',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mh_chat_emotion_session_user (session_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话情绪分析';

DROP TABLE IF EXISTS mh_emotion_diary;
CREATE TABLE mh_emotion_diary (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  diary_date DATE NOT NULL COMMENT '记录日期',
  mood_score INT NOT NULL COMMENT '情绪评分(1-10)',
  dominant_emotion VARCHAR(64) NOT NULL COMMENT '主导情绪',
  emotion_triggers VARCHAR(500) NOT NULL COMMENT '触发因素',
  diary_content TEXT NOT NULL COMMENT '日记内容',
  sleep_quality INT NOT NULL COMMENT '睡眠质量(1-10)',
  stress_level INT NOT NULL COMMENT '压力水平(1-10)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mh_emotion_diary_user_date (user_id, diary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪日记';

DROP TABLE IF EXISTS mh_knowledge_article;
CREATE TABLE mh_knowledge_article (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  summary VARCHAR(1000) DEFAULT NULL COMMENT '摘要',
  content LONGTEXT NOT NULL COMMENT '内容',
  category_name VARCHAR(100) DEFAULT '心理健康' COMMENT '分类名称',
  cover VARCHAR(500) DEFAULT NULL COMMENT '封面',
  author VARCHAR(100) DEFAULT 'AI心理健康助手' COMMENT '作者',
  read_count BIGINT DEFAULT 0 COMMENT '阅读量',
  like_count BIGINT DEFAULT 0 COMMENT '点赞量',
  comment_count BIGINT DEFAULT 0 COMMENT '评论数',
  publish_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  status CHAR(1) DEFAULT '1' COMMENT '状态(0草稿 1发布 2下架)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (id),
  KEY idx_mh_knowledge_article_status (status),
  KEY idx_mh_knowledge_article_read_count (read_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文章';

DROP TABLE IF EXISTS mh_file_record;
CREATE TABLE mh_file_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
  business_field VARCHAR(64) NOT NULL COMMENT '业务字段',
  origin_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  file_name VARCHAR(255) DEFAULT NULL COMMENT '系统文件名',
  file_url VARCHAR(500) DEFAULT NULL COMMENT '文件访问地址',
  file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  uploader_id BIGINT DEFAULT NULL COMMENT '上传人ID',
  uploader_name VARCHAR(100) DEFAULT NULL COMMENT '上传人账号',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_mh_file_record_biz (business_type, business_id, business_field)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录';

INSERT INTO mh_knowledge_article(title, summary, content, category_name, cover, author, read_count, like_count, comment_count, status)
VALUES
('如何缓解焦虑', '识别触发因素并练习呼吸放松', '当你感到焦虑时，可以先暂停手头工作，尝试4-7-8呼吸法。', '情绪管理', 'https://file.itndedu.com/psychology_ai.png', 'AI心理健康助手', 12, 3, 1, '1'),
('提升睡眠质量的5个习惯', '规律作息与睡前仪式能显著改善入睡质量', '建议在固定时间上床，睡前避免高强度信息刺激。', '睡眠健康', 'https://file.itndedu.com/psychology_ai.png', 'AI心理健康助手', 8, 2, 0, '1');
