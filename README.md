# 🌿 EcoMart

[![Nuxt](https://img.shields.io/badge/Nuxt-3-00dc82?logo=nuxtdotjs&logoColor=white)](https://nuxt.com)
[![Vue](https://img.shields.io/badge/Vue-3-4fc08d?logo=vuedotjs&logoColor=white)](https://vuejs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178c6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6db33f?logo=spring&logoColor=white)](https://spring.io)
[![Java](https://img.shields.io/badge/Java-21-f89820?logo=openjdk&logoColor=white)](https://www.java.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169e1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Zod](https://img.shields.io/badge/Zod-3-3e67b1?logo=zod&logoColor=white)](https://zod.dev)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed?logo=docker&logoColor=white)](https://www.docker.com)

**Siêu thị xanh trực tuyến** — nền tảng bán thực phẩm sạch, hữu cơ và bền vững. Khi mua sắm, hệ thống ghi nhận lượng **CO₂ tiết kiệm** và tích lũy **Eco Points** — mỗi giao dịch đều góp phần bảo vệ môi trường.

## ✨ Tính năng

### Khách hàng
- Đăng ký / đăng nhập (email + Google OAuth)
- Danh mục sản phẩm (cây), lọc / tìm kiếm / sắp xếp
- Chi tiết sản phẩm: chỉ số carbon, vật liệu, đánh giá
- Giỏ hàng
- Thanh toán: **PayOS QR** hoặc **COD**
- Lịch sử đơn hàng, hủy / theo dõi trạng thái
- **Eco Wallet** — tích điểm theo CO₂ tiết kiệm
- **Chat AI** (Google Gemini) tư vấn sản phẩm xanh
- Thông báo

### Quản trị (ADMIN)
- Dashboard tổng quan
- Quản lý sản phẩm / danh mục / banner
- Quản lý đơn hàng + cập nhật trạng thái
- Quản lý khách hàng

## 🏗️ Kiến trúc

Monorepo client-server — không có build tooling ở root, `client/` và `server/` build độc lập.

```
ecomart/
├── client/            # Nuxt 3 + Nuxt UI + TypeScript + Zod
├── server/            # Spring Boot 3.4 + Java 21 + PostgreSQL
├── docker-compose.yml # chạy toàn bộ (postgres, server, client)
├── .env               # một file cấu hình chứa mọi secrets
└── ARCHITECTURE.md    # tài liệu kiến trúc chi tiết
```

Xem chi tiết luồng dữ liệu, xác thực JWT, thanh toán,... tại **[ARCHITECTURE.md](ARCHITECTURE.md)**.

## 🚀 Bắt đầu nhanh

### Yêu cầu
- Node.js 20+
- JDK 21 (cho server) / Maven
- Docker + Docker Compose (tùy chọn)

### 1. Cấu hình môi trường

```bash
cp .env.example .env
```

Điền các giá trị thực vào `.env`: `JWT_SECRET`, `GEMINI_API_KEY`, `PAYOS_*` (bắt buộc cho thanh toán), `GOOGLE_CLIENT_ID`.

### 2. Chạy toàn bộ hệ thống (Docker)

```bash
docker compose up --build
```

- **Client**: http://localhost:3000
- **Server API**: http://localhost:8080/api
- **Database**: localhost:5432

> ⚠️ `.env` chứa toàn bộ bí mật — **không commit file này** vào git (repo chưa có `.gitignore`).

### Chạy dev riêng lẻ

**Client** (`./client`):
```bash
npm run dev        # http://localhost:3000
npm run typecheck  # kiểm tra type (là verification duy nhất; không có lint/test)
```

**Server** (`./server`) — cần Postgres tại `localhost:5432` (hoặc `docker compose up postgres`):
```bash
mvn spring-boot:run
mvn package        # đóng gói jar
```

## 🧪 Tài khoản demo

`DataSeeder` tự tạo khi DB trống — chỉ khi khởi động **server không qua Docker hoặc với volume mới**:

| Vai trò | Email | Mật khẩu |
|---------|-------|----------|
| ADMIN | `admin@ecomart.vn` | `Admin@123` |

## 🛠️ Công nghệ

- **Client**: Nuxt 3, Nuxt UI, Vue 3, TypeScript, Zod, Tailwind CSS
- **Server**: Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, Lombok
- **Dữ liệu**: PostgreSQL 16
- **Tích hợp**: Google Gemini (chat), PayOS (thanh toán QR), Google OAuth
- **Hạ tầng**: Docker Compose

## 📄 Giấy phép

Chưa xác định.
