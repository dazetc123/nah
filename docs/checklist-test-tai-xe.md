# Checklist test app Tài xế trên Android Studio (bản đầy đủ)

Phạm vi: mục 2.2 và 2.3 trong báo cáo, gồm 12 usecase của tác nhân Tài xế:
- **Quản lý chuyến:** xem danh sách, xem chi tiết, nhận, bắt đầu, hoàn thành.
- **Gửi vị trí GPS:** bật định vị, tắt định vị, gửi tọa độ.
- **Cập nhật trạng thái:** Đang giao, Đã đến công trình, Xác nhận giao hàng, Báo cáo sự cố.

Cách dùng: mỗi dòng ghi **Pass** hoặc **Fail**. Nếu Fail thì ghi thêm điều bạn thấy được (thông báo lỗi, ảnh chụp màn hình, log Logcat).

Ký hiệu cột "Tình trạng":
- 🟢 **API đã chạy thử:** backend đã được gọi thử trên MySQL/MariaDB thật và cho đúng kết quả. Bạn chỉ cần xác nhận phần giao diện trên app.
- 🔵 **Chưa chạy thử:** code đã có nhưng chưa chạy thử được ở đây, ví dụ GPS thật, quyền Android, thông báo. Cần test kỹ.
- ⚪ **Ngoài phạm vi:** báo cáo có nhắc nhưng chưa làm. Test sẽ **Fail**, bạn có thể ghi vào phần "Hạn chế" của báo cáo.

Luồng trạng thái của một chuyến:

```
Chờ nhận(0) → Đã nhận(1) → Đang giao(2) → Đã đến công trình(3) → Đã giao hàng(4) → Hoàn thành(5)
   [Nhận chuyến] [Bắt đầu chuyến] [Đã đến công trình] [Xác nhận giao hàng] [Hoàn thành chuyến]
```

---

## 0. Chuẩn bị môi trường

| # | Bước | Kết quả mong đợi | Pass/Fail |
|---|------|------------------|-----------|
| 0.1 | Chạy MySQL, tạo database `quanly_betong` | Kết nối được | |
| 0.2 | Đặt tài khoản MySQL (xem "Biến môi trường" bên dưới), rồi chạy `cd backend && mvnw spring-boot:run` | Log in ra `Started BetongApplication`. Bảng `chuyen` có thêm các cột mới (`thoi_gian_den`, `khoi_luong_thuc_giao`, `anh_minh_chung`, …) | |
| 0.3 | Bảng `vai_tro` có đủ 4 vai trò; có tài khoản `taixe01` (vai trò Tài xế) và bản ghi `tai_xe` tương ứng | Đăng nhập được | |
| 0.4 | Chạy `du_lieu_mau_quan_ly_chuyen.sql` (**bản mới**, đã có cột `version`) | Câu `SELECT` cuối trả về 1 chuyến có `trang_thai = 0` | |
| 0.4b | Chạy `du_lieu_test_nhieu_chuyen.sql` trong DBeaver (xem mục "Dữ liệu test nhiều chuyến") | Bảng kết quả cuối có thêm chuyến của `taixe01` và `taixe02` | |
| 0.5 | Android Studio: mở `mobile-app`, chờ Gradle sync xong (cần SDK 37) | **Build thành công** | |
| 0.6 | Chạy trên **emulator** (`10.0.2.2:8080`). Nếu dùng máy thật thì sửa `ApiClient.BASE_URL` thành IP LAN của máy tính | Mở màn hình Đăng nhập | |
| 0.7 | Android 13 trở lên: mở trang chủ lần đầu | Hiện hộp thoại xin quyền **Thông báo**. Bấm Cho phép | |

**Biến môi trường backend.** Chỉ cần đặt nếu tài khoản MySQL của bạn khác `hung` / mật khẩu trống (giống cấu hình gốc của nhóm). Các biến còn lại đều có giá trị mặc định để chạy thử:

| Biến | Mặc định | Khi nào cần đặt |
|------|----------|-----------------|
| `DB_USERNAME` | `hung` | MySQL của bạn dùng user khác (ví dụ `root`) |
| `DB_PASSWORD` | *(trống)* | User MySQL có mật khẩu |
| `JWT_SECRET` | chuỗi dùng thử | Khi chạy thật |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | `chua-cau-hinh` | Khi dùng đăng nhập Google |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | *(trống)* | Khi dùng gửi mail OTP / quên mật khẩu |

Ví dụ trong cmd (gõ trong cùng cửa sổ trước khi chạy `mvnw`):

```cmd
set DB_USERNAME=root
set DB_PASSWORD=matkhaucuaban
mvnw spring-boot:run
```

Trong IntelliJ: **Run → Edit Configurations → Environment variables**.

**Dữ liệu test nhiều chuyến** (`du_lieu_test_nhieu_chuyen.sql`). Mở file trong DBeaver và bấm **Execute SQL Script** (Alt+X). Chạy lại nhiều lần được, mỗi lần tạo thêm một bộ chuyến. File tạo ra:

| Dữ liệu | Dùng cho mục |
|---------|--------------|
| Tài khoản `quanly01` (Quản lý), `dieuphoi01` (Điều phối), `taixe02` (Tài xế); mật khẩu đều là `123456789` | Đăng nhập web, 1.2.5 |
| Đơn A giao **ngày mai**, 2 chuyến Chờ nhận của `taixe01` | 1.1.7 lọc ngày, 1.3.5, 1.5.7 |
| Đơn B giao **3 ngày trước**, 1 chuyến Hoàn thành | 1.1.7 lọc trạng thái/ngày |
| Đơn C, 1 chuyến của `taixe02` | 1.2.5 |
| Tọa độ cho trạm trộn mẫu (cách công trình khoảng 4 km) | 1.2.4 bản đồ và tuyến đường |

> **Về tài khoản admin:** web quản trị (`frontend-web`) hiện chưa có màn hình tạo chuyến, nên cách nhanh nhất để có thêm chuyến là chạy file SQL trên. Nếu muốn tạo chuyến bằng API: đăng nhập `dieuphoi01` ở Swagger (`POST /api/auth/dang-nhap`, header `ClientKey`), bấm **Authorize** rồi gọi `POST /api/dieu-phoi/xe/chuyen`.

**SQL tiện dùng khi test:**

```sql
-- Đặt lại 1 chuyến về Chờ nhận để test lại từ đầu
UPDATE chuyen SET trang_thai = 0, thoi_gian_nhan = NULL, thoi_gian_xuat_phat = NULL, thoi_gian_den = NULL,
  vi_do_den = NULL, kinh_do_den = NULL, khoang_cach_den = NULL, ghi_chu_den = NULL, can_kiem_tra_den = NULL,
  khoi_luong_thuc_giao = NULL, thoi_gian_giao_xong = NULL, ghi_chu_giao_hang = NULL, anh_minh_chung = NULL,
  thoi_gian_hoan_thanh = NULL
WHERE id_chuyen = <id>;

-- Tạo thêm 1 chuyến Chờ nhận cùng đơn hàng (để test luồng 5.b và đơn nhiều chuyến)
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai)
SELECT iddh, id_xe, idtx, id_tram, 4, 0 FROM chuyen ORDER BY id_chuyen LIMIT 1;

-- Xem kết quả
SELECT id_chuyen, trang_thai, khoang_cach_den, can_kiem_tra_den, khoi_luong_thuc_giao FROM chuyen;
SELECT * FROM vi_tri_gps ORDER BY id_vi_tri DESC LIMIT 20;
SELECT id_su_co, id_chuyen, loai_su_co, muc_do_uu_tien, trang_thai, dia_chi_hu FROM su_co ORDER BY id_su_co DESC;
SELECT iddh, trang_thai FROM don_hang;  -- 4 = Hoàn thành
```

**Giả lập GPS trên emulator:** bấm **⋯ (Extended controls) → Location**, nhập tọa độ rồi bấm **Set location**.
- Tọa độ công trình mẫu: `21.0045, 105.7985`.
- Trong bán kính 500 m: `21.0050, 105.7990` (cách khoảng 76 m).
- Ngoài bán kính: `21.0300, 105.8500` (cách khoảng 6 km).

> Mẹo: sau khi đổi vị trí, mở Google Maps trên emulator một lần để thiết bị ghi nhận vị trí mới.

---

## 1. Quản lý chuyến (2.2.1)

### 1.1 Xem danh sách chuyến được phân công

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.1.1 | Trang chủ → **Lịch trình** | Hiện danh sách chuyến của `taixe01` | 🟢 | |
| 1.1.2 | Kiểm tra từng dòng | Có mã chuyến, tên công trình, khối lượng, giờ giao dự kiến, biển số, nhãn trạng thái có màu | 🟢 | |
| 1.1.3 | Vuốt xuống để tải lại | Danh sách được làm mới | 🔵 | |
| 1.1.4 | Tài xế không có chuyến nào | Hiện thông báo "chưa có chuyến" | 🔵 | |
| 1.1.5 | **Đăng xuất, đăng nhập lại** (để xóa dữ liệu đã lưu), tắt backend rồi mở Lịch trình (luồng 3.a, chưa từng đồng bộ) | Sau tối đa khoảng 8 giây hiện màn hình **"Không kết nối được máy chủ"** (biểu tượng đỏ) và nút **Thử lại**. Không còn hiện nhầm "Chưa có chuyến" | 🔵 | |
| 1.1.6 | Bật backend, mở Lịch trình (tab Tất cả) để đồng bộ. Tắt backend, vuốt tải lại (luồng 3.a) | Vẫn thấy danh sách cũ, phía trên có thanh vàng **"Không có kết nối mạng. Đang hiển thị dữ liệu đã lưu lúc HH:mm dd/MM"** | 🔵 | |
| 1.1.6b | Vẫn tắt backend, bấm chip "Chờ nhận" | Lọc ngay trên dữ liệu đã lưu, thanh vàng vẫn hiện | 🔵 | |
| 1.1.6c | Bật lại backend, vuốt tải lại | Thanh vàng biến mất, dữ liệu mới nhất | 🔵 | |
| 1.1.7 | Bước 5: bấm từng chip trạng thái (Chờ nhận, Đã nhận, …, Hoàn thành) | Chỉ hiện chuyến đúng trạng thái | 🟢 API | |
| 1.1.7b | Bấm chip **"Ngày giao: tất cả"** → chọn ngày mai | Chỉ hiện chuyến của Đơn A; chip đổi thành "Ngày giao: dd/MM/yyyy" có dấu ✕ | 🟢 API | |
| 1.1.7c | Chọn trạng thái "Hoàn thành" + ngày mai | Hiện "Không có chuyến phù hợp" (khác với "Chưa có chuyến nào được phân công") | 🟢 API | |
| 1.1.7d | Bấm ✕ trên chip ngày | Bỏ lọc ngày | 🔵 | |
| 1.1.8 | Đăng nhập tài xế khác | Không thấy chuyến của `taixe01` | 🟢 | |
| 1.1.8b | Luồng 4.b phiên hết hạn: đang đăng nhập app, **tắt backend**, trong cmd đặt `set JWT_SECRET=chuoi-bi-mat-moi-de-test-het-han-123456` rồi chạy lại `mvnw spring-boot:run` (token cũ trên app thành không hợp lệ). Vào Lịch trình hoặc vuốt tải lại | Quay về màn hình **Đăng nhập** kèm thông báo phiên đã hết hạn. Đăng nhập lại là dùng được | 🟢 API trả 401 | |

### 1.2 Xem thông tin chi tiết chuyến

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.2.1 | Bấm vào 1 chuyến | Có mã chuyến, trạng thái, tên và địa chỉ công trình, mác bê tông, khối lượng, **trạm trộn kèm địa chỉ**, biển số, **giờ giao dự kiến**, thời gian xuất phát, ghi chú điều phối | 🟢 | |
| 1.2.2 | Bấm **Gọi** | Mở trình gọi điện với SĐT công trình | 🔵 | |
| 1.2.3 | Bấm **Chỉ đường** (luồng 4.a) | Mở ứng dụng bản đồ ngoài tại tọa độ công trình | 🔵 | |
| 1.2.4 | Bước 4: mở chuyến của Đơn A | Thẻ **bản đồ** (OpenStreetMap) có ghim công trình, biểu tượng trạm trộn, biểu tượng xe (nếu có vị trí), **đường màu tím** là tuyến gợi ý. Dòng dưới: "Tuyến gợi ý từ trạm trộn: x km, khoảng y phút" | 🔵 Cần internet trên máy ảo | |
| 1.2.4b | Kéo/zoom bản đồ bằng 2 ngón (trên máy ảo: giữ Ctrl + kéo chuột để zoom) | Bản đồ di chuyển, màn hình không bị cuộn theo | 🔵 | |
| 1.2.4c | Luồng 4.a: tắt mạng máy ảo (kéo thanh thông báo → tắt Wi-Fi/Data) rồi mở một chuyến **khác** | Dòng dưới bản đồ: "Không lấy được tuyến đường, đang hiển thị đường thẳng tham khảo…", vẫn dùng được nút **Chỉ đường** | 🔵 | |
| 1.2.4d | Chuyến mẫu ban đầu khi trạm chưa có tọa độ (`UPDATE tram_tron SET vi_do=NULL`) | "Trạm trộn chưa có tọa độ nên chưa vẽ được tuyến đường…" | 🔵 | |
| 1.2.5 | Luồng 2.b: xem hướng dẫn **"Cách test 1.2.5"** bên dưới | Hộp thoại **"Chuyến không còn hiệu lực"**, nút "Về danh sách" | 🟢 API trả 404 kèm thông báo | |
| 1.2.6 | Tắt backend, vuốt tải lại (luồng 2.a) | Báo lỗi, vuốt lại để thử | 🔵 | |

**Cách test 1.2.5** (chuyến bị chuyển cho tài xế khác khi đang xem):

1. Trên app (`taixe01`), mở chi tiết một chuyến Chờ nhận, ví dụ chuyến của Đơn A. Ghi lại **mã chuyến** ở đầu màn hình, ví dụ `Chuyến #4`.
2. Giữ nguyên màn hình đó. Trong DBeaver chạy (thay `4` bằng mã chuyến của bạn):
   ```sql
   UPDATE chuyen
   SET idtx = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe02'))
   WHERE id_chuyen = 4;
   ```
3. Quay lại app, **vuốt xuống** để tải lại → phải hiện hộp thoại "Chuyến không còn hiệu lực". Bấm "Về danh sách" → chuyến #4 không còn trong danh sách.
4. Trả chuyến lại cho `taixe01` để test tiếp:
   ```sql
   UPDATE chuyen
   SET idtx = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe01'))
   WHERE id_chuyen = 4;
   ```

### 1.3 Nhận chuyến (Chờ nhận → Đã nhận)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.3.1 | Mở chuyến Chờ nhận | Nút **Nhận chuyến**; **không** có nút "Sự cố" | 🔵 | |
| 1.3.2 | Bấm → **Hủy** (4.a) | Trạng thái giữ nguyên | 🔵 | |
| 1.3.3 | Bấm → **Xác nhận** | Nhãn "Đã nhận", nút đổi thành **Bắt đầu chuyến**, xuất hiện nút **Sự cố**. DB `trang_thai = 1`, có `thoi_gian_nhan` | 🟢 | |
| 1.3.4 | Luồng 5.a: mở chi tiết, đổi `trang_thai` trong DB sang 1, bấm Nhận | Snackbar "Chuyến không còn ở trạng thái Chờ nhận", màn hình tự tải lại | 🟢 API trả 409 | |
| 1.3.5 | Luồng 5.b: đã nhận 1 chuyến, mở chuyến Chờ nhận thứ hai và bấm Nhận | Snackbar **"Bạn đang có chuyến chưa hoàn thành, không thể nhận thêm chuyến"** | 🟢 | |
| 1.3.6 | Luồng 6.a: chế độ máy bay → bấm Nhận | Lưu tạm, gửi lại khi có mạng | ⚪ Hiện chỉ báo lỗi mạng | |

### 1.4 Bắt đầu chuyến (Đã nhận → Đang giao)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.4.1 | Gỡ quyền vị trí của app → bấm **Bắt đầu chuyến** | Hộp thoại xin quyền vị trí | 🔵 | |
| 1.4.2 | Từ chối quyền (4.a) | "Cần cấp quyền vị trí…", không bắt đầu chuyến | 🔵 | |
| 1.4.3 | Tắt Location của emulator → bấm Bắt đầu (3.a) | "Vui lòng bật GPS…" | 🔵 | |
| 1.4.4 | Cấp quyền, bật GPS → Xác nhận | Trạng thái "Đang giao", nút đổi thành **Đã đến công trình**, có thông báo "Đang gửi vị trí GPS". DB có `thoi_gian_xuat_phat` | 🟢 API / 🔵 thông báo | |
| 1.4.5 | Đợi khoảng 20 giây, đổi tọa độ 2–3 lần | `vi_tri_gps` có thêm bản ghi, khoảng 7 giây một bản ghi | 🟢 WebSocket / 🔵 trên máy | |

### 1.5 Hoàn thành chuyến (Đã giao hàng → Hoàn thành)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 1.5.1 | Chuyến ở Đang giao hoặc Đã đến | **Không** có nút Hoàn thành (nút lúc này là "Đã đến công trình" hoặc "Xác nhận giao hàng") | 🔵 | |
| 1.5.2 | Luồng 5.a: gọi thẳng API hoàn thành khi chưa giao hàng (Swagger: `POST /api/tai-xe/chuyen/{id}/hoan-thanh`) | 409 "Chuyến chưa được xác nhận giao hàng thành công…" | 🟢 | |
| 1.5.3 | Chuyến Đã giao hàng → **Hoàn thành chuyến** | Hộp thoại có **tóm tắt**: khối lượng đã giao, giờ xuất phát, giờ giao xong, thời gian thực hiện | 🔵 | |
| 1.5.4 | Bấm **Hủy** (4.a) | Trạng thái giữ nguyên | 🔵 | |
| 1.5.5 | Bấm **Hoàn thành** | Nhãn "Hoàn thành", thanh nút biến mất, thông báo GPS tắt. Thẻ "Tiến độ giao hàng" có dòng "Hoàn thành lúc" | 🟢 API / 🔵 GPS | |
| 1.5.6 | Kiểm tra DB | `tai_xe.trang_thai = 1`, `xe.trang_thai = 1`, có `thoi_gian_hoan_thanh` | 🟢 | |
| 1.5.7 | Đơn hàng có 2 chuyến: hoàn thành chuyến thứ nhất | `don_hang.trang_thai` **chưa** thành 4. Hoàn thành chuyến cuối thì mới thành 4 | 🟢 Unit test | |
| 1.5.8 | Luồng 5.b: hoàn thành khi mất mạng | | ⚪ | |

---

## 2. Gửi vị trí GPS (2.2.2)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 2.1 | Trang chủ → **Gửi vị trí GPS** → bật công tắc khi không có chuyến đang thực hiện | "Đã bật định vị, chưa có chuyến đang giao", hiện tọa độ; DB không có bản ghi mới | 🔵 | |
| 2.2 | Bật khi chưa có quyền → từ chối (3.a) | Báo không thể bật, công tắc về tắt | 🔵 | |
| 2.3 | Bật khi GPS thiết bị tắt (3.b) | Hộp thoại "GPS đang tắt" + nút "Mở cài đặt" | 🔵 | |
| 2.4 | Có chuyến Đang giao / Đã đến / Đã giao hàng → bật | "Đang gửi vị trí cho chuyến #…" | 🔵 | |
| 2.5 | **Sau khi bấm "Đã đến công trình"**, đợi 20 giây | Vẫn tiếp tục có bản ghi `vi_tri_gps` (gửi tới khi chuyến kết thúc) | 🟢 WebSocket nhận điểm khi chuyến ở trạng thái Đã đến | |
| 2.6 | Tắt công tắc khi chuyến đang thực hiện (3.a của Tắt định vị) | Cảnh báo "…đang thực hiện, không thể tắt định vị…", công tắc vẫn bật | 🔵 | |
| 2.7 | Tắt khi không có chuyến → **Hủy** (3.b) | Công tắc vẫn bật | 🔵 | |
| 2.8 | Tắt → **Xác nhận** | Dịch vụ dừng, thông báo biến mất | 🔵 | |
| 2.9 | Đang gửi thì bật chế độ máy bay khoảng 30 giây rồi tắt (3.a của Gửi tọa độ) | DB nhận bù các điểm bị lỡ (tối đa 50 điểm, lưu trong RAM) | 🔵 | |
| 2.10 | Đang gửi thì vuốt tắt app khỏi đa nhiệm | Dịch vụ vẫn gửi đúng chuyến (`idChuyen` được lưu lại khi dịch vụ khởi động lại) | 🔵 | |
| 2.11 | Đặt tọa độ `0,0` (4.a) | Server bỏ qua điểm, ghi log cảnh báo | 🟢 | |
| 2.12 | Gửi vị trí cho chuyến đã Hoàn thành | Server từ chối: "Chuyến không còn đang thực hiện, bỏ qua vị trí" | 🟢 | |
| 2.13 | Luồng 1.a: cảnh báo tín hiệu GPS yếu | | ⚪ | |
| 2.14 | Bước 5–6: bản đồ theo dõi trên web cập nhật realtime | | ⚪ Server lưu DB, chưa đẩy realtime sang web | |

---

## 3. Cập nhật trạng thái (2.2.3)

### 3.1 Cập nhật trạng thái Đang giao

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 3.1.1 | Giống mục 1.4: nút **Bắt đầu chuyến** chuyển chuyến từ Đã nhận sang Đang giao | Như 1.4.4. Trong báo cáo nên ghi rõ usecase này dùng chung nút với "Bắt đầu chuyến" | 🟢 | |
| 3.1.2 | Luồng 3.a: chuyển trạng thái không hợp lệ (đổi DB sang 5 rồi bấm) | Snackbar báo lỗi, tự tải lại | 🟢 API trả 409 | |

### 3.2 Xác nhận đã đến công trình (Đang giao → Đã đến công trình) — **MỚI**

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 3.2.1 | Chuyến Đang giao | Nút chính là **Đã đến công trình** | 🔵 | |
| 3.2.2 | Đặt tọa độ emulator **trong 500 m** (`21.0050, 105.7990`) → bấm → Xác nhận | Snackbar "Đã xác nhận đến công trình". Nhãn "Đã đến công trình". Thẻ "Tiến độ giao hàng" hiện "Đã đến công trình: dd/MM HH:mm · cách ~76 m" | 🟢 | |
| 3.2.3 | Kiểm tra DB | `trang_thai = 3`, có `thoi_gian_den`, `vi_do_den`, `kinh_do_den`, `can_kiem_tra_den = 0` | 🟢 | |
| 3.2.4 | Luồng 2.a: đặt tọa độ **ngoài 500 m** (`21.03, 105.85`) → bấm → Xác nhận | Hộp thoại **"Cần ghi chú để xác nhận"**: "Bạn đang cách công trình khoảng 6051 m (ngoài bán kính 500 m)…" | 🟢 | |
| 3.2.5 | Để trống ghi chú → **Gửi xác nhận** | Báo lỗi "Vui lòng nhập ghi chú", hộp thoại không đóng | 🔵 | |
| 3.2.6 | Nhập ghi chú → Gửi | Thành công, Snackbar "(điều phối sẽ kiểm tra lại vị trí)". Chi tiết hiện "· cần kiểm tra" và dòng "Ghi chú khi đến". DB `can_kiem_tra_den = 1` | 🟢 | |
| 3.2.7 | Luồng 2.b: gỡ quyền vị trí → bấm → từ chối quyền | Hộp thoại ghi chú "Không lấy được vị trí hiện tại…" → nhập ghi chú → thành công, cần kiểm tra | 🟢 API / 🔵 app | |
| 3.2.8 | Luồng 2.b: emulator chưa có vị trí nào (cold boot, chưa Set location) | Như 3.2.7 | 🔵 | |
| 3.2.9 | Luồng 3.a: chế độ máy bay → bấm | Snackbar lỗi mạng (chưa lưu tạm) | 🔵 / ⚪ phần lưu tạm | |

### 3.3 Xác nhận giao hàng thành công (Đã đến → Đã giao hàng) — **MỚI**

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 3.3.1 | Chuyến Đã đến công trình → bấm **Xác nhận giao hàng** | Mở màn **Xác nhận giao hàng** (phụ đề "Chuyến #…"): khối lượng của chuyến, ô khối lượng thực giao (điền sẵn), thời điểm kết thúc, ghi chú, chọn ảnh | 🔵 | |
| 3.3.2 | Luồng 4.a: xóa trống ô khối lượng → Xác nhận | Lỗi đỏ "Vui lòng nhập khối lượng thực giao lớn hơn 0" | 🟢 API / 🔵 app | |
| 3.3.3 | Nhập `0` → Xác nhận | Như 3.3.2 | 🟢 | |
| 3.3.4 | Luồng 4.b: nhập **lớn hơn** khối lượng chuyến (ví dụ 9 khi chuyến là 8.5), không ghi chú | Lỗi ở ô ghi chú: "Khối lượng vượt 8.5 m³ của chuyến, vui lòng nhập lý do" | 🟢 API trả 422 / 🔵 app | |
| 3.3.5 | Nhập ghi chú → Xác nhận | Thành công | 🟢 | |
| 3.3.6 | Chọn ảnh JPG/PNG → Xác nhận | Có ảnh xem trước. Sau khi gửi, DB `anh_minh_chung` là URL `/uploads/giao-hang/...`, mở được trên trình duyệt | 🟢 | |
| 3.3.7 | Chọn ảnh lớn hơn 5 MB hoặc ảnh dạng HEIC | Báo "Ảnh vượt quá 5 MB…" hoặc "Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP" | 🟢 API / 🔵 app | |
| 3.3.8 | Gửi hợp lệ | Quay về chi tiết, Snackbar "Xác nhận giao hàng thành công". Nhãn "Đã giao hàng", nút đổi thành **Hoàn thành chuyến**. Thẻ tiến độ có "Khối lượng thực giao" và "Đơn hàng đã giao x / y m³" | 🟢 | |
| 3.3.9 | Bước 6: kiểm tra log backend | Có dòng "Chuyến #… đã giao 8.5 m³. Đơn hàng đã giao 8.5/8.5 m³" | 🟢 | |
| 3.3.10 | Bấm **Hủy** trên màn xác nhận | Quay lại, trạng thái giữ nguyên | 🔵 | |
| 3.3.11 | Luồng 5.a: mất mạng khi gửi | Snackbar lỗi mạng, nút bấm được lại | 🔵 / ⚪ phần lưu tạm | |

### 3.4 Báo cáo sự cố — **MỚI** (trước đây chỉ có giao diện)

| # | Thao tác | Mong đợi | Tình trạng | Pass/Fail |
|---|----------|----------|-----------|-----------|
| 3.4.1 | Chi tiết chuyến (Đã nhận → Đã giao hàng) → nút **Sự cố** | Mở form, dòng đầu hiện "Chuyến #…" | 🔵 | |
| 3.4.2 | Trang chủ → **Báo cáo sự cố** | Dòng đầu hiện "Chuyến đang thực hiện (hệ thống tự xác định)" | 🔵 | |
| 3.4.3 | Form có đủ các trường | Loại sự cố (Hỏng xe / Tai nạn / Tắc đường kéo dài / Công trình chưa sẵn sàng / Khác), Mức độ ưu tiên (Thấp / **Trung bình** mặc định / Cao), vị trí GPS, mô tả, ảnh | 🔵 | |
| 3.4.4 | Đã có quyền vị trí | Ô vị trí tự điền tọa độ khi mở form; bấm "Lấy vị trí hiện tại" để cập nhật | 🔵 | |
| 3.4.5 | Luồng 4.a: không chọn loại, không nhập mô tả → Gửi | Lỗi "Vui lòng chọn loại sự cố" và "Vui lòng mô tả sự cố" | 🟢 API / 🔵 app | |
| 3.4.6 | Chọn **Hỏng xe** hoặc **Tai nạn** | Mức độ tự chuyển sang **Cao** (luồng 6.a) | 🟢 API / 🔵 app | |
| 3.4.7 | Điền đủ + ảnh → **Gửi báo cáo** | Hộp thoại "Đã gửi báo cáo sự cố": mã sự cố, chuyến, loại, mức độ, **Trạng thái xử lý: Mới tiếp nhận** | 🟢 | |
| 3.4.8 | Kiểm tra DB `su_co` | Có bản ghi gắn `id_chuyen`, `id_xe`, `idtx`, `dia_chi_hu` = tọa độ, `muc_do_uu_tien`, `trang_thai = 0`, URL ảnh | 🟢 | |
| 3.4.9 | Bước 6: log backend | Có "Sự cố #… cần Nhân viên điều phối xử lý", hoặc "[KHẨN] … cần điều động xe thay thế" nếu là Hỏng xe / Tai nạn | 🟢 | |
| 3.4.10 | Từ trang chủ khi **không** có chuyến đang thực hiện → Gửi | Snackbar "Bạn không có chuyến nào đang thực hiện để báo cáo sự cố" | 🟢 | |
| 3.4.11 | Đã nhập dữ liệu rồi bấm Back | Hỏi "Thoát báo cáo?" | 🔵 | |
| 3.4.12 | Luồng 4.b: mất mạng khi gửi | Snackbar lỗi mạng | 🔵 / ⚪ phần lưu tạm | |
| 3.4.13 | Thông báo đẩy (push) thật tới điều phối | | ⚪ Hệ thống hiện chỉ ghi log, giống các thông báo khác của dự án | |

---

## 4. Kịch bản test trọn một chuyến (end-to-end)

Chạy liền một mạch để kiểm tra toàn bộ luồng:

1. Đăng nhập `taixe01` → Lịch trình → chuyến Chờ nhận.
2. **Nhận chuyến** → **Bắt đầu chuyến** (cấp quyền, bật GPS) → thấy thông báo GPS.
3. Đặt vị trí emulator trong bán kính → **Đã đến công trình**.
4. Bấm **Sự cố** → Tắc đường kéo dài → mô tả → Gửi → Xong.
5. **Xác nhận giao hàng** → 8.5 → chọn ảnh → Xác nhận.
6. **Hoàn thành chuyến** → kiểm tra hộp thoại tóm tắt → Hoàn thành → thông báo GPS tắt.
7. Kiểm tra DB: `chuyen.trang_thai = 5`, `don_hang.trang_thai = 4`, `xe/tai_xe.trang_thai = 1`, `vi_tri_gps` có điểm, `su_co` có 1 bản ghi.

| Kết quả end-to-end | Pass/Fail |
|--------------------|-----------|
| Toàn bộ 7 bước | |

---

## 5. Tóm tắt tiến độ so với báo cáo

| Nhóm | Usecase | Backend | App | Còn thiếu |
|------|---------|---------|-----|-----------|
| Quản lý chuyến | Xem danh sách | ✅ lọc trạng thái + ngày | ✅ bộ lọc, dữ liệu lưu khi mất mạng, màn lỗi riêng | |
| | Xem chi tiết | ✅ | ✅ bản đồ + tuyến gợi ý | |
| | Nhận chuyến | ✅ có 5.b | ✅ | Offline (6.a) |
| | Bắt đầu chuyến | ✅ | ✅ | |
| | Hoàn thành chuyến | ✅ đúng 5.a | ✅ có tóm tắt | Offline (5.b) |
| Gửi vị trí GPS | Bật / Tắt định vị | – | ✅ | |
| | Gửi tọa độ realtime | ✅ tới khi chuyến kết thúc | ✅ | Cảnh báo GPS yếu, đẩy realtime sang web |
| Cập nhật trạng thái | Đang giao | ✅ | ✅ | Dùng chung nút Bắt đầu chuyến |
| | **Đã đến công trình** | ✅ bán kính 500 m | ✅ | Offline |
| | **Xác nhận giao hàng** | ✅ | ✅ màn hình mới | Offline |
| | **Báo cáo sự cố** | ✅ API mới | ✅ nối API | Push thật, offline |

Ghi chú về cách đã kiểm tra:
- **Backend:** biên dịch thành công, 18 unit test pass, và đã chạy thật trên MariaDB với đủ các trường hợp đúng/sai cho cả 4 API mới.
- **App Android:** code Java đã được kiểm tra lỗi biên dịch, nhưng **chưa được build bằng Gradle và chưa chạy trên thiết bị**, vì môi trường của mình không tải được Android SDK. Hãy chạy mục 0.5 trước tiên.
