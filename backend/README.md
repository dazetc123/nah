# Betong Platform

Backend Spring Boot cho hệ thống quản lý, đặt hàng và điều phối bê tông
thương phẩm.

## Tổng quan

Project sử dụng Java 17, Spring Boot 3.3.4, MySQL và Spring Data JPA.
Backend cung cấp REST API cho xác thực, phân quyền, hồ sơ người dùng, quản lý
tài khoản, công trình, xe, báo cáo sự cố và các dữ liệu vận hành liên quan.

Base URL mặc định:

```text
http://localhost:8080
```

Frontend mặc định của luồng Google OAuth2:

```text
http://localhost:5173/login
```

## Chức năng đã triển khai

### Xác thực và tài khoản

- Đăng ký tài khoản khách hàng.
- Đăng nhập bằng tên đăng nhập hoặc email.
- Mã hóa mật khẩu bằng BCrypt.
- Phát hành, kiểm tra và thu hồi JWT.
- Khóa/mở tài khoản và kiểm tra trạng thái tài khoản.
- Bắt buộc đổi mật khẩu lần đầu đối với tài khoản do Quản lý tạo.
- Đăng nhập Google OAuth2, tự tạo tài khoản Khách hàng khi email chưa tồn tại.
- Quên mật khẩu bằng OTP qua email hoặc SMS.
- Cấu hình thông báo mật khẩu tạm thời qua email/SMS.

### Hồ sơ cá nhân

Mọi tài khoản đăng nhập đều có thể xem và cập nhật hồ sơ của chính mình:

- Họ tên.
- Số điện thoại.
- Email.
- Ngày sinh.
- Giới tính.
- Địa chỉ thường trú.
- Ảnh đại diện.
- Đổi mật khẩu.

Thông tin số điện thoại và họ tên của tài xế được đồng bộ giữa tài khoản và
hồ sơ tài xế. Tài xế tự cập nhật hồ sơ; Quản lý không được sửa trực tiếp hồ
sơ tài xế.

### Quản lý tài khoản và phân quyền

Quản lý có thể:

- Xem, tìm kiếm, phân trang danh sách nhân viên điều phối và tài xế.
- Xem danh sách khách hàng.
- Xem chi tiết tài khoản theo nhóm.
- Xem ảnh đại diện và thông tin hồ sơ mở rộng.
- Tạo tài khoản nhân viên điều phối hoặc tài xế.
- Gửi mật khẩu tạm và yêu cầu đổi mật khẩu lần đầu.
- Khóa hoặc mở tài khoản.
- Đổi vai trò Nhân viên điều phối/Tài xế theo nghiệp vụ.

Quản lý không được sửa thông tin cá nhân của tài xế. API sửa tài khoản hoặc
hồ sơ tài xế bị từ chối với `403 Forbidden`.

### Quản lý công trình

Đã triển khai đầy đủ các chức năng cho Quản lý:

- Xem danh sách công trình có phân trang.
- Tìm kiếm theo tên công trình, địa chỉ hoặc số điện thoại.
- Xem chi tiết công trình.
- Thêm công trình cho khách hàng.
- Sửa thông tin công trình.
- Xóa công trình.
- Kiểm tra khách hàng tồn tại khi thêm.
- Không cho xóa công trình đã có đơn hàng liên quan.

Khách hàng có thể:

- Tìm kiếm và xem danh sách các công trình thuộc chính mình.
- Xóa công trình thuộc chính mình.
- Không thể truy cập hoặc xóa công trình của khách hàng khác.

Các trường công trình gồm tên, địa chỉ, vĩ độ, kinh độ và số điện thoại.
Validation kiểm tra dữ liệu bắt buộc, tọa độ và số điện thoại.

### Quản lý xe và báo cáo sự cố

- Chuẩn hóa biển số xe khi thêm hoặc cập nhật.
- Lọc danh sách xe theo trạng thái.
- Trạng thái xe chỉ nhận `1` (đang hoạt động) hoặc `0` (bảo trì).
- Quản lý chỉ cập nhật trạng thái xe qua API trạng thái riêng.
- Quản lý không cập nhật nội dung sự cố xe.
- Tài xế báo cáo sự cố xe được phân công.
- Nhân viên điều phối cũng có thể báo cáo sự cố.
- Báo cáo hỗ trợ trạng thái xe, địa chỉ hư, số điện thoại tài xế, nguyên nhân,
  nội dung lỗi và ảnh minh chứng.
- Quản lý xem danh sách báo cáo lỗi xe có phân trang.
- Ảnh hồ sơ và ảnh báo cáo được lưu trong thư mục upload và trả về URL đầy đủ.

## API chính

Tất cả API dưới `/api/quan-ly/**`, `/api/khach-hang/**` và
`/api/dieu-phoi/**` đều yêu cầu JWT và đúng vai trò.

### Xác thực và hồ sơ

| Method | Endpoint | Mô tả | Quyền |
|---|---|---|---|
| `POST` | `/api/auth/dang-ky` | Đăng ký khách hàng | Công khai |
| `POST` | `/api/auth/dang-nhap` | Đăng nhập | Công khai |
| `GET` | `/oauth2/authorization/google` | Bắt đầu Google OAuth2 | Công khai |
| `GET` | `/login/oauth2/code/google` | Callback Google OAuth2 | Công khai |
| `POST` | `/api/auth/quen-mat-khau` | Gửi OTP | Công khai |
| `POST` | `/api/auth/dat-lai-mat-khau` | Đặt lại mật khẩu | Công khai |
| `GET` | `/api/nguoi-dung/ho-so` | Xem hồ sơ cá nhân | JWT |
| `PUT` | `/api/nguoi-dung/ho-so` | Cập nhật hồ sơ | JWT |
| `POST` | `/api/nguoi-dung/ho-so/anh` | Cập nhật ảnh đại diện | JWT |
| `PUT` | `/api/nguoi-dung/doi-mat-khau` | Đổi mật khẩu | JWT |

### Công trình của Quản lý

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/quan-ly/cong-trinh` | Tìm kiếm hoặc lấy danh sách công trình |
| `GET` | `/api/quan-ly/cong-trinh/danh-sach` | Lấy danh sách công trình |
| `GET` | `/api/quan-ly/cong-trinh/{id}` | Xem chi tiết |
| `POST` | `/api/quan-ly/cong-trinh` | Thêm công trình |
| `PUT` | `/api/quan-ly/cong-trinh/{id}` | Sửa công trình |
| `DELETE` | `/api/quan-ly/cong-trinh/{id}` | Xóa công trình |

Request thêm công trình:

```json
{
  "idKH": 5,
  "tenCongTrinh": "Công trình nhà phố",
  "diaChi": "123 Nguyễn Trãi, Quận 1",
  "viDo": 10.7769,
  "kinhDo": 106.7009,
  "sdt": "0901234567"
}
```

Tham số danh sách/tìm kiếm:

```text
tuKhoa: tùy chọn, tìm theo tên, địa chỉ hoặc số điện thoại
trang: bắt đầu từ 1, mặc định 1
soLuong: số phần tử mỗi trang, mặc định 20
```

### Công trình của Khách hàng

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/khach-hang/cong-trinh` | Tìm kiếm công trình của chính khách hàng |
| `DELETE` | `/api/khach-hang/cong-trinh/{id}` | Xóa công trình của chính khách hàng |

API khách hàng tự lọc theo tài khoản đang đăng nhập, không nhận `idKH` từ
client để tránh xem hoặc xóa dữ liệu của khách hàng khác.

### Xe và báo cáo lỗi

| Method | Endpoint | Mô tả | Quyền |
|---|---|---|---|
| `GET` | `/api/quan-ly/xe` | Danh sách/tìm kiếm xe | Quản lý |
| `GET` | `/api/quan-ly/xe/{id}` | Chi tiết xe | Quản lý |
| `PUT` | `/api/quan-ly/xe/{id}/trang-thai` | Cập nhật trạng thái xe | Quản lý |
| `PUT` | `/api/quan-ly/xe/{id}/tai-xe` | Gán tài xế cho xe | Quản lý |
| `DELETE` | `/api/quan-ly/xe/{id}/tai-xe` | Hủy gán tài xế | Quản lý |
| `POST` | `/api/tai-xe/xe/{id}/bao-cao` | Tài xế báo cáo sự cố | Tài xế |
| `POST` | `/api/dieu-phoi/xe/{id}/bao-cao` | Điều phối báo cáo sự cố | Điều phối |
| `GET` | `/api/quan-ly/bao-cao-xe` | Danh sách báo cáo sự cố | Quản lý |

API báo cáo sự cố dùng `multipart/form-data` với các field:

```text
trangThaiXe
diaChiHu
soDienThoaiTaiXe
nguyenNhan
noiDung
anh
```

Không tự đặt header `Content-Type` khi gửi `FormData`; trình duyệt phải tự
thêm boundary cho request multipart.

## Quy tắc lỗi

Response lỗi có dạng:

```json
{
  "message": "Nội dung lỗi"
}
```

Các trường hợp validation trả `400 Bad Request`. Không tìm thấy dữ liệu trả
`404 Not Found`. Không có quyền trả `403 Forbidden`. Lỗi truy cập cơ sở dữ
liệu thường trả `500 Internal Server Error` với thông báo phù hợp từng nghiệp
vụ, ví dụ:

```text
Không thể tải dữ liệu, Yêu cầu kiểm tra kết nối
Cập nhật thông tin thất bại, yêu cầu thử lại sau
Thêm công trình thất bại, yêu cầu thử lại sau
Có lỗi, vui lòng thử lại sau
```

## Upload ảnh

- Thư mục gốc mặc định: `uploads`.
- Ảnh hồ sơ: `uploads/ho-so`.
- Ảnh báo cáo xe: `uploads/bao-cao-xe`.
- Định dạng hỗ trợ: JPG, PNG, WEBP.
- Kích thước tối đa: 5 MB.
- URL ảnh được trả về dạng đầy đủ, mặc định bắt đầu bằng
  `http://localhost:8080/uploads/...`.
- Endpoint `/uploads/**` được cho phép truy cập công khai để frontend hiển thị
  ảnh.

## Google OAuth2

Authorized redirect URI trong Google Cloud Console:

```text
http://localhost:8080/login/oauth2/code/google
```

Biến môi trường cần cấu hình:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
APP_LOGIN_URL=http://localhost:5173/login
APP_PUBLIC_BASE_URL=http://localhost:8080
```

Luồng OAuth2:

1. Frontend mở `/oauth2/authorization/google` bằng trình duyệt.
2. Backend chuyển hướng sang Google và lưu authorization request trong session.
3. Google callback về backend.
4. Backend tìm hoặc tạo tài khoản theo email Google.
5. Backend phát hành JWT và chuyển về frontend qua URL fragment.

Cookie session OAuth2 sử dụng tên `BETONG_SESSION`, `SameSite=Lax`, phục vụ việc
khôi phục authorization request giữa các bước đăng nhập.

## Cấu hình môi trường

Các cấu hình chính nằm trong `src/main/resources/application.properties`:

- MySQL: `spring.datasource.*`.
- Hibernate: `spring.jpa.hibernate.ddl-auto=update`.
- JWT: `jwt.secret`, `jwt.expiration`.
- SMTP: `spring.mail.*`, biến môi trường mật khẩu mail.
- SMS: `app.sms.webhook-url`.
- Upload: `app.upload-dir`, `app.public-base-url`.
- Google OAuth2: `spring.security.oauth2.client.registration.google.*`.

Không commit mật khẩu database, mật khẩu SMTP, OAuth client secret hoặc JWT
secret thực tế vào repository.

## Công nghệ

- Java 17.
- Spring Boot 3.3.4.
- Spring Web MVC.
- Spring Data JPA/Hibernate.
- MySQL Connector/J.
- Spring Security.
- JWT với JJWT 0.12.6.
- Google OAuth2 Client.
- BCrypt.
- Jakarta Bean Validation.
- Spring Mail.
- Spring WebSocket.
- Springdoc OpenAPI 2.6.0.
- Lombok.

## Cấu trúc chính

```text
src/main/java/com/example/betong/
├── Controller/       # REST controller
├── DTO/              # Request/response DTO
├── Exception/        # Exception và xử lý lỗi
├── Security/         # JWT và Google OAuth2
├── Service/          # Logic nghiệp vụ
├── config/           # Security, upload và cấu hình ứng dụng
├── entity/           # Entity JPA
└── repository/       # Spring Data repository
```

## Chạy project

Yêu cầu:

- JDK 17.
- MySQL đang chạy tại `localhost:3306`.
- Database `quanly_betong`.
- Các biến môi trường cần thiết cho SMTP/Google OAuth2 nếu sử dụng các luồng
  tương ứng.

Trên Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Biên dịch:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Chạy test:

```powershell
.\mvnw.cmd -q test
```

## Tài liệu API

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

