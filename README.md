# EcoMart

[![Vite](https://img.shields.io/badge/Vite-8-646cff?logo=vite&logoColor=white)](https://vitejs.dev)
[![Vue](https://img.shields.io/badge/Vue-3.5-4fc08d?logo=vuedotjs&logoColor=white)](https://vuejs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178c6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6db33f?logo=spring&logoColor=white)](https://spring.io)
[![Java](https://img.shields.io/badge/Java-25-f89820?logo=openjdk&logoColor=white)](https://www.java.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169e1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Zod](https://img.shields.io/badge/Zod-4.5-3e67b1?logo=zod&logoColor=white)](https://zod.dev)
[![Docker](https://img.shields.io/badge/Docker%20Compose-2496ed?logo=docker&logoColor=white)](https://www.docker.com)
[![Naive UI](https://img.shields.io/badge/Naive%20UI-2.45-18a058)](https://www.naiveui.com)
[![Vitest](https://img.shields.io/badge/Vitest-4.1-FCC72B?logo=vitest&logoColor=black)](https://vitest.dev)
[![Playwright](https://img.shields.io/badge/Playwright-1.62-2EAD33?logo=playwright&logoColor=white)](https://playwright.dev)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-1.21-2496ED)](https://java.testcontainers.org)
[![Flyway](https://img.shields.io/badge/Flyway-11.7-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org)

Nền tảng mua sắm thực phẩm trực tuyến với sản phẩm tươi sạch, giao hàng nhanh và thanh toán tiện lợi cho mọi gia đình Việt.

## Tính năng

### Khách hàng
- Đăng nhập hoặc đăng ký bằng email hoặc số điện thoại. Phiên dùng JWT access và refresh
- Duyệt danh mục, tìm kiếm và lọc sản phẩm
- Chi tiết sản phẩm với nhiều ảnh, đánh giá và badge hết hàng
- Giỏ hàng, thanh toán mã QR PayOS hoặc COD
- Theo dõi và hủy đơn hàng

### Quản trị
- Dashboard tổng quan và thống kê bán hàng
- Quản lý sản phẩm, danh mục và đơn hàng
- Quản lý tài khoản người dùng

## Bắt đầu nhanh

### Yêu cầu
- Node.js 20.19 trở lên hoặc 22.12 trở lên
- JDK 25 và Maven
- Docker và Docker Compose (tùy chọn)

### 1. Cấu hình môi trường

```bash
cp .env.example .env
```

Điền giá trị thực cho `JWT_SECRET` và các biến `PAYOS_*`.

### 2. Chạy hệ thống bằng Docker

Production:

```bash
docker compose --profile prod up --build
```

Development:

```bash
docker compose --profile dev up --build
```

| Thành phần | URL |
|------------|-----|
| Client (prod) | http://localhost:80 |
| Client (dev) | http://localhost:5173 |
| Server API | http://localhost:8080/api |
| Database | localhost:5432 |

Lệnh `docker compose up --build` không kèm profile chỉ khởi động Postgres. Cơ chế reload của profile dev được mô tả trong [ARCHITECTURE.md](ARCHITECTURE.md).

### 3. Chạy riêng lẻ

Client (trong `./client`):

```bash
npm run dev
npm run typecheck
npm test
```

Server (trong `./server`, cần Postgres tại `localhost:5432` và các biến từ `.env`):

```bash
mvn spring-boot:run
mvn package
```

## Tài khoản demo

Server tự tạo dữ liệu mẫu khi khởi động theo cách idempotent, không nhân đôi khi chạy lại.

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| ADMIN | `admin@ecomart.vn` | `Admin@123` |
| Customer | `customer@ecomart.vn` | `Customer@123` |

Ghi đè mật khẩu bằng `SEED_ADMIN_PASSWORD` và `SEED_CUSTOMER_PASSWORD`. Tắt seed bằng `SEED_ENABLED=false`. Xoá volume `pgdata` để reset dữ liệu.

## Công nghệ

| Tầng | Công nghệ |
|------|-----------|
| Client | Vue 3, Vite 8, Vue Router 5, Naive UI, TypeScript, Zod 4, Tailwind CSS, Axios, Chart.js |
| Server | Spring Boot 3.5, Spring Security, Spring Data JPA, Lombok, Flyway |
| Database | PostgreSQL 18 |
| Thanh toán | PayOS (mã QR và webhook), COD |
| Hạ tầng | Docker Compose |
| Kiểm thử | Vitest, JUnit, Testcontainers, Playwright |

## Kiểm thử

- Client: `npm run typecheck` và `npm test`
- Server: `mvn test` (Testcontainers cần Docker)
- End to end: Playwright trong `e2e/`

Xem [ARCHITECTURE.md](ARCHITECTURE.md) để nắm kiến trúc, luồng dữ liệu, xác thực và cấu hình chi tiết.