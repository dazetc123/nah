package com.example.betong.Common;

/**
 * Các giá trị của Chuyen.trangThai, dùng chung cho mọi module (Điều phối,
 * Tài xế, Theo dõi xe) để tránh mỗi nơi tự định nghĩa một kiểu khác nhau.
 *
 * Luồng vòng đời: CHO_NHAN -> DA_NHAN -> DANG_GIAO -> DA_DEN -> DA_GIAO_HANG -> HOAN_THANH
 * (Mục 2.2.1 - 2.2.3 báo cáo: Quản lý chuyến / Gửi vị trí GPS / Cập nhật trạng thái)
 */
public final class TrangThaiChuyen {

    public static final int CHO_NHAN = 0;      // Chuyến vừa được điều phối tạo, chờ tài xế nhận
    public static final int DA_NHAN = 1;        // Tài xế đã xác nhận nhận chuyến
    public static final int DANG_GIAO = 2;      // Tài xế đã bắt đầu chuyến, đang di chuyển
    public static final int DA_DEN = 3;         // Tài xế đã xác nhận đến công trình
    public static final int DA_GIAO_HANG = 4;   // Tài xế đã xác nhận giao hàng xong
    public static final int HOAN_THANH = 5;     // Chuyến kết thúc, xe và tài xế về Sẵn sàng

    /** Các trạng thái coi là "đang hoạt động" — xe/tài xế chưa rảnh để nhận chuyến khác. */
    public static final int[] DANG_HOAT_DONG = { CHO_NHAN, DA_NHAN, DANG_GIAO, DA_DEN, DA_GIAO_HANG };

    private TrangThaiChuyen() {}

    public static String tenHienThi(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case CHO_NHAN -> "Chờ nhận";
            case DA_NHAN -> "Đã nhận";
            case DANG_GIAO -> "Đang giao";
            case DA_DEN -> "Đã đến công trình";
            case DA_GIAO_HANG -> "Đã giao hàng";
            case HOAN_THANH -> "Hoàn thành";
            default -> "Không xác định";
        };
    }
}
