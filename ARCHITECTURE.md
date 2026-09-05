# Kiến trúc EcoMart

Tài liệu mô tả cách hệ thống EcoMart vận hành: luồng dữ liệu, các thành phần, xác thực, thanh toán và những quy ước quan trọng.

## Tổng quan

EcoMart là ứng dụng **siêu thị trực tuyến** dạng client-server monorepo. Dữ liệu xuyên suốt theo ngữ cảnh **mua sắm tiện lợi**: sản phẩm đa dạng, đặt hàng nhanh và thanh toán linh hoạt.

```
┌──────────────────┐  same-origin /api (Nitro proxy)   ┌──────────────────────┐
│  Nuxt 4 client   │ ─────────────────────────────────▶ │  Spring Boot server  │
│  (Vue + Nuxt UI) │ ◀───────────────────────────────── │  Java 25 + JPA       │
└──────────────────┘    Authorization: Bearer (JWT)     └──────────┬───────────┘
                                                                   │
                                                   PostgreSQL (ddl-auto: update
                                                   + Flyway, baseline-on-migrate)
```

- **client/** — Nuxt 4 + Nuxt UI 4 + TypeScript + Zod 4. Giao diện tiếng Việt. Có Nitro proxy `/api` ở `client/server/routes/api/[...].ts`, đọc target từ `runtimeConfig.apiTarget` (env `NUXT_API_TARGET`, mặc định `http://localhost:8080`). Nếu đặt `NUXT_PUBLIC_API_BASE`, client gọi thẳng backend qua CORS (profile `client-dev` làm vậy).
- **server/** — Spring Boot 3.5 + Spring Security (JWT access + refresh) + Spring Data JPA. 14 controller, mỗi resource một controller → service → repository.
- **PostgreSQL** — `ddl-auto: update` đồng bộ schema khi khởi động. Flyway đã bật (`baseline-on-migrate`, `locations: classpath:db/migration`) nhưng chưa có script migration thật.

## Luồng dữ liệu chính

### 1. Xác thực (JWT access + refresh, tự viết)

Toàn bộ luồng login/đăng ký được làm thủ công:

1. Client gọi `POST /api/auth/login` với `{ identifier, password }` (`identifier` là email **hoặc** số điện thoại), hoặc `POST /api/auth/register`, qua composable `useAuth()`.
2. Server trả về `AuthResponse` gồm `token` (access JWT), `refreshToken`, `expiresIn` (giây) và thông tin user/role.
3. Client lưu vào storage hai khóa: `ecomart_session` (JSON, chứa cả `refreshToken`) và `ecomart_token` (raw access token). Nếu chọn "Ghi nhớ đăng nhập" → `localStorage`, ngược lại → `sessionStorage`. Cả hai khóa và event `ecomart:unauthorized` do module `app/utils/session-storage.ts` sở hữu chung cho `useApi`/`useAuth`.
4. Plugin `plugins/auth.client.ts` gọi `useAuth().restore()` khi khởi động để nạp lại phiên.
5. **Mọi** request API đều đi qua `useApi()` (`client/app/composables/useApi.ts`), tự đính header `Authorization: Bearer <token>`. Khi gặp 401 (ngoài `/api/auth/**`): `useApi` chạy refresh **một lần** (single-flight, dùng chung `refreshInflight` cho mọi request song song), retry request; vẫn 401 thì xoá phiên và `navigateTo('/login')`.
6. Server: `JwtAuthenticationFilter` đọc/verify access token, dựng `Authentication`; `JwtTokenProvider` sinh/kiểm tra JWT; `SecurityConfig` tắt session (stateless), cho phép công khai các endpoint đọc và bắt buộc `authenticated()` với phần còn lại (`anyRequest().authenticated()`). Lỗi 401/403 trả về JSON tiếng Việt.
7. **Refresh token xoay vòng (rotation)**: `POST /api/auth/refresh` nhận `refreshToken`, băm SHA-256 tra cứu trong bảng `refresh_tokens`, cấp access token mới **và** refresh token mới; token cũ bị đánh dấu đã thay (`replacedBy`) — dùng lại token cũ sẽ bị từ chối và thu hồi cả chuỗi. `POST /api/auth/logout` thu hồi refresh token của user.

Các endpoint công khai duy nhất (phần còn lại yêu cầu xác thực):

- `POST /api/auth/**` — login, register, refresh, logout
- `GET /api/products/**`, `/api/categories/**`, `/api/banners/active`, `/api/reviews`
- `POST /api/payments/payos/webhook`
- `/error`

**Bảo vệ route trên client** bằng middleware (chỉ là UX, không phải ranh giới bảo mật):
- `middleware/auth.ts` — yêu cầu đã đăng nhập.
- `middleware/admin.ts` — yêu cầu role `ADMIN`.
- `middleware/customer.ts` — yêu cầu đã đăng nhập và không phải admin.

### 2. Duyệt và tìm sản phẩm

- `pages/index.vue` (trang chủ) gọi song song `GET /api/banners/active`, `GET /api/categories`, `GET /api/products/latest`.
- Trang danh mục/sản phẩm gọi `GET /api/products` với query params (filter theo `category` slug, tìm kiếm, sort) — phân trang dạng `PageResponse<T>`.
- Danh mục có cấu trúc **cây** (parent/children) — `children` dùng để hiển thị danh mục con. Hiện có 3 danh mục gốc và 7 danh mục lá (gồm `Trái cây sấy`).
- Sản phẩm mang `materials` (vật liệu + % thành phần, map icon/màu theo union `MaterialType`) và `images` (gallery 1–3 ảnh, `displayOrder == 0` là ảnh chính). Sản phẩm `stock == 0` vẫn hiển thị với badge "Hết hàng" và nút thêm giỏ bị vô hiệu (`AddToCartButton`).

### 3. Giỏ hàng (state phía server)

Giỏ hàng **không** lưu trong localStorage của trình duyệt; nó lưu trên server theo user (`Cart` + `CartItem` entities).

- `useCart()` (`client/app/composables/useCart.ts`) điều khiển giỏ qua `GET/POST/PUT/DELETE /api/cart`. Thêm/sửa quá tồn kho trả 400 với message riêng: hết hàng (`"Sản phẩm đã hết hàng"`) hoặc còn ít (`"Số lượng vượt quá tồn kho, chỉ còn X"`).
- Trạng thái giỏ được giữ bằng Nuxt `useState`, nạp lại mỗi khi đăng nhập.
- Chỉ hoạt động khi đã đăng nhập (`fetchCart` trả `null` nếu chưa login).

### 4. Đặt hàng và thanh toán

Luồng checkout (`pages/checkout.vue`):

1. Nạp danh sách địa chỉ (`GET /api/addresses`) và giỏ hàng (`useCart().fetchCart()`).
2. Người dùng chọn địa chỉ (có thể thêm mới, validate bằng `addressSchema` từ Zod) và phương thức thanh toán: **COD** hoặc **PayOS QR**.
3. `POST /api/orders/checkout` với `{ addressId, paymentMethod, notes }` (`paymentMethod: PaymentMethod`, `status` cập nhật dùng `OrderStatus` — enum, không còn parse String thủ công).
4. Server dựng đơn hàng (`OrderService.checkout` tách nhỏ: `resolveCart/validateStock/buildOrder/applyStockDecrement/createPayment/createPayOSLink/clearCart`), phí ship lấy từ config `app.shop.shipping-fee` (mặc định 20000):
   - Nếu **COD** → tạo đơn `PENDING`.
   - Nếu **PayOS** → tạo đơn và trả về `payosCheckoutUrl`; client mở tab mới, người dùng quét mã QR chuyển khoản.
5. Xác nhận thanh toán PayOS qua **hai đường**: `POST /api/payments/payos/webhook` (PayOS gọi trực tiếp, `permitAll`, verify chữ ký + parse `orderCode` nằm trong `PaymentService.handleWebhook`) và `POST /api/payments/payos/return?orderId=...` (khi người dùng quay lại từ PayOS, qua `PaymentService.handleReturn`). Ngoài ra còn `POST /api/orders/{id}/confirm-payment` để xác nhận thủ công.
6. Đơn hàng kèm `Payment` (method + status) và `OrderItem`. Danh sách đơn của user: `GET /api/orders/mine`.

Trạng thái đơn hàng: `PENDING → CONFIRMED → SHIPPING → COMPLETED | CANCELLED`.
Trạng thái thanh toán: `PENDING | PAID | FAILED | CANCELLED`.

### 5. Chat hỗ trợ (từ khóa + RAG đơn giản)

- `GET /api/chat/sessions` — lấy các phiên hội thoại của user.
- `POST /api/chat/send` — gửi tin nhắn, `ChatService` xử lý nội bộ (không gọi API ngoài):
  - Nhận diện **ý định** theo từ khóa chuẩn hóa (giá/khuyến mãi, giao hàng, đổi trả, thanh toán, liên hệ, tài khoản...).
  - **RAG đơn giản**: truy vấn `ProductRepository.searchActiveByKeyword` theo từng token (giới hạn 3 token × 20 kết quả) rồi chấm điểm in-memory trên tên/danh mục/mô tả, lấy tối đa 5 — không còn tải toàn bộ catalog mỗi tin nhắn.
- Tin nhắn lưu DB qua `ChatSession` + `ChatMessage`.
- `ChatWidget.vue` là widget nổi hiển thị trên tất cả các trang dùng layout mặc định, cung cấp truy cập nhanh vào chat ngoài trang `/chat` chuyên biệt.

### 6. Thông báo

- **Server** có `NotificationController` (`GET /api/notifications`, `GET /api/notifications/unread-count`) và entity `Notification` lưu DB, gắn với user, đã `@PreAuthorize("isAuthenticated()")`.
- Giao diện người dùng hiện chưa hiển thị danh sách thông báo (chưa có component poll).

## Cấu trúc mã nguồn

### Server (`server/src/main/java/com/ecomart/`)

```
controller/  14 controller, mỗi resource một controller mỏng (logic nằm ở service), mapping dưới /api/...
service/     nghiệp vụ chính, kiểm soát quyền và logic (chat + RAG ở ChatService/ChatBot,
             webhook PayOS ở PaymentService, phí ship + thống kê có typed config/DTO)
domain/
  entity/    JPA entities (User, Customer, Admin, Product, Category, Banner, Material,
             ProductImage, ProductMaterial, Cart, CartItem, Order, OrderItem, Payment,
             Review, Address, RefreshToken, Notification, ChatSession, ChatMessage, ...)
  enums/     UserRole, OrderStatus, PaymentMethod, PaymentStatus, MaterialType, ChatRole (chỉ USER/BOT), ...
repository/  Spring Data JPA repositories (truy vấn tìm kiếm có kiểu qua @Query, không còn Specification string-path)
dto/
  request/   record payloads vào (LoginRequest, RegisterRequest, RefreshTokenRequest,
             CheckoutRequest, AddToCartRequest, ProductRequest, CategoryRequest, BannerRequest, ...)
  response/  payloads ra (AuthResponse, ProductResponse, OrderResponse, PageResponse, AdminDashboardResponse, ...)
security/    JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, CorsConfig, UserDetailsServiceImpl
integration/
  payos/     PayOSClient — thanh toán QR
config/      AppConfig, WebConfig, RestTemplateConfig (timeout), DataSeeder, JwtProperties, PayOSProperties, ShopProperties
common/      Mapper (entity ↔ DTO hai chiều: toXxx + merge), exception handling
exception/   xử lý lỗi API (401 hết phiên ở AuthService, 403 cấm truy cập bằng AccessDeniedException)
```

Migrations: `server/src/main/resources/db/migration/` (Flyway directories; hiện chỉ chứa `.gitkeep`).

### Client (`client/`)

```
app/
  pages/             guest: index, login, register, products, products/[slug];
                     user: cart, checkout, orders, orders/[id], account, chat;
                     admin/: index, products, categories, orders, banners
  components/        ProductCard, FooterGlobal, ChatWidget, ChatThread, AuthShell, SectionHeader, Reveal,
                     UiImg, PaginationBar, OrderSummaryCard, AddressForm, AddressCard,
                     AddToCartButton, PasswordInput, ConfirmDialog
  composables/       useApi (mọi request + auto-refresh), useAuth (phiên/JWT),
                     useCart, useFormat, useStatusLabels, useFormErrors, useConfirm
  utils/             session-storage (sở hữu khóa ecomart_session/ecomart_token + event unauthorized)
  data/              home.ts (dữ liệu marketing tĩnh trang chủ)
  layouts/           default (public, gồm FooterGlobal), admin (trang auth dùng layout: false)
  middleware/        auth.ts (đã login), admin.ts (role ADMIN), customer.ts (đã login, không phải admin)
  schemas/           Zod validation — thông báo lỗi tiếng Việt (kèm form types suy ra từ schema)
  types/             TS interfaces phản ánh DTO của backend
  plugins/           auth.client.ts — khôi phục phiên khi load (chạy trước middleware)
  assets/css/        main.css
server/routes/api/[...].ts   Nitro proxy /api → backend (runtimeConfig.apiTarget)
```

## Điểm quan trọng khi làm việc

- **Đồng bộ types**: `client/app/types/index.ts` (TS) và `client/app/schemas/index.ts` (Zod) phải giữ song song với DTO backend. Thêm/sửa trường ở server → cập nhật cả hai.
- **Flyway vs ddl-auto**: Flyway đã bật (`enabled`, `baseline-on-migrate: true`, `locations: classpath:db/migration`) nhưng thư mục migration còn trống — schema vẫn do `ddl-auto: update` quản lý (`JPA_DDL_AUTO` ghi đè mặc định). Khi thêm migration thật, đặt file trong `server/src/main/resources/db/migration`. Với DB có sẵn dữ liệu, tránh xoá/đổi tên cột đang được dùng.
- **Mọi request qua `useApi()`**: không gọi `$fetch` trực tiếp trong page để đảm bảo header JWT luôn được đính và cơ chế auto-refresh hoạt động.
- **Quyền ADMIN kiểm soát ở server**: `SecurityConfig` bắt buộc xác thực tại tầng HTTP (`anyRequest().authenticated()`), admin write dùng `@PreAuthorize("hasRole('ADMIN')")`, controller user-scoped dùng `@PreAuthorize("isAuthenticated()")`. Hết phiên/refresh lỗi → 401 (`UnauthorizedException`, chỉ ở `AuthService` + webhook PayOS chưa auth); đã login nhưng đụng tài nguyên người khác → 403 (`AccessDeniedException`). Middleware client chỉ là UX.
- **Trang công khai dùng `useAsyncData`** (`index`, `products`, `products/[slug]` — SSR + SEO); trang đã xác thực/admin giữ fetch client (`onMounted`). Filter `products` đồng bộ 2 chiều với URL query.
- **DataSeeder** (`config/DataSeeder.java`) idempotent theo slug/tên (DB đã seed vẫn nhận hàng mới khi boot lại): tạo admin `admin@ecomart.vn`, customer `customer@ecomart.vn`, danh mục (3 gốc + 7 lá), vật liệu, banner, ~45 sản phẩm mẫu (mỗi SP 1–2 ảnh WebP trong `client/public/images/products/`, ít nhất 2 SP hết hàng `stock = 0` để e2e `cart-robustness` chạy thật). Vật liệu seed map theo nhóm danh mục (rau → Lá chuối, củ quả/ngũ cốc → Giấy, ...). Mật khẩu demo mặc định `Admin@123` / `Customer@123`, ghi đè qua `SEED_ADMIN_PASSWORD` / `SEED_CUSTOMER_PASSWORD`. Để reset dữ liệu demo, xoá volume `pgdata`.
- **Ảnh sản phẩm/banner** là file tĩnh trong `client/public/images/` (`products/<slug>-<n>.webp`, `banners/`), seed trong `DataSeeder` trỏ đường dẫn tương đối `/images/...`. Không còn endpoint upload server — admin thêm ảnh bằng URL trong form.

## Môi trường và cấu hình

Mọi bí mật nằm trong **một file `.env` duy nhất ở root** (được `docker-compose.yml` và server đọc). Server đọc env với fallback mặc định dev (`${VAR:default}` trong `application.yml`). Lưu ý `mvn spring-boot:run` không tự nạp `.env` — phải export vars hoặc chạy qua compose.

| Var (server) | Ý nghĩa |
|--------------|---------|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | kết nối Postgres (docker-compose truyền dạng này) |
| `JWT_SECRET` / `JWT_ACCESS_EXPIRATION` / `JWT_REFRESH_EXPIRATION` | ký + giới hạn access/refresh token |
| `PAYOS_CLIENT_ID` / `PAYOS_API_KEY` / `PAYOS_CHECKSUM_KEY` | thanh toán QR |
| `PAYOS_RETURN_URL` / `PAYOS_CANCEL_URL` | URL chuyển hướng trả về/huỷ từ PayOS |
| `CLIENT_URL` | nguồn CORS hợp lệ |
| `SEED_ENABLED` / `SEED_ADMIN_PASSWORD` / `SEED_CUSTOMER_PASSWORD` | bật/tắt + mật khẩu tài khoản demo |
| `SHIPPING_FEE` | phí giao hàng (`app.shop.shipping-fee`, mặc định 20000) |
| `HTTP_CONNECT_TIMEOUT_MS` / `HTTP_READ_TIMEOUT_MS` | timeout RestTemplate gọi PayOS |
| `FLYWAY_ENABLED` / `JPA_DDL_AUTO` | Flyway / cách đồng bộ schema (`ddl-auto`) |
| `GOOGLE_CLIENT_ID` | khai báo nhưng chưa bound/không dùng — không coi là tính năng hoạt động |

Biến chỉ dùng trong `docker-compose.yml`:

| Var | Ý nghĩa |
|-----|---------|
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` / `DB_PORT` | cấu hình service postgres |
| `NUXT_API_TARGET` | nơi Nitro proxy chuyển `/api` tới (prod: `http://server:8080`) |
| `NUXT_PUBLIC_API_BASE` | nếu đặt, client gọi thẳng backend qua CORS, bỏ proxy (`client-dev`) |

Chạy độc lập (dev): cần Postgres tại `localhost:5432` và nạp các biến từ `.env` cho `mvn spring-boot:run`. Hoặc chạy toàn bộ stack: `docker compose --profile prod up --build` (sản phẩm) hoặc `docker compose --profile dev up` (hot-reload, volume mount).