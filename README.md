# AI 编程小助手

> 基于大语言模型的智能编程学习与求职面试辅助工具，通过流式对话实时解答用户问题。

---

## 项目简介

AI 编程小助手是一款面向编程初学者和求职者的智能问答应用，整合了 **AI 大模型对话**、**RAG 知识检索**、**MCP 工具调用** 等能力，帮助用户高效学习编程知识、准备技术面试。

项目采用前后端分离架构：后端基于 Spring Boot + LangChain4j 构建 AI 服务层，前端基于 Vue3 实现聊天室风格的交互界面，通过 SSE 流式传输实现 AI 回复的实时逐字显示。

---

## 主要功能

1. **AI 智能对话** — 基于通义千问（Qwen）大模型，支持多轮连续对话，自动区分不同会话
2. **SSE 流式响应** — AI 回复内容通过 Server-Sent Events 实时推送到前端，逐字显示打字机效果
3. **RAG 知识增强** — 集成本地文档（编程学习路线、面试题库等），AI 回答时优先检索知识库内容
4. **输入安全过滤** — 内置 Guardrail 机制，对用户输入进行安全检测与过滤
5. **MCP 工具调用** — 支持通过 MCP 协议调用外部工具（如联网搜索）扩展 AI 能力
6. **会话记忆管理** — 自动为每个页面会话生成唯一 memoryId，实现上下文连贯的多轮对话

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Spring Boot 3.5.13、Java 21、LangChain4j 1.1.0、Reactor |
| **前端** | Vue 3、Vite、原生 EventSource (SSE) |
| **AI 模型** | 通义千问 Qwen-Plus（DashScope 平台） |
| **其他** | Lombok、jsoup、Easy RAG、MCP Client |

---

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- Maven 3.9+

### 1. 克隆项目

```bash
git clone git@github.com:Eliaukgit/ai-code-helper.git
cd ai-code-helper
```

### 2. 配置 API Key

编辑 `src/main/resources/application-local.yml`，填入你的 DashScope API Key：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: sk-your-dashscope-api-key
      streaming-chat-model:
        api-key: sk-your-dashscope-api-key
```

### 3. 启动后端

```bash
./mvnw spring-boot:run
```

后端服务启动在 `http://localhost:8081`，API 基础路径为 `/api`。

### 4. 启动前端

```bash
cd ai-code-helper-frontend
npm install
npm run dev
```

前端开发服务器启动在 `http://localhost:5173`，通过 Vite 代理自动转发 `/api` 请求到后端。

### 5. 访问应用

浏览器打开 http://localhost:5173 即可开始使用。

---

## 目录结构

```
ai-code-helper/
├── src/main/java/com/bear/aicodehelper/
│   ├── ai/                          # AI 服务层
│   │   ├── AiCodeHelper.java        # LangChain4j AI 服务接口
│   │   ├── AiCodeHelperService.java # AI 服务定义（对话、报告、RAG）
│   │   ├── AiCodeHelperServiceFactory.java
│   │   ├── guardrail/               # 输入安全过滤
│   │   ├── listener/                # 模型监听
│   │   ├── mcp/                     # MCP 配置
│   │   ├── model/                   # 模型配置
│   │   ├── rag/                     # RAG 检索配置
│   │   └── tools/                   # 工具定义
│   ├── config/                      # 全局配置（跨域等）
│   ├── controller/                  # REST 接口
│   │   └── AiController.java        # /ai/chat SSE 接口
│   └── AiCodeHelperApplication.java # 启动类
├── src/main/resources/
│   ├── docs/                        # RAG 知识库文档
│   ├── application.yml              # 主配置
│   ├── application-local.yml        # 本地环境配置（API Key）
│   └── system-prompt.txt            # AI 系统提示词
├── ai-code-helper-frontend/         # Vue3 前端项目
│   ├── src/
│   │   ├── App.vue                  # 聊天室主页面
│   │   ├── main.js                  # 入口
│   │   └── style.css                # 全局样式
│   ├── index.html
│   ├── package.json
│   └── vite.config.js               # Vite 代理配置
├── pom.xml
└── README.md
```

---

## API 接口

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/ai/chat` | `memoryId`, `message` | SSE 流式对话接口 |

---

## 注意事项

- **API Key 安全**：`application-local.yml` 中的密钥为本地开发使用，生产环境请通过环境变量或配置中心注入。
- **SSE 代理**：前端开发时通过 Vite 的 `proxy` 配置将 `/api` 转发到后端，生产部署需配置 Nginx 反向代理或使用同源部署。
- **会话隔离**：每次刷新页面会生成新的 `memoryId`，历史对话不会保留；如需持久化会话，需扩展数据库存储层。
- **模型依赖**：项目默认使用 DashScope 平台的 Qwen-Plus 模型，需要有效的阿里云账号和 API Key。
