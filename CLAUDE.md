# CLAUDE.md — xiaozhi-esp32-server (LittleWise)

Guide for AI assistants working on this repository. Keep it current as the code evolves.

## What this repo is

LittleWise is the open-source **server + management console** for XiaoZhi / LittleWise ESP32 AI voice devices. It terminates WebSocket / MQTT+UDP / MCP connections from firmware, runs the ASR → Intent → LLM → TTS pipeline, and provides admin UIs for users, agents, and device config.

Part of a three-repo ecosystem:
- `xiaozhi-esp32` — device firmware (ESP-IDF, C++)
- `xiaozhi-esp32-server` — **this repo** (Python server + Java API + Vue consoles)
- `xiaozhi-assets-generator` — browser-based theme/asset builder (Vue 3)

## Layout

```
main/
├── xiaozhi-server/        # Python core (async WebSocket + HTTP)
│   ├── app.py             # Entry point — asyncio.run(main())
│   ├── config.yaml        # Default config (do NOT commit secrets; override via data/.config.yaml)
│   ├── config/            # Config loader, manage_api sync
│   ├── core/
│   │   ├── websocket_server.py   # Device WS sessions
│   │   ├── http_server.py        # OTA + vision endpoints
│   │   ├── api/                  # HTTP handlers
│   │   ├── handle/               # Text/audio/intent handlers
│   │   ├── providers/            # Pluggable asr/ tts/ llm/ vllm/ intent/ memory/ tools/ vad/
│   │   └── utils/
│   ├── plugins_func/       # Extensible plugin tools
│   ├── performance_tester/ # ASR/LLM/TTS benchmark harness
│   └── test/               # test_page.html + manual audio test tools
├── manager-api/            # Java 21 / Spring Boot 3.4.3 REST API
│   ├── pom.xml             # Maven; MyBatis-Plus 3.5.5, Liquibase 4.20, Shiro 2.0.2
│   └── src/main/
│       ├── java/xiaozhi/modules/   # agent, config, device, sys, security, ...
│       └── resources/application.yml  # Tomcat port 8002, context /xiaozhi
├── manager-web/            # Vue 2 admin console
│   ├── package.json        # element-ui, vuex, vue-i18n, webpack
│   └── src/                # main.js mounts Vue instance
└── manager-mobile/         # Vue 3 + UniApp cross-platform app (pnpm)
docs/                       # 40+ md files: Deployment.md, Deployment_all.md, FAQ.md, integrations
docker-setup.sh             # One-click setup
Dockerfile-server / Dockerfile-server-base / Dockerfile-web
.github/workflows/          # docker-image.yml, build-base-image.yml (tag-triggered multi-arch)
```

## Tech stack

- **xiaozhi-server**: Python 3.10, asyncio, websockets 14.2, aiohttp, loguru, PyYAML, openai 2.8.1, funasr 1.2.7, torch 2.2.2, mcp 1.22.0
- **manager-api**: JDK 21, Spring Boot 3.4.3, Spring WebSocket, Shiro, MyBatis-Plus, Liquibase, Maven 3.9.4
- **manager-web**: Vue 2, element-ui, Vuex, vue-i18n, webpack — Node 18
- **manager-mobile**: Vue 3, UniApp, Pinia, Unocss — pnpm
- **Infra**: Docker multi-stage, multi-arch (amd64/arm64) images on ghcr.io

## Build / run

```bash
# Python server
cd main/xiaozhi-server
pip install -r requirements.txt
python app.py                   # WS :8000, HTTP :8003

# Java API
cd main/manager-api
mvn clean package -Dmaven.test.skip=true
java -jar target/xiaozhi-esp32-api-0.0.1.jar   # :8002/xiaozhi

# Web console
cd main/manager-web
npm install && npm run serve    # dev
npm run build                   # dist/

# Mobile
cd main/manager-mobile
pnpm install && pnpm dev:h5

# Docker (full stack)
./docker-setup.sh
```

## Configuration

- **Primary**: `main/xiaozhi-server/config.yaml` — server ports, auth_key, provider selection (ASR/LLM/TTS/VLLM/Memory/Intent), MQTT/UDP gateway, logging.
- **Secret override**: create `main/xiaozhi-server/data/.config.yaml` — this file is gitignored; put real API keys here, never in `config.yaml`.
- **Read-from-API mode**: server can sync config from `manager-api` at runtime (see `config/manage_api_client.py`).
- **Java**: `manager-api/src/main/resources/application.yml` (dev/test/prod profiles, i18n).
- **Frontend env**: `.env` files in `manager-web` / `manager-mobile` for build-time base URLs.

## Conventions

- **Python**: async-first; loguru with tag context (`logger.bind(tag=TAG)`); snake_case; provider pattern — each asr/tts/llm/etc. subdir exposes a factory + base class.
- **Java**: controller → service → dao layering; entity/DTO/VO separation; MyBatis-Plus mappers; Liquibase changelogs for schema migrations.
- **Vue 2 (manager-web)**: single-file components, Vuex modules per feature, element-ui UI kit.
- **Vue 3 (manager-mobile)**: Composition API, Pinia stores, Unocss utility classes.
- Commit style (see `git log`): `server(xiaozhi-server): <description>` — scoped prefix + concise imperative.

## Working on this repo

- When adding a new **provider** (ASR/TTS/LLM/etc.), follow the pattern in an existing sibling: new file under `core/providers/<kind>/<name>.py`, implement the base class, register in `core/providers/<kind>/__init__.py` factory.
- When adding a **plugin tool**, add a module in `plugins_func/functions/` and register it.
- Don't hand-edit generated artifacts (Maven `target/`, webpack `dist/`).
- The server reads from `data/.config.yaml` first — if you add a config key, document the default in `config.yaml` and reference it in the relevant `core/` module.
- Python logs use tagged structured logging; prefer `logger.bind(tag=TAG).info(...)` over raw prints.

## CI / releases

- `.github/workflows/docker-image.yml` builds multi-arch images for `xiaozhi-server` and `manager-web` on `v*.*.*` tags (or manual dispatch) and pushes to `ghcr.io`.
- `build-base-image.yml` prebuilds the Python base image used as a layer cache.
- Release flow: tag `vX.Y.Z`, push — images appear on ghcr.

## Branch policy for AI work

- Develop on `claude/add-claude-documentation-D9b9t` (or the branch specified in the task).
- Never push to `main` directly.
- PRs only when the user explicitly asks.

## Useful docs

- `docs/Deployment.md` — minimal single-module setup
- `docs/Deployment_all.md` — full stack (server + api + web + db)
- `docs/FAQ.md` — model selection, common issues
- `docs/mcp-*.md`, `docs/HomeAssistant-*.md`, `docs/Coze-*.md` — integration guides
