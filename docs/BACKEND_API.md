# AIgo Backend API 文档

## 概述

本文档定义了 AIgo 智能动漫生成系统的生产环境后端 API 规范。

**基本信息:**
- **Base URL**: `http://localhost:8080` (开发/生产环境)
- **协议**: HTTP
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT (JSON Web Token)

## 通用规范

### 请求头

所有 API 请求应包含以下请求头：

```http
Content-Type: application/json
Accept: application/json
```

需要认证的接口还需包含：

```http
Authorization: Bearer <JWT_TOKEN>
```

### 响应格式

#### 成功响应

```json
{
  "success": true,
  "data": { ... }
}
```

#### 错误响应

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述信息"
  }
}
```

### 错误代码

| 错误代码 | HTTP 状态码 | 说明 |
|---------|-----------|------|
| `UNAUTHORIZED` | 401 | 未授权，Token 无效或过期 |
| `FORBIDDEN` | 403 | 无权限访问该资源 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `VALIDATION_ERROR` | 400 | 请求参数验证失败 |
| `INSUFFICIENT_COINS` | 400 | 金币余额不足 |
| `ALREADY_PURCHASED` | 400 | 已购买过该内容 |
| `ALREADY_PUBLISHED` | 400 | 内容已发布 |
| `ALREADY_LIKED` | 400 | 已点赞过 |
| `RATE_LIMIT_EXCEEDED` | 429 | 请求频率超限 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

---

## API 端点

## 1. 用户认证

### 1.1 用户注册

**端点**: `POST /api/auth/register`

**描述**: 创建新用户账户

**请求体**:
```json
{
  "username": "string (3-20字符，字母数字下划线)",
  "email": "string (有效的邮箱地址)",
  "password": "string (8-50字符)"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "string",
      "username": "string",
      "email": "string",
      "coinBalance": 100,
      "createdAt": "2024-01-01T00:00:00.000Z"
    },
    "token": "string (JWT Token)"
  }
}
```

**说明**:
- 新注册用户获得 100 金币奖励
- Token 有效期 7 天

---

### 1.2 用户登录

**端点**: `POST /api/auth/login`

**描述**: 用户登录获取访问令牌

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "string",
      "username": "string",
      "email": "string",
      "coinBalance": 500
    },
    "token": "string (JWT Token)"
  }
}
```

---

### 1.3 获取用户金币余额

**端点**: `GET /api/user/balance`

**描述**: 获取当前用户的金币余额

**认证**: 必需

**响应**:
```json
{
  "success": true,
  "data": {
    "balance": 500
  }
}
```

---

### 1.4 金币充值

**端点**: `POST /api/user/recharge`

**描述**: 为当前用户充值金币

**认证**: 必需

**请求体**:
```json
{
  "amount": 100
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "newBalance": 600,
    "rechargeAmount": 100
  }
}
```

**说明**:
- `amount` 必须为正整数
- 充值成功后返回新的金币余额

---

## 2. 作品管理

### 2.1 创建作品

**端点**: `POST /api/works`

**描述**: 创建新作品

**认证**: 必需

**请求体**:
```json
{
  "title": "string (1-100字符)",
  "description": "string (可选，最多500字符)",
  "isPublic": false,
  "coverImage": "string (可选，图片URL)"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "userId": "string",
    "title": "string",
    "description": "string",
    "isPublic": false,
    "coverImage": "string",
    "likesCount": 0,
    "viewsCount": 0,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### 2.2 获取作品详情

**端点**: `GET /api/works/:id`

**描述**: 获取单个作品的详细信息

**参数**:
- `id` (path): 作品ID

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "userId": "string",
    "title": "string",
    "description": "string",
    "isPublic": true,
    "coverImage": "string",
    "likesCount": 156,
    "viewsCount": 1240,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### 2.3 更新作品

**端点**: `PUT /api/works/:id`

**描述**: 更新作品信息

**认证**: 必需

**参数**:
- `id` (path): 作品ID

**请求体** (所有字段可选):
```json
{
  "title": "string",
  "description": "string",
  "isPublic": true,
  "coverImage": "string"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "userId": "string",
    "title": "string (更新后)",
    "description": "string",
    "isPublic": true,
    "coverImage": "string",
    "likesCount": 156,
    "viewsCount": 1240,
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-02T00:00:00.000Z"
  }
}
```

**权限**: 仅作品创建者可更新

---

### 2.4 删除作品

**端点**: `DELETE /api/works/:id`

**描述**: 删除作品及其所有集数

**认证**: 必需

**参数**:
- `id` (path): 作品ID

**响应**:
```json
{
  "success": true,
  "data": {
    "message": "作品已删除"
  }
}
```

**权限**: 仅作品创建者可删除

---

## 3. 集数管理

### 3.1 创建集数

**端点**: `POST /api/works/:workId/episodes`

**描述**: 为作品创建新集数

**认证**: 必需

**参数**:
- `workId` (path): 作品ID

**请求体**:
```json
{
  "title": "string (1-100字符)",
  "novelText": "string (小说文本)",
  "scenes": [
    {
      "id": 1,
      "text": "string (场景文本)",
      "imageUrl": "string (可选)"
    }
  ],
  "isFree": true,
  "coinPrice": 0
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "episodeNumber": 1,
    "title": "string",
    "novelText": "string",
    "scenes": [...],
    "isFree": true,
    "coinPrice": 0,
    "isPublished": false,
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

**说明**:
- `episodeNumber` 自动递增
- 新建集数默认未发布 (`isPublished: false`)
- `coinPrice` 仅在 `isFree: false` 时有效

---

### 3.2 获取集数详情

**端点**: `GET /api/episodes/:id`

**描述**: 获取集数内容

**参数**:
- `id` (path): 集数ID

**响应 (免费或已购买)**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "episodeNumber": 1,
    "title": "string",
    "novelText": "string",
    "scenes": [
      {
        "id": 1,
        "text": "string",
        "imageUrl": "string"
      }
    ],
    "isFree": true,
    "coinPrice": 0,
    "isPublished": true,
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

**响应 (需要购买)**:
```json
{
  "success": false,
  "needsPurchase": true,
  "data": {
    "episodeId": "string",
    "title": "string",
    "coinPrice": 50
  }
}
```

**权限**:
- 未发布集数：仅创建者可访问
- 付费集数：需购买或为创建者

---

### 3.3 更新集数

**端点**: `PUT /api/episodes/:id`

**描述**: 更新集数内容

**认证**: 必需

**参数**:
- `id` (path): 集数ID

**请求体** (所有字段可选):
```json
{
  "title": "string",
  "novelText": "string",
  "scenes": [...],
  "isFree": true,
  "coinPrice": 0
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "episodeNumber": 1,
    "title": "string (更新后)",
    "novelText": "string",
    "scenes": [...],
    "isFree": true,
    "coinPrice": 0,
    "isPublished": false,
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

**限制**:
- 仅未发布 (`isPublished: false`) 的集数可编辑
- 仅创建者可编辑

---

### 3.4 发布集数

**端点**: `POST /api/episodes/:id/publish`

**描述**: 发布集数（发布后不可再编辑）

**认证**: 必需

**参数**:
- `id` (path): 集数ID

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "episodeNumber": 1,
    "title": "string",
    "isPublished": true,
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

**权限**: 仅创建者可发布

---

### 3.5 重试生成集数

**端点**: `POST /api/episodes/:id/retry`

**描述**: 重新生成失败或需要重新生成的集数内容

**认证**: 必需

**参数**:
- `id` (path): 集数ID

**响应**:
```json
{
  "success": true,
  "data": {
    "message": "集数重新生成任务已提交",
    "episodeId": "string"
  }
}
```

**权限**: 仅创建者可重试

**说明**:
- 用于重新生成集数的场景和图片
- 异步处理，返回任务提交成功消息

---

## 4. 角色管理

### 4.1 创建角色

**端点**: `POST /api/characters`

**描述**: 为作品创建角色

**认证**: 必需

**请求体**:
```json
{
  "workId": "string",
  "name": "string (角色名称)",
  "description": "string (角色描述)",
  "appearance": "string (外貌特征)",
  "personality": "string (性格特点)"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "name": "string",
    "description": "string",
    "appearance": "string",
    "personality": "string",
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### 4.2 根据 ID 获取角色

**端点**: `GET /api/characters/:id`

**描述**: 获取单个角色的详细信息

**参数**:
- `id` (path): 角色ID

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "name": "string",
    "description": "string",
    "appearance": "string",
    "personality": "string",
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### 4.3 根据名称获取角色

**端点**: `GET /api/characters/name/:name`

**描述**: 根据角色名称获取角色信息

**参数**:
- `name` (path): 角色名称

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "name": "string",
    "description": "string",
    "appearance": "string",
    "personality": "string",
    "createdAt": "2024-01-01T00:00:00.000Z"
  }
}
```

---

### 4.4 获取所有角色

**端点**: `GET /api/characters`

**描述**: 获取所有角色列表

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "workId": "string",
      "name": "string",
      "description": "string",
      "appearance": "string",
      "personality": "string",
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

---

### 4.5 搜索角色

**端点**: `GET /api/characters/search`

**描述**: 根据名称搜索角色

**查询参数**:
- `name` (必需): 角色名称关键词

**示例**: `GET /api/characters/search?name=主角`

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "workId": "string",
      "name": "string",
      "description": "string",
      "appearance": "string",
      "personality": "string",
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

---

### 4.6 获取作品的所有角色

**端点**: `GET /api/characters/work/:workId`

**描述**: 获取指定作品的所有角色

**参数**:
- `workId` (path): 作品ID

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "workId": "string",
      "name": "string",
      "description": "string",
      "appearance": "string",
      "personality": "string",
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

---

### 4.7 更新角色

**端点**: `PUT /api/characters/:id`

**描述**: 更新角色信息

**认证**: 必需

**参数**:
- `id` (path): 角色ID

**请求体** (所有字段可选):
```json
{
  "name": "string",
  "description": "string",
  "appearance": "string",
  "personality": "string"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "id": "string",
    "workId": "string",
    "name": "string",
    "description": "string",
    "appearance": "string",
    "personality": "string",
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-02T00:00:00.000Z"
  }
}
```

**权限**: 仅作品创建者可更新

---

### 4.8 删除角色

**端点**: `DELETE /api/characters/:id`

**描述**: 删除角色

**认证**: 必需

**参数**:
- `id` (path): 角色ID

**响应**:
```json
{
  "success": true,
  "data": {
    "message": "角色已删除"
  }
}
```

**权限**: 仅作品创建者可删除

---

## 5. 小说解析

### 5.1 解析小说文本

**端点**: `POST /api/novel/parse`

**描述**: 将小说文本解析为动漫分镜格式，自动提取角色和场景信息

**认证**: 必需

**请求体**:
```json
{
  "text": "string (小说文本内容)",
  "style": "string (可选，画面风格，如：写实、卡通、水墨等)",
  "targetAudience": "string (可选，目标受众，如：少年、青年、成人等)"
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "characters": [
      {
        "name": "string",
        "description": "string",
        "appearance": "string",
        "personality": "string"
      }
    ],
    "scenes": [
      {
        "id": 1,
        "text": "string (场景描述)",
        "characters": ["string (角色名称)"],
        "setting": "string (场景设定)",
        "mood": "string (氛围)"
      }
    ],
    "summary": "string (内容摘要)"
  }
}
```

**说明**:
- 使用 AI 自动分析小说文本，提取角色和场景信息
- `style` 和 `targetAudience` 参数会影响生成的场景描述风格
- 返回的数据可直接用于创建作品的集数和角色

---

## 6. 作品浏览

### 6.1 获取我的作品列表

**端点**: `GET /api/my-works`

**描述**: 获取当前用户的所有作品及集数

**认证**: 必需

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "userId": "string",
      "title": "string",
      "description": "string",
      "isPublic": true,
      "coverImage": "string",
      "likesCount": 156,
      "viewsCount": 1240,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z",
      "episodes": [
        {
          "id": "string",
          "episodeNumber": 1,
          "title": "string",
          "isFree": true,
          "coinPrice": 0,
          "isPublished": true
        }
      ]
    }
  ]
}
```

---

### 6.2 获取我的收藏列表

**端点**: `GET /api/my-favorites`

**描述**: 获取当前用户收藏的所有作品

**认证**: 必需

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "userId": "string",
      "title": "string",
      "description": "string",
      "coverImage": "string",
      "likesCount": 156,
      "viewsCount": 1240,
      "isLiked": true,
      "episodeCount": 5,
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

**说明**:
- 返回用户点赞过的所有公开作品
- 按收藏时间倒序排列

---

### 6.3 获取作品广场

**端点**: `GET /api/gallery`

**描述**: 获取所有公开作品

**查询参数**:
- `sortBy` (可选): 排序方式
  - `latest` (默认): 最新发布
  - `likes`: 点赞数

**响应**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "userId": "string",
      "title": "string",
      "description": "string",
      "coverImage": "string",
      "likesCount": 156,
      "viewsCount": 1240,
      "isLiked": false,
      "episodeCount": 5,
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

**说明**:
- 仅返回 `isPublic: true` 的作品
- `isLiked`: 当前用户是否已点赞（需认证）
- `episodeCount`: 已发布的集数数量

---

## 7. 购买系统

### 7.1 购买付费集数

**端点**: `POST /api/episodes/:id/purchase`

**描述**: 使用金币购买付费集数

**认证**: 必需

**参数**:
- `id` (path): 集数ID

**响应**:
```json
{
  "success": true,
  "data": {
    "episodeId": "string",
    "coinCost": 50,
    "newBalance": 450,
    "purchasedAt": "2024-01-01T00:00:00.000Z"
  }
}
```

**错误响应** (金币不足):
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "金币不足，需要 50 金币，当前余额 30"
  }
}
```

**业务规则**:
- 免费集数无需购买
- 已购买集数不可重复购买
- 作品创建者无需购买自己的集数
- 购买成功后扣除相应金币

---

## 8. 社交功能

### 8.1 点赞作品

**端点**: `POST /api/works/:id/like`

**描述**: 点赞作品

**认证**: 必需

**参数**:
- `id` (path): 作品ID

**响应**:
```json
{
  "success": true,
  "data": {
    "message": "点赞成功"
  }
}
```

**限制**: 每个用户对每个作品只能点赞一次

---

### 8.2 取消点赞

**端点**: `DELETE /api/works/:id/like`

**描述**: 取消点赞

**认证**: 必需

**参数**:
- `id` (path): 作品ID

**响应**:
```json
{
  "success": true,
  "data": {
    "message": "取消点赞成功"
  }
}
```

---

## 数据库设计

### 表结构

#### users (用户表)
```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(20) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  coin_balance INTEGER DEFAULT 100,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_email (email)
);
```

#### works (作品表)
```sql
CREATE TABLE works (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  is_public BOOLEAN DEFAULT FALSE,
  cover_image VARCHAR(500),
  likes_count INTEGER DEFAULT 0,
  views_count INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_is_public (is_public),
  INDEX idx_created_at (created_at)
);
```

#### episodes (集数表)
```sql
CREATE TABLE episodes (
  id VARCHAR(36) PRIMARY KEY,
  work_id VARCHAR(36) NOT NULL,
  episode_number INTEGER NOT NULL,
  title VARCHAR(100) NOT NULL,
  novel_text TEXT NOT NULL,
  scenes JSON,
  is_free BOOLEAN DEFAULT TRUE,
  coin_price INTEGER DEFAULT 0,
  is_published BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  UNIQUE KEY unique_work_episode (work_id, episode_number),
  INDEX idx_work_id (work_id)
);
```

#### purchases (购买记录表)
```sql
CREATE TABLE purchases (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  episode_id VARCHAR(36) NOT NULL,
  coin_cost INTEGER NOT NULL,
  purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (episode_id) REFERENCES episodes(id) ON DELETE CASCADE,
  UNIQUE KEY unique_user_episode (user_id, episode_id),
  INDEX idx_user_id (user_id),
  INDEX idx_episode_id (episode_id)
);
```

#### likes (点赞表)
```sql
CREATE TABLE likes (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  work_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  UNIQUE KEY unique_user_work (user_id, work_id),
  INDEX idx_user_id (user_id),
  INDEX idx_work_id (work_id)
);
```

---

## 安全要求

### 1. 身份认证
- 使用 JWT 进行身份认证
- Token 应包含：用户ID、用户名、过期时间
- Token 有效期建议 7 天
- 刷新机制：Token 过期前 1 天可刷新

### 2. 密码安全
- 使用 bcrypt 加密密码，成本因子至少 10
- 密码最小长度 8 字符
- 建议包含大小写字母、数字和特殊字符

### 3. 输入验证
- 所有用户输入必须进行验证和净化
- 防止 SQL 注入：使用参数化查询
- 防止 XSS：对输出进行 HTML 转义
- 文本长度限制：标题 100 字符，描述 500 字符

### 4. 权限控制
- 用户只能编辑/删除自己的作品和集数
- 未发布集数仅创建者可访问
- 付费集数需购买才能访问（创建者除外）

### 5. 速率限制
- 登录接口：每 IP 每分钟最多 5 次尝试
- API 接口：每用户每分钟最多 100 次请求
- 创建操作：每用户每分钟最多 10 次

### 6. HTTPS
- 生产环境必须使用 HTTPS
- 设置 HSTS 头

---

## 性能优化建议

### 1. 数据库优化
- 为常用查询字段添加索引
- 使用数据库连接池
- 实现查询缓存（Redis）

### 2. API 响应优化
- 实现分页（作品广场、我的作品）
- 使用 gzip 压缩响应
- 设置合理的缓存策略

### 3. 并发处理
- 使用事务处理金币扣除等关键操作
- 实现乐观锁防止并发问题

---

## 测试建议

### 1. 单元测试
- 测试所有 API 端点的正常和异常情况
- 测试权限验证逻辑
- 测试金币扣除的事务安全性

### 2. 集成测试
- 测试完整的用户流程
- 测试跨模块的数据一致性

### 3. 性能测试
- 负载测试：模拟高并发访问
- 压力测试：测试系统极限

---

## 版本历史

- **v1.0.0** (2025-10-24): 初始版本
  - 用户认证
  - 作品和集数管理
  - 购买和点赞功能

---

## 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues: https://github.com/yourusername/AIgo/issues
- Email: dev@yourdomain.com
