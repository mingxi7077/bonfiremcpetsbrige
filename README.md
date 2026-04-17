# BonfireMCpetsBridge

[English](#english) | [简体中文](#简体中文)

BonfireMCpetsBridge is Bonfire's backup and rollback bridge for legacy MCPets data.

BonfireMCpetsBridge 是 Bonfire 面向旧版 MCPets 数据的备份与回滚桥接层。

---

## English

BonfireMCpetsBridge is the Bonfire migration-side bridge for snapshotting, rollback, cleanup, and inspection flows around legacy MCPets data.

### What It Does

- Creates snapshots before risky migration or cleanup actions.
- Supports rollback-oriented admin flows for MCPets player data.
- Separates source detection, storage, and orchestration into dedicated services.
- Preserves the historical repository name `bonfiremcpetsbrige`, while the plugin-facing name remains `BonfireMCpetsBridge`.

### Core Commands

- `/bonfiremcpetsbridge status`
- `/bonfiremcpetsbridge backup`
- `/bonfiremcpetsbridge snapshot`
- `/bonfiremcpetsbridge rollback`
- `/bonfiremcpetsbridge cleanup`
- `/bonfiremcpetsbridge reload`

### Repository Layout

- `src/`: plugin source code
- `说明书/`: local operator notes
- `部署包/`: local deployment bundle workspace

### Build

```powershell
.\mvnw.cmd -q -DskipTests package
```

### License

This repository currently uses the `Bonfire Non-Commercial Source License 1.0`.
See [LICENSE](LICENSE) for the exact terms.

---

## 简体中文

BonfireMCpetsBridge 是 Bonfire 在 MCPets 迁移阶段使用的桥接插件，负责备份、快照、回滚、清理与状态查看等管理流程。

### 它的作用

- 在高风险迁移或清理动作前生成数据快照。
- 为 MCPets 玩家数据提供面向回滚的管理流程。
- 将源数据识别、存储处理与桥接编排拆成独立服务。
- 保留历史仓库名 `bonfiremcpetsbrige`，但插件对外名称仍为 `BonfireMCpetsBridge`。

### 主要命令

- `/bonfiremcpetsbridge status`
- `/bonfiremcpetsbridge backup`
- `/bonfiremcpetsbridge snapshot`
- `/bonfiremcpetsbridge rollback`
- `/bonfiremcpetsbridge cleanup`
- `/bonfiremcpetsbridge reload`

### 仓库结构

- `src/`：插件源码
- `说明书/`：本地运维说明
- `部署包/`：本地部署包工作区

### 构建方式

```powershell
.\mvnw.cmd -q -DskipTests package
```

### 授权

本仓库当前采用 `Bonfire Non-Commercial Source License 1.0`。
具体条款见 [LICENSE](LICENSE)。
