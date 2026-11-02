# Hệ Thống Quản Lý & Điều Hành Bê Tông (BetongOps)

Dự án gồm 3 thành phần chính:

```
he-thong-betong/
├── backend/          # Backend REST API (Java Spring Boot, Maven, MySQL)
├── frontend-web/     # Giao diện Web quản trị & khách hàng (React, Vite, TypeScript)
└── mobile-app/       # Ứng dụng di động dành cho Tài xế (Android Java, Retrofit)
```

## 1. Backend (`backend/`)
- **Công nghệ**: Java 17/21, Spring Boot, Spring Security, JPA/Hibernate, MySQL.
- **Khởi chạy**:
  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```
- Mặc định chạy ở cổng `8080`.
- **Biến môi trường** (không ghi mật khẩu thẳng vào `application.properties`): chỉ cần đặt
  `DB_USERNAME` (mặc định `hung`) và `DB_PASSWORD` (mặc định trống) nếu MySQL của bạn khác.
  `JWT_SECRET`, `GOOGLE_CLIENT_ID/SECRET`, `MAIL_USERNAME/PASSWORD` đều có giá trị mặc định để chạy thử.
- Dữ liệu mẫu cho app Tài xế: `du_lieu_mau_quan_ly_chuyen.sql`, sau đó `du_lieu_test_nhieu_chuyen.sql`
  (thêm tài khoản `quanly01`, `dieuphoi01`, `taixe02` - mật khẩu `123456789` - và nhiều chuyến để test).
- Checklist test app Tài xế: [`docs/checklist-test-tai-xe.md`](docs/checklist-test-tai-xe.md).

## 2. Frontend Web (`frontend-web/`)
- **Công nghệ**: React, Vite, TypeScript, Lucide Icons.
- **Khởi chạy**:
  ```bash
  cd frontend-web
  npm install
  npm run dev
  ```

## 3. Mobile App (`mobile-app/`)
- **Công nghệ**: Android Studio, Java, Material 3, Retrofit 2.
- Mở thư mục `mobile-app` bằng Android Studio và bấm **Run**.
