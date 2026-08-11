# AGENTS.md — EcoMart

Eco-friendly supermarket (siêu thị xanh). Client-server monorepo — there is **no root build tooling**; `client/` and `server/` are built independently.

## Architecture

- **client/** — Nuxt 3 + Nuxt UI + TS + Zod. App code (not lib); UI text and Zod error messages are Vietnamese.
- **server/** — Java 21, Spring Boot 3.4, Maven, Lombok, Spring Data JPA, PostgreSQL.
- **Infra** — `docker-compose.yml` runs all three (postgres, server, client). Single `.env` at repo root holds every secret.

### Key wiring (not obvious from filenames)

- **Auth is hand-rolled, no Nuxt auth module.** JWT lives in `localStorage` as `ecomart_session` + `ecomart_token`. `useAuth()` persists/restores it; every API call goes through the `useApi()` composable (`client/composables/useApi.ts`) which attaches the `Authorization: Bearer` header. Route guards: `middleware/auth.ts` (any logged-in) and `middleware/admin.ts` (ADMIN only).
- **Client types live in two places, keep both in sync with backend DTOs**: `client/types/index.ts` (TS interfaces mirroring backend responses) and `client/schemas/index.ts` (Zod validation schemas).
- **Server package layout**: controller → service per resource, 15 controllers each under `/api/...`. `integration/` holds Gemini and PayOS clients. `security/` is JWT + CORS config.
- **No DB migrations.** JPA `ddl-auto: update` in `server/src/main/resources/application.yml` — schema changes apply automatically on boot. Env vars fall back to localhost dev defaults via `${VAR:default}`.
- **Uploads** go to `UPLOAD_DIR` (server) via `UploadController`; in Docker it's the `uploads` volume.

## Commands

Must run inside `client/` or `server/` (no workspace root scripts).

**Client** (`./client`):
- Dev server: `npm run dev` (defaults to `http://localhost:3000`, API base `http://localhost:8080` from `NUXT_PUBLIC_API_BASE`)
- Typecheck: `npm run typecheck` (the only verification available; there is **no lint or test suite**)

**Server** (`./server`):
- `mvn spring-boot:run` (needs Postgres at `localhost:5432`, or via docker-compose)
- `mvn package` / `mvn test` (no tests currently — no `src/test`)

**Full stack** (repo root):
- `docker compose up --build` — reads `.env`, server uses `SPRING_DATASOURCE_*` env names (note: these differ from the `SPRING_DATASOURCE_URL`/`SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` keys in `docker-compose.yml`, which override defaults in `application.yml`).

## Gotchas

- **`.env` is required** and holds all secrets (JWT secret, Gemini, PayOS, Google OAuth). Copy `.env.example` → `.env` to bootstrap. There is **no `.gitignore` yet** — don't let `.env` get committed.
- UI is Vietnamese (`lang: vi`); keep new UI text and Zod messages in Vietnamese.
- Lombok is used — build under JDK 21 and ensure annotation processing is enabled in the IDE.
