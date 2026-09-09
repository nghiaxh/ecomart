# Kiến trúc EcoMart

Tài liệu mô tả cách hệ thống EcoMart vận hành: luồng dữ liệu, các thành phần, xác thực, thanh toán và những quy ước quan trọng.

## Tổng quan

EcoMart là ứng dụng **siêu thị trực tuyến** dạng client-server monorepo. Dữ liệu xuyên suốt theo ngữ cảnh **mua sắm tiện lợi**: sản phẩm đa dạng, đặt hàng nhanh và thanh toán linh hoạt.

```
┌──────────────────┐  same-origin /api (Vite dev proxy / nginx)  ┌──────────────────────┐
│  Vite SPA client │ ─────────────────────────────────────────▶ │  Spring Boot server  │
│  (Vue 3 + Naive UI)│ ◀───────────────────────────────────────── │  Java 25 + JPA       │
└──────────────────┘    Authorization: Bearer (JWT)              └──────────┬───────────┘
                                                                            │
                                                     PostgreSQL (ddl-auto: update
                                                     + Flyway, baseline-on-migrate)
```

- **client/** — Vue 3 + Vite 8 + Vue Router 5 + **Naive UI 2.45** (native light theme, CSS-in-JS, import thủ công từng component) + xicons (`@vicons/ionicons5` qua wrapper `UiIcon`) + Axios + TypeScript + Zod 4. Admin stats dùng Chart.js (`vue-chartjs`). SPA thuần, không SSR. Giao diện tiếng Việt (chuỗi tích hợp sẵn của Naive UI là tiếng Anh qua locale `enUS`). Không còn Nitro proxy: ở dev, Vite proxy `/api` tới `VITE_API_TARGET` (mặc định `http://localhost:8080`); ở prod, container nginx (nginx.conf) proxy `/api` tới service `server`. Nếu đặt `VITE_API_BASE`, client gọi thẳng backend qua CORS (bỏ proxy).
- **server/** — Spring Boot 3.5 + Spring Security (JWT access + refresh) + Spring Data JPA. 11 controller, mỗi resource một controller → service → repository.
- **PostgreSQL** — `ddl-auto: update` đồng bộ schema khi khởi động. Flyway bật (`baseline-on-migrate`, `baseline-version: 1`, `locations: classpath:db/migration`), hiện có migration `V2__drop_banners.sql` (xoá bảng `banners`).

## Luồng dữ liệu chính

### 1. Xác thực (JWT access + refresh, tự viết)

Toàn bộ luồng login/đăng ký được làm thủ công:

1. Client gọi `POST /api/auth/login` với `{ identifier, password }` (`identifier` là email **hoặc** số điện thoại), hoặc `POST /api/auth/register`, qua composable `useAuth()`.
2. Server trả về `AuthResponse` gồm `token` (access JWT), `refreshToken`, `expiresIn` (giây) và thông tin user/role.
3. Client lưu vào storage hai khóa: `ecomart_session` (JSON, chứa cả `refreshToken`) và `ecomart_token` (raw access token). Nếu chọn "Ghi nhớ đăng nhập" → `localStorage`, ngược lại → `sessionStorage`. Cả hai khóa và event `ecomart:unauthorized` do module `src/utils/session-storage.ts` sở hữu chung cho `useApi`/`useAuth`.
4. `main.ts` gọi `useAuth().restore()` khi khởi động để nạp lại phiên trước khi mount, đồng thời đăng ký listener `UNAUTHORIZED_EVENT`: ngoài `forceLogout()` còn `router.push('/login')` (SPA, không reload) nếu chưa ở trang login/register.
5. **Mọi** request API đều đi qua `useApi()` (`client/src/composables/useApi.ts`, bọc **Axios**), tự đính header `Authorization: Bearer <token>`. Khi gặp 401 (ngoài `/api/auth/**`): `useApi` chạy refresh **một lần** (single-flight, dùng chung `refreshInflight` cho mọi request song song), retry request; vẫn 401 thì xoá phiên và phát event unauthorized. Navigation về `/login` do listener trong `main.ts` xử lý, không nằm trong `useApi`.
6. Server: `JwtAuthenticationFilter` đọc/verify access token, dựng `Authentication`; `JwtTokenProvider` sinh/kiểm tra JWT; `SecurityConfig` tắt session (stateless), cho phép công khai các endpoint đọc và bắt buộc `authenticated()` với phần còn lại (`anyRequest().authenticated()`). Lỗi 401/403 trả về JSON tiếng Việt.
7. **Refresh token xoay vòng (rotation)**: `POST /api/auth/refresh` nhận `refreshToken`, băm SHA-256 tra cứu trong bảng `refresh_tokens`, cấp access token mới **và** refresh token mới; token cũ bị đánh dấu đã thay (`replacedBy`) — dùng lại token cũ sẽ bị từ chối và thu hồi cả chuỗi. `POST /api/auth/logout` thu hồi refresh token của user.

Các endpoint công khai duy nhất (phần còn lại yêu cầu xác thực):

- `POST /api/auth/**` — login, register, refresh, logout
- `GET /api/products/**`, `/api/categories/**`, `/api/reviews`
- `POST /api/payments/payos/webhook`
- `/error`

**Bảo vệ route trên client** bằng global guard `beforeEach` trong `src/router/index.ts` (chỉ là UX, không phải ranh giới bảo mật). Guard đọc `meta` trên route:
- `requiresAuth` — yêu cầu đã đăng nhập (`/account`).
- `requiresAdmin` — yêu cầu role `ADMIN` (`/admin/**`).
- `customerOnly` — yêu cầu đã đăng nhập và không phải admin (`/cart`, `/checkout`, `/orders`, `/payment-result`).

### 2. Duyệt và tìm sản phẩm

- `pages/index.vue` (trang chủ) gọi song song `GET /api/categories`, `GET /api/products/latest`; banner đầu trang là nội dung tĩnh (`homeBanners` trong `data/home.ts`, ảnh trong `public/images/banners/`).
- Trang danh mục/sản phẩm gọi `GET /api/products` với query params (filter theo `category` slug, tìm kiếm, sort) — phân trang dạng `PageResponse<T>`.
- Danh mục có cấu trúc **cây** (parent/children) — `children` dùng để hiển thị danh mục con. Hiện có 3 danh mục gốc (`Rau củ sạch`, `Trái cây tươi`, `Thực phẩm khô`) kèm icon và 7 danh mục lá (`Rau xanh`, `Củ quả`, `Trái cây nhiệt đới`, `Trái cây nhập khẩu`, `Trái cây sấy`, `Ngũ cốc`, `Đậu & hạt`).
- Sản phẩm mang `materials` (vật liệu + % thành phần, map icon/màu theo union `MaterialType`) và `images` (gallery 1–3 ảnh, `displayOrder == 0` là ảnh chính). Sản phẩm `stock == 0` vẫn hiển thị với badge "Hết hàng" và nút thêm giỏ bị vô hiệu (`AddToCartButton`).

### 3. Giỏ hàng (state phía server)

Giỏ hàng **không** lưu trong localStorage của trình duyệt; nó lưu trên server theo user (`Cart` + `CartItem` entities).

- `useCart()` (`client/src/composables/useCart.ts`) điều khiển giỏ qua `GET/POST/PUT/DELETE /api/cart`. Thêm/sửa quá tồn kho trả 400 với message riêng: hết hàng (`"Sản phẩm đã hết hàng"`) hoặc còn ít (`"Số lượng vượt quá tồn kho, chỉ còn X"`).
- Trạng thái giỏ được giữ bằng module-level `ref` trong composable, nạp lại mỗi khi đăng nhập (watch `isLoggedIn`).
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

### 5. Thông báo

- **Server** có `NotificationController` (`GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /{id}/read`, `PATCH /read-all`) và entity `Notification` lưu DB, gắn với user, đã `@PreAuthorize("isAuthenticated()")`.
- Giao diện người dùng hiện chưa hiển thị danh sách thông báo (chưa có component poll).

## Cấu trúc mã nguồn

### Server (`server/src/main/java/com/ecomart/`)

```
controller/  11 controller, mỗi resource một controller mỏng (logic nằm ở service), mapping dưới /api/...
service/     nghiệp vụ chính, kiểm soát quyền và logic (webhook PayOS ở PaymentService,
             thống kê admin ở AdminStatsService, quản lý người dùng admin ở AdminUserService)
domain/
  entity/    JPA entities (User, Customer, Admin, Product, Category, Material,
             ProductImage, ProductMaterial, Cart, CartItem, Order, OrderItem, Payment,
             Review, Address, RefreshToken, Notification, ...)
  enums/     UserRole, OrderStatus, PaymentMethod, PaymentStatus, MaterialType, NotificationType, ...
repository/  Spring Data JPA repositories (truy vấn tìm kiếm có kiểu qua @Query, không còn Specification string-path)
dto/
  request/   record payloads vào (LoginRequest, RegisterRequest, RefreshTokenRequest,
             CheckoutRequest, AddToCartRequest, ProductRequest, CategoryRequest,
             CreateUserRequest, UpdateUserRequest, ...)
  response/  payloads ra (AuthResponse, ProductResponse, OrderResponse, PageResponse,
             AdminDashboardResponse, AdminStatisticsResponse, NotificationResponse,
             UserSummaryResponse, ...)
security/    JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, CorsConfig, UserDetailsServiceImpl
integration/
  payos/     PayOSClient — thanh toán QR
config/      AppConfig, RestTemplateConfig (timeout), DataSeeder, JwtProperties, PayOSProperties, ShopProperties
common/      SecurityUtils (current user id), Mapper (entity ↔ DTO hai chiều: toXxx + merge)
exception/   xử lý lỗi API (ApiError, GlobalExceptionHandler, UnauthorizedException, ...)
```

Migrations: `server/src/main/resources/db/migration/` (hiện có `V2__drop_banners.sql`; schema mới tiếp theo đặt trong `V3__...`).

### Client (`client/`)

```
src/
  main.ts            bootstrap SPA: createHead (@unhead/vue), useAuth().restore(), listener UNAUTHORIZED_EVENT, mount #app
  App.vue            NConfigProvider + NMessageProvider + NDialogProvider + NGlobalStyle (Naive UI)
  router/            khai báo route + global guard beforeEach (meta: requiresAuth / requiresAdmin / customerOnly)
  pages/             guest: index, login, register, products, products/[slug];
                     user: cart, checkout, payment-result, orders, orders/[id], account;
                     admin/: statistic, products, categories, orders, users
  components/        ProductCard, FooterGlobal, UiImg, UiIcon, AuthShell, SectionHeader, Reveal,
                     PaginationBar, OrderSummaryCard, AddressForm, AddressCard,
                     AddToCartButton, PasswordInput
  composables/       useApi (mọi request qua Axios + auto-refresh), useAuth (phiên/JWT),
                     useCart, useFormat, useStatusLabels, useFormErrors, useConfirm, useToast
  utils/             session-storage (sở hữu khóa ecomart_session/ecomart_token + event unauthorized)
  data/              home.ts (dữ liệu marketing tĩnh trang chủ)
  layouts/           default (public, gồm navbar + FooterGlobal); page auth/login/register dùng layout trống
  schemas/           Zod validation — thông báo lỗi tiếng Việt (kèm form types suy ra từ schema)
  types/             TS interfaces phản ánh DTO của backend
  assets/css/        main.css
index.html           entry HTML (src/main.ts)
vite.config.ts       plugin-vue + @tailwindcss/vite + vite-plugin-vue-devtools (dev) + alias @ + proxy /api
nginx.conf           prod: serve dist/ + proxy /api → server
```

## Điểm quan trọng khi làm việc

- **Đồng bộ types**: `client/src/types/index.ts` (TS) và `client/src/schemas/index.ts` (Zod) phải giữ song song với DTO backend. Thêm/sửa trường ở server → cập nhật cả hai.
- **Flyway vs ddl-auto**: schema vẫn do `ddl-auto: update` quản lý (`JPA_DDL_AUTO` ghi đè mặc định); Flyway đã bật (`baseline-on-migrate: true`, `baseline-version: 1`) và hiện có migration `V2__drop_banners.sql`. Khi thêm migration mới, đặt file `V3__...` trong `server/src/main/resources/db/migration`. Với DB có sẵn dữ liệu, tránh xoá/đổi tên cột đang được dùng.
- **UI là Naive UI, không phải PrimeVue**: toàn bộ component/hook của Naive UI import thủ công theo file (`NButton`, `NInput`, `NDataTable`...) — không auto-import/unplugin. Toast/dialog đi qua `useToast`/`useConfirm` (bọc `useMessage`/`useDialog`) để call site và unit test không đổi. Icons dùng `UiIcon` (xicons Ionicon5), không dùng `pi pi-*`.
- **Mọi request qua `useApi()`**: không gọi Axios/`$fetch` trực tiếp trong page để đảm bảo header JWT luôn được đính và cơ chế auto-refresh hoạt động.
- **Quyền ADMIN kiểm soát ở server**: `SecurityConfig` bắt buộc xác thực tại tầng HTTP (`anyRequest().authenticated()`), admin write dùng `@PreAuthorize("hasRole('ADMIN')")`, controller user-scoped dùng `@PreAuthorize("isAuthenticated()")`. Hết phiên/refresh lỗi → 401 (`UnauthorizedException`, chỉ ở `AuthService` + webhook PayOS chưa auth); đã login nhưng đụng tài nguyên người khác → 403 (`AccessDeniedException`). Route guard client chỉ là UX.
- **Không SSR**: SPA thuần, mọi trang (kể cả trang công khai) fetch client-side trong `onMounted`. Filter `products` đồng bộ 2 chiều với URL query (`router.push`/`replace`).
- **DataSeeder** (`config/DataSeeder.java`) idempotent theo slug/tên (DB đã seed vẫn nhận hàng mới khi boot lại): tạo admin `admin@ecomart.vn`, customer `customer@ecomart.vn`, danh mục (3 gốc + 7 lá, kèm icon), vật liệu, 44 sản phẩm mẫu (mỗi SP 1–3 ảnh WebP trong `client/public/images/products/`, 2 SP hết hàng `stock = 0` để e2e `cart-robustness` chạy thật). Mật khẩu demo mặc định `Admin@123` / `Customer@123`, ghi đè qua `SEED_ADMIN_PASSWORD` / `SEED_CUSTOMER_PASSWORD`. Để reset dữ liệu demo, xoá volume `pgdata`.
- **Ảnh sản phẩm/banner** là file tĩnh trong `client/public/images/` (`products/<slug>-<n>.webp`, `banners/`). Banner trang chủ là nội dung tĩnh (`homeBanners` trong `data/home.ts`, không còn API banner / bảng `banners`). Không có endpoint upload server — admin thêm ảnh bằng URL trong form.

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
| `VITE_API_TARGET` | nơi Vite proxy chuyển `/api` tới ở dev (mặc định `http://localhost:8080`; trong compose `client-dev`: `http://server-dev:8080`) |
| `VITE_API_BASE` | nếu đặt, client gọi thẳng backend qua CORS, bỏ proxy |

Chạy độc lập (dev): cần Postgres tại `localhost:5432` và nạp các biến từ `.env` cho `mvn spring-boot:run`. Hoặc chạy toàn bộ stack: `docker compose --profile prod up --build` (sản phẩm, client ở cổng 80) hoặc `docker compose --profile dev up --watch` (server tự rebuild + recreate khi sửa code qua `develop.watch`, client HMR ở cổng 5173).