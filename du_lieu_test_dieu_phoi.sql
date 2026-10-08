-- =====================================================================
-- Dữ liệu test cho Nhân viên điều phối (docs/checklist-test-dieu-phoi.md)
-- Chạy SAU du_lieu_mau_quan_ly_chuyen.sql và du_lieu_test_nhieu_chuyen.sql.
-- MySQL Command Line:  charset utf8mb4   rồi   source C:/sql/du_lieu_test_dieu_phoi.sql
-- Chạy lại được nhiều lần: tài khoản/xe đã có thì bỏ qua, mỗi lần tạo thêm 2 đơn mới.
--
-- Tạo ra:
--   * taixe03 (mật khẩu 123456789) + xe 29C-333.33, đang RẢNH -> điều phối giao chuyến cho tài xế này
--   * Đơn D: Chờ xử lý, 6 m³ -> điều phối xác nhận, phân bổ trạm, tạo chuyến
--   * Đơn E: Chờ xử lý -> điều phối từ chối
-- =====================================================================
USE quanly_betong;
SET NAMES utf8mb4;

SET @mk = '$2b$10$rLLNt6W1iqjQp6FhPCLsc.RfSESQmM/olgTqimc6BD0W6dx0CeUlK'; -- 123456789
SET @vt_tx = CONVERT(UNHEX('54C3A0692078E1BABF') USING utf8mb4); -- 'Tài xế'

-- 1) Tài xế 3 + xe 3 (đều rảnh)
INSERT IGNORE INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
VALUES ('taixe03', @mk, 'taixe03@example.com', 'Le Van Ba', 1, 1, 0,
        (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_tx AS BINARY) LIMIT 1));
INSERT INTO tai_xe (ho_ten, sdt, sogplx, trang_thai, idtk)
SELECT 'Le Van Ba', '0900000003', 'GPLX-TEST-03', 1, idtk FROM tai_khoan
WHERE ten_dang_nhap = 'taixe03' AND NOT EXISTS (
  SELECT 1 FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe03'));
SET @tx3 = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe03'));

INSERT INTO xe (bien_so, trong_tai, trang_thai, idtx)
SELECT '29C-333.33', 9, 1, @tx3 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM xe WHERE bien_so = '29C-333.33');

SET @tram = (SELECT id_tram FROM tram_tron WHERE sdt = '0933333333' LIMIT 1);
SET @kh = (SELECT idkh FROM khach_hang WHERE sdt = '0911111111' LIMIT 1);
SET @ct = (SELECT idct FROM cong_trinh WHERE sdt = '0922222222' LIMIT 1);
SET @lbt = (SELECT idlbt FROM loai_be_tong WHERE mac_be_tong = 'M300' LIMIT 1);

-- Trạm trộn phải hoạt động và đủ công suất thì mới phân bổ / tạo chuyến được
UPDATE tram_tron SET trang_thai = 1, cong_suat = GREATEST(COALESCE(cong_suat, 0), 50) WHERE id_tram = @tram;

-- 2) Đơn D: chờ xử lý -> dùng cho xác nhận, phân bổ trạm, tạo chuyến
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR), 6, 9000000, 0,
        'Don D - test dieu phoi (xac nhan, phan bo tram, tao chuyen)', 0);
SET @dhD = LAST_INSERT_ID();
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (@dhD, @lbt, 6, 1500000, 9000000);

-- 3) Đơn E: chờ xử lý -> dùng cho từ chối
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 8 HOUR), 3, 4500000, 0,
        'Don E - test tu choi', 0);
SET @dhE = LAST_INSERT_ID();
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (@dhE, @lbt, 3, 1500000, 4500000);

-- Kiểm tra: ghi lại các mã này để dùng trong checklist
SELECT @dhD AS ma_don_D, @dhE AS ma_don_E, @tram AS ma_tram,
       (SELECT id_xe FROM xe WHERE bien_so = '29C-333.33') AS ma_xe_3, @tx3 AS ma_tai_xe_3;
