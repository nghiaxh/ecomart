# EcoMart

[![Nuxt](https://img.shields.io/badge/Nuxt-4.5-00dc82?logo=nuxtdotjs&logoColor=white)](https://nuxt.com)
[![Vue](https://img.shields.io/badge/Vue-3.5-4fc08d?logo=vuedotjs&logoColor=white)](https://vuejs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178c6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6db33f?logo=spring&logoColor=white)](https://spring.io)
[![Java](https://img.shields.io/badge/Java-25-f89820?logo=openjdk&logoColor=white)](https://www.java.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169e1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Zod](https://img.shields.io/badge/Zod-4.5-3e67b1?logo=zod&logoColor=white)](https://zod.dev)
[![Docker](https://img.shields.io/badge/Docker%20Compose-2496ed?logo=docker&logoColor=white)](https://www.docker.com)
[![Nuxt UI](https://img.shields.io/badge/Nuxt%20UI-4.11-48bb78?logo=nuxtdotjs&logoColor=white)](https://ui.nuxt.com)
[![Vitest](https://img.shields.io/badge/Vitest-4.1-FCC72B?logo=vitest&logoColor=black)](https://vitest.dev)
[![Playwright](https://img.shields.io/badge/Playwright-1.62-2EAD33?logo=playwright&logoColor=white)](https://playwright.dev)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-1.21-2496ED)](https://java.testcontainers.org)
[![Flyway](https://img.shields.io/badge/Flyway-11.7-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org)

Nền tảng mua sắm thực phẩm trực tuyến với sản phẩm tươi sạch, giao hàng nhanh và thanh toán tiện lợi cho mọi gia đình Việt.

## Tính năng

### Khách hàng
- Đăng ký / đăng nhập bằng email hoặc số điện thoại, ghi nhớ phiên (JWT access + refresh)
- Danh mục sản phẩm với bộ lọc, tìm kiếm, sắp xếp
- Chi tiết sản phẩm với gallery nhiều ảnh, đánh giá từ người dùng, badge "Hết hàng" khi hết tồn kho
- Giỏ hàng và thanh toán: **PayOS QR** hoặc **COD**
- Lịch sử đơn hàng, theo dõi và hủy đơn
- **Chat hỗ trợ** (từ khóa + RAG nội bộ) tư vấn mua sắm
- Hệ thống thông báo

### Quản trị
- Dashboard tổng quan
- Quản lý sản phẩm, danh mục và banner
- Quản lý đơn hàng và cập nhật trạng thái

## Kiến trúc

Monorepo client-server, mỗi module build độc lập, không có build tooling ở root.

```
ecomart/
├── client/            # Nuxt 4 + Nuxt UI 4 + TypeScript + Zod 4
├── server/            # Spring Boot 3.5 + Java 25 + PostgreSQL + Flyway
├── e2e/               # Playwright end-to-end
├── docker-compose.yml # chạy toàn bộ stack (profiles: prod / dev)
├── .env               # cấu hình bí mật
├── .gitignore
└── ARCHITECTURE.md    # tài liệu kiến trúc chi tiết
```

Xem chi tiết luồng dữ liệu, xác thực JWT, thanh toán tại **[ARCHITECTURE.md](ARCHITECTURE.md)**.

## Bắt đầu nhanh

### Yêu cầu
- Node.js 20.19+ / 22.12+ (Nuxt 4)
- JDK 25 / Maven
- Docker + Docker Compose (tùy chọn)

### 1. Cấu hình môi trường

```bash
cp .env.example .env
```

Điền giá trị thực vào `.env`: `JWT_SECRET`, `PAYOS_*`.

### 2. Chạy toàn bộ hệ thống (Docker)

```bash
docker compose --profile prod up --build
```

| Service | URL |
|---------|-----|
| Client | http://localhost:5173 |
| Server API | http://localhost:8080/api |
| Database | localhost:5432 |

`server`, `client` chạy dưới profile `prod` (`docker compose up --build` không tham số chỉ khởi động postgres). Với profile `dev`, máy chạy `server-dev` + `client-dev` có volume mount để hot-reload:

```bash
docker compose --profile dev up
```

Profile `dev` tự reload khi sửa code: server restart nhờ DevTools + watcher (`server/dev-watch.sh`, chỉ `--build` lại khi đổi `pom.xml`/`Dockerfile.dev`), client hot-reload qua HMR (bật polling `CHOKIDAR_USEPOLLING`).

### Chạy dev riêng lẻ

**Client** (`./client`):
```bash
npm run dev        # http://localhost:5173; /api chuyển tiếp tới backend qua Nitro proxy
npm run typecheck  # kiểm tra type
npm test           # test đơn vị (Vitest)
```

**Server** (`./server`) cần Postgres tại `localhost:5432` (hoặc `docker compose up postgres`):
```bash
mvn spring-boot:run
mvn package
```
Lưu ý: `mvn spring-boot:run` **không** tự đọc `.env` — cần nạp các biến môi trường từ `.env` (JWT_SECRET, PAYOS_*, DB_*) hoặc chạy qua `docker compose --profile dev up`.

## Tài khoản demo

`DataSeeder` tạo dữ liệu mẫu idempotent theo slug/tên (DB đã seed vẫn nhận hàng mới khi boot lại, không cần xoá volume), với mật khẩu demo (mặc định bên dưới, có thể ghi đè qua `SEED_ADMIN_PASSWORD` / `SEED_CUSTOMER_PASSWORD`). Tắt bằng `SEED_ENABLED=false`, reset bằng cách xoá volume `pgdata`. Seed gồm ~45 sản phẩm (mỗi SP 1–3 ảnh), 7 danh mục lá và ít nhất 2 sản phẩm hết hàng để kiểm thử luồng hết hàng.

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| ADMIN | `admin@ecomart.vn` | `Admin@123` |
| Customer | `customer@ecomart.vn` | `Customer@123` |

## Công nghệ

| Layer | Stack |
|-------|-------|
| Client | Nuxt 4, Nuxt UI 4, Vue 3.5, TypeScript, Zod 4, Tailwind CSS, Nitro proxy |
| Server | Spring Boot 3.5, Spring Security (JWT access + refresh), Spring Data JPA, Lombok, Flyway |
| Database | PostgreSQL 18 |
| Tích hợp | Chat từ khóa + RAG nội bộ, PayOS (thanh toán QR + webhook) |
| Hạ tầng | Docker Compose (profiles prod/dev) |
| Kiểm thử | Vitest (client), JUnit + Testcontainers (server), Playwright (e2e) |

## Kiểm thử

Mỗi tầng test chạy từ thư mục riêng, không có script ở root.

**Client** (`./client`):
```bash
npm run typecheck  # kiểm tra type
npm test           # test đơn vị (Vitest)
```

**Server** (`./server`):
```bash
mvn test # test đơn vị (JUnit + Mockito) và integration (Testcontainers, cần Docker)
```

**End to end** (`./e2e`): Playwright, nhắm vào stack đang chạy tại `http://127.0.0.1:5173`.
```bash
npx playwright install chromium
npm test
```

Bài viết [ARCHITECTURE.md](ARCHITECTURE.md) có chi tiết về Flyway và hạ tầng.
