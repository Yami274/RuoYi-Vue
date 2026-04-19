# ruoyi-mental

该目录是 AI 心理健康助手后端服务模块（基于若依）新建文件夹，承载用户端业务接口实现。

## 已实现接口

- POST `/api/user/login`
- POST `/api/user/add`
- POST `/api/user/logout`
- GET `/api/psychological-chat/sessions`
- GET `/api/psychological-chat/sessions/{sessionId}/messages`
- POST `/api/psychological-chat/session/start`
- POST `/api/psychological-chat/stream` (SSE)
- GET `/api/psychological-chat/session/{sessionId}/emotion`
- DELETE `/api/psychological-chat/sessions/{sessionId}`
- POST `/api/emotion-diary`
- GET `/api/knowledge/article/page`
- GET `/api/knowledge/article/{id}`

## 运行前准备

1. 导入建表脚本：`RuoYi-Vue/sql/mental_backend.sql`
2. 启动 Redis 与 MySQL

## 启动命令

在 `RuoYi-Vue` 目录执行：

```bash
mvn -pl ruoyi-admin -am spring-boot:run
```

或在 IDE 直接运行 `ruoyi-admin` 的 `RuoYiApplication`。
