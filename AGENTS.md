# AGENTS.md — EcoMart

Eco-friendly supermarket (siêu thị xanh). Client-server monorepo with no root build tooling. `client/` and `server/` are built independently.

## Architecture

- **client/** — Nuxt 4 + Nuxt UI 4 + TS + Zod 4. App code (not lib). UI text and Zod error messages are Vietnamese. Uses `app/` directory layout.
- **server/** — Java 25, Spring Boot 3.5, Maven, Lombok, Spring Data JPA, PostgreSQL.
- **Infra** — `docker-compose.yml` runs all three (postgres, server, client). Single `.env` at repo root holds every secret.

### Key wiring (not obvious from filenames)

- **Auth is hand-rolled, no Nuxt auth module.** JWT lives in `localStorage` as `ecomart_session` + `ecomart_token`. `useAuth()` persists/restores it. Every API call goes through the `useApi()` composable (`client/app/composables/useApi.ts`) which attaches the `Authorization: Bearer` header. Route guards: `middleware/auth.ts` (any logged-in), `middleware/admin.ts` (ADMIN only), `middleware/customer.ts` (logged-in non-admin).
- **Client types live in two places, keep both in sync with backend DTOs**: `client/app/types/index.ts` (TS interfaces mirroring backend responses) and `client/app/schemas/index.ts` (Zod validation schemas).
- **Server package layout**: controller → service per resource, 14 controllers each under `/api/...`. `integration/` holds PayOS client (chat runs in-process, keyword + simple RAG over `ProductRepository`). `security/` is JWT + CORS config.
- **No DB migrations.** JPA `ddl-auto: update` in `server/src/main/resources/application.yml`, schema changes apply automatically on boot. Env vars fall back to localhost dev defaults via `${VAR:default}`.
- **Uploads** go to `UPLOAD_DIR` (server) via `UploadController`. In Docker it's the `uploads` volume. Client currently uses URL-based images (Unsplash) from seeder.
- **`ChatWidget.vue`** is a floating chat widget rendered on all default-layout pages, providing quick access to the support chat beyond the dedicated `/chat` page.

## Commands

Must run inside `client/` or `server/` (no workspace root scripts).

**Client** (`./client`):
- Dev server: `npm run dev` (defaults to `http://localhost:5173`, API base `http://localhost:8080` from `NUXT_PUBLIC_API_BASE`)
- Typecheck: `npm run typecheck` (the only verification available; there is **no lint or test suite**)

**Server** (`./server`):
- `mvn spring-boot:run` (needs Postgres at `localhost:5432`, or via docker-compose)
- `mvn package` / `mvn test` (no tests currently, no `src/test`)

**Full stack** (repo root):
- `docker compose up --build` — reads `.env`. Server uses `SPRING_DATASOURCE_*` env names (note: these differ from the `SPRING_DATASOURCE_URL`/`SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` keys in `docker-compose.yml`, which override defaults in `application.yml`).

## Gotchas

- **`.env` is required** and holds all secrets (JWT secret, PayOS). Copy `.env.example` → `.env` to bootstrap. `.gitignore` exists at root and correctly ignores `.env`.
- UI is Vietnamese (`lang: vi`). Keep new UI text and Zod messages in Vietnamese.
- Lombok is used. Build under JDK 25 and ensure annotation processing is enabled in the IDE.
- **Admin protection is inconsistent** — `SecurityConfig` permits all `/api/**`; only `AdminController` uses `@PreAuthorize("hasRole('ADMIN')")`. Other admin endpoints rely on client middleware + service logic.
- **No Google OAuth** — `GOOGLE_CLIENT_ID` is declared in `application.yml` but not bound or used. Do not reference it as a working feature.
