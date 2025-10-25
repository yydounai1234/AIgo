# AIgo Backend

基于 Spring Boot 3.2 + LangChain4j 的智能动漫生成系统后端服务。

## 技术栈

- **Spring Boot 3.2.0** - 核心框架
- **LangChain4j 0.35.0** - AI集成框架
- **Java 17** - 开发语言
- **Maven** - 依赖管理

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/aigo/
│   │   │   ├── AigoApplication.java          # 主应用类
│   │   │   └── controller/
│   │   │       ├── HealthController.java     # 健康检查接口
│   │   │       └── LangChainTestController.java  # LangChain测试接口
│   │   └── resources/
│   │       └── application.properties        # 应用配置
│   └── test/
└── pom.xml                                    # Maven配置
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+

### 运行应用

```bash
cd backend
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 测试接口

#### 1. 健康检查

```bash
curl http://localhost:8080/api/health
```

响应示例：
```json
{
  "status": "UP",
  "timestamp": "2025-10-24T15:30:00",
  "service": "AIgo Backend",
  "version": "1.0.0"
}
```

#### 2. Hello 接口

```bash
curl http://localhost:8080/api/hello
```

响应示例：
```json
{
  "message": "Hello from AIgo Backend!",
  "description": "Spring Boot + LangChain4j integration is ready"
}
```

#### 3. LangChain 测试接口

```bash
curl http://localhost:8080/api/langchain/test
```

#### 4. AI 聊天接口

```bash
curl -X POST http://localhost:8080/api/langchain/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello AI"}'
```

## 配置说明

### application.properties

```properties
# 应用配置
spring.application.name=aigo-backend
server.port=8080

# OpenAI API密钥（需要配置真实的密钥）
openai.api.key=demo-key

# 日志配置
logging.level.root=INFO
logging.level.com.aigo=DEBUG
```

### 配置 OpenAI API

要使用真实的 AI 功能，需要配置 OpenAI API 密钥。推荐使用环境变量方式，避免将敏感信息提交到代码仓库。

#### 方式一：使用环境变量（推荐）

**开发环境：**

1. 在项目根目录创建 `.env.dev` 文件（该文件已在 `.gitignore` 中，不会被提交到 Git）：
   ```bash
   # 创建 .env.dev 文件
   cat > backend/.env.dev << 'EOF'
   OPENAI_API_KEY=your-openai-api-key-here
   SPRING_PROFILES_ACTIVE=dev
   EOF
   ```

2. 运行应用时加载环境变量：
   ```bash
   cd backend
   export $(cat .env.dev | xargs) && mvn spring-boot:run
   ```

**生产环境：**

1. 创建 `.env.prod` 文件：
   ```bash
   cat > backend/.env.prod << 'EOF'
   OPENAI_API_KEY=your-production-api-key-here
   SPRING_PROFILES_ACTIVE=prod
   EOF
   ```

2. 运行应用：
   ```bash
   cd backend
   export $(cat .env.prod | xargs) && mvn spring-boot:run
   ```

**获取 OpenAI API 密钥：**
1. 访问 [OpenAI Platform](https://platform.openai.com/api-keys)
2. 登录或注册账号
3. 创建新的 API 密钥
4. 将密钥复制到对应的 `.env` 文件中

⚠️ **安全提示**：
- `.env*` 文件已添加到 `.gitignore`，不会被提交到代码仓库
- 不要将真实的 API 密钥直接写入 `application.properties`
- 生产环境建议使用更安全的密钥管理方式（如 Vault、云服务密钥管理等）

#### 方式二：直接设置环境变量

```bash
export OPENAI_API_KEY=your-real-api-key
mvn spring-boot:run
```

#### 方式三：修改 application.properties（不推荐）

仅用于本地测试，不要提交到 Git：
```properties
openai.api.key=your-real-api-key
```

## 构建部署

### 打包应用

```bash
mvn clean package
```

生成的 JAR 文件位于 `target/aigo-backend-1.0.0.jar`

### 运行 JAR

```bash
java -jar target/aigo-backend-1.0.0.jar
```

## 开发指南

### 添加新的 REST 接口

1. 在 `src/main/java/com/aigo/controller/` 创建新的 Controller 类
2. 使用 `@RestController` 和 `@RequestMapping` 注解
3. 实现业务逻辑

### 集成 LangChain4j

项目已集成 LangChain4j，可以使用以下功能：

- **ChatLanguageModel** - 对话模型
- **OpenAiChatModel** - OpenAI 集成
- 更多功能参考 [LangChain4j 文档](https://github.com/langchain4j/langchain4j)

## DeepSeek-R1-0528 集成 (七牛云)

### 功能说明
本项目已集成七牛云提供的 DeepSeek-R1-0528 大语言模型,用于将小说文本转换为动漫桥段。

### API 接口

#### 解析小说文本
**端点**: `POST /api/novel/parse`

**请求体**:
```json
{
  "text": "这是一段小说文本...",
  "style": "热血动漫",
  "targetAudience": "青少年"
}
```

**响应**:
```json
{
  "characters": [
    {
      "name": "角色名称",
      "description": "角色描述",
      "appearance": "外貌特征",
      "personality": "性格特点"
    }
  ],
  "scenes": [
    {
      "sceneNumber": 1,
      "visualDescription": "画面描述,用于 AI 图像生成",
      "atmosphere": "场景氛围",
      "dialogues": ["对话1", "对话2"],
      "action": "动作描述"
    }
  ],
  "plotSummary": "剧情总结",
  "genre": "类型",
  "mood": "情绪基调"
}
```

### 配置 DeepSeek API

创建 `.env` 文件并配置:
```bash
DEEPSEEK_API_KEY=your-qiniu-deepseek-api-key
DEEPSEEK_API_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL_NAME=deepseek-reasoner
```

运行时加载环境变量:
```bash
export $(cat .env | xargs) && mvn spring-boot:run
```

### 测试示例

```bash
curl -X POST http://localhost:8080/api/novel/parse \
  -H "Content-Type: application/json" \
  -d '{
    "text": "夜幕降临,城市的霓虹灯开始闪烁。李明站在天台上,望着远方的天空,心中充满了对未来的期待。",
    "style": "都市青春",
    "targetAudience": "青少年"
  }'
```

## 文生图功能 (七牛云)

### 功能说明
系统已集成七牛云文生图大模型,可以自动为每个场景生成对应的图片。图片生成与小说解析流程无缝集成,无需额外 API 调用。

### 工作流程
1. 调用 `/api/novel/parse` 解析小说文本
2. 系统自动提取角色信息和场景描述
3. 为每个 `sceneNumber` 生成一张图片
4. 图片 URL 保存在场景的 `imageUrl` 字段中

### 角色一致性保证
为确保同一角色在不同场景中外观一致,系统采用以下策略:
- 提取角色的外貌 (appearance) 和描述 (description) 作为固定提示词
- 每次生成图片时,始终包含该角色的完整描述
- 使用较低的 temperature (0.3) 参数减少生成的随机性
- 根据 visualDescription、atmosphere 和 action 综合生成场景图片

### 配置文生图 API

在 `.env` 文件中添加:
```bash
# 七牛云文生图 API 配置
QINIU_TEXT2IMG_API_KEY=your-qiniu-text2img-api-key
QINIU_TEXT2IMG_API_BASE_URL=https://openai.qiniu.com/v1
QINIU_TEXT2IMG_MODEL_NAME=gemini-2.5-flash-image

# 七牛云对象存储配置 (用于保存生成的图片)
QINIU_STORAGE_ACCESS_KEY=your-qiniu-access-key
QINIU_STORAGE_SECRET_KEY=your-qiniu-secret-key
QINIU_STORAGE_BUCKET_NAME=aigo-images
QINIU_STORAGE_DOMAIN=your-domain.qiniucdn.com
```

参考 `.env.example` 查看完整配置示例。

### 图片存储说明

生成的图片会自动上传到七牛云对象存储,并返回公网可访问的 URL:
- **存储位置**: 七牛云对象存储 (Kodo)
- **文件命名**: `scene_{场景号}_{时间戳}_{UUID}.png`
- **返回格式**: 完整的公网 URL (如 `https://your-domain.qiniucdn.com/scene_1_1234567890_abc.png`)
- **访问方式**: 公网可直接访问,无需额外认证

**配置说明**:
- `QINIU_STORAGE_ACCESS_KEY`: 七牛云 AccessKey
- `QINIU_STORAGE_SECRET_KEY`: 七牛云 SecretKey  
- `QINIU_STORAGE_BUCKET_NAME`: 存储空间名称
- `QINIU_STORAGE_DOMAIN`: 绑定的 CDN 加速域名

### API 响应示例

```json
{
  "characters": [...],
  "scenes": [
    {
      "sceneNumber": 1,
      "character": "李明",
      "dialogue": "今天会是美好的一天!",
      "visualDescription": "李明站在天台上,面带微笑仰望天空",
      "atmosphere": "充满希望",
      "action": "伸展双臂,深呼吸",
      "imageUrl": "https://your-domain.qiniucdn.com/scene_1_1234567890_abc.png"
    }
  ],
  "plotSummary": "...",
  "genre": "...",
  "mood": "..."
}
```

### 注意事项
- Demo 模式 (API key 为 `demo-key`) 会返回占位图片
- 生产环境需配置真实的七牛云 API 密钥和对象存储凭证
- 图片生成为串行处理,场景较多时可能需要较长时间
- 图片存储到七牛云后返回公网 URL,支持 CDN 加速访问

## 文字转语音功能 (七牛云 TTS)

### 功能说明
系统已集成七牛云文字转语音 (TTS) 服务,可以自动为每个对话生成语音。语音生成与小说解析流程无缝集成,无需额外 API 调用。

### 工作流程
1. 调用 `/api/novel/parse` 解析小说文本
2. 系统自动提取角色信息和对话内容
3. 为每个场景的 `dialogue` 生成对应的语音
4. 语音 URL 保存在场景的 `audioUrl` 字段中

### 角色声音一致性保证
为确保同一角色在不同场景中声音一致,系统采用以下策略:
- **智能性别识别**: 通过分析角色的描述 (description)、外貌 (appearance) 和性格 (personality) 自动识别性别
- **关键词检测**: 系统会检测"男"、"女"、"他"、"她"等性别关键词
- **姓名分析**: 当角色信息不足时,通过姓名特征推断性别
- **角色-音色映射**: 每个角色首次分配音色后,系统会缓存该映射关系,确保后续场景使用相同音色

### 性别识别规则
系统使用以下规则识别角色性别:

**男性关键词**: 男、他、先生、男性、男孩、男人、少年、哥哥、兄弟、父亲、爸爸  
**女性关键词**: 女、她、女士、女性、女孩、女人、少女、姐姐、妹妹、母亲、妈妈

**姓名特征**:
- 女性: 娜、婷、丽、芳、静、雅、兰、燕、莉、萍
- 男性: 明、强、刚、军、伟、涛、龙、杰、鹏、磊

### 默认音色配置
- **女性角色**: `qiniu_zh_female_wwxkjx` (甜美女声)
- **男性角色**: `qiniu_zh_male_default` (标准男声)
- **未识别角色**: 默认使用女性音色

### 配置 TTS API

在 `.env` 文件中添加:
```bash
# 七牛云 TTS API 配置
QINIU_TTS_API_KEY=your-qiniu-tts-api-key
QINIU_TTS_API_BASE_URL=https://openai.qiniu.com/v1

# 七牛云对象存储配置 (用于保存生成的语音)
QINIU_STORAGE_ACCESS_KEY=your-qiniu-access-key
QINIU_STORAGE_SECRET_KEY=your-qiniu-secret-key
QINIU_STORAGE_BUCKET_NAME=aigo-images
QINIU_STORAGE_DOMAIN=your-domain.qiniucdn.com
```

参考 `.env.example` 查看完整配置示例。

### 音频存储说明

生成的音频会自动上传到七牛云对象存储,并返回公网可访问的 URL:
- **存储位置**: 七牛云对象存储 (Kodo)
- **文件命名**: `scene_{场景号}_{时间戳}_{UUID}.mp3`
- **音频格式**: MP3
- **返回格式**: 完整的公网 URL (如 `https://your-domain.qiniucdn.com/scene_1_1234567890_abc.mp3`)
- **访问方式**: 公网可直接访问,无需额外认证

### API 响应示例

```json
{
  "characters": [
    {
      "name": "李明",
      "description": "年轻男孩",
      "appearance": "短发,阳光",
      "personality": "开朗"
    }
  ],
  "scenes": [
    {
      "sceneNumber": 1,
      "character": "李明",
      "dialogue": "今天会是美好的一天!",
      "visualDescription": "李明站在天台上,面带微笑仰望天空",
      "atmosphere": "充满希望",
      "action": "伸展双臂,深呼吸",
      "imageUrl": "https://your-domain.qiniucdn.com/scene_1_1234567890_abc.png",
      "audioUrl": "https://your-domain.qiniucdn.com/scene_1_1234567890_def.mp3"
    }
  ],
  "plotSummary": "...",
  "genre": "...",
  "mood": "..."
}
```

### 注意事项
- Demo 模式 (API key 为 `demo-key`) 会返回占位音频 URL
- 生产环境需配置真实的七牛云 TTS API 密钥和对象存储凭证
- 音频生成为串行处理,对话较多时可能需要较长时间
- 音频存储到七牛云后返回公网 URL,支持 CDN 加速访问
- 空对话或无文本的场景不会生成音频

## 下一步

- [ ] 实现用户认证系统
- [ ] 集成数据库（PostgreSQL/MySQL）
- [ ] 实现完整的 API（参考 `docs/BACKEND_API.md`）
- [ ] 添加单元测试和集成测试
- [ ] 配置生产环境参数

## 许可证

MIT
