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

### 环境配置

项目使用环境变量来管理敏感配置信息（如 API 密钥）。

#### 1. 创建环境变量文件

根据你的环境（开发或生产），创建相应的 `.env` 文件：

**开发环境（.env.dev）：**
```bash
# 复制示例文件
cp .env.example .env.dev

# 编辑文件，填入你的 OpenAI API 密钥
# .env.dev
OPENAI_API_KEY=your-openai-api-key-here
SPRING_PROFILES_ACTIVE=dev
```

**生产环境（.env.prod）：**
```bash
# 复制示例文件
cp .env.example .env.prod

# 编辑文件，填入你的生产环境 API 密钥
# .env.prod
OPENAI_API_KEY=your-production-api-key-here
SPRING_PROFILES_ACTIVE=prod
```

#### 2. 获取 OpenAI API 密钥

1. 访问 [OpenAI Platform](https://platform.openai.com/api-keys)
2. 登录或注册账号
3. 创建新的 API 密钥
4. 将密钥复制到对应的 `.env` 文件中

⚠️ **重要提示**：
- `.env` 文件包含敏感信息，已在 `.gitignore` 中排除，不会被提交到 Git
- 不要将真实的 API 密钥提交到代码仓库
- 生产环境建议使用更安全的密钥管理方式（如 Vault、云服务密钥管理等）

### 运行应用

#### 开发环境

```bash
cd backend

# 方式 1: 使用环境变量文件
export $(cat .env.dev | xargs) && mvn spring-boot:run

# 方式 2: 直接设置环境变量
export OPENAI_API_KEY=your-api-key
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# 方式 3: 使用 Maven 传递环境变量
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 生产环境

```bash
cd backend

# 使用生产环境配置
export $(cat .env.prod | xargs) && mvn spring-boot:run

# 或者运行打包后的 JAR
java -jar -Dspring.profiles.active=prod target/aigo-backend-1.0.0.jar
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

### 环境配置文件

项目支持多环境配置，通过 Spring Profiles 机制切换：

#### application.properties（基础配置）
```properties
spring.application.name=aigo-backend
server.port=8080

# OpenAI API Key (use environment variable)
openai.api.key=${OPENAI_API_KEY:demo-key}

spring.jackson.time-zone=Asia/Shanghai
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss

# Default logging configuration
logging.level.root=INFO
logging.level.com.aigo=DEBUG
```

#### application-dev.properties（开发环境）
```properties
# OpenAI API Key (from environment variable)
openai.api.key=${OPENAI_API_KEY:demo-key}

# Development logging
logging.level.root=INFO
logging.level.com.aigo=DEBUG
```

#### application-prod.properties（生产环境）
```properties
# OpenAI API Key (from environment variable)
openai.api.key=${OPENAI_API_KEY}

# Production logging
logging.level.root=WARN
logging.level.com.aigo=INFO
```

### 环境变量说明

| 环境变量 | 说明 | 必需 | 默认值 |
|---------|------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 密钥 | 是（生产环境） | demo-key（开发环境） |
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件（dev/prod） | 否 | 无 |

### Demo 模式

如果不配置 `OPENAI_API_KEY` 或使用 `demo-key`，应用将以 Demo 模式运行：
- `/api/langchain/chat` 接口将返回回声响应
- 不会调用真实的 OpenAI API
- 适用于开发和测试场景

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

## 下一步

- [ ] 实现用户认证系统
- [ ] 集成数据库（PostgreSQL/MySQL）
- [ ] 实现完整的 API（参考 `docs/BACKEND_API.md`）
- [ ] 添加单元测试和集成测试
- [ ] 配置生产环境参数

## 许可证

MIT
