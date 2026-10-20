# Học nhanh: luồng Tài xế trong bản mới của nhóm (betong-main_2)

> Tài liệu này mô tả **code của trưởng nhóm** (bản zip thứ 2). Bản này không dùng code trong `docs/BAO_CAO_THAY_DOI.md`.
>
> Mọi luồng dưới đây đã được chạy thử thật với backend và database.

## 1. Cần nhớ trước tiên: bảng trạng thái chuyến (`enums/TrangThaiChuyen.java`)

| Mã | Tên | Ý nghĩa thực tế |
|---|---|---|
| **5** | Chờ xác nhận | Điều phối vừa giao chuyến. Tài xế có **5 phút** để nhận hoặc từ chối. |
| **6** | Đã từ chối | Tài xế bấm từ chối, **hoặc** hết 5 phút không trả lời (hệ thống tự chuyển) |
| **3** | Đang giao | Tài xế đã nhận chuyến, xe đang chạy tới công trình |
| **2** | Đã đến công trình | Xe đã vào bán kính **200 m** quanh công trình |
| **4** | Hoàn thành | Đã đổ bê tông xong, xe được tính là đã về trạm |
| **7** | Sự cố / Hủy | Tài xế báo sự cố, chuyến bị hủy, xe chuyển sang bảo trì |
| 8, 9 | Về trạm / Đã tới trạm | Chỉ dùng khi tắt chế độ tài xế xác nhận (`app.dieu-phoi.tai-xe-xac-nhan=false`). Mặc định không dùng. |
| 0, 1 | Mới tạo / Xuất phát | Mã cũ, giữ lại để tương thích |

Lưu ý: thứ tự mã **không theo thứ tự thời gian**. Luồng chính là 5 → 3 → 2 → 4.

Trạng thái xe (`TrangThaiXe`):

| Mã | Ý nghĩa |
|---|---|
| 1 | Sẵn sàng hoạt động |
| 0 | Bảo trì / Sự cố |
| 2 | Đang giao hàng |

Trạng thái đơn hàng liên quan:

| Mã | Ý nghĩa |
|---|---|
| 6 | Đã phân bổ trạm (điều phối tạo chuyến được) |
| 3 | Đang giao |
| 4 | Hoàn thành |

## 2. Sơ đồ luồng

```
 Điều phối tạo chuyến (web)
          │  chuyến = 5 (Chờ xác nhận), app tài xế đổ chuông + đếm ngược 5 phút
          ▼
 ┌──────────────────┐   Từ chối (chọn lý do)      ┌───────────────┐
 │ 5  Chờ xác nhận  │ ──────────────────────────► │ 6  Đã từ chối │ → điều phối giao lại
 └──────────────────┘   hoặc quá 5 phút           └───────────────┘
          │ Nhận chuyến (bắt buộc có số GPLX)
          ▼
 ┌──────────────────┐   Báo sự cố                 ┌───────────────┐
 │ 3  Đang giao     │ ──────────────────────────► │ 7  Sự cố/Hủy  │ → xe = bảo trì,
 └──────────────────┘                             └───────────────┘   đơn quay về 6
          │ "Đã đến" (GPS phải cách công trình ≤ 200 m)       ▲
          ▼                                                    │ Báo sự cố
 ┌──────────────────┐ ─────────────────────────────────────────┘
 │ 2  Đã đến        │
 └──────────────────┘
          │ "Về trạm & Hoàn thành"
          ▼
 ┌──────────────────┐
 │ 4  Hoàn thành    │ → xe = sẵn sàng, vị trí xe đặt về trạm, đơn = 4 Hoàn thành
 └──────────────────┘
```

Trong suốt lúc chuyến ở trạng thái 3 và 2, app gửi GPS liên tục (khoảng 2 giây một lần) để điều phối theo dõi trên bản đồ.

## 3. Chi tiết từng usecase

Với mỗi usecase: làm gì trên app → API gọi → backend kiểm tra gì → kết quả.

### 3.1 Nhận đề nghị chuyến
- **App:**
  - `OfferWatcher.java` hỏi server khoảng 3,5 giây một lần (giãn ra 8 giây khi mạng lỗi).
  - Có chuyến mới thì hiện hộp thoại kèm chuông, rung và đồng hồ đếm ngược.
- **API:** `GET /api/tai-xe/chuyen/cua-toi?nhom=cho-xac-nhan`. Kết quả có trường `hanXacNhan` là hạn chót.
- **Hết hạn:** `ChuyenHetHanScheduler` chạy mỗi 30 giây. Chuyến ở trạng thái 5 quá 5 phút thì chuyển sang 6 và báo cho điều phối lẫn tài xế.

### 3.2 Nhận chuyến / Từ chối chuyến
- **API:** `PUT /api/tai-xe/chuyen/{id}/phan-hoi`, body `{"chapNhan": true}` hoặc `{"chapNhan": false, "lyDo": "..."}`.
- **Backend kiểm tra:**
  - Chuyến phải là của chính tài xế này, nếu không trả 403.
  - Chuyến phải đang ở trạng thái 5, nếu không trả 409 ("có thể đã hết hạn").
  - Khi nhận: tài xế **phải có số GPLX** trong hồ sơ, nếu không trả 403.
- **Kết quả khi nhận:**
  - Chuyến chuyển 5 → **3**, ghi thời gian xuất phát.
  - Đơn chuyển sang 3 (Đang giao).
  - Gửi thông báo cho điều phối và khách hàng.
- **Kết quả khi từ chối:** chuyến chuyển 5 → **6**, lưu lý do, báo điều phối phân công lại.
- **Lý do từ chối có sẵn trên app:** "Xe đang gặp sự cố", "Tôi đang bận", "Công trình quá xa", "Lý do khác".

### 3.3 Xem lịch trình / chuyến hiện tại
- **App:** `TripsActivity`. Bản đồ osmdroid vẽ lộ trình trạm → công trình (dùng OSRM). Bên dưới là bảng thông tin: khách hàng, mác bê tông, khối lượng, biển số.
- **API:** `GET /api/tai-xe/chuyen/cua-toi?nhom=hien-tai`. Ngoài `hien-tai` còn có `cho-xac-nhan` và `lich-su` (lịch sử gồm chuyến 4 và 7).

### 3.4 Gửi vị trí GPS
- **App:**
  - `TrackingService` là foreground service.
  - Nhận vị trí mỗi khoảng 2 giây và gọi `POST /api/tai-xe/gps/ping` với `{idXe, idChuyen, viDo, kinhDo, tocDo}`.
- **Backend (`GpsController`):**
  1. Ghi đè vị trí mới nhất vào **Redis**, key `xe:location:{idXe}`.
  2. Phát realtime qua WebSocket STOMP: `/topic/xe/{idXe}` và `/topic/chuyen/{idChuyen}`. Web điều phối nghe các kênh này để vẽ xe chạy.
  3. Lưu lịch sử vào bảng `vi_tri_gps`, tối đa 1 bản ghi mỗi 3 giây cho mỗi chuyến.
- **Bắt buộc:** **phải chạy Redis**. Không có Redis thì API này trả 500 và web không thấy xe di chuyển. Các luồng khác vẫn chạy bình thường.

### 3.5 Xác nhận đã đến công trình
- **App:** bấm nút chính khi chuyến ở trạng thái 3, hộp thoại hỏi "Xác nhận đã đến?". App gửi kèm tọa độ hiện tại.
- **API:** `PUT /api/tai-xe/chuyen/{id}/trang-thai`, body `{"trangThai": 2, "viDo": .., "kinhDo": ..}`.
- **Backend kiểm tra:**
  - Chuyến phải đang ở 3.
  - Tính khoảng cách tới công trình bằng công thức Haversine. **Lớn hơn 200 m thì trả 400**, ví dụ "Bạn chưa tới gần công trình (còn cách khoảng 3619 m, cần trong 200 m)".
  - Nếu app không gửi tọa độ, backend lấy điểm GPS gần nhất đã lưu.
- **Kết quả:** chuyến chuyển sang **2**, ghi thời gian đến, báo khách hàng "Xe đã đến công trình".

### 3.6 Hoàn thành chuyến (Về trạm & Hoàn thành)
- **App:** hộp thoại "Về trạm & Hoàn thành chuyến?". Bản đồ đưa xe về trạm.
- **API:** `PUT /api/tai-xe/chuyen/{id}/trang-thai`, body `{"trangThai": 4}`.
- **Kết quả:**
  - Chuyến chuyển sang **4**.
  - Xe chuyển sang 1 (sẵn sàng) và được ghi một điểm GPS tại tọa độ trạm.
  - Đơn chuyển sang 4 (Hoàn thành).
  - Báo điều phối và khách hàng.
- **Lưu ý (điểm yếu có thể bị hỏi):**
  - Backend cho hoàn thành cả khi chuyến đang ở 3, tức là bỏ qua bước "Đã đến". App thì luôn đi qua bước 2.
  - Đơn hàng chuyển Hoàn thành ngay, không kiểm tra đơn còn chuyến khác hay không.

### 3.7 Báo cáo sự cố trên chuyến
- **App:** nút báo sự cố trong `TripsActivity`. Chọn lý do, app gửi kèm vị trí.
- **API:** `PUT /api/tai-xe/chuyen/{id}/bao-cao-su-co`, body `{"lyDo": "...", "viDo": .., "kinhDo": ..}`.
- **Kết quả** (không áp dụng cho chuyến đã hoàn thành, khi đó trả 409):
  - Chuyến chuyển sang **7** (Sự cố / Hủy).
  - **Xe chuyển sang 0 (bảo trì)**.
  - Tạo bản ghi `su_co` với mức ưu tiên 3 (cao).
  - Đơn quay về **6** để điều phối tạo chuyến thay thế.
  - Thông báo khẩn cho điều phối và quản lý; khách hàng nhận thông báo giao chậm.

### 3.8 Cập nhật trạng thái xe
- **App:** `VehicleStatusActivity`.
- **API:** `GET /api/tai-xe/xe` để xem xe của mình. `PUT /api/tai-xe/xe/trang-thai` với body `{"trangThai": 1}` (sẵn sàng) hoặc `0` (bảo trì).
- **Quy tắc:**
  - Chỉ nhận giá trị 0 hoặc 1.
  - Không được chuyển sang bảo trì khi xe đang có chuyến.
  - Sau khi báo sự cố, xe bị khoá ở 0. Tài xế phải bật lại 1 thì điều phối mới giao chuyến được.
- **Báo cáo hư hỏng xe (có ảnh):** `FaultReportActivity` gửi multipart tới `POST /api/dieu-phoi/xe/bao-cao`. Đây là báo cáo tình trạng xe gửi quản lý, khác với sự cố trên chuyến ở mục 3.7.

### 3.9 Thông báo
- **API:** `GET /api/nguoi-dung/thong-bao`, `GET /api/nguoi-dung/thong-bao/chua-doc` (số chưa đọc), `PUT /api/nguoi-dung/thong-bao/{id}/da-doc`.
- **App:** `NotificationsActivity`; trang chủ hiện số thông báo chưa đọc.
- Thông báo được lưu trong bảng `thong_bao`, tạo ra ở mỗi bước nhận, từ chối, hết hạn, đến nơi, hoàn thành và sự cố.

## 4. Câu hỏi hay bị hỏi khi bảo vệ (trả lời ngắn)

- **Vì sao phải nhận chuyến mà không giao thẳng?** Tài xế có thể đang bận hoặc xe có vấn đề. Cơ chế xác nhận trong 5 phút giúp điều phối biết sớm để giao lại, tránh xe hoặc tài xế bị "giữ chỗ" mãi.
- **Làm sao chống bấm "Đã đến" khi chưa tới?** Backend tự tính khoảng cách GPS tới tọa độ công trình và chỉ chấp nhận trong 200 m. Kiểm tra nằm ở server nên không thể lách bằng cách sửa app.
- **Vì sao dùng Redis cho GPS?** Mỗi xe gửi vị trí khoảng 2 giây một lần. Redis chỉ giữ vị trí mới nhất trong bộ nhớ nên đọc ghi rất nhanh. Database chỉ lưu lịch sử thưa hơn (3 giây một lần) để đỡ tải.
- **Realtime lên web bằng gì?** WebSocket STOMP: server đẩy (push) tới `/topic/xe/{id}`, web không phải hỏi liên tục.
- **App nhận chuyến mới bằng gì?** Polling khoảng 3,5 giây (`OfferWatcher`), kèm chuông, rung và đếm ngược.
- **Báo sự cố thì chuyện gì xảy ra với đơn?** Chuyến chuyển 7, xe chuyển bảo trì, đơn quay về "Đã phân bổ trạm" để điều phối tạo chuyến thay thế bằng xe khác.
- **Phân quyền?** `/api/tai-xe/**` yêu cầu đăng nhập JWT với role Tài xế (hoặc Điều phối/Quản lý). Mỗi API còn kiểm tra chuyến có đúng của tài xế đang đăng nhập không. Mọi request phải có header `ClientKey` (app dùng `betongmobile`).
