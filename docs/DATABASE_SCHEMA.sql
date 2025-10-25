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
