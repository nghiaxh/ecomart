# Quy ước phân nhánh và commit

## Mô hình

```
main  ───────────────────────────────────────●  (production, chỉ merge từ dev khi release)
  │                                          │
dev  ───────────────────●────────●───────────●  (integration, mọi feature nhập vào đây)
  │                    │        │
  feat/tim-kiem-vip    │        │             (feature branch)
  fix/cart-stock       │
  ...
```

## Quy tắc

| Branch           | Vai trò                     | Quy tắc                                                         |
| ---------------- | --------------------------- | --------------------------------------------------------------- |
| `main`           | Production-ready            | **Không commit trực tiếp.** Chỉ nhận merge từ `dev` khi release |
| `dev`            | Integration                 | Branch phát triển chính, mọi feature/fix gộp về đây qua PR      |
| `feat/<ten>`     | Tính năng mới               | Nhánh ra từ `dev`, merge vào `dev`                              |
| `fix/<ten>`      | Sửa lỗi                     | Nhánh ra từ `dev`, merge vào `dev`                              |
| `refactor/<ten>` | Refactor giữ nguyên hành vi | Nhánh ra từ `dev`                                               |
| `docs/<ten>`     | Tài liệu                    | Nhánh ra từ `dev`                                               |

## Workflow

### 1. Khởi tạo nhánh dev (làm một lần)

```bash
git checkout -b dev
git push -u origin dev
```

### 2. Tạo feature branch từ dev

```bash
git checkout dev
git pull
git checkout -b feat/ten-feature
```

### 3. Làm việc và commit

Commit theo **Conventional Commits**: lowercase, dạng mệnh lệnh, không dấu chấm cuối. Commit message có thể sử dụng tiếng Anh hoặc tiếng Việt.

```
feat: them bo loc gia san pham
fix: xu ly loi vuot ton kho khi them gio
refactor: tach OrderService.checkout thanh cac buoc nho
test: them unit test cho useCart
docs: cap nhat huong dan chay test
style: can chinh khoang cach nut chinh sua
```

Các type chính: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `build`, `ci`, `chore`, `revert`. Commit nhỏ, một commit một mục đích.

### 4. Đẩy lên và tạo PR

```bash
git push -u origin feat/ten-feature
# Tạo PR: feat/ten-feature → dev (GitHub UI hoặc: gh pr create)
```

Merge PR vào `dev` bằng **squash merge** để lịch sử dev gọn. PR đang làm dở đánh dấu _draft_.

### 5. Release

```bash
# Sau khi dev ổn định:
# Tạo PR: dev → main, merge khi test (client + server + e2e) đều xanh.
```

## Lưu ý

- Không bao giờ push trực tiếp lên `main`.
- Luôn `git pull` nhánh `dev` trước khi tạo branch mới để tránh conflict.
- Không nhầm lẫn: cấu trúc nhánh là `dev → feat/ten-feature`, không tách branch trực tiếp từ `main`.
