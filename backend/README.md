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

## 下一步

- [ ] 实现用户认证系统
- [ ] 集成数据库（PostgreSQL/MySQL）
- [ ] 实现完整的 API（参考 `docs/BACKEND_API.md`）
- [ ] 添加单元测试和集成测试
- [ ] 配置生产环境参数

## 许可证

MIT
