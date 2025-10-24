# AIgo - 智能动漫生成系统

基于 React + Node.js 开发的智能动漫生成平台，将小说文本转换为精美动漫作品，支持多集管理、付费观看、作品分享等社交功能。

## 项目结构

```
AIgo/
├── front/              # 前端项目 (React + Vite)
│   ├── src/
│   │   ├── components/    # 公共组件
│   │   ├── pages/         # 页面组件
│   │   ├── services/      # API 服务层
│   │   └── ...
│   ├── package.json
│   └── README.md
├── docs/              # 文档目录
│   └── BACKEND_API.md    # 后端 API 文档
└── README.md          # 项目总览
```

## 功能特性

### 🎨 核心功能

- **作品管理**: 创建、编辑、删除作品，支持标题和简介
- **集数管理**: 每个作品可创建多集，支持草稿和发布状态
- **付费系统**: 设置金币价格，购买付费内容（100金币 = 1元）
- **作品广场**: 浏览所有公开作品，支持点赞和排序
- **漫画浏览器**: 场景浏览，图文展示，支付购买流程
- **用户中心**: 查看历史作品和集数管理

### 💡 技术亮点

- **前后端分离**: 清晰的架构，易于扩展和维护
- **Mock API**: 前端可独立开发，支持一键切换真实 API
- **响应式设计**: 完美支持桌面和移动设备
- **模块化组件**: 可复用的组件架构

## 快速开始

### 前端开发

```bash
cd front
npm install
npm run dev
```

访问 http://localhost:5173

### 后端开发

查看 `docs/BACKEND_API.md` 了解完整的 API 规范。

## 技术栈

### 前端
- React 18 - UI 框架
- React Router v7 - 路由管理
- Vite 4 - 构建工具
- 纯 CSS - 样式实现

### 后端（推荐）
- Node.js + Express - Web 框架
- PostgreSQL / MongoDB - 数据库
- JWT - 身份认证
- Redis - 缓存（可选）

## 文档

- [前端 README](./front/README.md) - 前端项目说明
- [后端 API 文档](./docs/BACKEND_API.md) - 完整的后端 API 规范

## 开发路线图

### ✅ 已完成
- [x] 前端页面系统
- [x] Mock API 层
- [x] 作品和集数管理
- [x] 付费和购买流程
- [x] 作品广场和点赞

### 🚧 进行中
- [ ] 后端 API 实现
- [ ] 用户认证系统
- [ ] 数据库设计

### 📋 计划中
- [ ] 支付系统集成
- [ ] AI 图像生成集成
- [ ] TTS 语音合成
- [ ] 视频导出功能

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT
