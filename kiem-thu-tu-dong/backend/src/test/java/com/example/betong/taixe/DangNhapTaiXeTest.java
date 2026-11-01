package com.example.betong.taixe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kiểm thử chức năng Đăng nhập ứng dụng Tài xế (phần backend). */
@DisplayName("Đăng nhập ứng dụng Tài xế")
class DangNhapTaiXeTest extends KiemThuTaiXeCoSo {

    @Test
    @DisplayName("DN-02 Đăng nhập hợp lệ: trả token và vai trò Tài xế")
    void dn02_dangNhapHopLe() throws Exception {
        TaiXeTest tx = taoTaiXe();

        mvc.perform(post("/api/auth/dang-nhap").header("ClientKey", KEY_APP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dinhDanh\":\"" + tx.tenDangNhap() + "\",\"matKhau\":\"123456789\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tenVaiTro").value("Tài xế"))
                .andExpect(jsonPath("$.phaiDoiMatKhau").value(false));
    }

    @Test
    @DisplayName("DN-04 Sai mật khẩu: bị từ chối, không cấp token")
    void dn04_saiMatKhau() throws Exception {
        TaiXeTest tx = taoTaiXe();

        mvc.perform(post("/api/auth/dang-nhap").header("ClientKey", KEY_APP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dinhDanh\":\"" + tx.tenDangNhap() + "\",\"matKhau\":\"saimatkhau99\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("DN-06 Gọi API tài xế khi chưa đăng nhập: trả 401")
    void dn06_chuaDangNhap() throws Exception {
        mvc.perform(get("/api/tai-xe/chuyen/cua-toi").header("ClientKey", KEY_APP))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DN-07 Thiếu header ClientKey: trả 400")
    void dn07_thieuClientKey() throws Exception {
        TaiXeTest tx = taoTaiXe();

        mvc.perform(get("/api/tai-xe/chuyen/cua-toi").header("Authorization", "Bearer " + tx.token()))
                .andExpect(status().isBadRequest());
    }
}
