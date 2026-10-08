# Checklist test phía Nhân viên điều phối

Mục đích: kiểm tra **điều phối gửi gì cho tài xế** (duyệt đơn, tạo chuyến, phân công) và **tài xế gửi gì về cho điều phối** (trạng thái chuyến, vị trí GPS, đã đến công trình, giao hàng, sự cố), có tới nơi và đúng không.

Ký hiệu cột "Tình trạng":
- 🟢 **Đã chạy thử:** mình đã gọi thử trên MySQL/MariaDB thật và cho đúng kết quả.
- 🔵 **Chưa chạy thử trọn:** code đã có nhưng mình chưa kiểm tra hết bước này, bạn test kỹ.
- ⚪ **Chưa có:** chức năng chưa làm, test sẽ Fail. Có thể ghi vào phần "Hạn chế" của báo cáo.

> **Lưu ý quan trọng:** web quản trị (`frontend-web`) hiện **chưa có màn hình nào cho điều phối** để xem đơn, tạo chuyến, theo dõi xe hay xem sự cố. Menu điều phối chỉ có "Báo cáo tình trạng xe". Vì vậy phía điều phối test bằng **Swagger** (trang tài liệu API có sẵn trong backend, gọi API ngay trên trình duyệt). Phía tài xế vẫn test bằng app trên máy ảo.

---

## 0. Chuẩn bị

### 0.1 Lấy code mới, chạy backend

```cmd
cd "C:\Users\LENOVO\Downloads\Be tong\betong-test"
git pull
cd backend
mvnw spring-boot:run
```

Nhớ `set DB_USERNAME=...` / `set DB_PASSWORD=...` trước nếu MySQL của bạn không phải `hung` với mật khẩu trống.

### 0.2 Nạp dữ liệu test

Copy 3 file `.sql` ở thư mục gốc dự án sang `C:\sql\`. Mở **MySQL 8.0 Command Line Client** và chạy:

```sql
charset utf8mb4
source C:/sql/du_lieu_mau_quan_ly_chuyen.sql
source C:/sql/du_lieu_test_nhieu_chuyen.sql
source C:/sql/du_lieu_test_dieu_phoi.sql
```

Hai file đầu có thể bạn đã chạy rồi, khi đó chỉ cần chạy file thứ ba.

Bảng cuối của file thứ ba in ra các mã cần dùng. **Ghi lại** để thay vào các bước bên dưới:

| Tên trong checklist | Cột trong kết quả | Ví dụ |
|---|---|---|
| `{D}` | `ma_don_D` | 8 |
| `{E}` | `ma_don_E` | 9 |
| `{TRAM}` | `ma_tram` | 1 |
| `{XE3}` | `ma_xe_3` | 3 |
| `{TX3}` | `ma_tai_xe_3` | 3 |

Tài khoản dùng để test (mật khẩu đều là `123456789`):
- `dieuphoi01`: Nhân viên điều phối, đăng nhập trên Swagger.
- `taixe03`: tài xế đang rảnh, đăng nhập trên app máy ảo, là người nhận chuyến từ điều phối.
- `taixe01`: tài xế đang có chuyến dở, dùng để test các trường hợp bị chặn.

### 0.3 Đăng nhập điều phối trên Swagger

1. Mở trình duyệt máy tính: `http://localhost:8080/swagger-ui.html`
2. Bấm nút **Authorize** (ổ khóa, góc trên phải). Ở ô **clientKey** nhập `betong-web-local`, bấm **Authorize**, rồi **Close**.
3. Tìm nhóm **auth-controller**, mở `POST /api/auth/dang-nhap`, bấm **Try it out**, dán vào ô body rồi bấm **Execute**:
   ```json
   { "dinhDanh": "dieuphoi01", "matKhau": "123456789" }
   ```
4. Ở phần **Response body**, copy chuỗi dài trong `"accessToken": "eyJ..."`. Chỉ copy phần trong ngoặc kép.
5. Bấm **Authorize** lần nữa. Ở ô **bearerAuth** dán token vừa copy, bấm **Authorize**, rồi **Close**.

Từ giờ mọi API gọi trên Swagger đều chạy bằng tài khoản `dieuphoi01`.

**Cách gọi một API trên Swagger:** tìm đúng dòng (ví dụ `PUT /api/dieu-phoi/don-hang/{idDH}/xac-nhan`), bấm vào để mở, bấm **Try it out**, điền tham số, rồi bấm **Execute**. Xem kết quả ở **Code** (200 là thành công) và **Response body**.

| # | Bước | Kết quả mong đợi | Tình trạng | Pass/Fail |
|---|------|------------------|-----------|-----------|
| 0.1 | Chạy backend | Có dòng `Started BetongApplication` | 🟢 | |
| 0.2 | Chạy file `du_lieu_test_dieu_phoi.sql` | In ra bảng có `ma_don_D`, `ma_don_E`, … | 🟢 | |
| 0.3 | Đăng nhập Swagger bằng `dieuphoi01` | `POST /api/auth/dang-nhap` trả 200, có `accessToken`, `tenVaiTro` = "Nhân viên điều phối" | 🟢 | |
| 0.4 | Thử gọi API **trước khi** nhập clientKey | Trả **400** "ClientKey không hợp lệ" (để biết vì sao phải nhập clientKey) | 🟢 | |

---

## 1. Điều phối duyệt đơn hàng

Quy trình đơn hàng: **Chờ xử lý (0)** → Xác nhận → **Đã xác nhận (1)** → Phân bổ trạm → **Đã phân bổ trạm (6)** → Tạo chuyến → **Đang giao (3)** → **Hoàn thành (4)**.

| # | Thao tác (Swagger, tài khoản điều phối) | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.1 | `GET /api/dieu-phoi/don-hang` | Danh sách đơn, có đơn `{D}` và `{E}` ở trạng thái "Chờ xử lý" | 🟢 | |
| 1.2 | `GET /api/dieu-phoi/don-hang/{idDH}` với `idDH = {D}` | Chi tiết đơn D, 6 m³, ghi chú "Don D - test dieu phoi…" | 🟢 | |
| 1.3 | `PUT /api/dieu-phoi/don-hang/{idDH}/xac-nhan` với `{D}` | 200, `tenTrangThai` = **"Đã xác nhận"** | 🟢 | |
| 1.4 | Gọi lại 1.3 lần nữa | **409** "Đơn hàng đã thay đổi trạng thái" | 🟢 | |
| 1.5 | `GET /api/dieu-phoi/don-hang/{idDH}/tram-tron-kha-dung` với `{D}` | Danh sách có "Trạm trộn số 1", công suất ≥ 6 | 🟢 | |
| 1.6 | `PUT /api/dieu-phoi/don-hang/{idDH}/phan-bo-tram-tron/{idTram}` với `{D}`, `{TRAM}` | 200, `tenTrangThai` = **"Đã phân bổ trạm trộn"** | 🟢 | |
| 1.7 | `PUT /api/dieu-phoi/don-hang/{idDH}/tu-choi` với `{E}`, body `{ "lyDo": "Hết công suất" }` | 200, `tenTrangThai` = **"Bị từ chối"** | 🟢 | |
| 1.8 | Từ chối mà để trống lý do: body `{ "lyDo": "" }` (làm **trước** 1.7, hoặc với một đơn Chờ xử lý khác) | **400** "Lý do từ chối không được để trống" | 🟢 | |

---

## 2. Điều phối tạo chuyến và gửi cho tài xế

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 2.1 | `GET /api/dieu-phoi/xe` | Danh sách **xe rảnh**, có `29C-333.33` | 🟢 | |
| 2.2 | `GET /api/dieu-phoi/xe/tai-xe` | Danh sách **tài xế rảnh**, có "Le Van Ba" (`taixe03`). Không có `taixe01` vì đang có chuyến | 🟢 | |
| 2.3 | `POST /api/dieu-phoi/xe/chuyen` giao cho **tài xế đang bận**: body `{ "idDH": {D}, "idTram": {TRAM}, "idXe": {XE3}, "idTX": 1 }` | **409** "Tài xế không khả dụng hoặc đã được phân công cho chuyến khác" | 🟢 | |
| 2.4 | `POST /api/dieu-phoi/xe/chuyen` cho **đơn chưa phân bổ trạm** (dùng `{E}`) | **409** "Đơn hàng chưa được phân bổ trạm trộn" | 🟢 | |
| 2.5 | `POST /api/dieu-phoi/xe/chuyen` hợp lệ: body `{ "idDH": {D}, "idTram": {TRAM}, "idXe": {XE3}, "idTX": {TX3} }` | 200, "Tạo chuyến và phân công tài xế thành công". **Ghi lại `idChuyen`**, gọi là `{C}` | 🟢 | |
| 2.6 | Gọi lại 2.5 | **409**, không tạo thêm chuyến (thông báo "Đơn hàng chưa được phân bổ trạm trộn" vì đơn đã chuyển sang Đang giao) | 🟢 | |
| 2.7 | **Log backend** (cửa sổ cmd chạy `mvnw`) | Có dòng "Thông báo phân công chuyến #{C} cho tài xế Le Van Ba - xe 29C-333.33" | 🟢 | |
| 2.8 | **App máy ảo:** đăng xuất, đăng nhập `taixe03`, vào **Lịch trình** | Thấy **Chuyến #{C}**, trạng thái **Chờ nhận**, 6 m³, xe 29C-333.33. Đây là bước kiểm tra chuyến điều phối gửi **đã tới tài xế** | 🟢 API | |
| 2.9 | `GET /api/dieu-phoi/don-hang/{idDH}` với `{D}` | Đơn D chuyển sang "Đang giao" | 🟢 | |

---

## 3. Điều phối nhận thông tin tài xế gửi về

Làm song song: thao tác trên **app (taixe03)** và kiểm tra trên **Swagger (điều phối)**.

API điều phối dùng để theo dõi:
- `GET /api/dieu-phoi/theo-doi-xe`: danh sách chuyến đang hoạt động.
- `GET /api/dieu-phoi/theo-doi-xe/{idChuyen}/trang-thai`: trạng thái và tiến độ của một chuyến.
- `GET /api/dieu-phoi/theo-doi-xe/{idChuyen}/vi-tri`: vị trí GPS mới nhất.

| # | Trên app (taixe03) | Kiểm tra phía điều phối | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|----------|-----------|-----------|
| 3.1 | *(chưa làm gì)* | `…/theo-doi-xe/{C}/trang-thai` | `tenTrangThai` = "Chờ nhận", có `bienSo`, `tenTaiXe` | 🟢 | |
| 3.2 | **Nhận chuyến** | `…/trang-thai` | "Đã nhận". Log: "Tài xế Le Van Ba đã nhận chuyến #{C}" | 🟢 | |
| 3.3 | **Bắt đầu chuyến** (đặt vị trí máy ảo trước, ví dụ trạm trộn `21.0362, 105.7906`) | `…/trang-thai` | "Đang giao", có `thoiGianXuatPhat`. Log: "Chuyến #{C} đã bắt đầu…" | 🟢 | |
| 3.4 | Ngay sau khi bắt đầu, khi app chưa kịp gửi GPS | `…/theo-doi-xe/{C}/vi-tri` | Có thể thấy `thongBao` = "Không thể cập nhật vị trí xe" (chưa có điểm GPS nào) | 🟢 | |
| 3.5 | Đợi khoảng 10 giây | `…/vi-tri` | Có `viDo`, `kinhDo`, `tocDo`, `thoiDiemGPS` đúng vị trí máy ảo | 🟢 | |
| 3.6 | Đổi vị trí máy ảo sang `21.0200, 105.7950`, đợi 10 giây | `…/vi-tri` (bấm Execute lại) | `viDo`/`kinhDo` **đổi theo**, `thoiDiemGPS` mới hơn. Đây là bước kiểm tra vị trí tài xế gửi **đã tới điều phối** | 🟢 | |
| 3.7 | Đặt vị trí **ngoài bán kính** `21.0300, 105.8500` → **Đã đến công trình** → nhập ghi chú "Cổng sau" | `…/trang-thai` | "Đã đến công trình", `canKiemTraDen` = **true**, `khoangCachDen` khoảng 6051, `ghiChuDen` = "Cổng sau". Log: "…đã đến công trình - cần điều phối kiểm tra lại vị trí" | 🟢 | |
| 3.8 | **Xác nhận giao hàng**: 6 m³ + ảnh | `…/trang-thai` | "Đã giao hàng", `khoiLuongThucGiao` = 6, `anhMinhChung` là đường link. Mở link trên trình duyệt thấy ảnh. Log: "Chuyến #{C} đã giao 6.0 m³. Đơn hàng đã giao 6.0/6.0 m³" | 🟢 | |
| 3.9 | **Hoàn thành chuyến** | `GET /api/dieu-phoi/theo-doi-xe` | Chuyến `{C}` **không còn** trong danh sách đang hoạt động | 🟢 | |
| 3.10 | | `GET /api/dieu-phoi/don-hang/{idDH}` với `{D}` | Đơn D = **"Hoàn thành"** | 🟢 | |
| 3.11 | | `GET /api/dieu-phoi/xe/tai-xe` | `taixe03` lại xuất hiện trong danh sách **tài xế rảnh** | 🔵 | |

---

## 4. Sự cố: tài xế gửi, điều phối nhận và xử lý

Làm trong lúc chuyến `{C}` đang thực hiện, tức là trước bước 3.9. Nếu chuyến đã xong, tạo chuyến mới bằng cách chạy lại `du_lieu_test_dieu_phoi.sql`, rồi làm lại 1.3 → 1.6 → 2.5 → 3.2 → 3.3.

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 4.1 | **App:** chi tiết chuyến → **Sự cố** → **Hỏng xe**, mô tả "Nổ lốp" → Gửi | Hộp thoại có mã sự cố (ghi lại, gọi là `{SC}`), mức độ **Cao**, "Mới tiếp nhận" | 🟢 | |
| 4.2 | **Log backend** *(chính là mục 3.4.9 của checklist tài xế)* | Có dòng **"[KHẨN] Sự cố #{SC} (Hỏng xe) trên chuyến #{C} - cần điều động xe thay thế"** | 🟢 | |
| 4.3 | **App:** gửi thêm sự cố **Tắc đường kéo dài**, mức Trung bình | Log: "Sự cố #… (Tắc đường kéo dài) trên chuyến #… cần Nhân viên điều phối xử lý" (không có chữ KHẨN) | 🟢 | |
| 4.4 | **Swagger:** `GET /api/dieu-phoi/su-co`, `trangThai` = `0` | Thấy cả 2 sự cố. Sự cố **Hỏng xe (Cao) đứng trước** Tắc đường (Trung bình). Mỗi dòng có `idChuyen`, `bienSo`, `tenTaiXe`, `loaiSuCo`, `moTa`, `viTri`, `anhMinhChung` (nếu có) | 🟢 | |
| 4.5 | `PUT /api/dieu-phoi/su-co/{id}/trang-thai` với `{SC}`, body `{ "trangThai": 1 }` | 200, `tenTrangThai` = **"Đang xử lý"** | 🟢 | |
| 4.6 | Gọi lại 4.5 (vẫn `trangThai: 1`) | **409** "…không thể chuyển về Đang xử lý" (không cho đi lùi hoặc lặp lại) | 🟢 | |
| 4.7 | Body `{ "trangThai": 2 }` | 200, **"Đã xử lý"** | 🟢 | |
| 4.8 | Body `{ "trangThai": 5 }` | **400** "Trạng thái chỉ được là 1 (Đang xử lý) hoặc 2 (Đã xử lý)" | 🟢 | |
| 4.9 | `GET /api/dieu-phoi/su-co`, `trangThai` = `2` | Sự cố `{SC}` nằm trong danh sách "Đã xử lý" | 🟢 | |
| 4.10 | Tài xế xem lại trạng thái xử lý (Swagger: đăng nhập `taixe03` với clientKey `betongmobile`, gọi `GET /api/tai-xe/su-co`) | Sự cố `{SC}` hiện **"Đã xử lý"**. Đây là bước kiểm tra kết quả xử lý của điều phối **đã về tới tài xế** | 🟢 API / ⚪ app chưa có màn xem lại | |

---

## 5. Phân quyền: tài xế không được dùng chức năng điều phối

Trên Swagger, đăng nhập bằng `taixe01` (clientKey `betongmobile`) và dán token mới vào **bearerAuth**.

| # | Thao tác (bằng token tài xế) | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 5.1 | `POST /api/dieu-phoi/xe/chuyen` | **403** (tài xế không được tự tạo chuyến) | 🟢 *(trước đây là lỗ hổng, đã sửa)* | |
| 5.2 | `PUT /api/dieu-phoi/don-hang/{idDH}/xac-nhan` | **403** | 🟢 *(trước đây là lỗ hổng, đã sửa)* | |
| 5.3 | `GET /api/dieu-phoi/su-co` | **403** | 🟢 | |
| 5.4 | Ngược lại: token **điều phối** gọi `GET /api/tai-xe/chuyen` | **403** (điều phối không giả làm tài xế được) | 🟢 | |
| 5.5 | Không có token: gọi bất kỳ API điều phối | **401** | 🟢 | |

---

## 6. Hạn chế hiện tại (ghi vào báo cáo)

| Hạng mục | Tình trạng |
|----------|-----------|
| Màn hình web cho điều phối (duyệt đơn, tạo chuyến, bản đồ theo dõi xe, danh sách sự cố) | ⚪ Chưa có. Chỉ có API, test qua Swagger |
| Thông báo cho điều phối khi tài xế cập nhật trạng thái hoặc báo sự cố | ⚪ Hiện **chỉ ghi log** ở backend, chưa gửi push/email/hiện trên web |
| Vị trí xe trên màn hình điều phối tự cập nhật (realtime) | ⚪ Điều phối phải gọi lại API `…/vi-tri` để lấy vị trí mới |
| App tài xế xem lại danh sách sự cố đã gửi và trạng thái xử lý | ⚪ Có API `GET /api/tai-xe/su-co`, app chưa có màn hình |
| Trang web "Lịch trình" của tài xế | ⚪ Gọi API `/api/dieu-phoi/chuyen-xe` không tồn tại (lỗi có sẵn của nhóm, không thuộc app di động) |

---

## 7. Cách đọc log backend (dùng cho 2.7, 3.x, 4.2, 4.3 và mục 3.4.9 bên checklist tài xế)

Log là các dòng chữ chạy liên tục trong **cửa sổ cmd đang chạy `mvnw spring-boot:run`**.

1. Làm thao tác trên app (ví dụ gửi sự cố), rồi chuyển sang cửa sổ cmd đó.
2. Các dòng mới nhất nằm ở **cuối cửa sổ**. Tìm dòng có chữ `NotificationServiceImpl`, ví dụ:
   ```
   ... WARN ... c.e.b.S.n.impl.NotificationServiceImpl : [KHẨN] Sự cố #3 (Hỏng xe) trên chuyến #12 - cần điều động xe thay thế
   ... INFO ... c.e.b.S.n.impl.NotificationServiceImpl : Sự cố #4 (Tắc đường kéo dài) trên chuyến #12 cần Nhân viên điều phối xử lý
   ```
3. Log quá nhiều, khó tìm? Bấm **Ctrl+F** trong cửa sổ cmd (hoặc chuột phải thanh tiêu đề → **Edit → Find**), gõ `Sự cố #` hoặc `KHẨN` rồi Enter.
4. Nếu dòng tiếng Việt bị lỗi font trong cmd thì vẫn tính là Pass. Muốn hiện đẹp, trước khi chạy `mvnw` gõ `chcp 65001`.
