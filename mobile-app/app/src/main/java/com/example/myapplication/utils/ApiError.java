package com.example.myapplication.utils;

import org.json.JSONObject;
import retrofit2.Response;

/** Lấy câu thông báo lỗi tiếng Việt mà backend trả về ({"message": "..."}) để hiển thị cho tài xế. */
public final class ApiError {

    private ApiError() {}

    public static String message(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                String msg = new JSONObject(raw).optString("message", "");
                if (!msg.isEmpty()) return msg;
            }
        } catch (Exception ignored) {
            // Body không phải JSON - dùng thông báo mặc định theo mã lỗi bên dưới
        }
        switch (response.code()) {
            case 401: return "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại";
            case 403: return "Bạn không có quyền thực hiện thao tác này";
            case 404: return "Chuyến không còn hiệu lực hoặc đã được chuyển cho tài xế khác";
            case 409: return "Trạng thái chuyến đã thay đổi, vui lòng tải lại";
            default: return "Không thực hiện được thao tác (mã lỗi " + response.code() + ")";
        }
    }
}
