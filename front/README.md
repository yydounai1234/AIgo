# AIgo Frontend

基于 React 的智能动漫生成系统前端应用。

## 功能特性

- 📝 **作品管理**: 创建、编辑、删除作品
- 📚 **集数管理**: 创建多集内容，设置免费/付费
- 💰 **金币系统**: 购买付费内容（100金币 = 1元）
- 🌐 **作品广场**: 浏览和点赞公开作品
- 🎬 **漫画浏览器**: 场景浏览，图文展示
- 👤 **用户中心**: 查看历史作品和集数

## 技术栈

- **React 18** - UI 框架
- **React Router v7** - 路由管理
- **Vite 4** - 构建工具
- **纯 CSS** - 样式实现

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

应用将在 http://localhost:5173 启动

### 生产构建

```bash
npm run build
```

构建产物在 `dist/` 目录

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
front/
├── src/
│   ├── components/          # 公共组件
│   │   ├── Navigation.jsx   # 导航栏
│   │   └── ...
│   ├── pages/               # 页面组件
│   │   ├── Home.jsx         # 首页
│   │   ├── WorkEditor.jsx   # 作品编辑
│   │   ├── MyWorks.jsx      # 我的作品
│   │   ├── Gallery.jsx      # 作品广场
│   │   └── EpisodeViewer.jsx # 漫画浏览器
│   ├── services/            # API 服务
│   │   ├── api.js           # API 入口
│   │   ├── mockApi.js       # Mock API
│   │   └── mockData.js      # Mock 数据
│   ├── App.jsx              # 根组件
│   ├── App.css              # 全局样式
│   ├── main.jsx             # 入口文件
│   └── index.css            # 基础样式
├── index.html
├── package.json
└── vite.config.js
```

## Mock API vs 生产 API

默认使用 Mock API 进行开发。要切换到生产 API：

编辑 `src/services/api.js`:

```javascript
const USE_MOCK = false  // 切换到生产 API
```

## 环境变量

创建 `.env` 文件：

```env
VITE_API_BASE_URL=https://your-api-domain.com
```

## 后端 API 文档

详见项目根目录的 `docs/BACKEND_API.md`

## License

MIT
