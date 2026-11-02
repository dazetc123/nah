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

    /**
     * Khoảng thời gian giữa 2 mốc ISO-8601, dạng "\nThời gian thực hiện: 1 giờ 25 phút".
     * Trả về chuỗi rỗng nếu thiếu mốc hoặc không đọc được.
     */
    public static String thoiLuong(String tuIso, String denIso) {
        if (tuIso == null || denIso == null || tuIso.length() < 19 || denIso.length() < 19) return "";
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            long ms = f.parse(denIso.substring(0, 19)).getTime() - f.parse(tuIso.substring(0, 19)).getTime();
            if (ms < 0) return "";
            long phut = ms / 60000;
            String text = phut >= 60 ? (phut / 60) + " giờ " + (phut % 60) + " phút" : phut + " phút";
            return "\nThời gian thực hiện: " + text;
        } catch (Exception e) {
            return "";
        }
    }
}
