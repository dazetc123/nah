# Luyện vấn đáp – Test chức năng Tài xế trên App Android

Cách dùng:
- Chạy app trên máy ảo theo bảng 21 bước trong `HUONG_DAN_TEST_APP_VA_WEB.md` (phần C). Mã ca khớp với file `Kiem-thu-chuc-nang-Tai-xe.docx`.
- Mỗi phần dưới đây là **những gì giáo viên hay hỏi khi bạn đang demo bước đó**. Tự trả lời thành tiếng trước, rồi mới xem đáp án.
- Chỗ nào ghi **(theo code)** là rút ra từ việc đọc code, nên tự kiểm chứng lại trên máy ảo.

---

## 0. Môi trường chạy app

**H: App kết nối tới máy chủ bằng địa chỉ nào? Sao không phải `localhost`?**
Đ: Mặc định là `http://10.0.2.2:8080/` (trong `ApiClient.java`). Trong máy ảo Android, `localhost` là **chính máy ảo**. Địa chỉ `10.0.2.2` là địa chỉ đặc biệt để máy ảo trỏ ra **máy tính thật** đang chạy backend. Muốn dùng điện thoại thật thì vào **"⚙️ Cấu hình địa chỉ máy chủ"** ở màn đăng nhập và nhập IP LAN của máy tính, ví dụ `http://192.168.1.x:8080/`.

**H: App gọi API thế nào?**
Đ: Dùng thư viện **Retrofit** (chạy trên OkHttp). Mỗi request tự gắn 2 header:
- `ClientKey: betongmobile`: mã nhận diện app, thiếu thì máy chủ trả 400.
- `Authorization: Bearer <token>`: token đăng nhập.

Thời gian chờ kết nối 15 giây, chờ đọc 20 giây.

**H: Làm sao test GPS trên máy ảo khi ngồi ở nhà?**
Đ: Máy ảo → **⋯ (Extended controls)** → **Location** → nhập vĩ độ, kinh độ → **Set location**. Ví dụ đặt trạm trộn `21.0362, 105.7906` để thử "còn xa", đặt `21.0046, 105.7984` để thử "đã tới gần".

**H: Muốn xem app đang làm gì bên trong thì xem ở đâu?**
Đ: Cửa sổ **Logcat** trong Android Studio. Ví dụ lọc `TrackingService` sẽ thấy dòng *"Đã gửi GPS (200): …"* mỗi lần gửi vị trí.

---

## 1. Đăng nhập (DN-01 → DN-05)

**H: Bỏ trống hoặc nhập mật khẩu 7 ký tự, lỗi được kiểm tra ở đâu?**
Đ: Ngay trên app (`LoginActivity`), **chưa gửi lên máy chủ**. App hiện chữ đỏ dưới ô nhập: *"Vui lòng nhập tên đăng nhập"*, *"Vui lòng nhập mật khẩu"*, *"Mật khẩu phải từ 8 ký tự trở lên"*. Kiểm tra trên app giúp báo lỗi nhanh và không tốn request.

**H: Nhập sai mật khẩu thì sao?**
Đ: App gửi lên máy chủ. Máy chủ so mật khẩu với bản đã mã hoá **BCrypt** trong database, sai thì trả 401 *"Tên đăng nhập/email hoặc mật khẩu không đúng"*. App hiện câu đó dưới ô mật khẩu.

**H: Tài khoản điều phối đăng nhập app được không?**
Đ: Máy chủ vẫn đăng nhập thành công và trả về vai trò "Nhân viên điều phối". Nhưng app kiểm tra vai trò: khác "Tài xế" thì báo *"Ứng dụng này chỉ dành cho tài khoản Tài xế"* và không lưu phiên. Đây là chặn **ở phía app**. Phía máy chủ còn phân quyền riêng theo vai trò trên từng API.

**H: Token đăng nhập lưu ở đâu? Có an toàn không?**
Đ: Lưu trong `SharedPreferences` qua lớp `SessionManager`, **đã mã hoá AES-256-GCM** bằng khoá nằm trong **Android Keystore** (kho khoá của hệ điều hành). App khác không đọc được token. Lần sau mở app, nếu còn token thì vào thẳng trang chủ, không phải đăng nhập lại.

**H: Tài khoản mới được cấp lần đầu đăng nhập thì sao?**
Đ: Nếu máy chủ báo `phaiDoiMatKhau = true`, app chuyển sang màn **đổi mật khẩu lần đầu** (`FirstPasswordActivity`) trước khi dùng các chức năng khác.

---

## 2. Quyền vị trí và GPS (GPS-01, 02, 03, 06)

**H: App xin những quyền gì? Vì sao?**
Đ:
- **Vị trí** (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`): để gửi GPS và kiểm tra đã tới công trình chưa.
- **Thông báo** (`POST_NOTIFICATIONS`, Android 13 trở lên): để hiện thông báo dịch vụ định vị đang chạy.

Android từ bản 6 bắt buộc **xin quyền lúc chạy**, nên lần đầu mở Lịch trình mới thấy hộp thoại xin quyền.

**H: Từ chối quyền vị trí thì app xử lý thế nào?**
Đ: App không bị văng, hiện *"Chưa cấp quyền vị trí"* và không gửi vị trí. Muốn bật lại thì vào Settings → Apps → app → Permissions → Location.

**H: Tắt GPS điện thoại thì sao?**
Đ: App nhắc *"Hãy bật định vị (GPS) trên điện thoại"*.

**H: Vì sao tắt màn hình hoặc bấm Home mà vẫn gửi được vị trí?**
Đ: App dùng **foreground service** (`TrackingService`): dịch vụ chạy nền có thông báo cố định *"Đang thực hiện chuyến giao bê tông – Đang chia sẻ vị trí xe theo thời gian thực"*. Từ Android 8, app chạy nền mà không có thông báo như vậy sẽ bị hệ điều hành hạn chế hoặc tắt. Dịch vụ này khai báo kiểu `location` trong `AndroidManifest.xml`.

**H: Bao lâu gửi một lần? Có lọc gì không?**
Đ: Lấy GPS khoảng **2 giây/lần**, định vị mạng 4 giây/lần làm dự phòng. Trước khi gửi, app lọc:
- Hai lần gửi cách nhau dưới 1,2 giây thì bỏ lần sau.
- Tọa độ **ngoài Việt Nam** thì bỏ qua (máy ảo mặc định đặt ở Mỹ).
- Dịch chuyển dưới 2,5 m thì tính tốc độ là 0.

Sau đó app gửi `POST /api/tai-xe/gps/ping`. Máy chủ ghi Redis, đẩy WebSocket cho bản đồ điều phối, và lưu lịch sử vào `vi_tri_gps`.

**H: Làm sao chứng minh app đang gửi vị trí?**
Đ: Có 3 cách:
1. Logcat có dòng *"Đã gửi GPS (200)"*.
2. CMD chạy `redis-cli get xe:location:1`: tọa độ đổi theo vị trí máy ảo.
3. Chạy `SELECT COUNT(*) FROM vi_tri_gps WHERE id_chuyen = …;`: số dòng tăng dần.

---

## 3. Nhận chuyến (QLC-01 → QLC-08)

**H: Điều phối vừa tạo chuyến, sao app biết ngay?**
Đ: Lớp `OfferWatcher` hỏi máy chủ **mỗi 3,5 giây** (polling): `GET /api/tai-xe/chuyen/cua-toi?nhom=cho-xac-nhan`. Mất mạng thì giãn ra 8 giây/lần cho đỡ tốn pin. Có chuyến mới thì app:
- phát **âm thanh thông báo** mặc định của máy (`RingtoneManager`);
- **rung** theo nhịp (`Vibrator`);
- hiện hộp thoại có mã đơn, biển số và đồng hồ *"Còn mm:ss để trả lời"*.

**H: Đồng hồ 5 phút tính từ đâu?**
Đ: Từ cột `thoi_gian_phan_cong` của bảng `chuyen`. Máy chủ trả về `hanXacNhan` bằng thời điểm phân công cộng 5 phút, app đếm ngược tới mốc đó. Quá hạn thì tác vụ chạy định kỳ trên máy chủ (`ChuyenHetHanScheduler`, 30 giây/lần) chuyển chuyến sang **6 – Đã từ chối**.

**H: Vì sao app không dùng WebSocket mà hỏi liên tục?**
Đ: Trên điện thoại, mạng hay đổi qua lại giữa 4G và wifi, app chạy nền hay bị hệ điều hành tắt, nên giữ một kết nối mở liên tục dễ đứt. Polling đơn giản và ổn định, chậm tối đa khoảng 3,5 giây, không đáng kể so với hạn 5 phút. Cách tốt hơn là **thông báo đẩy Firebase (FCM)**.

**H: Từ chối chuyến có những lý do gì? Bấm "Quay lại" thì sao?**
Đ: Có 4 lý do: *Xe đang gặp sự cố*, *Tôi đang bận, không thể nhận*, *Công trình quá xa*, *Lý do khác*. Chọn lý do là gửi `PUT /phan-hoi` với `chapNhan = false`, chuyến sang 6 và điều phối được báo. Bấm **"Quay lại"** thì chỉ đóng danh sách lý do, hộp thoại đề nghị chuyến vẫn còn.

**H: Tài xế chưa có GPLX bấm nhận thì sao?**
Đ: Máy chủ trả 403. App hiện hộp thoại *"Cần bổ sung bằng lái xe (GPLX)"* với nút **"Cập nhật bằng lái"** để mở màn sửa hồ sơ. Chuyến vẫn chờ xác nhận.

**H: Nhận chuyến xong, trong hệ thống thay đổi gì?**
Đ:
- Chuyến 5 → **3 (Đang giao)**.
- Đơn hàng → **3 (Đang giao)**.
- Xe → **2 (Đang giao hàng)**.
- Điều phối và khách hàng nhận thông báo.

Trên app, `TrackingService` bắt đầu gửi GPS.

---

## 4. Màn Lịch trình, bản đồ, gọi điện (QLC-09 → QLC-13)

**H: Bản đồ dùng gì? Đường đi lấy ở đâu?**
Đ: Bản đồ **osmdroid** (dữ liệu OpenStreetMap). Đường đi từ trạm trộn tới công trình lấy từ dịch vụ **OSRM** trên mạng. Nếu không tải được, app vẽ tạm **đường thẳng** và tự thử tải lại sau 2,5 giây. Khi xe đi lệch tuyến quá 250 m, app tự tính lại đường (tối đa 15 giây/lần).

**H: Nút gọi khách hàng hoạt động thế nào? Có cần quyền gọi điện không?**
Đ: App mở **trình quay số** đã điền sẵn số khách (`Intent.ACTION_DIAL`), người dùng tự bấm gọi. Vì không gọi thẳng nên **không cần xin quyền** `CALL_PHONE`.

**H: Nút chỉ đường làm gì?**
Đ: Mở **Google Maps** chế độ dẫn đường tới tọa độ công trình (`google.navigation:q=…`). Máy không có Google Maps thì mở ứng dụng bản đồ khác (`geo:`). Không có ứng dụng nào thì báo *"Không tìm thấy ứng dụng bản đồ"*.

**H: Tắt máy chủ thì app ra sao?**
Đ: Retrofit báo lỗi mạng. App hiện *"Mất kết nối máy chủ"* và *"Không kết nối được máy chủ. Kiểm tra mạng và thử lại"*, **không bị văng**.

**H: Token hết hạn thì app làm gì?**
Đ: App được viết để khi nhận mã **401** thì xoá phiên, tắt dịch vụ GPS và quay về màn đăng nhập (`handleUnauthorized`). Nhưng kiểm thử tự động phát hiện **máy chủ lại trả 302** (chuyển sang trang Google) thay vì 401 (ca DN-06, Chưa đạt). Vì vậy app không nhận ra để quay về đăng nhập. Cách sửa nằm ở `SecurityConfig` phía máy chủ.

---

## 5. Đến công trình, hoàn thành, về trạm (CNTT-01 → 06, GPS-07, 08)

**H: Vào gần công trình thì app làm gì?**
Đ: App tính khoảng cách bằng công thức **Haversine** mỗi khi có vị trí mới. Dưới 200 m thì hiện gợi ý *"Bạn đã tới gần công trình (trong phạm vi 200m) — bấm "Đã đến công trình" để xác nhận"*. Đây chỉ là **gợi ý**, không phải chốt chặn.

**H: Vậy ai chặn khi còn xa?**
Đ: **Máy chủ.** App gửi tọa độ kèm `trangThai = 2`. Máy chủ (`kiemTraGanCongTrinh`) so với tọa độ công trình trong bảng `cong_trinh`, xa hơn 200 m thì trả *"Bạn chưa tới gần công trình (còn cách khoảng X m, cần trong 200 m)"*.

**H: Có lỗ hổng nào không? (câu hỏi sâu)**
Đ **(theo code)**: Trong `advanceStatus`, nếu app **chưa có vị trí** hoặc vị trí **ngoài Việt Nam**, app gửi luôn **tọa độ công trình** thay vị trí thật, nên máy chủ thấy khoảng cách bằng 0 và cho qua. Vì vậy khi test ca "còn xa" (CNTT-03), **phải đặt vị trí máy ảo về Hà Nội** (trạm trộn). Để vị trí mặc định ở Mỹ thì app sẽ cho xác nhận sai. Đề xuất sửa: không cho gửi khi chưa có GPS, phát hiện vị trí giả, đối chiếu với lịch sử hành trình.

**H: "Hoàn thành" và "Đã về tới trạm" khác nhau thế nào?**
Đ:
- **"Hoàn thành" (trạng thái 8):** đổ bê tông xong. Đơn hàng sang Hoàn thành, khách được báo, **app tắt dịch vụ GPS**. Xe vẫn bận vì đang chạy về.
- **"Đã về tới trạm" (trạng thái 4):** chuyến kết thúc. Xe về **1 (Sẵn sàng)**, vị trí xe đặt đúng tọa độ trạm trộn, app hiện hộp thoại *"Đã hoàn thành chuyến"*.

**H: Sau khi bấm "Hoàn thành", tiêu đề màn hình hiện gì?**
Đ **(theo code)**: nhiều khả năng vẫn hiện *"Tài xế đang đến"* và nhãn *"Đang giao"*, vì hàm `bindTrip` chưa có nhãn riêng cho trạng thái 8. Nên chụp màn hình lúc test. Nếu đúng như vậy thì đây là một lỗi giao diện nhỏ.

---

## 6. Báo cáo sự cố (CNTT-07 → 09)

**H: Báo sự cố trên app gồm những bước nào?**
Đ:
1. Bấm Báo sự cố → chọn lý do (*Xe hỏng hóc giữa đường*, *Nổ lốp / chết máy xe*, *Va chạm / tai nạn giao thông*, …) → **Tiếp tục**.
2. Nhập chi tiết (không bắt buộc) → **Xác nhận hủy chuyến**.

Ở bước 1 có nút **Đóng**, ở bước 2 có nút **Quay lại**: bấm vào thì không tạo sự cố.

**H: Báo sự cố xong hệ thống làm gì?**
Đ:
- Chuyến → **7 (Sự cố/Hủy)**, xe → **0 (Bảo trì)**.
- Đơn hàng quay về **6** để điều phối tạo chuyến thay thế.
- Ghi một bản sự cố mức ưu tiên 3 (cao).
- Thông báo khẩn cho điều phối và quản lý; khách hàng được báo giao chậm.
- App tắt dịch vụ GPS.

**H: Báo sự cố cho chuyến đã hoàn thành được không?**
Đ: Không. Máy chủ chặn: *"Chuyến hàng đã hoàn thành, không thể báo cáo sự cố để hủy"*.

---

## 7. Trạng thái xe và báo hỏng xe (CNTT-10 → 16)

**H: Màn Trạng thái xe gửi gì lên máy chủ?**
Đ: Chọn **Sẵn sàng hoạt động** (gửi `1`) hoặc **Đang bảo trì** (gửi `0`), rồi bấm **Cập nhật**. App gọi `PUT /api/tai-xe/xe/trang-thai`. Nút Cập nhật chỉ sáng khi chọn trạng thái **khác** trạng thái hiện tại.

**H: Đang chạy chuyến mà chuyển sang bảo trì được không?**
Đ: Không. Máy chủ trả 409 *"Xe đang thực hiện chuyến, hãy hoàn thành chuyến trước khi chuyển sang bảo trì"*. Nếu xe hỏng giữa đường thì phải dùng **Báo sự cố**.

**H: App và web khác nhau ở chức năng này thế nào?** (câu ăn điểm)
Đ: App gửi đúng mã **0 = bảo trì**, nên chạy đúng. Web ở tab "Xe phụ trách" lại gửi mã **2** (mã 2 là "Đang giao hàng"), nên máy chủ từ chối: *"Trạng thái xe chỉ được là sẵn sàng hoạt động (1) hoặc bảo trì (0)"*. Lỗi này tìm được khi test web (WCNTT-07).

**H: "Báo hỏng / sự cố" ở màn Trạng thái xe khác gì Báo sự cố trên chuyến?**
Đ:
- **Báo hỏng xe** (`FaultReportActivity`): báo cáo **tình trạng xe** gửi cho điều phối và quản lý. Có biển số, SĐT (app **tự điền sẵn** từ xe được phân công và hồ sơ), vị trí (bấm GPS thì điền địa chỉ), mô tả và **bắt buộc ảnh minh chứng**. Gửi multipart tới `POST /api/dieu-phoi/xe/bao-cao`.
- **Báo sự cố trên chuyến:** **hủy chuyến đang chạy** để điều phối sắp xe thay thế.

**H: Thiếu dữ liệu thì sao? Thoát giữa chừng thì sao?**
Đ:
- Thiếu dữ liệu: app báo ngay *"Vui lòng nhập biển số xe"*, *"Vui lòng mô tả sự cố"*, *"Vui lòng chọn ảnh minh chứng"* và không gửi.
- Đã nhập dở mà bấm quay lại: app hỏi *"Thoát báo cáo?"*, chọn **Ở lại** thì dữ liệu còn nguyên.

---

## 8. Câu hỏi tổng hợp

**H: Em test app bằng cách nào? Có test tự động không?**
Đ:
- **Test tay:** chạy trên máy ảo theo 21 bước, giả vị trí bằng Extended controls, đối chiếu kết quả trên màn hình, trong database và trong Redis.
- **Test tự động:**
  - Phía máy chủ có 30 test JUnit + MockMvc chạy bằng `mvnw test`.
  - Phía app có test giao diện **Espresso** mẫu cho màn đăng nhập: máy tự gõ, tự bấm, rồi kiểm tra dòng báo lỗi.

**H: Em tìm được lỗi gì trên app?**
Đ:
1. **Token hết hạn** không đưa về màn đăng nhập, vì máy chủ trả 302 thay vì 401 (DN-06).
2. **(theo code)** Chưa có GPS hoặc vị trí ngoài Việt Nam thì app gửi tọa độ công trình, nên xác nhận "Đã đến" bị cho qua sai.
3. **(theo code)** Sau khi "Hoàn thành", tiêu đề vẫn ghi "Tài xế đang đến".

> Ghi kết quả thật của bạn khi test ca 2 và 3 rồi hãy nói chắc chắn.

---

## 9. Kịch bản demo app 5 phút

1. **Đăng nhập** sai rồi đúng (`taixe01 / 123456789`). Nói: kiểm tra trên app, kiểm tra trên máy chủ, token mã hoá bằng Android Keystore.
2. Trên web, **điều phối tạo chuyến** → app **đổ chuông, rung, đếm ngược**. Nói: polling 3,5 giây, hạn 5 phút.
3. **Nhận chuyến** → thanh thông báo *"Đang chia sẻ vị trí…"*. Nói: foreground service; mở `redis-cli get xe:location:1` cho thấy vị trí.
4. Đặt vị trí **trạm trộn** → bấm "Đã đến", bị chặn 3619 m. Đặt **sát công trình** → được. Nói: máy chủ kiểm tra 200 m bằng Haversine.
5. **Hoàn thành** → **Đã về tới trạm**. Nói: trạng thái 8 và 4, xe về Sẵn sàng.
6. **Kết:** số ca đã test, số Đạt / Chưa đạt, lỗi tìm được.
