# Kế hoạch Refactor EcoMart

Mục tiêu: cải thiện khả năng đọc, mở rộng và bảo trì mã nguồn; mở rộng dữ liệu seed (sản phẩm có ảnh Unsplash, ít nhất một sản phẩm hết hàng).

## Nguyên tắc chung

- Mỗi giai đoạn giữ cho repo **build/test được**: client typecheck + vitest, server `mvn test`, e2e luôn xanh tại ranh giới giai đoạn.
- Không thêm dependency runtime mới trừ khi thật cần thiết. Dùng converter viết tay (đã chốt), không dùng MapStruct.
- Mã chết bị xoá, không comment lại. Không đổi hành vi trừ khi là fix tường minh.
- Seed idempotent (guard theo slug/tên) để DB đã seed sẵn vẫn nhận được hàng mới mà không cần xoá volume.

---

## Giai đoạn 1 — Mở rộng dữ liệu seed + hỗ trợ hết hàng

Tập trung ở `server/.../config/DataSeeder.java`.

### 1.1 Bổ sung DataSeeder
- Giữ nguyên 25 sản phẩm hiện có (guard theo `slug`). Thêm **~18–20 sản phẩm mới** để mỗi danh mục lá có 6–8 mặt hàng:
  - Sản phẩm mới trong `rau-xanh`, `cu-qua`, `trai-cay-nhiet-doi` (ví dụ bơ sáp, sầu riêng, thanh long, khoai lang, rau cải...), `trai-cay-nhap-khau`, `ngu-coc`, `dau-va-hat`.
  - Thêm **1 danh mục lá mới**: `Trái cây sấy` (thuộc "Trái cây tươi") + 2–3 sản phẩm (xoài sấy, chuối sấy...).
- **Bộ sưu tập nhiều ảnh**: mở rộng `SeedProduct.images` để mỗi sản phẩm mới có **2–3 ảnh Unsplash khác nhau**; thêm ảnh thứ hai cho ~8 sản phẩm hiện có. `seedProduct` đã lặp `images[]` và đánh dấu `order==0` là ảnh chính — tái dùng nguyên vẹn.
- **Ít nhất một sản phẩm có `stock = 0`** và `isActive = true` (vẫn hiển thị trên kệ hàng, hiện "Hết hàng"). Chọn 1–2 sản phẩm (ví dụ "Sầu riêng Ri6" `stock=0`, "Bơ sáp" `stock=0`).
- **Sửa smell composite-id** (`new ProductMaterialId(0L, ...)` tại DataSeeder.java:288): `save(p)` trước, sau đó mới gắn `ProductMaterial` — giống hệt `ProductService.create`. Code cũ chỉ chạy được vì `@MapsId` ghi đè lúc flush.
- **Vật liệu có nghĩa**: thay "luôn lấy material đầu tiên, percentage 100" bằng mapping theo nhóm (rau → "Túi vải"/"Lá chuối", củ quả → "Giấy", hạt → "Hộp nhựa"...).
- Sản phẩm dưa chua ảnh bị lỗi tìm ảnh khác thay thế?

### 1.2 Hỗ trợ hết hàng (server + client)
- `CartService.add` (CartService.java:63): khi `product.getStock() == 0` ném `BadRequestException("Sản phẩm đã hết hàng")` thay vì thông báo chung "vượt quá tồn kho". Tương tự trong `updateQuantity`.
- Khối mua hàng `[slug].vue` + `ProductCard`: vô hiệu ngừng thêm vào giỏ khi `stock === 0` (gộp vào `AddToCartButton` mới ở Giai đoạn 3).
- E2E `e2e/tests/cart-robustness.spec.ts` hiện `test.skip` khi không có sản phẩm hết hàng — với seed 0 stock nó sẽ **kích hoạt**. Xác minh test chạy qua (đã viết cho kịch bản này).

### 1.3 Xác minh
- Validate mọi URL Unsplash mới trả về 200 (script tạm / curl trong lúc triển khai).
- `mvn test`; khởi động lại DB đã seed và xác nhận: sản phẩm mới trên `/products`, gallery trên trang chi tiết, render "Hết hàng".

---

## Giai đoạn 2 — Refactor server

### 2.1 Xoá mã chết (rẻ, không rủi ro)
- `AppSetting` entity + `AppSettingRepository` (không nơi nào tham chiếu).
- `ConflictException` (có handler nhưng chưa từng được ném — xoá).
- Method repo không dùng: `ProductRepository.findByIsActiveTrue(Pageable)`, `OrderRepository.findByCustomerIdOrderByCreatedAtDesc`, `CartItemRepository.findByCartId`, `PaymentRepository.findByPayosOrderCode`.
- Enum value không dùng: `NotificationType.PROMO`, `ChatRole.SYSTEM`.
- `AuthService` import thừa (Files, Paths, MultipartFile, ChatRole, MaterialType, NotificationType, Page, Pageable...).
- Trường `Payment.transactionRef` (không nơi nào viết/đọc). Lưu ý: `ddl-auto: update` không tự xoá cột — chấp nhận được.
- `Admin.hireDate` — giữ lại (seeder có gán; xoá đụng entity, giá trị thấp). Đánh dấu tuỳ chọn.

### 2.2 Sửa vi phạm tầng (controller → service)
- **PaymentController** (nặng nhất): chuyển verify chữ ký webhook, trích `orderCode`, map `NumberFormatException → BadRequestException`, và injection `PayOSClient` **vào `PaymentService`** (method mới `handleWebhook` / `handleReturn`). Controller trở nên mỏng.
- **AddressController.setDefault**: bỏ stream/filter/find trong controller — trả entity trực tiếp từ `AddressService.setDefault`.
- **ProductController.search**: chuyển logic tính `onlyActive` vào `ProductService.search`.
- **Không nhất quán 401/403 ở PaymentController**: nó ném custom `UnauthorizedException` (→401) trong khi các đường khác trả Spring `AccessDeniedException` (→403). Chuẩn hoá: user đã đăng nhập đụng tài nguyên người khác là **403**. Nếu `UnauthorizedException` hết dùng thì xoá luôn cùng handler.

### 2.3 Tập trung hoá chuyển đổi request→entity (chốt: viết tay)
- Mở rộng `common/Mapper.java` với helper request→entity/merge (`toProduct(entity, ProductRequest)`, `toAddress`, `toCategory`, `toBanner`) — đối xứng với hướng entity→DTO đã có. Xoá 4 method `apply()` gần giống nhau trong `ProductService`, `CategoryService`, `BannerService`, `AddressService`.
- Chuyển 3 class request mutable (`ProductRequest`, `BannerRequest`, `CategoryRequest`) thành record cho khớp 10 class còn lại.

### 2.4 Tách god-service
- **OrderService.checkout** (90 dòng, 11 dependency, OrderService.java): tách thành các hàm/đối tượng nhỏ — `resolveCart`, `buildOrder`, `applyStockDecrement`, `createPayment`, `createPayOSLink`, `clearCart`. Gộp logic parse status + kiểm tra sở hữu lặp lại thành helper. Đưa `SHIPPING_FEE = 20000` vào `application.yml` → typed property (`app.shop.shipping-fee`) qua record `ShopProperties` mới.
- **ChatBot.retrieve** (ChatBot.java: nạp toàn bộ danh mục mỗi tin nhắn): thay scan in-memory bằng truy vấn DB — thêm `ProductRepository.findTopByQuery(String keyword, Pageable)` dùng `LIKE`/`ILIKE` trên name/description với `LIMIT` (ví dụ 5), thay vì tải mọi sản phẩm active rồi chấm điểm. Giữ nhận diện intent, bỏ hot-path O(catalog).
- **ProfileService**: đưa regex số điện thoại + "password ≥ 6" cứng vào annotation validation của DTO (`ProfileUpdateRequest`).

### 2.5 Giết stringly-typed
- `UpdateOrderStatusRequest.status`: `@NotNull String` → enum `OrderStatus`. `CheckoutRequest.paymentMethod`: `@NotBlank String` → enum `PaymentMethod`; xoá helper tự chế `parseMethod`.
- `CartController.updateQuantity`: param `@RequestParam int quantity` chưa validate → validate (record request nhỏ + `@Min`).
- `ProductService` Specification: thay đường dẫn chuỗi (`root.get("isActive")`) bằng truy vấn có kiểu — spec tìm kiếm đủ nhỏ để diễn đạt bằng predicates `@Query` hoặc static spec builder; không cần dependency metamodel JPA.
- Dashboard `AdminController` trả `Map<String, Object>` thô → record `AdminDashboardResponse` mới; `AdminStatsService.dashboard` trả kiểu đó.

### 2.6 Nhất quán + gộp cấu hình
- `ReviewController.toggleHidden` trả `void`(204) → trả `ReviewResponse` như mọi mutation khác.
- `ReviewService`: bỏ `com.ecomart.common.SecurityUtils` fully-qualified (chuyển thành import), thay Spring `AccessDeniedException` bằng exception của project theo mục 2.2.
- `UploadService`: injection `@Value` field → constructor (khớp phần còn lại).
- `RestTemplateConfig`: RestTemplate + timeout (connect/read 120s) để PayOSClient không treo vô hạn.
- Gộp kích thước phân trang mặc định (12/8/10) và hằng số seeder/chatbot vào `application.yml` + typed properties khi là business fact; giữ nguyên HTTP defaults thuần trong controller.
- `Mapper.newNotification` (factory entity): chuyển việc dựng entity về `NotificationService` (lớp Mapper chỉ nên convert, không tạo entity).

**Ngoài phạm vi (đã chốt hoãn):** Flyway `V1__init.sql` và chuyển `ddl-auto`. Schema vẫn do Hibernate quản lý.

---

## Giai đoạn 3 — Refactor client

### 3.1 Nền tảng: composables & shared state
- **Phá vòng phụ thuộc `useApi` ↔ `useAuth` + loại duplication storage**: tạo `app/utils/session-storage.ts` (sở hữu khoá `ecomart_session` / `ecomart_token`, `save/load/clear`, và hằng số event `ecomart:unauthorized`). Cả `useApi` và `useAuth` đều phụ thuộc module này. Xoá `clearSession`/`clearStorages` trùng lặp + 4 chuỗi khoá trùng.
- **`useAuth`**: thay interface `Session` tự chế bằng `Pick<AuthResponse, ...>` (hết drift `numberPhone`/`expiresIn`). Bỏ try/catch đôi ở `logout()`. Thêm kiểm tra shape lúc khôi phục từ storage.
- **`useApi`**: bỏ các return không dùng `getToken`/`apiBase`.
- **`useFormErrors`** composable (~8 dòng) — thay vòng lặp `for (const issue of issue.error.issues)...` đang lặp 5 lần (account, checkout, admin products/categories/banners).
- **`useConfirm`** composable dựa trên Nuxt UI `UModal` (thay native `confirm()` ở 3 trang admin, đồng bộ với `useToast`).
- Xoá `useStatusLabels.notificationType` chết + type `NotificationItem`; thay vào đó các map icon/màu vật liệu ở `[slug].vue` dùng union `MaterialType` đã có (hồi sinh đúng cách một type đang chết).
- Sửa duplication toast-fallback trong `useCart` và `formatKg(0)` trả `''`.

### 3.2 Tách component dùng chung
- `PaginationBar.vue` — gộp UI phân trang lặp ở 4 trang (products/index, orders/index, admin/products, admin/orders).
- `OrderSummaryCard.vue` — cart.vue + checkout.vue (cũng hiện hàng "Phí giao hàng" thật thay vì "Miễn phí" cứng).
- `AddressForm.vue` + `AddressCard.vue` — form checkout inline + danh sách account (gộp luôn badge "Mặc định").
- `ChatThread.vue` — một cài đặt duy nhất cho bubble/gửi/nạp tin nhắn dùng chung `chat.vue` và `ChatWidget.vue` (xoá duplication toàn phần, thêm xử lý lỗi còn thiếu).
- `UiImg.vue` — fallback ảnh `@error → placeholder`, thay handler inline ở 5 file.
- `AddToCartButton.vue` — CTA desktop + sticky mobile trong `[slug].vue` (gộp logic nhãn/vô hiệu + vô hiệu khi hết hàng từ Giai đoạn 1).
- `PasswordInput.vue` — toggle hiện/ẩn mật khẩu lặp ở login + register.
- Đưa `FooterGlobal` vào `layouts/default.vue` (hiện chỉ `index.vue` render nó → các trang khác mất footer).

### 3.3 Trang: tách phần + `useAsyncData` (chỉ trang công khai, đã chốt)
- **Home `index.vue` (345)**: tách dữ liệu marketing tĩnh (stats/features/steps/testimonials, map ảnh danh mục) vào `app/data/home.ts`; tách phần hợp lý (`SectionHero / BannerCarousel / CategoryGrid / StepsSection...`); chuyển 3 fetch `onMounted` (banners/categories/latest) thành một `useAsyncData` với `Promise.all`.
- **`products/index.vue`**: `useAsyncData` cho danh sách sản phẩm + danh mục; giữ client-fetch cho thứ phải phản ứng theo input (search debounce đã client-side). Đồng bộ filter state ↔ URL query (hiện drift).
- **`products/[slug].vue`**: `useAsyncData` cho sản phẩm + đánh giá; tách phần theo 3.2; thêm `useHead` cho title.
- Trang admin (`admin/products` 237, `admin/categories` 188): tách form CRUD inline và dòng bảng thành component; **giữ client fetch** (đã xác thực). Sửa lệch hình dạng client/server bằng cách map trường ảnh đơn của admin thành `images: [{url, primary:true, displayOrder:0}]` trong payload và sửa `productSchema` tương ứng (giữ `ProductRequest`/`Product` đồng bộ mà chưa cần UI upload nhiều ảnh).
- `checkout.vue`: bỏ import `Cart` không dùng + shipping cứng (3.2), tách address + summary.
- `chat.vue`, `orders/index.vue`, `orders/[id].vue`: thêm try/catch còn thiếu; tái dùng component đã tách.
- `login.vue`/`register.vue`: dùng `PasswordInput`.
- `layouts/auth.vue`: thay bằng `definePageMeta({ layout: false })` trên hai trang auth (layout hiện là vỏ rỗng).

### 3.4 Đồng bộ types & schemas (luôn phải khớp DTO backend)
- Xuất form types từ cả 8 Zod schema (`profileSchema`, `productSchema`, `categorySchema`, `bannerSchema`, `reviewSchema` hiện không có).
- Sửa lệch `images` vs `imageUrl` (3.3), lật qua lật lại `price` string/number, `CheckoutResult.status` string lỏng lẻo.
- Hợp nhất validate số điện thoại: thống nhất `receiverPhone`/phone giữa `addressSchema`, `registerSchema`, `profileSchema` (hiện lệch `min(10)` vs regex).
- Xoá export chết: `MaterialType` (tái sinh thành map có kiểu ở 3.1), `NotificationItem`, `AddressRequest`, `AuthResponse.expiresIn`.

### 3.5 Dọn cấu hình/entry
- `nuxt.config.ts`: bỏ `runtimeConfig.apiTarget` chết (không nơi nào đọc); proxy `server/routes/api/[...].ts` đọc `runtimeConfig` (`NUXT_API_TARGET`) thay vì `process.env` trần, gộp hai tên env.
- `plugins/auth.client.ts`: bỏ guard `import.meta.client` thừa; xoá lời gọi `restore()` trùng trong `default.vue`/`admin.vue` (plugin đã chạy trước middleware).

---

## Giai đoạn 4 — Test & xác minh xuyên suốt
1. **Server**: `mvn test` (unit + Testcontainers integration); cập nhật test khẳng định endpoint/type đã xoá, thêm contract test cho `AdminDashboardResponse` / producer nơi chữ ký đổi, e2e cho seed 0 stock (`cart-robustness.spec.ts` giờ được chạy thật).
2. **Client**: `npm run typecheck`, `npm test` (vitest). Cân nhắc thêm test đơn vị cho `useApi`, util session-storage, và proxy (hai đơn vị dễ vỡ nhất, chưa có test — theo audit).
3. **e2e**: chạy các suite Playwright — xác nhận kệ hàng hiện sản phẩm seed mới, hành vi hết hàng, không regression từ việc tách component/trang.
4. **Smoke thủ công**: `docker compose --profile dev up`, reset `pgdata`, xác nhận seed idempotent lúc boot lần 2 và render gallery / "Hết hàng".

---

## Danh sách file động tới (tóm tắt)
- **Seed**: `DataSeeder.java`, `CartService.java` (+ message), client `[slug].vue`/`ProductCard`/`AddToCartButton`.
- **Server**: controller Payment/Address/Product, `Mapper.java`, `PaymentService` (mới hoặc mở rộng), `OrderService` (+ `ShopProperties`), `ChatBot`, `ProfileService` + `ProfileUpdateRequest`, `UpdateOrderStatusRequest`, `CheckoutRequest`, `CartController`, `AdminStatsService` + `AdminDashboardResponse`, `UploadService`, `RestTemplateConfig`, `NotificationService`, `ReviewService`/`ReviewController`, các class chết bị xoá + `application.yml`.
- **Client**: component/composable dùng chung mới liệt kê ở trên, util session-storage, `useApi`, `useAuth`, các trang động theo 3.3, `types/index.ts`, `schemas/index.ts`, layouts, plugin, `nuxt.config.ts`, Nitro proxy.
- **Test**: cập nhật suite server/client/e2e + phủ mới cho các mạch nối chưa có test.

## Rủi ro / quyết định đã chốt
- **Rủi ro lớn nhất**: tách component ở các trang người dùng xem nhiều nhất — giảm thiểu bằng e2e ổn định chạy theo từng giai đoạn.
- **Hoãn lại**: migration Flyway (schema vẫn `ddl-auto: update`), chiến lược fetch `useAsyncData` cho trang authed/admin, và UI admin upload nhiều ảnh (tạm ship workaround).
- **Không có thay đổi phá vỡ schema** ngoài hai trường dead bị xoá — an toàn dưới `ddl-auto: update`.

## Trình tự triển khai
1. Giai đoạn 1 (seed data + 0 stock) — làm trước, win nhanh, độc lập.
2. Giai đoạn 2 (server) — phụ giai đoạn 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6.
3. Giai đoạn 3 (client) — phụ giai đoạn 3.1 → 3.2 → 3.3 → 3.4 → 3.5.
4. Giai đoạn 4 (test & xác minh) — chạy toàn bộ sau mỗi giai đoạn để giữ chuỗi xanh.