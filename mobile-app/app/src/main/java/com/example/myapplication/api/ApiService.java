package com.example.myapplication.api;

import com.example.myapplication.models.AuthResponse;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.ProfileResponse;
import com.example.myapplication.models.ChangePasswordRequest;
import com.example.myapplication.models.common.PageResponse;
import com.example.myapplication.models.trip.ChuyenChiTiet;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.example.myapplication.models.trip.DaDenRequest;
import com.example.myapplication.models.trip.SuCo;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/dang-nhap")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("api/nguoi-dung/ho-so")
    Call<ProfileResponse> getProfile();

    @PUT("api/nguoi-dung/doi-mat-khau")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);

    // ===================== Mục 2.2.1 - Quản lý chuyến =====================

    @GET("api/tai-xe/chuyen")
    Call<PageResponse<ChuyenDanhSach>> getDanhSachChuyen(
            @Query("trangThai") Integer trangThai,
            @Query("ngay") String ngay,
            @Query("trang") int trang,
            @Query("soLuong") int soLuong);

    @GET("api/tai-xe/chuyen/{id}")
    Call<ChuyenChiTiet> getChiTietChuyen(@Path("id") long idChuyen);

    @POST("api/tai-xe/chuyen/{id}/nhan")
    Call<ChuyenChiTiet> nhanChuyen(@Path("id") long idChuyen);

    @POST("api/tai-xe/chuyen/{id}/bat-dau")
    Call<ChuyenChiTiet> batDauChuyen(@Path("id") long idChuyen);

    @POST("api/tai-xe/chuyen/{id}/hoan-thanh")
    Call<ChuyenChiTiet> hoanThanhChuyen(@Path("id") long idChuyen);

    // ===================== Mục 2.2.3 - Cập nhật trạng thái =====================

    @POST("api/tai-xe/chuyen/{id}/da-den")
    Call<ChuyenChiTiet> xacNhanDaDen(@Path("id") long idChuyen, @Body DaDenRequest request);

    /** anhMinhChung có thể null - Retrofit tự bỏ qua part null. */
    @Multipart
    @POST("api/tai-xe/chuyen/{id}/giao-hang")
    Call<ChuyenChiTiet> xacNhanGiaoHang(@Path("id") long idChuyen,
                                        @Part("khoiLuongThucGiao") RequestBody khoiLuongThucGiao,
                                        @Part("ghiChu") RequestBody ghiChu,
                                        @Part MultipartBody.Part anhMinhChung);

    /** idChuyen null = server tự lấy chuyến đang thực hiện của tài xế. */
    @Multipart
    @POST("api/tai-xe/su-co")
    Call<SuCo> baoCaoSuCo(@Part("idChuyen") RequestBody idChuyen,
                          @Part("loaiSuCo") RequestBody loaiSuCo,
                          @Part("moTa") RequestBody moTa,
                          @Part("mucDoUuTien") RequestBody mucDoUuTien,
                          @Part("viDo") RequestBody viDo,
                          @Part("kinhDo") RequestBody kinhDo,
                          @Part MultipartBody.Part anh);
}
