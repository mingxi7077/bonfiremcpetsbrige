# BonfireMCpetsBridge

![License](https://img.shields.io/badge/license-BNSL--1.0-red)
![Commercial Use](https://img.shields.io/badge/commercial-use%20by%20written%20permission%20only-critical)
![Platform](https://img.shields.io/badge/platform-Paper%201.21.8-brightgreen)
![Storage](https://img.shields.io/badge/storage-MySQL%20%2B%20SQLite-blueviolet)
![Status](https://img.shields.io/badge/status-active-success)

BonfireMCpetsBridge is the backup and rollback bridge for legacy MCPets data during Bonfire migration work.

## Highlights

- Creates snapshots before risky migration or cleanup operations.
- Supports rollback-oriented admin flows for MCPets player data.
- Separates source detection, storage, and bridge orchestration into dedicated services.
- Keeps the historical repository name `bonfiremcpetsbrige`, while the plugin name remains `BonfireMCpetsBridge`.

## Core Commands

- `/bonfiremcpetsbridge status`
- `/bonfiremcpetsbridge backup`
- `/bonfiremcpetsbridge snapshot`
- `/bonfiremcpetsbridge rollback`
- `/bonfiremcpetsbridge cleanup`
- `/bonfiremcpetsbridge reload`

## Build

```powershell
.\mvnw.cmd -q -DskipTests package
```

## Repository Scope

- Source, config templates, and migration notes only.
- Deployment bundles and local probes are excluded from Git.

## License

Bonfire Non-Commercial Source License 1.0

Commercial use is prohibited unless you first obtain written permission from `mingxi7707@qq.com`.
