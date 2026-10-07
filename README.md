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
