# AIgo 程序运行步骤

本文档详细说明如何在本地环境搭建并运行 AIgo 智能动漫生成系统。

## 环境要求

### 前端
- Node.js 16.x 或更高版本
- npm 8.x 或更高版本

### 后端
- JDK 17 或更高版本
- Maven 3.6+ 
- MySQL 8.0+

### 可选服务
- 七牛云账号（用于对象存储）
- OpenAI API Key 或 DeepSeek API Key（用于 AI 功能）

## 一、前端运行步骤

### 1. 安装依赖

```bash
cd front
npm install
```

### 2. 配置环境（可选）

如果需要连接真实的后端 API，可以修改 `front/src/services/api.js` 中的 API 地址：

```javascript
const API_BASE_URL = 'http://localhost:8080';
```

### 3. 启动开发服务器

```bash
npm run dev
```

前端应用将在 `http://localhost:5173` 启动。

### 4. 其他常用命令

```bash
# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint
```

## 二、后端运行步骤

### 1. 配置数据库

#### 创建数据库

```sql
CREATE DATABASE aigo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 配置数据库连接

在 `backend/src/main/resources/application.properties` 中配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/aigo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
```

### 2. 配置 AI 服务（可选）

如果需要使用 AI 功能，需要配置 API 密钥。推荐使用环境变量方式：

#### 方式一：使用 .env 文件（推荐）

在 `backend/` 目录下创建 `.env` 文件：

```bash
cd backend
cat > .env << 'EOF'
# DeepSeek API 配置（用于小说解析）
DEEPSEEK_API_KEY=your-deepseek-api-key
DEEPSEEK_API_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL_NAME=deepseek-reasoner

# 七牛云文生图 API 配置
QINIU_TEXT2IMG_API_KEY=your-qiniu-text2img-api-key
QINIU_TEXT2IMG_API_BASE_URL=https://openai.qiniu.com/v1
QINIU_TEXT2IMG_MODEL_NAME=gemini-2.5-flash-image

# 七牛云 TTS API 配置
QINIU_TTS_API_KEY=your-qiniu-tts-api-key
QINIU_TTS_API_BASE_URL=https://openai.qiniu.com/v1

# 七牛云对象存储配置
QINIU_STORAGE_ACCESS_KEY=your-qiniu-access-key
QINIU_STORAGE_SECRET_KEY=your-qiniu-secret-key
QINIU_STORAGE_BUCKET_NAME=aigo-images
QINIU_STORAGE_DOMAIN=your-domain.qiniucdn.com
EOF
```

参考 `backend/.env.example` 查看完整配置示例。

#### 方式二：直接设置环境变量

```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
export QINIU_TEXT2IMG_API_KEY=your-qiniu-text2img-api-key
export QINIU_TTS_API_KEY=your-qiniu-tts-api-key
export QINIU_STORAGE_ACCESS_KEY=your-qiniu-access-key
export QINIU_STORAGE_SECRET_KEY=your-qiniu-secret-key
```

### 3. 安装依赖并运行

#### 使用 Maven 运行

```bash
cd backend

# 加载环境变量并运行（如果使用 .env 文件）
export $(cat .env | xargs) && mvn spring-boot:run

# 或直接运行（如果使用系统环境变量）
mvn spring-boot:run
```

后端应用将在 `http://localhost:8080` 启动。

#### 打包运行

```bash
# 打包
mvn clean package

# 运行 JAR 文件
java -jar target/aigo-backend-1.0.0.jar
```

### 4. 测试 AI 功能（可选）

如果配置了 AI 服务，可以测试小说解析功能：

```bash
curl -X POST http://localhost:8080/api/novel/parse \
  -H "Content-Type: application/json" \
  -d '{
    "text": "夜幕降临,城市的霓虹灯开始闪烁。李明站在天台上,望着远方的天空,心中充满了对未来的期待。",
    "style": "都市青春",
    "targetAudience": "青少年"
  }'
```

## 三、完整系统运行

### 1. 启动后端服务

```bash
cd backend
export $(cat .env | xargs) && mvn spring-boot:run
```

### 2. 启动前端服务

在新的终端窗口中：

```bash
cd front
npm run dev
```

### 3. 访问应用

在浏览器中访问 `http://localhost:5173`

## 四、常见问题

### 1. 前端无法连接后端

检查前端 API 配置是否正确：
- 确认后端服务在 `http://localhost:8080` 运行
- 检查 CORS 配置是否正确

### 2. 后端数据库连接失败

- 确认 MySQL 服务已启动
- 检查数据库用户名和密码是否正确
- 确认数据库 `aigo` 已创建

### 3. AI 功能无法使用

- 确认已配置正确的 API 密钥
- 检查网络连接是否正常
- 查看后端日志确认错误信息

### 4. 端口冲突

如果端口被占用，可以修改配置：

**前端端口**：修改 `front/vite.config.js`
```javascript
export default defineConfig({
  server: {
    port: 5173  // 改为其他端口
  }
})
```

**后端端口**：修改 `backend/src/main/resources/application.properties`
```properties
server.port=8080  # 改为其他端口
```

## 五、开发模式说明

### Demo 模式

如果未配置 AI API 密钥，系统会运行在 Demo 模式：
- 使用模拟数据进行演示
- 返回占位图片和音频
- 适合快速体验系统功能

### 生产模式

配置真实的 API 密钥后，系统将：
- 调用真实的 AI 服务
- 生成实际的图片和语音
- 数据持久化到数据库和对象存储

## 六、部署说明

### 前端部署

```bash
cd front
npm run build
```

生成的静态文件在 `front/dist/` 目录，可以部署到任何静态文件服务器（Nginx、Apache 等）。

### 后端部署

```bash
cd backend
mvn clean package
```

生成的 JAR 文件在 `backend/target/` 目录，可以部署到任何支持 Java 的服务器。

建议使用 Docker 容器化部署：

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/aigo-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 七、文档参考

- [前端 README](./front/README.md) - 前端项目详细说明
- [后端 README](./backend/README.md) - 后端项目详细说明
- [后端 API 文档](./docs/BACKEND_API.md) - 完整的 API 规范

## 八、获取帮助

如有问题，请：
1. 查看项目 [Issues](https://github.com/yydounai1234/AIgo/issues)
2. 提交新的 Issue
3. 查看文档目录下的其他文档

---

**祝您使用愉快！**
