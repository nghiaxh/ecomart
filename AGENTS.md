# AGENTS.md — EcoMart

Eco-friendly supermarket (siêu thị xanh). Client-server monorepo with no root build tooling. `client/` and `server/` are built independently.

## Architecture

- **client/** — Nuxt 4 + Nuxt UI 4 + TS + Zod 4. App code (not lib). UI text and Zod error messages are Vietnamese. Uses `app/` directory layout. Ships a Nitro API proxy at `client/server/routes/api/[...].ts`.
- **server/** — Java 25, Spring Boot 3.5, Maven, Lombok, Spring Data JPA, PostgreSQL, Flyway.
- **Infra** — `docker-compose.yml` runs postgres and two profiles: `prod` (server + client) and `dev` (`server-dev` + `client-dev` with volume mounts). Single `.env` at repo root holds every secret. Note: plain `mvn spring-boot:run` does **not** read `.env` — load the vars yourself or use compose.

### Key wiring (not obvious from filenames)

- **Auth is hand-rolled, no Nuxt auth module.** JWT is **access + refresh**. `useAuth()` persists `ecomart_session` (JSON, includes `refreshToken`) + `ecomart_token` (raw access JWT) to `localStorage` when "remember me" is on, otherwise `sessionStorage`. `POST /api/auth/refresh` rotates the refresh token (SHA-256 hash stored in `refresh_tokens`, old one revoked); `POST /api/auth/logout` revokes. Every API call goes through `useApi()` (`client/app/composables/useApi.ts`): it attaches `Authorization: Bearer`, and on a 401 (outside `/api/auth/**`) runs a single-flight refresh, retries once, then clears session and redirects to `/login`. Route guards: `middleware/auth.ts` (any logged-in), `middleware/admin.ts` (ADMIN only), `middleware/customer.ts` (logged-in non-admin). Login uses `{ identifier, password }` (email or phone).
- **API routing**: by default `NUXT_PUBLIC_API_BASE` is empty, so `useApi()` calls same-origin `/api/...` and the Nitro proxy forwards to `NUXT_API_TARGET` (default `http://localhost:8080`). Set `NUXT_PUBLIC_API_BASE` to bypass the proxy and call the backend directly (client-dev compose does this).
- **Client types live in two places, keep both in sync with backend DTOs**: `client/app/types/index.ts` (TS interfaces mirroring backend responses) and `client/app/schemas/index.ts` (Zod validation schemas).
- **Server package layout**: controller → service per resource, 14 controllers each under `/api/...`. `integration/` holds the PayOS client (chat runs in-process in `service/`: keyword intent matching + simple RAG over `ProductRepository`). `security/` is JWT filter/provider + CORS + `SecurityConfig`, which enforces authentication at the HTTP layer.
- **DB schema**: `ddl-auto: update` by default (`JPA_DDL_AUTO` overrides). Flyway is wired (`enabled`, `baseline-on-migrate`, `locations: classpath:db/migration`) but the migration dir is currently empty — no real migrations yet. Prefer adding Flyway scripts over relying on `ddl-auto` when you can.
- **Uploads** go to `UPLOAD_DIR` (server) via `UploadController` (`@PreAuthorize("hasRole('ADMIN')")`). In Docker it's the `uploads` volume. Product/banner images come from URL-based Unsplash in the seeder; `client/public/images/` holds static UI art (hero, steps, features) only.
- **`ChatWidget.vue`** is a floating chat widget rendered on all default-layout pages, providing quick access to the support chat beyond the dedicated `/chat` page.

## Commands

Must run inside `client/` or `server/` (no workspace root scripts).

**Client** (`./client`):
- Dev server: `npm run dev` (http://localhost:5173; `/api` proxies to `http://localhost:8080` via `NUXT_API_TARGET`)
- Typecheck: `npm run typecheck`
- Tests: `npm test` (Vitest + `@nuxt/test-utils`). There is **no lint** target.

**Server** (`./server`):
- `mvn spring-boot:run` (needs Postgres at `localhost:5432` and env vars — see JWT/PAYOS keys in `.env`; not auto-loaded)
- `mvn package` / `mvn test` (JUnit + Testcontainers; tests need Docker, `src/test/resources/application.yml` disables Flyway and uses `create-drop`)
- Or dev via compose: `docker compose --profile dev up` (postgres + `server-dev` with `m2-cache`)

**Full stack** (repo root):
- Prod: `docker compose --profile prod up --build`
- Dev: `docker compose --profile dev up`
- Plain `docker compose up --build` only starts postgres (server/client run under profiles). Server env names in compose match `application.yml`: `SPRING_DATASOURCE_URL`/`_USERNAME`/`_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION`, `PAYOS_*`, `SEED_*`, `UPLOAD_DIR`, `CLIENT_URL`.

## Gotchas

- **`.env` is required** and holds all secrets (JWT, PayOS, DB). Copy `.env.example` → `.env` to bootstrap. `.gitignore` exists at root and correctly ignores `.env`.
- UI is Vietnamese (`lang: vi`). Keep new UI text and Zod messages in Vietnamese.
- Lombok is used. Build under JDK 25 and ensure annotation processing is enabled in the IDE.
- **Authorization is enforced server-side**: `SecurityConfig` permits only public reads (POST `/api/auth/**`, GET products/categories/banners/active/reviews, POST PayOS `/webhook`, GET `/uploads/**`); everything else is `authenticated()`. Admin write endpoints use `@PreAuthorize("hasRole('ADMIN')")`; user-scoped controllers use `@PreAuthorize("isAuthenticated()")`. Client middleware is UX only, not a security boundary.
- **No Google OAuth** — `GOOGLE_CLIENT_ID` is declared in `application.yml` but not bound or used. Do not reference it as a working feature.
- **Testing is expected**: client Vitest, server JUnit + Testcontainers, `e2e/` Playwright. Don't leave docs bumping their features without touching tests.