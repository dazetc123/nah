-- =====================================================================
-- Dữ liệu test bổ sung cho app Tài xế (checklist docs/checklist-test-tai-xe.md)
-- Chạy SAU du_lieu_mau_quan_ly_chuyen.sql. Nên chạy trong DBeaver (UTF-8).
-- Chạy lại nhiều lần được: tài khoản/xe đã có sẽ được bỏ qua, mỗi lần chạy
-- sẽ tạo thêm một bộ đơn hàng + chuyến mới.
--
-- Tạo ra:
--   * Tài khoản (mật khẩu đều là 123456789):
--       quanly01   - Quản lý (đăng nhập web)
--       dieuphoi01 - Nhân viên điều phối (đăng nhập web / Swagger)
--       taixe02    - Tài xế thứ 2 (để test chuyến bị chuyển cho tài xế khác)
--   * Tọa độ cho trạm trộn mẫu -> bản đồ vẽ được tuyến đường gợi ý
--   * Cho taixe01:
--       - Đơn A (giao NGÀY MAI) có 2 chuyến Chờ nhận  -> test 1.3.5 (5.b) và 1.5.7
--       - Đơn B (giao 3 NGÀY TRƯỚC) có 1 chuyến Hoàn thành -> test lọc trạng thái/ngày
--   * Cho taixe02: Đơn C có 1 chuyến Chờ nhận -> test 1.2.5
-- =====================================================================
USE quanly_betong;
SET NAMES utf8mb4;

-- Mật khẩu 123456789 (BCrypt, giống file dữ liệu mẫu của nhóm)
SET @mk = '$2b$10$rLLNt6W1iqjQp6FhPCLsc.RfSESQmM/olgTqimc6BD0W6dx0CeUlK';

-- Tên vai trò viết bằng UNHEX để không lỗi mã hóa khi chạy bằng MySQL Command Line trên Windows
SET @vt_ql = CONVERT(UNHEX('5175E1BAA36E206CC3BD') USING utf8mb4); -- 'Quản lý'
SET @vt_dp = CONVERT(UNHEX('4E68C3A26E207669C3AA6E20C49169E1BB8175207068E1BB9169') USING utf8mb4); -- 'Nhân viên điều phối'
SET @vt_tx = CONVERT(UNHEX('54C3A0692078E1BABF') USING utf8mb4); -- 'Tài xế'

-- 1) Vai trò (chỉ thêm nếu chưa có)
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_ql FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_ql AS BINARY));
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_dp FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_dp AS BINARY));
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_tx FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_tx AS BINARY));

-- 2) Tài khoản quản lý, điều phối, tài xế 2
INSERT IGNORE INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
VALUES ('quanly01', @mk, 'quanly01@example.com', 'Quản lý Test', 1, 1, 0,
        (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_ql AS BINARY) LIMIT 1));
INSERT IGNORE INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
VALUES ('dieuphoi01', @mk, 'dieuphoi01@example.com', 'Điều phối Test', 1, 1, 0,
        (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_dp AS BINARY) LIMIT 1));
INSERT IGNORE INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
VALUES ('taixe02', @mk, 'taixe02@example.com', 'Trần Văn Hai', 1, 1, 0,
        (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_tx AS BINARY) LIMIT 1));

INSERT INTO tai_xe (ho_ten, sdt, sogplx, trang_thai, idtk)
SELECT 'Trần Văn Hai', '0900000002', 'GPLX-TEST-02', 1, idtk FROM tai_khoan
WHERE ten_dang_nhap = 'taixe02' AND NOT EXISTS (
  SELECT 1 FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe02'));

SET @tx1 = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe01'));
SET @tx2 = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe02'));

-- 3) Xe thứ 2 cho taixe02
INSERT INTO xe (bien_so, trong_tai, trang_thai, idtx)
SELECT '29C-678.90', 9, 1, @tx2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM xe WHERE bien_so = '29C-678.90');

SET @xe1 = (SELECT id_xe FROM xe WHERE bien_so = '29C-123.45');
SET @xe2 = (SELECT id_xe FROM xe WHERE bien_so = '29C-678.90');
SET @tram = (SELECT id_tram FROM tram_tron WHERE sdt = '0933333333' LIMIT 1);
SET @kh = (SELECT idkh FROM khach_hang WHERE sdt = '0911111111' LIMIT 1);
SET @ct = (SELECT idct FROM cong_trinh WHERE sdt = '0922222222' LIMIT 1);
SET @lbt = (SELECT idlbt FROM loai_be_tong WHERE mac_be_tong = 'M300' LIMIT 1);

-- 4) Tọa độ trạm trộn (Cầu Giấy, cách công trình mẫu khoảng 4 km) -> bản đồ vẽ tuyến
UPDATE tram_tron SET vi_do = 21.0362, kinh_do = 105.7906 WHERE id_tram = @tram;

-- 5) Đơn A: giao ngày mai, 9 m³, 2 chuyến Chờ nhận cho taixe01
--    (đơn đã có chuyến nên trạng thái đơn là 3 = Đang giao, giống khi điều phối tạo chuyến trên web)
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 9 HOUR), 9, 13500000, 3,
        'Đơn A - 2 chuyến (test nhận 2 chuyến, đơn nhiều chuyến)', @tram, 0);
SET @dhA = LAST_INSERT_ID();
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (@dhA, @lbt, 9, 1500000, 13500000);
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai) VALUES (@dhA, @xe1, @tx1, @tram, 5, 0);
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai) VALUES (@dhA, @xe1, @tx1, @tram, 4, 0);

-- 6) Đơn B: giao 3 ngày trước, 1 chuyến đã Hoàn thành cho taixe01
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram, version)
VALUES (@kh, @ct, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR),
        6, 9000000, 4, 'Đơn B - đã giao xong (test lọc)', @tram, 0);
SET @dhB = LAST_INSERT_ID();
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (@dhB, @lbt, 6, 1500000, 9000000);
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai,
                    thoi_gian_nhan, thoi_gian_xuat_phat, thoi_gian_den, khoi_luong_thuc_giao, thoi_gian_giao_xong, thoi_gian_hoan_thanh)
VALUES (@dhB, @xe1, @tx1, @tram, 6, 5,
        DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 12 HOUR),
        DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 13 HOUR),
        DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR), 6,
        DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 15 HOUR),
        DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 16 HOUR));

-- 7) Đơn C: 1 chuyến Chờ nhận của taixe02 (dùng cho test 1.2.5)
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 17 HOUR), 7, 10500000, 3,
        'Đơn C - của taixe02', @tram, 0);
SET @dhC = LAST_INSERT_ID();
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (@dhC, @lbt, 7, 1500000, 10500000);
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai) VALUES (@dhC, @xe2, @tx2, @tram, 7, 0);

-- Sửa các đơn hàng cũ thiếu version (tránh lỗi 500 khi Hoàn thành chuyến)
UPDATE don_hang SET version = 0 WHERE version IS NULL;
-- Đơn đã có chuyến thì không còn ở "Đã phân bổ trạm" (6) mà là "Đang giao" (3)
UPDATE don_hang SET trang_thai = 3
WHERE trang_thai = 6 AND iddh IN (SELECT iddh FROM (SELECT DISTINCT iddh FROM chuyen) AS c);

-- Kiểm tra
SELECT c.id_chuyen, tk.ten_dang_nhap AS tai_xe, c.trang_thai, c.khoi_luong, d.iddh, d.thoi_gian_giao, d.ghi_chu
FROM chuyen c
JOIN tai_xe t ON t.idtx = c.idtx JOIN tai_khoan tk ON tk.idtk = t.idtk
JOIN don_hang d ON d.iddh = c.iddh
ORDER BY c.id_chuyen;
