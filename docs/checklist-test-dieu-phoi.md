# Checklist test phía Nhân viên điều phối (trên web)

Mục đích: kiểm tra **điều phối gửi gì cho tài xế** (duyệt đơn, tạo chuyến, phân công) và **tài xế gửi gì về cho điều phối** (trạng thái chuyến, vị trí GPS, đã đến công trình, giao hàng, sự cố), có tới nơi và đúng không.

Cách test: **điều phối** dùng **web** (`frontend-web`), **tài xế** dùng **app trên máy ảo**. Mở hai màn hình cạnh nhau cho dễ theo dõi.

Ký hiệu cột "Tình trạng":
- 🟢 **Đã chạy thử:** mình đã chạy trên web thật bằng trình duyệt tự động (Chromium) với MySQL/MariaDB thật, kết quả đúng.
- 🔵 **Chưa chạy thử trọn:** code đã có nhưng mình chưa kiểm tra hết bước này, bạn test kỹ.
- ⚪ **Chưa có:** chức năng chưa làm, test sẽ Fail. Có thể ghi vào phần "Hạn chế" của báo cáo.

---

## 0. Chuẩn bị

### 0.1 Lấy code mới, chạy backend

```cmd
cd "C:\Users\LENOVO\Downloads\Be tong\betong-test"
git pull
cd backend
mvnw spring-boot:run
```

Nhớ `set DB_USERNAME=...` / `set DB_PASSWORD=...` trước nếu MySQL của bạn không phải `hung` với mật khẩu trống. Giữ nguyên cửa sổ cmd này.

### 0.2 Chạy web (mở **cửa sổ cmd thứ hai**)

Lần đầu cần cài Node.js nếu máy chưa có: tải bản **LTS** tại https://nodejs.org rồi cài như phần mềm bình thường.

```cmd
cd "C:\Users\LENOVO\Downloads\Be tong\betong-test\frontend-web"
npm install
npm run dev
```

- `npm install` chỉ cần chạy lần đầu, mất khoảng 1–2 phút.
- Khi thấy dòng `Local: http://localhost:5173/` thì mở **Chrome trên máy tính** và vào địa chỉ `http://localhost:5173`.
- Giữ nguyên cửa sổ cmd này.

### 0.3 Nạp dữ liệu test

Copy các file `.sql` ở thư mục gốc dự án sang `C:\sql\`. Trong **MySQL 8.0 Command Line Client** chạy:

```sql
charset utf8mb4
source C:/sql/du_lieu_test_dieu_phoi.sql
```

Nếu chưa từng chạy hai file `du_lieu_mau_quan_ly_chuyen.sql` và `du_lieu_test_nhieu_chuyen.sql` thì chạy hai file đó **trước**. File `du_lieu_test_dieu_phoi.sql` chạy lại được nhiều lần, mỗi lần tạo thêm **Đơn D** (để duyệt, phân bổ trạm, tạo chuyến) và **Đơn E** (để từ chối).

Tài khoản (mật khẩu đều là `123456789`):
- `dieuphoi01`: Nhân viên điều phối, đăng nhập **trên web**.
- `taixe03`: tài xế đang rảnh, đăng nhập **trên app máy ảo**, là người nhận chuyến điều phối tạo.
- `taixe01`: tài xế khác, dùng để test chặn quyền.

| # | Bước | Kết quả mong đợi | Tình trạng | Pass/Fail |
|---|------|------------------|-----------|-----------|
| 0.1 | Chạy backend | Có dòng `Started BetongApplication` | 🟢 | |
| 0.2 | Chạy web, mở `http://localhost:5173` | Hiện trang đăng nhập BetongOps | 🟢 | |
| 0.3 | Đăng nhập web bằng `dieuphoi01` | Vào trang Tổng quan, góc dưới trái ghi "Nhân viên điều phối". Menu có nhóm **ĐIỀU PHỐI** gồm: Đơn hàng & tạo chuyến, Theo dõi xe, Sự cố từ tài xế | 🟢 | |
| 0.4 | Trang Tổng quan | Có 3 ô lối tắt: Đơn hàng & tạo chuyến, Theo dõi xe, Sự cố từ tài xế | 🔵 | |
| 0.5 | Đăng nhập **app** bằng `dieuphoi01` | App báo "Ứng dụng này chỉ dành cho tài khoản Tài xế" (đúng thiết kế) | 🟢 | |

---

## 1. Duyệt đơn hàng — menu **Đơn hàng & tạo chuyến**

Trang có 3 nút lọc theo đúng thứ tự công việc: **Chờ xử lý** → **Đã xác nhận** → **Đã phân bổ trạm**.

| # | Thao tác trên web | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.0 | *(Cách thật nhất để có đơn mới)* Web: đăng nhập `khachhang01` / `123456789` → **Đặt bê tông** → đặt một đơn | Đơn vừa đặt hiện ở tab **Chờ xử lý** của điều phối | 🔵 | |
| 1.1 | Mở trang, nút **Chờ xử lý** đang chọn | Thấy Đơn D ("Don D - test dieu phoi…") và Đơn E ("Don E - test tu choi"): mã đơn, công trình + địa chỉ, M300 · khối lượng, giờ giao, tổng tiền | 🟢 | |
| 1.2 | Dòng **Đơn E** → **Từ chối** → để trống lý do → **Từ chối đơn** | Báo đỏ "Vui lòng nhập lý do từ chối", hộp thoại không đóng | 🟢 | |
| 1.3 | Nhập "Hết công suất trong ngày" → **Từ chối đơn** | Thông báo xanh "Từ chối đơn hàng thành công", Đơn E biến khỏi danh sách | 🟢 | |
| 1.4 | Dòng **Đơn D** → **Xác nhận** → **Xác nhận** | "Xác nhận đơn hàng thành công", Đơn D chuyển sang tab **Đã xác nhận** | 🟢 | |
| 1.5 | Bấm nút **Đã xác nhận** → dòng Đơn D → **Phân bổ trạm** | Hộp thoại hiện "Trạm trộn số 1 — Cầu Giấy, Hà Nội (công suất 50 m³)" | 🟢 | |
| 1.6 | Bấm **Phân bổ** | "Phân bổ trạm trộn thành công", Đơn D chuyển sang tab **Đã phân bổ trạm**, cột Trạm trộn = Trạm trộn số 1 | 🟢 | |

---

## 2. Tạo chuyến và gửi cho tài xế

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 2.0 | Tab **Đã phân bổ trạm** | Chỉ hiện đơn **chưa có chuyến**. Đơn đã có chuyến (ví dụ Đơn A, Đơn C trong dữ liệu test) không hiện ở đây | 🟢 | |
| 2.1 | Web: nút **Đã phân bổ trạm** → dòng Đơn D → **Tạo chuyến** | Hộp thoại: thông tin đơn, ô **Xe rảnh** có `29C-333.33`, ô **Tài xế rảnh** có "Le Van Ba". **Không** có `taixe01` vì đang có chuyến dở | 🟢 | |
| 2.1b | Khi mọi tài xế đều đang có chuyến chưa hoàn thành | Hộp thoại báo đỏ "Không có tài xế rảnh: tài xế đang có chuyến chưa hoàn thành…" | 🔵 | |
| 2.2 | Không chọn gì, bấm **Tạo chuyến** | Báo "Vui lòng chọn xe và tài xế" | 🟢 | |
| 2.3 | Chọn xe `29C-333.33` | Ô tài xế **tự chọn** "Le Van Ba" (tài xế đang được gán cho xe đó), dòng báo lỗi tự biến mất | 🟢 | |
| 2.4 | Bấm **Tạo chuyến** | "Tạo chuyến và phân công tài xế thành công — Chuyến #…". **Ghi lại số chuyến**, gọi là `{C}`. Đơn D biến khỏi danh sách | 🟢 | |
| 2.5 | Log backend (cmd chạy `mvnw`) | "Thông báo phân công chuyến #{C} cho tài xế Le Van Ba - xe 29C-333.33" | 🟢 | |
| 2.6 | **App máy ảo:** đăng nhập `taixe03` → **Lịch trình** | Thấy **Chuyến #{C}**, **Chờ nhận**, 6 m³, xe 29C-333.33. Đây là bước kiểm tra chuyến điều phối tạo **đã tới tài xế** | 🟢 API | |
| 2.7 | Web: **Theo dõi xe** | Có dòng **#{C}**: 29C-333.33 · Le Van Ba, nhãn **Chờ nhận**, "Chưa bắt đầu chuyến" | 🟢 | |

---

## 3. Theo dõi tài xế — menu **Theo dõi xe**

Trang **tự làm mới mỗi 10 giây**, dòng trên cùng ghi "Cập nhật lúc …". Bạn chỉ cần bấm trên app rồi chờ, **không cần tải lại web**.

| # | Trên app (taixe03) | Trên web, dòng chuyến #{C} | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 3.1 | **Nhận chuyến** | Trong ≤ 10 giây nhãn đổi thành **Đã nhận** | 🟢 | |
| 3.2 | Đặt vị trí máy ảo `21.0362, 105.7906` (trạm trộn) → **Bắt đầu chuyến** | Nhãn **Đang giao**, cột tiến độ có "Xuất phát: HH:mm" | 🟢 | |
| 3.3 | Chờ khoảng 10–20 giây | Cột **Vị trí GPS mới nhất** hiện tọa độ (khoảng 21.03620, 105.79060), dòng dưới "x giây trước". Bấm vào tọa độ thì mở Google Maps đúng chỗ | 🟢 | |
| 3.4 | Đổi vị trí máy ảo sang `21.0200, 105.7950`, chờ 20 giây | Tọa độ trên web **đổi theo**, "x giây trước" nhỏ lại. Đây là bước kiểm tra vị trí tài xế gửi **đã tới điều phối** | 🟢 | |
| 3.5 | Bấm **Tạm dừng tự làm mới**, đổi vị trí lần nữa, chờ 20 giây | Web **không** đổi. Bấm **Tải lại** thì mới đổi. Bấm **Bật tự làm mới** để bật lại | 🔵 | |
| 3.6 | Đặt vị trí **ngoài bán kính** `21.0300, 105.8500` → **Đã đến công trình** → ghi chú "Cổng sau" | Nhãn **Đã đến công trình**, tiến độ: "Đến công trình: HH:mm · cách ~6051 m", nhãn đỏ **Cần kiểm tra vị trí**, ghi chú "Cổng sau". Log: "…đã đến công trình - cần điều phối kiểm tra lại vị trí" | 🟢 | |
| 3.7 | (Làm thêm) với chuyến khác: đến công trình **trong bán kính** `21.0050, 105.7990` | "cách ~76 m", **không** có nhãn Cần kiểm tra | 🔵 | |
| 3.8 | **Xác nhận giao hàng**: 6 m³ + chọn ảnh | Nhãn **Đã giao hàng**, tiến độ: "Đã giao: 6 m³ lúc HH:mm · Ảnh minh chứng". Bấm "Ảnh minh chứng" mở được ảnh | 🟢 | |
| 3.9 | **Hoàn thành chuyến** | Dòng **#{C} biến khỏi** Theo dõi xe (chỉ hiện chuyến đang hoạt động) | 🟢 | |
| 3.10 | Web: **Đơn hàng & tạo chuyến** → **Tạo chuyến** với một đơn đã phân bổ trạm khác | `taixe03` lại xuất hiện trong ô **Tài xế rảnh** (cần chạy lại `du_lieu_test_dieu_phoi.sql` để có đơn mới) | 🔵 | |

---

## 4. Sự cố — menu **Sự cố từ tài xế**

Làm trong lúc chuyến đang chạy (trước bước 3.9). Nếu chuyến đã xong, chạy lại `du_lieu_test_dieu_phoi.sql` rồi tạo chuyến mới như mục 1–2.

Trang **tự làm mới mỗi 15 giây**.

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 4.1 | **App:** chi tiết chuyến → **Sự cố** → **Tai nạn**, mô tả "Va quệt nhẹ" → Gửi | App hiện mã sự cố, mức **Cao**, "Mới tiếp nhận". Ghi lại mã, gọi là `{SC}` | 🟢 | |
| 4.2 | **Web:** mở **Sự cố từ tài xế** (nút **Mới tiếp nhận** đang chọn) | Trong ≤ 15 giây thấy sự cố `{SC}` ở **trên cùng**. Có: thời điểm, Chuyến #{C}, 29C-333.33 · Le Van Ba, **Tai nạn**, nhãn đỏ **Ưu tiên Cao**, dòng đỏ "Cần điều động xe thay thế", mô tả, link **Xem vị trí** (mở Google Maps), **Xem ảnh** (nếu có gửi ảnh) | 🟢 | |
| 4.3 | Phía trên bảng | Khung đỏ "Có N sự cố mức ưu tiên Cao đang chờ tiếp nhận" | 🟢 | |
| 4.4 | Log backend | "[KHẨN] Sự cố #{SC} (Tai nạn) trên chuyến #{C} - cần điều động xe thay thế" *(đây cũng là mục 3.4.9 của checklist tài xế)* | 🟢 | |
| 4.5 | **App:** gửi thêm sự cố **Tắc đường kéo dài**, mức Trung bình | Web: sự cố này nằm **dưới** sự cố Tai nạn (ưu tiên cao xếp trước), nhãn vàng "Ưu tiên Trung bình", **không** có dòng "Cần điều động xe thay thế" | 🔵 | |
| 4.6 | Web: dòng `{SC}` → **Nhận xử lý** → **Nhận xử lý** | "Sự cố #{SC}: Đang xử lý", sự cố rời khỏi tab Mới tiếp nhận | 🟢 | |
| 4.7 | Bấm nút **Đang xử lý** | Thấy `{SC}`, nhãn xanh dương **Đang xử lý**, chỉ còn nút **Đã xử lý** | 🟢 | |
| 4.8 | Bấm **Đã xử lý** → **Đã xử lý** | "Sự cố #{SC}: Đã xử lý". Ở tab **Đã xử lý** có `{SC}`, nhãn xanh lá, không còn nút nào | 🟢 | |
| 4.9 | **App (taixe03):** trang chủ → **Sự cố đã báo** | Thấy `{SC}` với nhãn xanh lá **Đã xử lý** và dòng "Điều phối đã xử lý xong sự cố này". Màn hình tự làm mới mỗi 15 giây, nên nếu đang mở sẵn trong lúc điều phối bấm thì trạng thái tự đổi: Mới tiếp nhận → Đang xử lý → Đã xử lý | 🔵 | |

---

## 5. Phân quyền

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 5.1 | Web: đăng xuất, đăng nhập `taixe01`, gõ thẳng địa chỉ `http://localhost:5173/dieu-phoi/su-co` | Bị đưa về trang Tổng quan, menu không có nhóm ĐIỀU PHỐI | 🟢 | |
| 5.2 | Như 5.1 với `/dieu-phoi/don-hang` và `/dieu-phoi/theo-doi-xe` | Bị đưa về Tổng quan | 🔵 | |
| 5.3 | Đăng nhập `quanly01` trên web | Menu là của Quản lý, không có nhóm ĐIỀU PHỐI | 🔵 | |
| 5.4 | *(Nâng cao, bằng Swagger)* token **tài xế** gọi `POST /api/dieu-phoi/xe/chuyen`, `PUT /api/dieu-phoi/don-hang/{id}/xac-nhan`, `GET /api/dieu-phoi/su-co` | Đều **403**. Trước đây tài xế tự tạo chuyến và duyệt đơn được, đây là lỗ hổng đã sửa | 🟢 | |

---

## 6. Kịch bản test trọn một vòng (end-to-end)

1. **Web (điều phối):** xác nhận Đơn D → phân bổ trạm → tạo chuyến cho Le Van Ba.
2. **App (taixe03):** thấy chuyến → **Nhận** → **Bắt đầu**.
3. **Web:** Theo dõi xe thấy Đang giao và vị trí xe đổi theo máy ảo.
4. **App:** **Sự cố** → Hỏng xe → Gửi.
5. **Web:** Sự cố từ tài xế thấy sự cố khẩn → **Nhận xử lý** → **Đã xử lý**.
6. **App:** **Đã đến công trình** → **Xác nhận giao hàng** (6 m³, ảnh) → **Hoàn thành**.
7. **Web:** Theo dõi xe thấy từng bước (Đã đến, Đã giao 6 m³ + ảnh), cuối cùng chuyến biến khỏi danh sách.

| Kết quả end-to-end | Pass/Fail |
|--------------------|-----------|
| Toàn bộ 7 bước | |

---

## 6b. Xe bảo trì (tài xế báo từ app)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 6b.1 | **App (taixe03, không có chuyến dở):** **Trạng thái xe** → **Đang bảo trì** → điền vị trí, nguyên nhân, mô tả, ảnh → Cập nhật | "Xe đã chuyển sang Bảo trì…" | 🟢 API | |
| 6b.2 | **Web (điều phối):** Tạo chuyến cho một đơn | Xe `29C-333.33` **không còn** trong ô "Xe rảnh" | 🟢 | |
| 6b.3 | **App:** **Trạng thái xe** → **Sẵn sàng hoạt động** → Cập nhật (không cần ảnh) | Web: xe `29C-333.33` xuất hiện lại trong "Xe rảnh" | 🟢 | |
| 6b.4 | **Web (quanly01):** menu **Báo cáo sự cố** | Thấy 2 báo cáo của xe 29C-333.33 (Bảo trì, rồi Sẵn sàng). **Không** lẫn sự cố trong chuyến (sự cố chuyến chỉ ở trang của điều phối) | 🟢 | |

---

## 7. Hạn chế hiện tại (ghi vào báo cáo)

| Hạng mục | Tình trạng |
|----------|-----------|
| Thông báo đẩy cho điều phối khi tài xế cập nhật hoặc báo sự cố | ⚪ Backend chỉ ghi log. Web tự làm mới mỗi 10–15 giây thay cho thông báo đẩy |
| Bản đồ nhúng trên web để xem vị trí xe | ⚪ Web hiện tọa độ, bấm vào thì mở Google Maps |
| Trang web "Lịch trình" của tài xế | ⚪ Gọi API `/api/dieu-phoi/chuyen-xe` không tồn tại (lỗi có sẵn của nhóm, không thuộc phần điều phối) |

---

## 8. Cách đọc log backend

Log là các dòng chữ trong **cửa sổ cmd đang chạy `mvnw spring-boot:run`** (cửa sổ thứ nhất).

1. Làm thao tác trên app hoặc web, rồi chuyển sang cửa sổ cmd đó.
2. Dòng mới nhất nằm **cuối cửa sổ**. Tìm dòng có chữ `NotificationServiceImpl`, ví dụ:
   ```
   ... WARN ... NotificationServiceImpl : [KHẨN] Sự cố #3 (Hỏng xe) trên chuyến #12 - cần điều động xe thay thế
   ... INFO ... NotificationServiceImpl : Thông báo phân công chuyến #14 cho tài xế Le Van Ba - xe 29C-333.33
   ```
3. Khó tìm thì bấm **Ctrl+F** trong cửa sổ cmd và gõ `KHẨN`, `Sự cố #` hoặc `phân công`.
4. Chữ tiếng Việt bị lỗi font vẫn tính là Pass. Muốn hiện đẹp, gõ `chcp 65001` trước khi chạy `mvnw`.
