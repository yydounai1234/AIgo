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

要使用真实的 AI 功能，需要配置 OpenAI API 密钥：

1. 获取 OpenAI API 密钥
2. 在 `application.properties` 中设置：
   ```properties
   openai.api.key=your-real-api-key
   ```

或者通过环境变量：
```bash
export OPENAI_API_KEY=your-real-api-key
mvn spring-boot:run
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

## 下一步

- [ ] 实现用户认证系统
- [ ] 集成数据库（PostgreSQL/MySQL）
- [ ] 实现完整的 API（参考 `docs/BACKEND_API.md`）
- [ ] 添加单元测试和集成测试
- [ ] 配置生产环境参数

## 许可证

MIT
