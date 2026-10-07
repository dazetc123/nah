package com.example.myapplication.utils;

import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;

/**
 * Hằng số trạng thái Chuyen (khớp com.example.betong.Common.TrangThaiChuyen
 * bên backend) + các hàm hiển thị dùng chung giữa danh sách và chi tiết.
 */
public final class TrangThaiChuyenUtil {

    public static final int CHO_NHAN = 0;
    public static final int DA_NHAN = 1;
    public static final int DANG_GIAO = 2;
    public static final int DA_DEN = 3;
    public static final int DA_GIAO_HANG = 4;
    public static final int HOAN_THANH = 5;

    private TrangThaiChuyenUtil() {}

    public static void toMauTrangThai(TextView tv, int trangThai) {
        int bg, textColor;
        switch (trangThai) {
            case DA_NHAN:
            case DANG_GIAO:
            case DA_DEN:
                bg = R.drawable.bg_icon_indigo;
                textColor = R.color.colorPrimary;
                break;
            case DA_GIAO_HANG:
            case HOAN_THANH:
                bg = R.drawable.bg_icon_green;
                textColor = R.color.colorSuccess;
                break;
            case CHO_NHAN:
            default:
                bg = R.drawable.bg_icon_amber;
                textColor = R.color.colorWarning;
                break;
        }
        tv.setBackgroundResource(bg);
        tv.setTextColor(ContextCompat.getColor(tv.getContext(), textColor));
    }

    /**
     * API trả ISO-8601 không timezone (vd "2026-10-15T09:30:00" hoặc
     * "2026-10-15T09:30:00.123"); hiển thị dạng dd/MM HH:mm.
     * Không dùng java.time vì minSdk=24 (java.time chỉ có sẵn từ API 26
     * trở lên nếu không bật core library desugaring) — tự tách chuỗi.
     */
    public static String dinhDangNgayGio(String isoValue) {
        if (isoValue == null || isoValue.length() < 16 || isoValue.charAt(4) != '-'
                || isoValue.charAt(7) != '-' || isoValue.charAt(10) != 'T') {
            return isoValue == null ? "" : isoValue;
        }
        try {
            String mm = isoValue.substring(5, 7);
            String dd = isoValue.substring(8, 10);
            String hh = isoValue.substring(11, 13);
            String mi = isoValue.substring(14, 16);
            return dd + "/" + mm + " " + hh + ":" + mi;
        } catch (Exception e) {
            return isoValue;
        }
    }
}
