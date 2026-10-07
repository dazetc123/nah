-- Dữ liệu mẫu để test nhóm chức năng "Quản lý chuyến" (mục 2.2.1) từ app di động.
-- Chạy sau khi đã có sẵn tài khoản tài xế "taixe01" (xem hướng dẫn trước đó).
-- Dùng UNHEX(...) để tránh lỗi mã hóa tiếng Việt khi gõ trong cmd (giống lần trước).

USE quanly_betong;
SET NAMES utf8mb4;

-- 1) Khách hàng (gắn với 1 tài khoản khách hàng giả)
INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, email, ho_ten, trang_thai, da_xac_thuc, phaidoimatkhau, id_vai_tro)
VALUES ('khachhang01',
        '$2b$10$rLLNt6W1iqjQp6FhPCLsc.RfSESQmM/olgTqimc6BD0W6dx0CeUlK', -- mật khẩu: 123456789
        'khachhang01@example.com',
        CONVERT(UNHEX('4E677579E1BB856E205468E1BB8B20486F61') USING utf8mb4),
        1, 1, 0,
        (SELECT id_vai_tro FROM vai_tro WHERE ten_vai_tro = CONVERT(UNHEX('4B68C3A163682068C3A06E67') USING utf8mb4)));

INSERT INTO khach_hang (idtk, ten_khach_hang, sdt, email)
VALUES ((SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'khachhang01'),
        CONVERT(UNHEX('4E677579E1BB856E205468E1BB8B20486F61') USING utf8mb4),
        '0911111111', 'khachhang01@example.com');

-- 2) Công trình
INSERT INTO cong_trinh (idkh, ten_cong_trinh, dia_chi, vi_do, kinh_do, sdt)
VALUES ((SELECT idkh FROM khach_hang WHERE sdt = '0911111111'),
        CONVERT(UNHEX('43C3B46E67207472C3AC6E682073E1BB912031') USING utf8mb4),
        CONVERT(UNHEX('32333320486FC3A06E67205175E1BB9163205669E1BB87742C2043E1BAA775204769E1BAA5792C2048C3A0204EE1BB9969') USING utf8mb4),
        21.0045, 105.7985, '0922222222');

-- 3) Trạm trộn (trạng thái 1 = hoạt động)
INSERT INTO tram_tron (ten_tram, dia_chi, cong_suat, sdt, trang_thai)
VALUES (CONVERT(UNHEX('5472E1BAA16D207472E1BB996E2073E1BB912031') USING utf8mb4),
        CONVERT(UNHEX('43E1BAA775204769E1BAA5792C2048C3A0204EE1BB9969') USING utf8mb4),
        50, '0933333333', 1);

-- 4) Loại bê tông
INSERT INTO loai_be_tong (mac_be_tong, thanh_phan, don_gia, trang_thai, mo_ta)
VALUES ('M300',
        CONVERT(UNHEX('5869206DC4836E672C2063C3A1742C20C491C3A12C206EC6B0E1BB9B63') USING utf8mb4),
        1500000, 1,
        CONVERT(UNHEX('42C3AA2074C3B46E672063C6B0E1BB9D6E6720C491E1BB992063616F') USING utf8mb4));

-- 5) Xe (trạng thái 1 = sẵn sàng). PK thật của bảng xe là id_xe (có gạch dưới).
INSERT INTO xe (bien_so, trong_tai, trang_thai)
VALUES ('29C-123.45', 9, 1);

-- 6) Gán xe cho tài xế taixe01. Cột FK sang tai_xe trong bảng xe tên là idtx (không gạch dưới).
UPDATE xe SET idtx = (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe01'))
WHERE bien_so = '29C-123.45';

-- 7) Đơn hàng (trạng thái 6 = đã phân bổ trạm, điều kiện để tạo chuyến)
INSERT INTO don_hang (idkh, idct, ngay_dat, thoi_gian_giao, tong_khoi_luong, tong_tien, trang_thai, ghi_chu, id_tram)
VALUES ((SELECT idkh FROM khach_hang WHERE sdt = '0911111111'),
        (SELECT idct FROM cong_trinh WHERE sdt = '0922222222'),
        CURDATE(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 8.5, 12750000, 6,
        CONVERT(UNHEX('4769616F207468E1BBAD206E676869E1BB876D') USING utf8mb4),
        (SELECT id_tram FROM tram_tron WHERE sdt = '0933333333'));

INSERT INTO chi_tiet_don_hang (iddh, idlbt, khoi_luong, don_gia, thanh_tien)
VALUES ((SELECT iddh FROM don_hang ORDER BY iddh DESC LIMIT 1),
        (SELECT idlbt FROM loai_be_tong WHERE mac_be_tong = 'M300'),
        8.5, 1500000, 12750000);

-- 8) Chuyến (trạng thái 0 = Chờ nhận) -- sẵn sàng để test "Nhận chuyến" trên app.
-- PK bảng chuyen là id_chuyen; FK sang xe là id_xe; FK sang tai_xe là idtx (không gạch dưới).
INSERT INTO chuyen (iddh, id_xe, idtx, id_tram, khoi_luong, trang_thai)
VALUES ((SELECT iddh FROM don_hang ORDER BY iddh DESC LIMIT 1),
        (SELECT id_xe FROM xe WHERE bien_so = '29C-123.45'),
        (SELECT idtx FROM tai_xe WHERE idtk = (SELECT idtk FROM tai_khoan WHERE ten_dang_nhap = 'taixe01')),
        (SELECT id_tram FROM tram_tron WHERE sdt = '0933333333'),
        8.5, 0);

-- Kiểm tra
SELECT c.id_chuyen, c.trang_thai, x.bien_so, d.iddh
FROM chuyen c JOIN xe x ON x.id_xe = c.id_xe JOIN don_hang d ON d.iddh = c.iddh
ORDER BY c.id_chuyen DESC LIMIT 1;
