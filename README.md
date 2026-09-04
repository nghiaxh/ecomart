# EcoMart

[![Nuxt](https://img.shields.io/badge/Nuxt-4-00dc82?logo=nuxtdotjs&logoColor=white)](https://nuxt.com)
[![Vue](https://img.shields.io/badge/Vue-3.5-4fc08d?logo=vuedotjs&logoColor=white)](https://vuejs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178c6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6db33f?logo=spring&logoColor=white)](https://spring.io)
[![Java](https://img.shields.io/badge/Java-25-f89820?logo=openjdk&logoColor=white)](https://www.java.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169e1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Zod](https://img.shields.io/badge/Zod-4-3e67b1?logo=zod&logoColor=white)](https://zod.dev)
[![Docker](https://img.shields.io/badge/Docker%20Compose-2496ed?logo=docker&logoColor=white)](https://www.docker.com)

Nền tảng mua sắm thực phẩm trực tuyến với sản phẩm tươi sạch, giao hàng nhanh và thanh toán tiện lợi cho mọi gia đình Việt.

## Tính năng

### Khách hàng
- Đăng ký / đăng nhập (email)
- Danh mục sản phẩm với bộ lọc, tìm kiếm, sắp xếp
- Chi tiết sản phẩm với đánh giá từ người dùng
- Giỏ hàng và thanh toán: **PayOS QR** hoặc **COD**
- Lịch sử đơn hàng, theo dõi và hủy đơn
- **Chat hỗ trợ** (từ khóa + RAG nội bộ) tư vấn mua sắm
- Hệ thống thông báo

### Quản trị
- Dashboard tổng quan
- Quản lý sản phẩm, danh mục và banner
- Quản lý đơn hàng và cập nhật trạng thái
- Quản lý tài khoản khách hàng

## Kiến trúc

Monorepo client-server, mỗi module build độc lập, không có build tooling ở root.

```
ecomart/
├── client/            # Nuxt 4 + Nuxt UI 4 + TypeScript + Zod 4
├── server/            # Spring Boot 3.5 + Java 25 + PostgreSQL
├── docker-compose.yml # chạy toàn bộ stack
├── .env               # cấu hình bí mật
├── .gitignore
└── ARCHITECTURE.md    # tài liệu kiến trúc chi tiết
```

Xem chi tiết luồng dữ liệu, xác thực JWT, thanh toán tại **[ARCHITECTURE.md](ARCHITECTURE.md)**.

## Bắt đầu nhanh

### Yêu cầu
- Node.js 20+
- JDK 25 / Maven
- Docker + Docker Compose (tùy chọn)

### 1. Cấu hình môi trường

```bash
cp .env.example .env
```

Điền giá trị thực vào `.env`: `JWT_SECRET`, `PAYOS_*`.

### 2. Chạy toàn bộ hệ thống (Docker)

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Client | http://localhost:5173 |
| Server API | http://localhost:8080/api |
| Database | localhost:5432 |

### Chạy dev riêng lẻ

**Client** (`./client`):
```bash
npm run dev        # http://localhost:5173
npm run typecheck  # kiểm tra type
```

**Server** (`./server`) cần Postgres tại `localhost:5432` (hoặc `docker compose up postgres`):
```bash
mvn spring-boot:run
mvn package
```

## Tài khoản demo

`DataSeeder` tự tạo dữ liệu mẫu khi DB trống, chỉ khi khởi động server không qua Docker hoặc với volume mới.

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| ADMIN | `admin@ecomart.vn` | `Admin@123` |
| Customer | `customer@ecomart.vn` | `Customer@123` |

## Công nghệ

| Layer | Stack |
|-------|-------|
| Client | Nuxt 4, Nuxt UI 4, Vue 3.5, TypeScript, Zod 4, Tailwind CSS |
| Server | Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, Lombok |
| Database | PostgreSQL 18 |
| Tích hợp | Chat từ khóa + RAG nội bộ, PayOS (thanh toán QR) |
| Hạ tầng | Docker Compose |

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
