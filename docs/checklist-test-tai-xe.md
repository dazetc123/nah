# Checklist test app Tài xế trên Android Studio

Phạm vi: mục 2.2 và 2.3 trong báo cáo, gồm Quản lý chuyến, Gửi vị trí GPS và Cập nhật trạng thái.
Cách dùng: mỗi dòng ghi **Pass** hoặc **Fail**. Nếu Fail thì ghi thêm điều bạn thấy được.

Ký hiệu:
- ✅ Code đã có, cần test để xác nhận.
- ⚠️ Code có nhưng khác kịch bản trong báo cáo. Nhiều khả năng sẽ **Fail** nếu test đúng theo kịch bản.
- ❌ Chưa có code. Test chắc chắn **Fail**, cần làm thêm.

---

## 0. Chuẩn bị môi trường

| # | Bước | Kết quả mong đợi | Pass/Fail |
|---|------|------------------|-----------|
| 0.1 | Chạy MySQL, tạo database `quanly_betong` | Kết nối được | |
| 0.2 | `cd backend && ./mvnw spring-boot:run` | Log in ra `Started BetongApplication`, cổng 8080 | |
| 0.3 | Mở `http://localhost:8080/swagger-ui.html` | Thấy nhóm API `/api/tai-xe/chuyen` | |
| 0.4 | Tạo tài khoản tài xế `taixe01` (vai trò "Tài xế", có hồ sơ `tai_xe`) | Đăng nhập được | |
| 0.5 | Chạy `du_lieu_mau_quan_ly_chuyen.sql` | Câu `SELECT` cuối trả về 1 chuyến có `trang_thai = 0` | |
| 0.6 | Android Studio: mở thư mục `mobile-app`, chờ Gradle sync xong (cần cài SDK 37) | Build thành công, không lỗi | |
| 0.7 | Chạy trên **emulator** (BASE_URL đang là `10.0.2.2:8080`). Nếu dùng máy thật thì sửa `ApiClient.BASE_URL` thành IP LAN của máy tính | App mở màn hình Đăng nhập | |
| 0.8 | Đăng nhập `taixe01` | Vào được trang chủ, hiện tên tài xế | |
| 0.9 | Đăng nhập bằng tài khoản không phải tài xế | Báo "Ứng dụng này chỉ dành cho tài khoản Tài xế" | |

Lệnh SQL tiện dùng khi test:

```sql
-- Đặt lại chuyến về Chờ nhận để test lại từ đầu
UPDATE chuyen SET trang_thai = 0, thoi_gian_xuat_phat = NULL, thoi_gian_den = NULL WHERE id_chuyen = <id>;
-- Xem trạng thái chuyến
SELECT id_chuyen, trang_thai, thoi_gian_xuat_phat, thoi_gian_den FROM chuyen ORDER BY id_chuyen DESC;
-- Xem các điểm GPS đã gửi lên
SELECT * FROM vi_tri_gps ORDER BY id_vi_tri DESC LIMIT 20;
```

Giả lập GPS trên emulator: bấm **⋯ (Extended controls) → Location**, nhập tọa độ (ví dụ `21.0045, 105.7985`) rồi bấm **Set location**. Có thể bấm **Routes** để cho xe chạy theo tuyến.

---

## 1. Quản lý chuyến (2.2.1)

### 1.1 Xem danh sách chuyến được phân công

| # | Thao tác | Mong đợi | | Pass/Fail |
|---|----------|----------|---|-----------|
| 1.1.1 | Trang chủ → **Lịch trình** | Hiện danh sách chuyến của `taixe01` | ✅ | |
| 1.1.2 | Kiểm tra từng dòng trong danh sách | Có mã chuyến, tên công trình, khối lượng, giờ giao dự kiến, biển số, nhãn trạng thái có màu | ✅ | |
| 1.1.3 | Vuốt xuống để tải lại | Danh sách được làm mới | ✅ | |
| 1.1.4 | Xóa hết chuyến của tài xế trong DB rồi mở lại | Hiện thông báo "chưa có chuyến" | ✅ | |
| 1.1.5 | Tắt backend rồi mở danh sách (luồng 3.a) | Hiện Snackbar "Không kết nối được máy chủ…" | ✅ | |
| 1.1.6 | Luồng 3.a: hiển thị **dữ liệu đã lưu tạm** khi mất mạng | Vẫn thấy danh sách cũ | ❌ App chưa lưu cache | |
| 1.1.7 | Bước 5: **lọc theo trạng thái / theo ngày** | Có bộ lọc trên màn hình | ❌ Backend có tham số `trangThai` nhưng app chưa có giao diện lọc, chưa lọc theo ngày | |
| 1.1.8 | Luồng 4.b: token hết hạn (xóa token hoặc đợi hết hạn) | Bị đưa về màn hình Đăng nhập | ⚠️ Trang chủ có xử lý 401, nhưng màn Lịch trình chỉ hiện "mã lỗi 401" | |
| 1.1.9 | Đăng nhập tài xế khác | Không thấy chuyến của `taixe01` | ✅ | |

### 1.2 Xem thông tin chi tiết chuyến

| # | Thao tác | Mong đợi | | Pass/Fail |
|---|----------|----------|---|-----------|
| 1.2.1 | Bấm vào 1 chuyến | Hiện mã chuyến, trạng thái, tên và địa chỉ công trình, mác bê tông, khối lượng, trạm trộn, biển số, ghi chú điều phối | ✅ | |
| 1.2.2 | Bấm **Gọi** | Mở trình gọi điện với SĐT công trình (`0922222222`) | ✅ | |
| 1.2.3 | Bấm **Bản đồ** | Mở ứng dụng bản đồ ngoài tại tọa độ công trình (đây là luồng 4.a) | ✅ | |
| 1.2.4 | Bước 4: bản đồ **nhúng trong app** kèm tuyến đường từ trạm trộn | | ❌ Chưa có, app chỉ mở bản đồ ngoài | |
| 1.2.5 | Kiểm tra dòng "thời gian giao dự kiến" | Có hiển thị | ⚠️ Màn chi tiết đang hiện "Thời gian xuất phát", chưa hiện giờ giao dự kiến. Cũng chưa hiện địa chỉ trạm trộn | |
| 1.2.6 | Luồng 2.b: đang mở chi tiết thì đổi `idtx` của chuyến sang tài xế khác, rồi vuốt tải lại | Báo "chuyến không còn hiệu lực" và quay lại danh sách | ⚠️ Chỉ hiện "mã lỗi 404" | |
| 1.2.7 | Tắt backend rồi vuốt tải lại (luồng 2.a) | Báo lỗi, cho thử lại | ✅ | |

### 1.3 Nhận chuyến (Chờ nhận → Đã nhận)

| # | Thao tác | Mong đợi | | Pass/Fail |
|---|----------|----------|---|-----------|
| 1.3.1 | Mở chuyến `trang_thai = 0` | Nút **Nhận chuyến** | ✅ | |
| 1.3.2 | Bấm → **Hủy** (luồng 4.a) | Đóng hộp thoại, trạng thái giữ nguyên | ✅ | |
| 1.3.3 | Bấm → **Xác nhận** | Báo "Cập nhật trạng thái thành công", nhãn đổi sang "Đã nhận", DB `trang_thai = 1` | ✅ | |
| 1.3.4 | Luồng 5.a: mở chi tiết, đổi `trang_thai` trong DB sang 1, rồi bấm Nhận | Báo chuyến không còn khả dụng | ⚠️ Backend trả 409 đúng, nhưng app chỉ hiện "mã lỗi 409" | |
| 1.3.5 | Luồng 5.b: tài xế đang có chuyến khác chưa hoàn thành | Bị chặn, không nhận thêm được | ❌ Backend chưa kiểm tra | |
| 1.3.6 | Luồng 6.a: bật chế độ máy bay rồi bấm Nhận | Yêu cầu được lưu tạm và gửi lại khi có mạng | ❌ Chưa có hàng đợi offline | |
| 1.3.7 | Bước 6: lưu thời điểm nhận chuyến | | ❌ Bảng `chuyen` chưa có cột này | |

### 1.4 Bắt đầu chuyến (Đã nhận → Đang giao)

| # | Thao tác | Mong đợi | | Pass/Fail |
|---|----------|----------|---|-----------|
| 1.4.1 | Gỡ quyền vị trí của app (Settings → Apps), mở chuyến Đã nhận → **Bắt đầu chuyến** | Hiện hộp thoại xin quyền | ✅ | |
| 1.4.2 | Từ chối quyền (luồng 4.a) | Báo "Cần cấp quyền vị trí…", không bắt đầu chuyến | ✅ | |
| 1.4.3 | Tắt Location của emulator → bấm Bắt đầu (luồng 3.a) | Nhắc bật GPS | ✅ | |
| 1.4.4 | Cấp quyền, bật GPS → Xác nhận | Trạng thái "Đang giao", DB có `thoi_gian_xuat_phat`, thanh thông báo hiện "Đang gửi vị trí GPS" | ✅ | |
| 1.4.5 | Đợi khoảng 20 giây, đổi tọa độ emulator 2–3 lần | Bảng `vi_tri_gps` có thêm bản ghi, mỗi bản ghi cách nhau khoảng 7 giây | ✅ | |
| 1.4.6 | Android 13 trở lên: kiểm tra thanh thông báo | Thông báo dịch vụ GPS hiển thị | ⚠️ App chưa xin quyền `POST_NOTIFICATIONS`, nên thông báo có thể bị ẩn. Dịch vụ vẫn chạy | |

### 1.5 Hoàn thành chuyến

| # | Thao tác | Mong đợi theo báo cáo | | Pass/Fail |
|---|----------|----------------------|---|-----------|
| 1.5.1 | Chuyến "Đang giao" có hiện nút **Hoàn thành chuyến** không? | Báo cáo yêu cầu phải ở trạng thái **Đã giao hàng** mới được hoàn thành (luồng 5.a) | ⚠️ App và backend đang cho hoàn thành ngay từ "Đang giao", bỏ qua 2 bước "Đã đến" và "Đã giao hàng" | |
| 1.5.2 | Bấm → hộp thoại xác nhận | Có tóm tắt khối lượng đã giao và thời gian thực hiện | ⚠️ Chỉ có câu hỏi xác nhận, chưa có tóm tắt | |
| 1.5.3 | Bấm Hủy (luồng 4.a) | Trạng thái giữ nguyên | ✅ | |
| 1.5.4 | Xác nhận | Trạng thái "Hoàn thành" (DB = 5), nút hành động biến mất, thông báo GPS tắt | ✅ | |
| 1.5.5 | Kiểm tra DB | `tai_xe.trang_thai = 1`, `xe.trang_thai = 1`, có `thoi_gian_den` | ✅ | |
| 1.5.6 | Đơn hàng có nhiều chuyến: hoàn thành 1 chuyến | Đơn hàng chưa được đóng | ⚠️ Code đang đặt đơn hàng thành Hoàn thành ngay khi 1 chuyến xong | |
| 1.5.7 | Luồng 5.b: hoàn thành khi mất mạng | Lưu tạm và đồng bộ lại sau | ❌ | |

---

## 2. Gửi vị trí GPS (2.2.2)

| # | Thao tác | Mong đợi | | Pass/Fail |
|---|----------|----------|---|-----------|
| 2.1 | Trang chủ → **Gửi vị trí GPS** → bật công tắc (không có chuyến Đang giao) | Hiện "Đã bật định vị, chưa có chuyến đang giao" và tọa độ hiện tại; DB không có bản ghi mới | ✅ | |
| 2.2 | Bật khi chưa có quyền → từ chối (luồng 3.a) | Báo không thể bật, công tắc trở về tắt | ✅ | |
| 2.3 | Bật khi GPS thiết bị đang tắt (luồng 3.b) | Hộp thoại "GPS đang tắt", có nút "Mở cài đặt" | ✅ | |
| 2.4 | Có chuyến Đang giao → bật | Hiện "Đang gửi vị trí cho chuyến #…", tọa độ và tốc độ cập nhật | ✅ | |
| 2.5 | Tắt công tắc khi chuyến đang giao (luồng 3.a của Tắt định vị) | Cảnh báo không được tắt, công tắc vẫn bật | ✅ | |
| 2.6 | Tắt khi không có chuyến → **Hủy** (luồng 3.b) | Công tắc vẫn bật, tiếp tục định vị | ✅ | |
| 2.7 | Tắt → **Xác nhận** | Dịch vụ dừng, thông báo biến mất | ✅ | |
| 2.8 | Đang gửi thì bật chế độ máy bay khoảng 30 giây, rồi tắt (luồng 3.a của Gửi tọa độ) | Sau khi có mạng lại, DB nhận bù các điểm bị lỡ | ✅ Hàng đợi chỉ nằm trong RAM, tối đa 50 điểm | |
| 2.9 | Vuốt tắt app khỏi danh sách đa nhiệm trong lúc đang giao | Vẫn tiếp tục gửi vị trí | ⚠️ Khi hệ thống khởi động lại dịch vụ, `idChuyen` bị mất, nên có thể ngừng gửi mà không báo | |
| 2.10 | Đặt tọa độ emulator `0,0` (luồng 4.a) | Server bỏ qua điểm này và ghi log | ✅ | |
| 2.11 | Mất tín hiệu GPS (luồng 1.a) | Cảnh báo "tín hiệu GPS yếu" | ❌ | |
| 2.12 | Bước 5–6: màn hình theo dõi của điều phối (web) cập nhật vị trí xe | Bản đồ trên web di chuyển theo xe | ⚠️ Server chỉ lưu DB, chưa đẩy realtime. Cần kiểm tra trang theo dõi trên web có tự tải lại không | |

---

## 3. Cập nhật trạng thái (2.2.3)

| # | Usecase | | Pass/Fail |
|---|---------|---|-----------|
| 3.1 | **Cập nhật trạng thái Đang giao** | ✅ Đang gộp chung với nút "Bắt đầu chuyến" (cùng chuyển Đã nhận → Đang giao). Nên ghi rõ điều này trong báo cáo | |
| 3.2 | **Xác nhận đã đến công trình** (Đang giao → Đã đến, so sánh tọa độ với bán kính công trình) | ❌ Chưa có API và chưa có nút trên app | |
| 3.3 | **Xác nhận giao hàng thành công** (form khối lượng thực giao và ảnh minh chứng → Đã giao hàng) | ❌ Chưa có API, chưa có màn hình | |
| 3.4 | **Báo cáo sự cố**: mở form từ trang chủ, bỏ trống để thử validate, gửi | ⚠️ Giao diện và validate có, nhưng **chưa gửi lên server** (`// TODO`). Form chưa có loại sự cố, mức độ ưu tiên và chưa gắn với mã chuyến như báo cáo mô tả | |

---

## 4. Tóm tắt tiến độ so với báo cáo

| Nhóm | Usecase | Backend | App | Ghi chú |
|------|---------|---------|-----|---------|
| Quản lý chuyến | Xem danh sách | ✅ | ✅ | Thiếu lọc, thiếu cache offline |
| | Xem chi tiết | ✅ | ✅ | Thiếu bản đồ nhúng, thiếu giờ giao dự kiến |
| | Nhận chuyến | ✅ | ✅ | Thiếu kiểm tra 5.b, thiếu offline |
| | Bắt đầu chuyến | ✅ | ✅ | |
| | Hoàn thành chuyến | ⚠️ | ⚠️ | Điều kiện trạng thái sai so với báo cáo |
| Gửi vị trí GPS | Bật định vị | – | ✅ | |
| | Tắt định vị | – | ✅ | |
| | Gửi tọa độ realtime | ✅ WebSocket | ✅ | Chưa đẩy realtime sang web |
| Cập nhật trạng thái | Đang giao | ✅ | ✅ | Gộp với Bắt đầu chuyến |
| | Đã đến công trình | ❌ | ❌ | |
| | Xác nhận giao hàng | ❌ | ❌ | |
| | Báo cáo sự cố | ❌ | ⚠️ chỉ giao diện | |

**Nên làm tiếp theo thứ tự:** (1) API và nút **Đã đến công trình** → (2) API và form **Xác nhận giao hàng** → (3) sửa điều kiện **Hoàn thành chuyến** thành chỉ cho phép khi Đã giao hàng → (4) API **Báo cáo sự cố** và nối form trên app → (5) các luồng phụ (offline, lọc, 401).
