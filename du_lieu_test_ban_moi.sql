-- =====================================================================
-- Dữ liệu test cho BẢN MỚI của nhóm (betong-main_2) - luồng Tài xế
-- Chạy được bằng MySQL Command Line:  source C:/duong/dan/du_lieu_test_ban_moi.sql
-- (dùng dấu / trong đường dẫn). Chạy lại nhiều lần được: cái gì có rồi thì bỏ qua,
-- mỗi lần chạy thêm 2 đơn hàng mới ở trạng thái 6 (Đã phân bổ trạm) để điều phối tạo chuyến.
--
-- Mật khẩu mọi tài khoản: 123456789
--   quanly01    - Quản lý (web)
--   dieuphoi01  - Nhân viên điều phối (web) -> tạo chuyến cho tài xế
--   taixe01     - Tài xế, xe 29C-123.45 (có GPLX)
--   taixe02     - Tài xế, xe 29C-678.90 (có GPLX)
--   khachhang01 - Khách hàng
-- Trạm trộn: 21.0362, 105.7906 (Cầu Giấy) | Công trình: 21.0045, 105.7985 (~3,6 km)
-- =====================================================================
USE quanly_betong;
SET NAMES utf8mb4;

SET @mk = '$2b$10$rLLNt6W1iqjQp6FhPCLsc.RfSESQmM/olgTqimc6BD0W6dx0CeUlK';
SET @vt_ql = CONVERT(UNHEX('5175E1BAA36E206CC3BD') USING utf8mb4);                                -- Quản lý
SET @vt_dp = CONVERT(UNHEX('4E68C3A26E207669C3AA6E20C49169E1BB8175207068E1BB9169') USING utf8mb4); -- Nhân viên điều phối
SET @vt_tx = CONVERT(UNHEX('54C3A0692078E1BABF') USING utf8mb4);                                   -- Tài xế
SET @vt_kh = CONVERT(UNHEX('4B68C3A163682068C3A06E67') USING utf8mb4);                             -- Khách hàng

-- 1) Vai trò
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_ql FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_ql AS BINARY));
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_dp FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_dp AS BINARY));
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_tx FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_tx AS BINARY));
INSERT INTO vai_tro (ten_vai_tro) SELECT @vt_kh FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_kh AS BINARY));
SET @r_ql = (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_ql AS BINARY) LIMIT 1);
SET @r_dp = (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_dp AS BINARY) LIMIT 1);
SET @r_tx = (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_tx AS BINARY) LIMIT 1);
SET @r_kh = (SELECT id_vai_tro FROM vai_tro WHERE CAST(ten_vai_tro AS BINARY) = CAST(@vt_kh AS BINARY) LIMIT 1);

-- 2) Tài khoản (tên không dấu để chạy được trong cmd)
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, sdt, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
SELECT * FROM (
  SELECT 'quanly01' u, @mk m, 'quanly01@example.com' e, 'Quan Ly Test' h, '0900000010' s, 1 t, 1 x, 0 p, @r_ql r UNION ALL
  SELECT 'dieuphoi01', @mk, 'dieuphoi01@example.com', 'Dieu Phoi Test', '0900000011', 1, 1, 0, @r_dp UNION ALL
  SELECT 'taixe01', @mk, 'taixe01@example.com', 'Nguyen Van Tai', '0900000001', 1, 1, 0, @r_tx UNION ALL
  SELECT 'taixe02', @mk, 'taixe02@example.com', 'Tran Van Hai', '0900000002', 1, 1, 0, @r_tx UNION ALL
  SELECT 'khachhang01', @mk, 'khachhang01@example.com', 'Nguyen Thi Hoa', '0911111111', 1, 1, 0, @r_kh
) v WHERE NOT EXISTS (SELECT 1 FROM tai_khoan WHERE ten_dang_nhap = v.u);

-- 3) Hồ sơ tài xế (bắt buộc có số GPLX thì mới nhận chuyến được)
INSERT INTO tai_xe (ho_ten, sdt, sogplx, trang_thai, idtk)
SELECT 'Nguyen Van Tai', '0900000001', 'GPLX-TEST-01', 1, idtk FROM tai_khoan
WHERE ten_dang_nhap = 'taixe01' AND idtk NOT IN (SELECT idtk FROM tai_xe);
INSERT INTO tai_xe (ho_ten, sdt, sogplx, trang_thai, idtk)
SELECT 'Tran Van Hai', '0900000002', 'GPLX-TEST-02', 1, idtk FROM tai_khoan
WHERE ten_dang_nhap = 'taixe02' AND idtk NOT IN (SELECT idtk FROM tai_xe);
SET @tx1 = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe01'));
SET @tx2 = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe02'));
UPDATE tai_xe SET sogplx = COALESCE(NULLIF(sogplx, ''), 'GPLX-TEST'), trang_thai = 1 WHERE idtx IN (@tx1, @tx2);

-- 4) Khách hàng + công trình (có tọa độ: app kiểm tra bán kính 200 m khi bấm "Đã đến")
INSERT INTO khach_hang (idtk, ten_khach_hang, sdt, email)
SELECT idtk, 'Nguyen Thi Hoa', '0911111111', 'khachhang01@example.com' FROM tai_khoan
WHERE ten_dang_nhap = 'khachhang01' AND idtk NOT IN (SELECT idtk FROM khach_hang);
SET @kh = (SELECT idkh FROM khach_hang WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'khachhang01'));

INSERT INTO cong_trinh (idkh, ten_cong_trinh, dia_chi, vi_do, kinh_do, sdt)
SELECT @kh, 'Cong trinh so 1', '233 Hoang Quoc Viet, Cau Giay, Ha Noi', 21.0045, 105.7985, '0922222222' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cong_trinh WHERE sdt = '0922222222');
SET @ct = (SELECT idct FROM cong_trinh WHERE sdt = '0922222222' LIMIT 1);
UPDATE cong_trinh SET vi_do = 21.0045, kinh_do = 105.7985 WHERE idct = @ct;

-- 5) Trạm trộn (hoạt động, có tọa độ)
INSERT INTO tram_tron (ten_tram, dia_chi, cong_suat, sdt, trang_thai, vi_do, kinh_do)
SELECT 'Tram tron so 1', 'Cau Giay, Ha Noi', 50, '0933333333', 1, 21.0362, 105.7906 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM tram_tron WHERE sdt = '0933333333');
SET @tram = (SELECT id_tram FROM tram_tron WHERE sdt = '0933333333' LIMIT 1);
UPDATE tram_tron SET vi_do = 21.0362, kinh_do = 105.7906, trang_thai = 1 WHERE id_tram = @tram;

-- 6) Loại bê tông
INSERT INTO loai_be_tong (mac_be_tong, thanh_phan, don_gia, trang_thai, mo_ta)
SELECT 'M300', 'Xi mang, cat, da, nuoc', 1500000, 1, 'Be tong thuong pham' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM loai_be_tong WHERE mac_be_tong = 'M300');
SET @lbt = (SELECT idlbt FROM loai_be_tong WHERE mac_be_tong = 'M300' LIMIT 1);

-- 7) Xe, mỗi tài xế 1 xe, trạng thái 1 = Đang hoạt động (sẵn sàng)
INSERT INTO xe (bien_so, trong_tai, trang_thai, idtx) SELECT '29C-123.45', 9, 1, @tx1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM xe WHERE bien_so = '29C-123.45');
INSERT INTO xe (bien_so, trong_tai, trang_thai, idtx) SELECT '29C-678.90', 9, 1, @tx2 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM xe WHERE bien_so = '29C-678.90');
UPDATE xe SET idtx = @tx1 WHERE bien_so = '29C-123.45';
UPDATE xe SET idtx = @tx2 WHERE bien_so = '29C-678.90';

-- 8) Hai đơn hàng trạng thái 6 = Đã phân bổ trạm -> điều phối tạo chuyến trên web
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 8, 12000000, 6, 'Don test 1', @tram, 0);
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (LAST_INSERT_ID(), @lbt, 8, 1500000, 12000000);
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram, version)
VALUES (@kh, @ct, CURDATE(), DATE_ADD(NOW(), INTERVAL 3 HOUR), 6, 9000000, 6, 'Don test 2', @tram, 0);
INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien) VALUES (LAST_INSERT_ID(), @lbt, 6, 1500000, 9000000);

-- Kiểm tra
SELECT tk.ten_dang_nhap, tx.idtx, tx.sogplx, x.id_xe, x.bien_so, x.trang_thai AS trang_thai_xe
FROM tai_xe tx JOIN tai_khoan tk ON tk.idtk = tx.idtk LEFT JOIN xe x ON x.idtx = tx.idtx;
SELECT iddh, trang_thai, tong_khoi_luong, id_tram, ghi_chu FROM don_hang WHERE trang_thai = 6 ORDER BY iddh DESC;
