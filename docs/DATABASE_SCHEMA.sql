-- AIgo Database Schema
-- 数据库创建语句

-- 创建数据库
CREATE DATABASE IF NOT EXISTS aigo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE aigo;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户唯一标识',
    username VARCHAR(20) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    coin_balance INTEGER DEFAULT 100 NOT NULL COMMENT '金币余额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 作品表
CREATE TABLE IF NOT EXISTS works (
    id VARCHAR(36) PRIMARY KEY COMMENT '作品唯一标识',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    title VARCHAR(100) NOT NULL COMMENT '作品标题',
    description TEXT COMMENT '作品描述',
    is_public BOOLEAN DEFAULT FALSE NOT NULL COMMENT '是否公开',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    likes_count INTEGER DEFAULT 0 NOT NULL COMMENT '点赞数',
    views_count INTEGER DEFAULT 0 NOT NULL COMMENT '浏览数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_public (is_public),
    INDEX idx_created_at (created_at),
    INDEX idx_likes_count (likes_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品表';

-- 集数表
CREATE TABLE IF NOT EXISTS episodes (
    id VARCHAR(36) PRIMARY KEY COMMENT '集数唯一标识',
    work_id VARCHAR(36) NOT NULL COMMENT '作品ID',
    episode_number INTEGER NOT NULL COMMENT '集数编号',
    title VARCHAR(100) NOT NULL COMMENT '集数标题',
    novel_text TEXT NOT NULL COMMENT '小说文本',
    scenes JSON COMMENT '场景数据',
    is_free BOOLEAN DEFAULT TRUE NOT NULL COMMENT '是否免费',
    coin_price INTEGER DEFAULT 0 NOT NULL COMMENT '金币价格',
    is_published BOOLEAN DEFAULT FALSE NOT NULL COMMENT '是否已发布',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '集数状态 (PENDING/PROCESSING/COMPLETED/FAILED)',
    characters JSON COMMENT '角色信息 (JSON数组)',
    plot_summary TEXT COMMENT '剧情摘要',
    genre VARCHAR(100) COMMENT '类型/题材',
    mood VARCHAR(100) COMMENT '氛围/情绪',
    error_message TEXT COMMENT '错误信息（生成失败时）',
    style VARCHAR(100) COMMENT '风格',
    target_audience VARCHAR(100) COMMENT '目标受众',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
    UNIQUE KEY unique_work_episode (work_id, episode_number),
    INDEX idx_work_id (work_id),
    INDEX idx_is_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='集数表';

-- 购买记录表
CREATE TABLE IF NOT EXISTS purchases (
    id VARCHAR(36) PRIMARY KEY COMMENT '购买记录唯一标识',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    episode_id VARCHAR(36) NOT NULL COMMENT '集数ID',
    coin_cost INTEGER NOT NULL COMMENT '消耗金币数',
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '购买时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_episode (user_id, episode_id),
    INDEX idx_user_id (user_id),
    INDEX idx_episode_id (episode_id),
    INDEX idx_purchased_at (purchased_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购买记录表';

-- 点赞表
CREATE TABLE IF NOT EXISTS likes (
    id VARCHAR(36) PRIMARY KEY COMMENT '点赞记录唯一标识',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    work_id VARCHAR(36) NOT NULL COMMENT '作品ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '点赞时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_work (user_id, work_id),
    INDEX idx_user_id (user_id),
    INDEX idx_work_id (work_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 角色表
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description TEXT COMMENT '角色描述',
    appearance TEXT COMMENT '外观描述',
    personality TEXT COMMENT '性格描述',
    work_id VARCHAR(36) COMMENT '关联作品ID',
    is_protagonist BOOLEAN DEFAULT FALSE COMMENT '是否主角',
    image_url VARCHAR(500) COMMENT '角色图片URL',
    gender VARCHAR(50) COMMENT '性别',
    body_type TEXT COMMENT '体型描述',
    facial_features TEXT COMMENT '面部特征',
    hair_type VARCHAR(100) COMMENT '发型',
    hair_color VARCHAR(100) COMMENT '发色',
    face_shape VARCHAR(100) COMMENT '脸型',
    eye_type VARCHAR(100) COMMENT '眼睛类型',
    eye_color VARCHAR(100) COMMENT '眼睛颜色',
    nose_type VARCHAR(100) COMMENT '鼻子类型',
    mouth_type VARCHAR(100) COMMENT '嘴巴类型',
    skin_tone VARCHAR(100) COMMENT '肤色',
    height VARCHAR(100) COMMENT '身高',
    build VARCHAR(100) COMMENT '体格',
    clothing_style TEXT COMMENT '服装风格',
    distinguishing_features TEXT COMMENT '显著特征',
    is_placeholder_name BOOLEAN DEFAULT FALSE COMMENT '是否为占位符名称',
    nicknames JSON COMMENT '昵称列表 (JSON数组)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    INDEX idx_work_id (work_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '场景唯一标识',
    scene_number INTEGER NOT NULL COMMENT '场景编号',
    character VARCHAR(100) COMMENT '角色名称',
    dialogue TEXT COMMENT '对话内容',
    visual_description TEXT COMMENT '视觉描述',
    atmosphere TEXT COMMENT '氛围',
    action TEXT COMMENT '动作',
    content TEXT COMMENT '场景内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    INDEX idx_scene_number (scene_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景表';

-- 索引说明：
-- 1. users表：
--    - idx_username: 用于登录时根据用户名查询
--    - idx_email: 用于注册时检查邮箱是否存在，以及支持邮箱登录
--
-- 2. works表：
--    - idx_user_id: 用于查询用户的所有作品（我的作品页面）
--    - idx_is_public: 用于作品广场只显示公开作品
--    - idx_created_at: 用于作品广场按时间排序
--    - idx_likes_count: 用于作品广场按点赞数排序
--
-- 3. episodes表：
--    - idx_work_id: 用于查询作品的所有集数
--    - idx_is_published: 用于查询已发布的集数数量
--    - unique_work_episode: 保证同一作品的集数编号唯一
--
-- 4. purchases表：
--    - idx_user_id: 用于查询用户的购买记录
--    - idx_episode_id: 用于查询集数的购买用户
--    - idx_purchased_at: 用于按购买时间排序
--    - unique_user_episode: 防止重复购买
--
-- 5. likes表：
--    - idx_user_id: 用于查询用户点赞的作品
--    - idx_work_id: 用于查询作品被哪些用户点赞
--    - unique_user_work: 防止重复点赞
--
-- 6. characters表：
--    - idx_work_id: 用于查询作品的所有角色
--    - idx_name: 用于根据角色名称查询
--
-- 7. scenes表：
--    - idx_scene_number: 用于按场景编号查询

-- 业务规则说明：
-- 1. 作品广场：只显示 is_public = true 的作品，可以看到所有公开作品
-- 2. 我的作品：通过 user_id 过滤，只显示登录用户创建的作品
-- 3. 作品的集数：通过 work_id 关联，查询时可以一起返回
-- 4. 权限控制：
--    - 只有作品创建者可以修改/删除作品
--    - 只有作品创建者可以创建/修改/发布集数
--    - 未发布的集数只有创建者可以访问
--    - 付费集数需要购买才能访问（创建者除外）
-- 5. 金币系统：
--    - 新用户注册获得100金币
--    - 购买付费集数扣除相应金币
--    - 金币不足无法购买
-- 6. 角色管理：
--    - 角色可以关联到作品（work_id）
--    - 支持详细的外观、性格、体征等属性
--    - 支持多个昵称（JSON 数组）
-- 7. 集数状态管理：
--    - PENDING: 等待处理
--    - PROCESSING: 处理中
--    - COMPLETED: 已完成
--    - FAILED: 失败（需查看 error_message）
-- 8. 场景数据：
--    - episodes 表中的 scenes 字段存储场景的 JSON 数据
--    - scenes 表提供独立的场景数据存储（可选）
