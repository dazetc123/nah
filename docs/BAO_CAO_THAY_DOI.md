# Báo cáo thay đổi so với mã nguồn gốc của nhóm (betong-main)

**Phạm vi:** phần **Tài xế** (mục 2.2, 2.3 trong báo cáo), gồm Quản lý chuyến, Gửi vị trí GPS và Cập nhật trạng thái (12 usecase). Ngoài ra có phần **Nhân viên điều phối** cần để nhận và xử lý dữ liệu từ tài xế.

**So sánh với:** `betong-main` (bản zip trưởng nhóm gửi).

**Tóm tắt:**
- Không xoá file nào của nhóm.
- Thêm mới 53 file.
- Sửa 40 file. Phần sửa chủ yếu là thêm code; logic cũ chỉ thay đổi ở một số chỗ, được liệt kê rõ ở mục 6.

---

## 1. Tổng quan

| Phần | Thêm mới | Sửa |
|---|---|---|
| Backend (Spring Boot) | API tài xế (chuyến, sự cố, xe của tôi), WebSocket GPS, API điều phối xử lý sự cố, 18 unit test | Entity `Chuyen`, `TramTron`; 3 repository; `SecurityConfig`, `SwaggerConfig`; tạo chuyến; theo dõi xe; báo cáo tình trạng xe; `application.properties` |
| App Android (tài xế) | Chi tiết chuyến, xác nhận giao hàng, gửi GPS nền, danh sách sự cố đã báo, các model/util | Trang chủ, Lịch trình, Báo cáo sự cố, Báo cáo tình trạng xe (trước đây là giao diện tĩnh, nay gọi API thật) |
| Web (React) | 3 trang Điều phối: Đơn hàng & tạo chuyến, Theo dõi xe, Sự cố từ tài xế | Sidebar, routes, trang chủ, API client, types |
| Dữ liệu và tài liệu | 3 file SQL test, 2 checklist test, báo cáo này | README |

**Database:** không cần chạy script tạo bảng. `spring.jpa.hibernate.ddl-auto=update` tự thêm các cột mới khi khởi động backend. Không đổi hay xoá cột cũ.

---

## 2. Backend

### 2.1 File mới

| File | Nội dung |
|---|---|
| `Common/TrangThaiChuyen.java` | Hằng số trạng thái chuyến: 0 Chờ nhận, 1 Đã nhận, 2 Đang giao, 3 Đã đến công trình, 4 Đã giao hàng, 5 Hoàn thành. Có hàm `tenHienThi()`. |
| `Controller/taixechuyen/ChuyenTaiXeController.java` | API `/api/tai-xe/chuyen` (bảng ở mục 2.2) |
| `Controller/taixechuyen/XeCuaToiController.java` | `GET /api/tai-xe/xe-cua-toi`: trả về xe đang gán cho tài xế, trạng thái xe và việc xe có đang chạy chuyến hay không |
| `Service/taixechuyen/ChuyenTaiXeService(+Impl)` | Nghiệp vụ các usecase quản lý chuyến |
| `DTO/request/taixechuyen/DaDenCongTrinhRequest` | Tọa độ GPS và ghi chú khi xác nhận đã đến |
| `DTO/response/taixechuyen/ChuyenDanhSachResponse`, `ChuyenChiTietResponse` | Dữ liệu danh sách và chi tiết chuyến cho app, gồm tọa độ trạm/công trình và tiến độ |
| `Controller/suco/SuCoTaiXeController.java` | `POST/GET /api/tai-xe/su-co`: tài xế báo sự cố, xem các sự cố đã báo |
| `Controller/suco/SuCoDieuPhoiController.java` | `GET /api/dieu-phoi/su-co`, `PUT /api/dieu-phoi/su-co/{id}/trang-thai`: điều phối xem và xử lý sự cố |
| `Service/suco/SuCoTaiXeService(+Impl)`, `DTO/response/suco/SuCoResponse` | Nghiệp vụ sự cố |
| `Websocket/GpsWebSocketHandler`, `GpsHandshakeInterceptor`, `dto/` | Nhận tọa độ GPS qua WebSocket `/ws/vi-tri-gps`. Kiểm tra JWT khi bắt tay; chỉ nhận điểm khi chuyến ở trạng thái 2–4. |
| `config/WebSocketConfig.java` | Đăng ký endpoint WebSocket |
| `src/test/.../ChuyenTaiXeServiceImplTest` (13 test), `SuCoTaiXeServiceImplTest` (5 test) | Unit test. Chạy bằng `./mvnw test`; tất cả pass. |

### 2.2 API mới cho Tài xế (role `Tài xế`)

| Method + URL | Usecase | Quy tắc chính |
|---|---|---|
| `GET /api/tai-xe/chuyen?trangThai=&ngay=&trang=&soLuong=` | Xem danh sách chuyến được phân công | Chỉ trả chuyến của chính tài xế. Lọc được theo trạng thái và theo ngày. |
| `GET /api/tai-xe/chuyen/{id}` | Xem chi tiết chuyến | Chuyến của người khác trả 404 |
| `POST /api/tai-xe/chuyen/{id}/nhan` | Nhận chuyến | Chỉ nhận được khi chuyến ở trạng thái 0. Trả 409 nếu tài xế đang có chuyến khác chưa xong (luồng 5.b). |
| `POST /api/tai-xe/chuyen/{id}/bat-dau` | Bắt đầu chuyến | Chuyển 1 → 2; sau đó app bật gửi GPS |
| `POST /api/tai-xe/chuyen/{id}/da-den` | Xác nhận đã đến công trình | Bán kính 500 m (công thức Haversine). Ngoài bán kính hoặc không có GPS thì bắt buộc nhập ghi chú, nếu không trả 422. Khi có ghi chú, chuyến được đánh dấu `canKiemTraDen` để điều phối kiểm tra. |
| `POST /api/tai-xe/chuyen/{id}/giao-hang` (multipart) | Xác nhận giao hàng thành công | Bắt buộc nhập khối lượng thực giao. Giao vượt khối lượng thì phải có ghi chú. Ảnh minh chứng không bắt buộc. |
| `POST /api/tai-xe/chuyen/{id}/hoan-thanh` | Hoàn thành chuyến | Chỉ thực hiện được khi chuyến ở trạng thái 4. Đơn hàng chỉ chuyển sang Hoàn thành khi không còn chuyến nào khác của đơn chưa xong. |
| `POST /api/tai-xe/su-co` (multipart) | Báo cáo sự cố | Loại sự cố: Hỏng xe, Tai nạn, Tắc đường kéo dài, Công trình chưa sẵn sàng, Khác. Hỏng xe và Tai nạn tự đặt mức ưu tiên Cao. Nếu không truyền id chuyến, server tự lấy chuyến đang chạy. |
| `GET /api/tai-xe/su-co` | Xem trạng thái xử lý sự cố đã báo | |
| `GET /api/tai-xe/xe-cua-toi` | Lấy xe của tài xế (dùng cho màn Báo cáo tình trạng xe) | |
| WebSocket `ws://.../ws/vi-tri-gps?token=...` | Gửi vị trí GPS | App gửi mỗi 5–10 giây khi chuyến ở trạng thái 2–4 |

### 2.3 File của nhóm đã sửa

| File | Thay đổi | Ảnh hưởng tới code cũ |
|---|---|---|
| `entity/Chuyen.java` | Thêm cột: `viDoDen`, `kinhDoDen`, `khoangCachDen`, `ghiChuDen`, `canKiemTraDen`, `khoiLuongThucGiao`, `thoiGianGiaoXong`, `ghiChuGiaoHang`, `anhMinhChung`, `thoiGianNhan`, `thoiGianHoanThanh` | Chỉ thêm cột, đều cho phép null |
| `entity/TramTron.java` | Thêm `viDo`, `kinhDo` | Dùng cho nút Chỉ đường (Google Maps) từ trạm đến công trình. Có thể để trống. |
| `repository/ChuyenRepository.java` | Các truy vấn "xe/tài xế đang bận" và "chuyến đang chạy" tính thêm trạng thái 3 và 4. Thêm các truy vấn cho tài xế. | **Đổi logic:** trước đây chuyến ở trạng thái 3 (đã đến) bị coi là xe rảnh. Giờ chuyến chưa ở trạng thái 5 thì xe và tài xế vẫn bận. |
| `repository/SuCoRepository.java`, `DonHangRepository.java` | Thêm truy vấn | Không ảnh hưởng |
| `Service/dieuphoi/impl/DieuPhoiXeServiceImpl.java` | Chuyến mới tạo có trạng thái **0 – Chờ nhận** (trước đây là `DANG_GIAO`) | **Đổi logic:** tài xế phải bấm Nhận chuyến rồi Bắt đầu chuyến trên app, đúng theo usecase. Đơn hàng vẫn chuyển sang Đang giao như cũ. |
| `Service/dieuphoi/impl/TheoDoiXeServiceImpl.java`, `TheoDoiXeResponse` | Tên trạng thái lấy từ `TrangThaiChuyen`. Thêm các trường tiến độ: đã đến chưa, khối lượng giao, cần kiểm tra. | Bản cũ ghi nhầm 0/1/3 là "Đang giao" và 2 là "Đã đến"; đã sửa cho đúng thang trạng thái |
| `Controller/dieuphoi/DieuPhoiDonHangController.java`, `DieuPhoiDonHangService(+Impl)` | `GET /api/dieu-phoi/don-hang` có thêm tham số `trangThai` (không bắt buộc). Với trạng thái 6 (đã phân bổ trạm), chỉ trả các đơn chưa có chuyến. | Không truyền tham số thì hoạt động như cũ |
| `Service/notification/NotificationService(+Impl)` | Thêm `notifyDispatchersOfIncident`. Hiện chỉ ghi log, sự cố nghiêm trọng có tiền tố `[KHẨN]`. | Không ảnh hưởng |
| `Service/xe/impl/BaoCaoTinhTrangXeServiceImpl.java`, `Controller/dieuphoi/BaoCaoXeDieuPhoiController.java` | Khi báo bảo trì (0): trả 409 nếu xe đang có chuyến chưa xong; bắt buộc địa chỉ, nguyên nhân, nội dung và ảnh như cũ. Khi báo "đã sửa xong" (1): các trường đó không bắt buộc. Danh sách của Quản lý không lẫn sự cố trên chuyến (chỉ lấy bản ghi có `chuyen IS NULL`). | **Đổi logic:** các tham số chuyển sang `required=false`; việc kiểm tra bắt buộc chuyển vào service |
| `config/SecurityConfig.java` | Xem mục 2.4 | |
| `config/SwaggerConfig.java` | Thêm ô nhập header `ClientKey` ở nút Authorize | Trước đây không thử được API trên Swagger vì thiếu ClientKey |
| `resources/application.properties` | Xem mục 2.5 | |
| `pom.xml` | `lombok.version` 1.18.34 → 1.18.42 | Bản 1.18.34 lỗi biên dịch với JDK mới (21+) |

### 2.4 Bảo mật (`SecurityConfig`)

1. Thêm `/api/tai-xe/**` và `/ws/**`, chỉ role **Tài xế** được gọi.
2. **Sửa lỗ hổng:** trước đây role Tài xế và Khách hàng gọi được `/api/dieu-phoi/**` (duyệt đơn, tạo chuyến). Bây giờ:
   - `/api/dieu-phoi/xe/chuyen` (tạo chuyến): chỉ **Nhân viên điều phối**.
   - `/api/dieu-phoi/**` còn lại: **Nhân viên điều phối** và **Quản lý**.
3. Token sai hoặc hết hạn khi gọi `/api/**` và `/ws/**` nay trả **401**. Trước đây trả 302 chuyển sang trang đăng nhập Google, khiến app và web không biết phải đăng xuất. Đăng nhập Google trên trình duyệt vẫn hoạt động như cũ.

### 2.5 Cấu hình (`application.properties`)

| Khoá | Bản gốc | Bây giờ | Lý do |
|---|---|---|---|
| `spring.datasource.username/password` | Ghi cứng `hung` và mật khẩu rỗng | `${DB_USERNAME:hung}`, `${DB_PASSWORD:}` | Mặc định vẫn là `hung` / rỗng; thành viên khác có thể đặt biến môi trường thay vì sửa file |
| `jwt.secret` | Ghi cứng trong file | `${JWT_SECRET:dev-only-...}` | Không để secret thật trong git |
| `spring.mail.username` | `${MAIL_PASSWORD}` | `${MAIL_USERNAME:}` | **Lỗi gõ nhầm** của bản gốc: username lại đọc biến MAIL_PASSWORD |
| `spring.mail.*`, `google.client-id/secret` | Không có giá trị mặc định | Có giá trị mặc định | Bản gốc không khởi động được nếu máy chưa đặt các biến này |

Lưu ý: khi chạy thật cần đặt `JWT_SECRET` (ít nhất 32 ký tự). Nếu đổi secret, mọi token đã đăng nhập trước đó không còn hiệu lực.

---

## 3. App Android (`mobile-app`)

### 3.1 Màn hình

| Màn hình | Trạng thái | Nội dung |
|---|---|---|
| `TripsActivity` (Lịch trình) | Sửa | Gọi API thật. Lọc theo trạng thái (chip) và theo ngày. Khi mất mạng, hiện dữ liệu lưu gần nhất kèm banner. Có màn hình lỗi với nút Thử lại. Gặp 401 thì quay về màn đăng nhập. |
| `TripDetailActivity` | **Mới** | Thông tin chuyến và các nút hành động theo trạng thái: Nhận chuyến → Bắt đầu → Đã đến → Giao hàng → Hoàn thành. Có thẻ Tiến độ, nút Báo sự cố và nút **Chỉ đường** mở Google Maps từ trạm trộn đến công trình. |
| `DeliveryConfirmActivity` | **Mới** | Xác nhận giao hàng: khối lượng thực giao, ghi chú, ảnh minh chứng |
| `GpsLocationActivity` + `LocationService` | **Mới** | Bật/tắt gửi GPS bằng foreground service, gửi qua WebSocket. Tự chạy lại khi bị hệ thống tắt (START_STICKY). |
| `FaultReportActivity` (Báo cáo sự cố) | Sửa | Trước đây chỉ có giao diện, nay gọi API thật. Chọn loại sự cố, mức ưu tiên, mô tả, ảnh và vị trí GPS. |
| `MyIncidentsActivity` | **Mới** | "Sự cố đã báo": xem trạng thái xử lý từ điều phối, tự làm mới mỗi 15 giây |
| `VehicleStatusActivity` (Báo cáo tình trạng xe) | Sửa | Trước đây chỉ có giao diện, nay gọi API thật. Báo xe bảo trì/hỏng (khoá xe) hoặc đã sửa xong (mở khoá). Không cho báo bảo trì khi đang chạy chuyến; khi đó app hướng sang Báo cáo sự cố. |
| `MainActivity` (Trang chủ) | Sửa | Thêm mục Sự cố đã báo và lối vào GPS. Xin quyền thông báo (Android 13+). |

### 3.2 File phụ trợ mới
- **models/trip:** `ChuyenDanhSach`, `ChuyenChiTiet`, `DaDenRequest`, `SuCo`.
- **models/common:** `PageResponse`.
- **adapters:** `TripAdapter`.
- **utils:**
  - `ApiError`: đọc thông báo lỗi từ server.
  - `FormParts`: tạo multipart.
  - `LocationUtil`
  - `TripCache`: lưu lịch trình để xem khi offline.
  - `TrangThaiChuyenUtil`
- **layout:**
  - `activity_trip_detail`, `activity_delivery_confirm`, `activity_gps_location`, `activity_my_incidents`
  - `item_trip`, `item_detail_row`, `item_incident`
  - drawable `bg_icon_blue`

### 3.3 File cấu hình đã sửa
- `AndroidManifest.xml`:
  - Thêm quyền `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`.
  - Khai báo các activity mới và `LocationService`.
- `app/build.gradle.kts`: thêm `androidx.recyclerview:recyclerview:1.3.2`.
- `api/ApiService.java`: thêm endpoint mới. `api/ApiClient.java`: thêm `getWsUrl()`, connect timeout 8 giây.
- Đã kiểm tra chạy được trên **Android 7.0 (API 24)**. Dùng `ContextCompat.startForegroundService`, vì `startForegroundService` chỉ có từ API 26 và gây crash trên API 24.

---

## 4. Web (`frontend-web`) – vai trò Nhân viên điều phối

| File | Trạng thái | Nội dung |
|---|---|---|
| `pages/DispatchOrdersPage.tsx` | **Mới** | Đơn hàng theo tab trạng thái: xác nhận/từ chối, phân bổ trạm, tạo chuyến (chọn xe và tài xế đang rảnh). Tab "Đã phân bổ trạm" chỉ hiện đơn chưa có chuyến. |
| `pages/DispatchTrackingPage.tsx` | **Mới** | Theo dõi xe và tiến độ chuyến, tự làm mới mỗi 10 giây |
| `pages/DispatchIncidentsPage.tsx` | **Mới** | Sự cố từ tài xế: lọc theo trạng thái, Nhận xử lý → Đã xử lý. Tự làm mới mỗi 15 giây. |
| `components/layout/RequireDispatcher.tsx` | **Mới** | Chặn các role khác vào `/dieu-phoi/*` |
| `App.tsx` | Sửa | Thêm route `/dieu-phoi/don-hang`, `/dieu-phoi/theo-doi-xe`, `/dieu-phoi/su-co` |
| `components/layout/Sidebar.tsx` | Sửa | Thêm nhóm menu ĐIỀU PHỐI. Bỏ mục "Báo cáo tình trạng xe" khỏi menu của điều phối, vì việc này nay do tài xế báo từ app; Quản lý vẫn xem được. |
| `AppShell.tsx`, `HomePage.tsx`, `lib/auth.tsx` (`isDispatcher`), `lib/nav.ts` | Sửa | Tiêu đề trang, thẻ truy cập nhanh trên trang chủ |
| `lib/api/index.ts`, `types/domain.ts` | Sửa | Thêm `dispatchOrderApi`, `dispatchTripApi`, `dispatchIncidentApi` và các kiểu dữ liệu tương ứng |

`npm run build` chạy không lỗi. Đã chạy thử toàn bộ luồng điều phối trên trình duyệt.

---

## 5. Dữ liệu test và tài liệu (thư mục gốc)

| File | Mục đích |
|---|---|
| `du_lieu_mau_quan_ly_chuyen.sql` | Dữ liệu mẫu ban đầu cho tài xế (1 chuyến). Đơn hàng có `version = 0`, vì nếu `version` NULL thì Hoàn thành chuyến bị lỗi 500. |
| `du_lieu_test_nhieu_chuyen.sql` | Tài khoản `quanly01`, `dieuphoi01`, `taixe02`; xe 29C-678.90; tọa độ trạm; nhiều chuyến ở các trạng thái khác nhau |
| `du_lieu_test_dieu_phoi.sql` | Tài khoản `taixe03`, xe 29C-333.33, 2 đơn hàng chờ xử lý để test luồng điều phối |
| `docs/checklist-test-tai-xe.md` | Checklist test app tài xế (đã test pass phần lớn) |
| `docs/checklist-test-dieu-phoi.md` | Checklist test web điều phối |

Các file SQL chạy được bằng MySQL Command Line: `source đường_dẫn/file.sql;`. Tên vai trò tiếng Việt được ghi bằng `UNHEX` để không bị lỗi font.

---

## 6. Những thay đổi có thể ảnh hưởng tới phần của thành viên khác

1. **Chuyến mới tạo có trạng thái 0 (Chờ nhận)** thay vì Đang giao. Màn hình nào đang lọc chuyến theo `trangThai` cần dùng đúng thang 0–5 trong `Common/TrangThaiChuyen`.
2. **Xe và tài xế được coi là bận cho đến khi chuyến ở trạng thái 5**, không chỉ ở trạng thái 0–2 như trước.
3. **Phân quyền `/api/dieu-phoi/**` chặt hơn.** Code phía khách hàng hoặc tài xế nếu đang gọi các API này sẽ nhận 403.
4. **401 thay cho 302** khi token hết hạn. Web và app nên xử lý 401 bằng cách quay về trang đăng nhập.
5. **`application.properties`:** cách cấu hình biến môi trường đã đổi (mục 2.5). Với máy dùng user `hung` / mật khẩu rỗng thì không cần làm gì thêm.

## 7. Chưa làm / hạn chế
- Thông báo cho điều phối khi có sự cố hiện mới chỉ ghi log. Chưa có push notification hay email.
- Chưa có bản đồ realtime vị trí xe trên web. Hiện chỉ có bảng tiến độ và tọa độ.
- App chưa lưu thao tác để tự gửi lại khi có mạng. Khi mất mạng chỉ xem được dữ liệu đã lưu.
- `DriverTripsPage` (code cũ của nhóm) gọi `/api/dieu-phoi/chuyen-xe`, nhưng backend không có endpoint này. Chưa sửa vì nằm ngoài phần của tôi.

## 8. Cách chạy và kiểm tra nhanh
1. **Backend:**
   ```
   cd backend
   ./mvnw spring-boot:run
   ```
   Chạy unit test: `./mvnw test` (18 test).
2. **Dữ liệu test:** trong MySQL chạy `source du_lieu_test_nhieu_chuyen.sql;` và `source du_lieu_test_dieu_phoi.sql;`.
3. **Web:**
   ```
   cd frontend-web
   npm install
   npm run dev
   ```
   Đăng nhập `dieuphoi01` (mật khẩu `123456789`) để vào menu ĐIỀU PHỐI.
4. **App:** mở thư mục `mobile-app` bằng Android Studio, đăng nhập `taixe02` (mật khẩu `123456789`). Kiểm tra theo `docs/checklist-test-tai-xe.md`.
5. **Swagger:** http://localhost:8080/swagger-ui.html. Bấm Authorize, nhập JWT và ClientKey (`betong-web-local` cho web, `betongmobile` cho app).
