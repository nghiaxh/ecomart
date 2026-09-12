# Hướng dẫn chạy test

Tài liệu tổng hợp cách chạy toàn bộ test của dự án EcoMart: unit, integration và end-to-end.

## Tổng quan

| Tầng | Framework | Cần Docker | Lệnh |
|------|-----------|:---:|------|
| Client (unit) | Vitest + happy-dom + @vue/test-utils | Không | `npm test` |
| Client (type) | vue-tsc | Không | `npm run typecheck` |
| Server (unit + integration) | JUnit 5 + Testcontainers | Có | `mvn test` |
| Server (coverage) | JaCoCo | Có | `mvn verify` |
| E2E | Playwright (Chromium) | Không (cần stack dev đang chạy) | `npm test` |

---

## 1. Client unit tests

Chạy trong `client/`:

```bash
npm test            # vitest run — chạy một lần
npm run test:watch  # vitest — watch mode
npm run typecheck   # vue-tsc --noEmit, kiểm tra type
```

Cấu hình: `vitest.config.ts` (happy-dom, globals, restoreMocks). Không có linter.

### Danh sách test (13 files trong `src/`)

| File | Nội dung |
|------|----------|
| `composables/useAuth.test.ts` | Luồng login/logout/restore phiên JWT |
| `composables/useApi.test.ts` | Bọc Axios, tự refresh khi gặp 401 |
| `composables/useCart.test.ts` | Composable giỏ hàng (CRUD qua `/api/cart`) |
| `composables/useFormat.test.ts` | Format tiền tệ, ngày tháng |
| `composables/useFormErrors.test.ts` | Map lỗi Zod về từng field form |
| `composables/useStatusLabels.test.ts` | Map trạng thái → nhãn/NTag |
| `schemas/index.test.ts` | Quy tắc validate Zod |
| `utils/session-storage.test.ts` | Quản lý khóa localStorage/sessionStorage |
| `components/ProductCard.test.ts` | Render card sản phẩm, badge hết hàng |
| `components/PaginationBar.test.ts` | Điều khiển phân trang |
| `components/OrderSummaryCard.test.ts` | Hiển thị tóm tắt đơn hàng |
| `components/AddToCartButton.test.ts` | Nút thêm giỏ, vô hiệu khi stock = 0 |
| `pages/products/index.test.ts` | Render trang danh sách sản phẩm |

---

## 2. Server tests

Chạy trong `server/` (**bắt buộc có Docker** — Testcontainers chạy PostgreSQL thật):

```bash
mvn test      # unit + integration
mvn verify    # như trên + sinh báo cáo JaCoCo (target/site/jacoco)
```

Cấu hình test: `src/test/resources/application.yml` (`ddl-auto: create-drop`, JWT/seed test-only). Container PostgreSQL chung dùng chung qua `AbstractPostgresIntegrationTest`.

### Unit tests (mock bean)

| File | Nội dung |
|------|----------|
| `service/AuthServiceTest.java` | Login, register, refresh, logout |
| `service/ProductServiceTest.java` | CRUD, tìm kiếm sản phẩm |
| `service/OrderServiceTest.java` | Checkout, chuyển trạng thái đơn |
| `service/CartServiceTest.java` | Thêm/sửa/xóa giỏ hàng |
| `service/CategoryServiceTest.java` | CRUD danh mục |
| `service/ReviewServiceTest.java` | Tạo/lấy đánh giá |
| `service/AddressServiceTest.java` | CRUD địa chỉ |
| `service/ProfileServiceTest.java` | Cập nhật hồ sơ user |
| `service/AdminStatsServiceTest.java` | Thống kê dashboard |
| `service/AdminUserServiceTest.java` | Quản lý user phía admin |
| `service/NotificationServiceTest.java` | CRUD thông báo |
| `common/MapperTest.java` | Map entity ↔ DTO hai chiều |
| `exception/GlobalExceptionHandlerTest.java` | Format lỗi JSON |
| `security/JwtTokenProviderTest.java` | Ký và verify JWT |
| `security/JwtAuthenticationFilterTest.java` | Chuỗi filter xác thực |
| `security/UserDetailsServiceImplTest.java` | Nạp UserDetails |
| `integration/payos/PayOSClientTest.java` | Client gọi PayOS QR |

### Integration tests (Testcontainers PostgreSQL)

| File | Nội dung |
|------|----------|
| `security/AuthFlowIntegrationTest.java` | Luồng xác thực đầy đủ với DB thật |
| `security/AdminAuthorizationIntegrationTest.java` | Kiểm tra RBAC admin |
| `security/AdminCrudIntegrationTest.java` | CRUD qua endpoint admin |
| `security/CommerceFlowIntegrationTest.java` | Luồng mua hàng hoàn chỉnh |
| `repository/RefreshTokenRepositoryTest.java` | Lưu truy vấn refresh token |
| `repository/OrderRepositoryTest.java` | Truy vấn đơn hàng |
| `repository/CompositeKeyAndInheritanceTest.java` | Ánh xạ JPA (khóa phức hợp, thừa kế) |

Hỗ trợ: `common/HmacTestUtil.java` (tạo chữ ký HMAC cho test webhook PayOS).

---

## 3. E2E tests (Playwright)

Chạy trong `e2e/` — yêu cầu stack dev đang chạy trên `http://localhost:5173` (seed data có sẵn):

```bash
docker compose --profile dev up --build   # từ repo root, chạy stack dev
npm install                               # (e2e/) lần đầu
npm test                                  # headless, chromium
npm run test:headed                       # mở browser
npm run test:debug                        # debug từng bước
npm run test:ui                           # Playwright UI mode
npm run test:report                       # mở HTML report
```

Cấu hình: `playwright.config.ts` — baseURL `E2E_BASE_URL` (mặc định `http://localhost:5173`), single project chromium, retry 2 lần khi chạy CI.

### Danh sách spec (11 files trong `tests/`)

| File | Nội dung |
|------|----------|
| `auth.spec.ts` | Đăng nhập/đăng ký/đăng xuất |
| `cart.spec.ts` | Thêm/sửa/xóa sản phẩm giỏ |
| `cart-robustness.spec.ts` | Hết hàng, giới hạn số lượng |
| `checkout.spec.ts` | Thanh toán COD + PayOS QR |
| `order-flow.spec.ts` | Theo dõi và hủy đơn hàng |
| `product-filter.spec.ts` | Lọc danh mục, tìm kiếm, sort |
| `review.spec.ts` | Đánh giá sản phẩm |
| `account.spec.ts` | Hồ sơ cá nhân, quản lý địa chỉ |
| `admin-crud.spec.ts` | CRUD sản phẩm/danh mục/user phía admin |
| `rbac.spec.ts` | Kiểm soát quyền theo role |
| `ui-states.spec.ts` | Trạng thái rỗng, loading, lỗi |

Hỗ trợ: `fixtures.ts` (setup login/dữ liệu dùng chung), `helpers.ts` (hàm tiện ích).

---

## Ghi chú

- Chạy test server cần Docker vì Testcontainers khởi động PostgreSQL riêng; test client và e2e không cần.
- E2E cần stack dev đang chạy và seed data (mặc định bật `SEED_ENABLED`). Reset dữ liệu demo bằng cách xóa volume `pgdata`.
- JaCoCo chỉ sinh report khi chạy `mvn verify` (assessment báo cáo trong `server/target/site/jacoco`).