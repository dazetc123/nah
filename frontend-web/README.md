# BetongOps — Frontend

Viết lại theo đúng route/DTO của backend hiện tại (`betong__3_.zip`), bám usecase
báo cáo (Bảng 3.1–3.11, 3.15–3.25, 3.39–3.40). React 18 + TypeScript + Vite +
react-router-dom + lucide-react — không có framework CSS ngoài, CSS thuần để
kiểm soát đúng bảng màu yêu cầu.

## Chạy thử

```bash
npm install
npm run dev
```

Mặc định gọi backend ở `http://localhost:8080`. Đổi bằng file `.env`:

```
VITE_API_URL=http://may-chu-khac:8080
VITE_CLIENT_KEY=ma-client-key-do-backend-cung-cap
```

`VITE_CLIENT_KEY` phải được điền đúng giá trị do backend cung cấp. Frontend sẽ
tự gửi giá trị này trong header `ClientKey` cho tất cả request. Sau khi sửa
file `.env`, cần khởi động lại Vite để biến môi trường được nạp lại.

## Bắt buộc: áp dụng bản vá backend trong `backend-patches/`

Có 2 lỗi tích hợp phía backend cần vá thì frontend mới hoạt động đúng, xem
chi tiết từng file trong thư mục `backend-patches/`:

1. **`TaiKhoanResponse.java` + `TaiKhoanAdminServiceImpl.java`** — bổ sung field
   `idTX` để màn "Gán tài xế cho xe" hiển thị đúng tên tài xế thay vì phải gõ
   tay mã số.
2. **`GoogleOAuth2SuccessHandler.java`** — bản gốc trả thẳng JSON ra trình
   duyệt sau khi đăng nhập Google (người dùng thấy 1 trang chữ JSON, không
   quay lại được giao diện). Bản vá redirect về `/oauth2/callback` của
   frontend kèm dữ liệu đăng nhập trong URL fragment.
   Cần thêm dòng cấu hình trong `application.properties` (có giá trị mặc
   định nếu bỏ qua): `app.frontend-url=http://localhost:5173`

Copy 3 file trên đè vào đúng vị trí trong project Java rồi build lại.

## Bảng màu

- Nền sáng: trắng (`--bg: #fff`) + xanh blue làm điểm nhấn (`--accent: #1d4ed8`)
- Nền tối: đen tuyền (`--bg: #000`) + cùng tông xanh blue sáng hơn (`--accent: #3b82f6`)
- Không bo góc ở bất kỳ đâu
- Bấm icon mặt trời/mặt trăng ở góc trên bên phải (hoặc trang đăng nhập) để đổi giao diện, tự nhớ lựa chọn ở `localStorage`

## Xử lý lỗi

`src/lib/api/client.ts` chỉ đọc đúng field `message` từ `ErrorResponse` của
backend để hiển thị — không bao giờ in nguyên chuỗi JSON ra giao diện. Lỗi
không có `message` (lỗi hệ thống, mất kết nối…) dùng câu tiếng Việt mặc định
theo mã HTTP.
