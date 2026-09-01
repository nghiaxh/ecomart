# Kiến trúc EcoMart

Tài liệu mô tả cách hệ thống EcoMart vận hành: luồng dữ liệu, các thành phần, xác thực, thanh toán và những quy ước quan trọng.

## Tổng quan

EcoMart là ứng dụng **siêu thị trực tuyến** dạng client-server monorepo. Dữ liệu xuyên suốt theo ngữ cảnh **mua sắm tiện lợi**: sản phẩm đa dạng, đặt hàng nhanh và thanh toán linh hoạt.

```
┌──────────────────┐     HTTP/JSON (REST)     ┌──────────────────────┐
│  Nuxt 3 client   │ ───────────────────────▶ │  Spring Boot server  │
│  (Vue + Nuxt UI) │ ◀─────────────────────── │  Java 21 + JPA       │
└──────────────────┘   Authorization: Bearer  └──────────┬───────────┘
                                                         │
                                              PostgreSQL (ddl-auto: update)
```

- **client/** - Nuxt 3 + Nuxt UI + TypeScript + Zod. Giao diện tiếng Việt.
- **server/** - Spring Boot 3.4 + Spring Security (JWT) + Spring Data JPA. 15 controller, mỗi resource một controller → service → repository.
- **PostgreSQL** - không có migration; `ddl-auto: update` tự đồng bộ schema khi khởi động.

## Luồng dữ liệu chính

### 1. Xác thực - JWT tự viết (không dùng module auth của Nuxt)

Toàn bộ luồng login/đăng ký được làm thủ công:

1. Client gọi `POST /api/auth/login` (hoặc `/register`) qua composable `useAuth()`.
2. Server trả về `AuthResponse` gồm `token` (JWT) + thông tin user/role.
3. Client lưu token vào `localStorage` dưới hai khóa: `ecomart_session` (payload JSON) và `ecomart_token` (raw JWT).
4. Plugin `plugins/auth.client.ts` gọi `useAuth().restore()` khi khởi động để nạp lại phiên.
5. **Mọi** request API đều đi qua `useApi()` (`client/composables/useApi.ts`), tự đính header `Authorization: Bearer <token>`.
6. Server: `JwtAuthenticationFilter` đọc/verify token, dựng `Authentication`; `JwtTokenProvider` sinh/kiểm tra JWT; `SecurityConfig` tắt session (stateless) và hiện đang **cho phép tất cả** `/api/**` (`permitAll`) - quyền hạn được kiểm soát ở tầng service, không phải security filter.

**Bảo vệ route trên client** bằng middleware:
- `middleware/auth.ts` - yêu cầu đã đăng nhập.
- `middleware/admin.ts` - yêu cầu role `ADMIN`.

### 2. Duyệt & tìm sản phẩm

- `pages/index.vue` (trang chủ) gọi song song `GET /api/banners/active`, `GET /api/categories`, `GET /api/products/latest`.
- Trang danh mục/sản phẩm gọi `GET /api/products` với query params (filter theo `category` slug, tìm kiếm, sort) - phân trang dạng `PageResponse<T>`.
- Danh mục có cấu trúc **cây** (parent/children) - `children` dùng để hiển thị danh mục con.
- Sản phẩm mang `materials` (vật liệu + % thành phần) và `images`.

### 3. Giỏ hàng - state phía server

Giỏ hàng **không** lưu trong localStorage của trình duyệt; nó lưu trên server theo user (`Cart` + `CartItem` entities).

- `useCart()` (`client/composables/useCart.ts`) điều khiển giỏ qua `GET/POST/PUT/DELETE /api/cart`.
- Trạng thái giỏ được giữ bằng Nuxt `useState`, nạp lại mỗi khi đăng nhập.
- Chỉ hoạt động khi đã đăng nhập (`fetchCart` trả `null` nếu chưa login).

### 4. Đặt hàng & thanh toán

Luồng checkout (`pages/checkout.vue`):

1. Nạp danh sách địa chỉ (`GET /api/addresses`) + giỏ hàng (`useCart().fetchCart()`).
2. Người dùng chọn địa chỉ (có thể thêm mới, validate bằng `addressSchema` từ Zod) và phương thức thanh toán: **COD** hoặc **PayOS QR**.
3. `POST /api/orders/checkout` với `{ addressId, paymentMethod, notes }`.
4. Server dựng đơn hàng:
   - Nếu **COD** → tạo đơn `PENDING`.
   - Nếu **PayOS** → tạo đơn và trả về `payosCheckoutUrl`; client mở tab mới, người dùng quét mã QR chuyển khoản.
5. Sau khi thanh toán qua PayOS, PayOS gọi lại `POST /api/payments/payos/return?orderId=...` → server đánh dấu thanh toán đã trả (`confirmPayment`). Ngoài ra còn có `POST /api/orders/{id}/confirm-payment` để xác nhận thủ công.
6. Đơn hàng kèm `Payment` (method + status) và `OrderItem`.

Trạng thái đơn hàng: `PENDING → CONFIRMED → SHIPPING → COMPLETED | CANCELLED`.
Trạng thái thanh toán: `PENDING | PAID | FAILED | CANCELLED`.

### 5. Chat AI (Gemini)

- `GET /api/chat/sessions` - lấy các phiên hội thoại của user.
- `POST /api/chat/send` - gửi tin nhắn, `GeminiClient` (`integration/gemini/`) gọi Google Generative Language API.
- Gemini là **non-RAG**: không có retrieval; chỉ truyền prompt hệ thống + câu hỏi. Nếu thiếu `GEMINI_API_KEY`, trả về câu trả lời mặc định tiếng Việt.
- Tin nhắn lưu DB qua `ChatSession` + `ChatMessage`.

### 6. Thông báo

- **Server** có `NotificationController` (`GET /api/notifications`, `GET /api/notifications/unread-count`) và entity `Notification` lưu DB, gắn với user.
- Giao diện người dùng hiện chưa hiển thị danh sách thông báo (README mô tả "DB-based, poll" nhưng code client hiện chưa có component poll).

## Cấu trúc mã nguồn

### Server (`server/src/main/java/com/ecomart/`)

```
controller/  15 controller, mỗi resource một controller, mapping dưới /api/...
service/     nghiệp vụ chính - nơi kiểm soát quyền & logic
domain/
  entity/    JPA entities (User, Product, Order, Cart, Payment, ...)
  enums/     UserRole, OrderStatus, PaymentMethod, ...
repository/  Spring Data JPA repositories
dto/
  request/   payloads vào (LoginRequest, CheckoutRequest, ...)
  response/  payloads ra (AuthResponse, ProductResponse, OrderResponse, ...)
security/    JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, CorsConfig
integration/
  gemini/    GeminiClient - chat AI
  payos/     PayOSClient - thanh toán QR
config/      AppConfig, DataSeeder, properties (Jwt/Gemini/PayOS), security/filter bean
common/      Mapper (entity ↔ DTO), exception handling
exception/   xử lý lỗi API
```

### Client (`client/`)

```
pages/       các route (index, products, cart, checkout, orders, chat, account, admin/...)
components/  UI tái dùng (ProductCard, FooterGlobal)
composables/ useApi (mọi request), useAuth (phiên/JWT), useCart, useFormat, useStatusLabels
layouts/     default (public), auth, admin
middleware/  auth.ts (đã login), admin.ts (role ADMIN)
schemas/     Zod validation - thông báo lỗi tiếng Việt
types/       TS interfaces phản ánh DTO của backend
plugins/     auth.client.ts - khôi phục phiên khi load
```

## Điểm quan trọng khi làm việc

- **Đồng bộ types**: `client/types/index.ts` (TS) và `client/schemas/index.ts` (Zod) phải giữ song song với DTO backend. Thêm/sửa trường ở server → cập nhật cả hai.
- **Không có migration**: đổi entity JPA là đổi schema tự động khi khởi động. Với DB có sẵn dữ liệu, tránh xoá cột/đổi tên cột cũ đang được dùng.
- **Mọi request qua `useApi()`**: không gọi `$fetch` trực tiếp trong page - để đảm bảo header JWT luôn được đính.
- **Quyền ADMIN**: kiểm soát bằng middleware `admin.ts` ở client + logic ở service (backend chưa giới hạn theo role ở tầng HTTP).
- **DataSeeder** (`config/DataSeeder.java`) tự chạy khi DB trống: tạo admin `admin@ecomart.vn` / `Admin@123`, danh mục, vật liệu, banner, vài sản phẩm mẫu. Để reset dữ liệu demo, xoá volume `pgdata`.
- **Upload** đi qua `UploadController` lưu vào `UPLOAD_DIR`; trong Docker nằm trong volume `uploads`. Client quản lý ảnh qua URL (nhiều ảnh thực tế đang dùng URL Unsplash trong seeder).

## Môi trường & cấu hình

Mọi bí mật nằm trong **một file `.env` duy nhất ở root** (được `docker-compose.yml` và server đọc). Server đọc env với fallback mặc định dev (`${VAR:default}` trong `application.yml`).

| Var (server) | Ý nghĩa |
|--------------|---------|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | kết nối Postgres (docker-compose truyền dạng này) |
| `JWT_SECRET` / `JWT_EXPIRATION` | ký/giới hạn JWT |
| `GEMINI_API_KEY` | bật chat AI (để trống → fallback) |
| `PAYOS_CLIENT_ID` / `PAYOS_API_KEY` / `PAYOS_CHECKSUM_KEY` | thanh toán QR |
| `GOOGLE_CLIENT_ID` | Google OAuth |
| `UPLOAD_DIR` | thư mục lưu ảnh upload |
| `CLIENT_URL` | nguồn CORS hợp lệ |

Chạy độc lập (dev) cần Postgres tại `localhost:5432` hoặc toàn bộ stack qua `docker compose up --build`.
