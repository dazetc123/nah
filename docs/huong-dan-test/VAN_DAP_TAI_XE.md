# Luyện vấn đáp – Kiểm thử chức năng Tài xế (Web + App)

Cách dùng tài liệu này:
1. Tự chạy từng ca trong `docs/Kiem-thu-chuc-nang-Tai-xe-Web.docx` (web) và `docs/Kiem-thu-chuc-nang-Tai-xe.docx` (app). Cách bật hệ thống và giả vị trí xem `HUONG_DAN_TEST_APP_VA_WEB.md`.
2. Đọc các câu hỏi dưới đây, **tự trả lời thành tiếng trước**, rồi mới xem đáp án gợi ý.
3. Khi vấn đáp, thường giáo viên bảo demo 1–2 ca rồi hỏi "vì sao", "nếu… thì sao". Phần 5 có kịch bản demo 5 phút.

---

## 1. Câu hỏi về cách thiết kế ca kiểm thử

**H: Em thiết kế các ca kiểm thử theo kỹ thuật nào?**
Đ: Kiểm thử hộp đen, dùng 4 kỹ thuật:
- **Phân vùng tương đương**: chia dữ liệu thành vùng hợp lệ và không hợp lệ, mỗi vùng thử 1 giá trị. Ví dụ đăng nhập: đúng tài khoản (WDN-01), bỏ trống (WDN-02), sai mật khẩu (WDN-04).
- **Phân tích giá trị biên**: thử ngay sát các mốc giới hạn. Hệ thống có 3 mốc: mật khẩu **8 ký tự**, bán kính **200 m**, hạn xác nhận **5 phút**.
- **Kiểm thử chuyển trạng thái**: chuyến đi qua các trạng thái 5 → 3 → 2 → 8 → 4, cộng 2 nhánh 6 (từ chối) và 7 (sự cố). Em thử cả bước chuyển đúng lẫn bước chuyển sai (ví dụ cập nhật "Đã đến" khi chưa nhận chuyến thì bị chặn).
- **Kiểm thử theo use case**: mỗi use case có luồng chính (nhận chuyến → giao → về trạm) và luồng phụ (từ chối, hết hạn, báo sự cố, mất GPS).

**H: Giá trị biên của bán kính 200 m em thử thế nào?**
Đ: Giả vị trí ngay trong và ngay ngoài mốc 200 m quanh công trình (21.0045, 105.7985):
| Vị trí giả lập | Cách công trình | Kết quả mong đợi |
|---|---|---|
| 21.0046, 105.7984 | ~11 m | Cho xác nhận |
| **21.00621, 105.7985** | **~190 m** | Cho xác nhận (vẫn trong 200 m) |
| **21.00639, 105.7985** | **~210 m** | Bị chặn, báo còn cách khoảng 210m |
| 21.0362, 105.7906 (trạm trộn) | ~3619 m | Bị chặn |

> Hai dòng in đậm là ca biên. Nên tự thử thêm để có câu trả lời chắc chắn khi bị hỏi.

**H: Giá trị biên của mật khẩu?**
Đ: 7 ký tự thì bị chặn ngay trên giao diện ("Mật khẩu phải có ít nhất 8 ký tự"). 8 ký tự thì giao diện cho gửi, sau đó server mới kiểm tra đúng hay sai.

**H: Sao có ca "Đánh giá: Chưa đạt"? Ca chưa đạt nghĩa là gì?**
Đ: Kết quả thực tế khác kết quả mong đợi, tức là tìm ra lỗi. Kiểm thử là để tìm lỗi, nên ca chưa đạt là có giá trị: ghi lại để nhóm sửa, rồi kiểm thử lại (**kiểm thử hồi quy**).

---

## 2. Câu hỏi "chuyện gì xảy ra bên trong" khi demo

**H: Khi điều phối tạo chuyến, tài xế biết bằng cách nào?**
Đ: Chuyến được tạo ở trạng thái **5 – Chờ xác nhận**. Web và app của tài xế liên tục hỏi máy chủ (web 1,5 giây/lần, app khoảng 3,5 giây/lần) nên hiện hộp thoại "Chuyến giao mới" kèm đồng hồ đếm ngược 5 phút. App còn đổ chuông và rung.

**H: Nếu tài xế không bấm gì thì sao?**
Đ: Máy chủ có tác vụ chạy mỗi 30 giây (`ChuyenHetHanScheduler`). Chuyến chờ quá 5 phút bị chuyển sang **6 – Đã từ chối** với lý do "Hết thời gian chờ tài xế xác nhận"; điều phối và tài xế đều nhận thông báo. Nhờ vậy xe và tài xế không bị "giữ chỗ" mãi.

**H: Vì sao phải có GPLX mới nhận được chuyến?**
Đ: Đây là quy tắc nghiệp vụ: không có bằng lái thì không được lái xe. Hệ thống chặn ở cả giao diện web (báo ngay) và máy chủ (trả lỗi 403), nên dù gọi thẳng API cũng không lách được.

**H: Kiểm tra bán kính 200 m làm ở đâu?**
Đ: Ở **cả hai nơi**:
- **Trình duyệt/app:** tính khoảng cách bằng công thức Haversine để báo lỗi ngay cho người dùng.
- **Máy chủ:** tính lại lần nữa, xa hơn 200 m thì trả lỗi.

Kiểm tra trên giao diện giúp người dùng biết lỗi nhanh. Kiểm tra trên máy chủ để chống gian lận, vì người dùng có thể sửa code giao diện, nhưng không sửa được máy chủ.

**H: Làm sao em test được 200 m khi ngồi ở nhà?**
Đ: Giả lập vị trí:
- **Web:** Chrome DevTools → Sensors → Location.
- **Máy ảo Android:** Extended controls → Location.

Đặt tọa độ công trình là test được mà không cần ra hiện trường.

**H: "Hoàn thành" và "Đã về tới trạm" khác nhau thế nào?**
Đ:
- **"Hoàn thành" (trạng thái 8):** đã đổ bê tông xong. Đơn hàng chuyển Hoàn thành, khách được báo, app ngừng gửi GPS, nhưng xe **vẫn bận** vì còn đang chạy về.
- **"Đã về tới trạm" (trạng thái 4):** chuyến kết thúc. Xe về **Sẵn sàng**, vị trí xe đặt đúng tọa độ trạm trộn, điều phối giao được chuyến mới.

**H: Gửi vị trí GPS hoạt động thế nào? Vì sao dùng Redis?**
Đ:
- **Cách gửi:** web dùng `navigator.geolocation`, app dùng dịch vụ nền `TrackingService`. Cả hai gửi tọa độ về `POST /api/tai-xe/gps/ping`.
- **Máy chủ làm 3 việc:**
  1. Lưu vị trí mới nhất vào **Redis**.
  2. Đẩy realtime qua **WebSocket** cho bản đồ điều phối.
  3. Ghi lịch sử vào bảng `vi_tri_gps`, tối đa 1 điểm mỗi 3 giây.
- **Vì sao dùng Redis:** Redis nằm trong bộ nhớ nên đọc ghi rất nhanh, hợp với dữ liệu thay đổi vài giây một lần. Database chỉ ghi thưa hơn để đỡ tải.

**H: Báo sự cố thì hệ thống làm gì?**
Đ:
- Chuyến chuyển **7 – Sự cố/Hủy** và xe chuyển **Bảo trì**.
- Đơn hàng quay về **"Đã phân bổ trạm trộn"** để điều phối tạo chuyến thay thế bằng xe khác.
- Ghi một bản sự cố mức ưu tiên cao.
- Thông báo khẩn cho điều phối và quản lý; khách hàng được báo giao chậm.

**H: Phân quyền tài xế thế nào?**
Đ:
- **Đăng nhập** xong nhận **JWT** (token) có vai trò "Tài xế". Mọi API của tài xế (`/api/tai-xe/**`) đều kiểm tra token và vai trò.
- **Mỗi API** còn kiểm tra chuyến có đúng của tài xế đang đăng nhập không: nhận chuyến của người khác bị báo "Bạn không được phân công chuyến này".
- **Header `ClientKey`:** request phải kèm header này, sai hoặc thiếu thì bị chặn.
- **Menu web** chỉ hiện các mục của vai trò Tài xế.

---

## 3. Câu hỏi "nếu… thì sao"

| Câu hỏi | Trả lời gợi ý |
|---|---|
| Nếu trình duyệt chặn quyền vị trí? | Bấm "Đã đến công trình" báo "Không xác định được vị trí GPS hiện tại của bạn. Hãy bật GPS và thử lại!" (WGPS-03) |
| Nếu mất mạng / tắt máy chủ? | App báo "Không kết nối được máy chủ. Kiểm tra mạng và thử lại", không bị văng (QLC-13) |
| Nếu không bật Redis? | Các chức năng khác vẫn chạy, riêng gửi vị trí GPS bị lỗi nên bản đồ điều phối không cập nhật |
| Nếu bấm "Đã đến" khi chưa nhận chuyến? | Máy chủ chặn: "Bạn cần nhận chuyến trước khi cập nhật hành trình" (CNTT-01) |
| Nếu báo sự cố cho chuyến đã hoàn thành? | Bị chặn: "Chuyến hàng đã hoàn thành, không thể báo cáo sự cố để hủy" (CNTT-09) |
| Nếu chuyển xe sang bảo trì khi đang chạy chuyến? | Bị chặn: "Xe đang thực hiện chuyến, hãy hoàn thành chuyến trước khi chuyển sang bảo trì" (CNTT-11). Trên web, nút Bảo trì bị ẩn khi đang có chuyến |
| Nếu nhận một chuyến đã hết hạn? | "Chuyến này không còn ở trạng thái chờ xác nhận (có thể đã hết hạn)" (QLC-07) |
| Nếu bấm Hủy ở hộp thoại lý do từ chối? | Hộp thoại đóng, chuyến vẫn ở tab "Chờ nhận", nhận lại được (WQLC-03) |

---

## 4. Lỗi đã phát hiện (nên nắm để trả lời khi bị hỏi "có tìm được lỗi nào không?")

1. **Nút "Bảo trì" ở tab "Xe phụ trách" trên web (WCNTT-07).** Khi mình chạy thử, bấm nút báo *"Trạng thái xe chỉ được là sẵn sàng hoạt động (1) hoặc bảo trì (0)"* và xe không đổi.
   - **Nguyên nhân:** code web gửi mã **2** (mã 2 nghĩa là "Đang giao hàng"), trong khi mã đúng của bảo trì là **0**.
   - **Cách sửa:** trong `DriverTripsPage.tsx`, nút Bảo trì gọi `handleToggleVehicleStatus(0)` thay vì `(2)`, và điều kiện tô màu/khóa nút đổi `=== 2` thành `=== 0`.
   - Bạn tự bấm thử để xác nhận rồi ghi kết quả.
2. **Phiên đăng nhập hết hạn (DN-06).** Gọi API khi chưa đăng nhập, máy chủ trả **302** (chuyển sang trang đăng nhập Google) thay vì **401**, nên app không biết để quay về màn đăng nhập. Phát hiện bằng kiểm thử tự động. Cách sửa: thêm `exceptionHandling` trả 401 cho `/api/**` trong `SecurityConfig`.

---

## 5. Kịch bản demo 5 phút (nếu được bảo "em demo đi")

1. **Đăng nhập sai rồi đúng** (WDN-04 → WDN-01): nói về phân vùng tương đương.
2. **Điều phối tạo chuyến**, tài xế thấy **đề nghị + đếm ngược** (WQLC-01): nói về hạn 5 phút.
3. **Nhận chuyến** (WQLC-04) → bấm **Đã đến** khi đang ở trạm, bị chặn **3619 m** (WCNTT-01). Đổi vị trí sát công trình, thành công (WCNTT-02): nói về giá trị biên 200 m và kiểm tra 2 lớp.
4. **Hoàn thành** → **Đã về tới trạm** (WCNTT-03, 04): nói về trạng thái 8 và 4.
5. **Báo sự cố** ở một chuyến khác (WCNTT-05): nói về luồng phụ và việc điều phối được báo.
6. **Kết:** đã chạy bao nhiêu ca, bao nhiêu ca đạt / chưa đạt, lỗi tìm được là gì (Phần 4).

---

## 6. Kiểm thử tay và kiểm thử tự động (nếu bị hỏi)

- **Test tay:** người bấm trên giao diện và nhìn kết quả. Phù hợp kiểm tra giao diện, trải nghiệm, những thứ cần mắt người: bản đồ, chuông, thông báo.
- **Test tự động:** viết code để máy tự chạy (bộ 30 test JUnit + MockMvc ở `kiem-thu-tu-dong/`). Chạy lại nhanh mỗi khi sửa code (kiểm thử hồi quy), kiểm tra được cả những thứ khó thao tác tay như hết hạn 5 phút (lùi thời gian trong test) hay gọi API khi chưa đăng nhập.
- **Nhóm kết hợp cả hai:** tự động cho nghiệp vụ phía máy chủ, test tay cho giao diện web và app.
