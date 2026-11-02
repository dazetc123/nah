package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.trip.ChuyenChiTiet;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.FormParts;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.3 - Usecase "Xác nhận giao hàng thành công": form khối lượng thực
 * giao, thời điểm kết thúc, ghi chú và ảnh minh chứng / chữ ký.
 * Trả RESULT_OK cho TripDetailActivity khi server xác nhận thành công.
 */
public class DeliveryConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_ID_CHUYEN = "extra_id_chuyen";
    public static final String EXTRA_KHOI_LUONG = "extra_khoi_luong";
    private static final double SAI_SO = 1e-6;

    private long idChuyen;
    private Double khoiLuongChuyen;
    private Uri anhUri;

    private TextInputLayout tilKhoiLuong, tilGhiChu;
    private TextInputEditText etKhoiLuong, etGhiChu;
    private ImageView ivPreview;
    private MaterialButton btnXacNhan, btnChonAnh;

    private final ActivityResultLauncher<String> chonAnh =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    anhUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    btnChonAnh.setText("Đổi ảnh khác");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_confirm);
        idChuyen = getIntent().getLongExtra(EXTRA_ID_CHUYEN, -1);
        if (idChuyen <= 0) { finish(); return; }
        if (getIntent().hasExtra(EXTRA_KHOI_LUONG)) {
            khoiLuongChuyen = getIntent().getDoubleExtra(EXTRA_KHOI_LUONG, 0);
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Chuyến #" + idChuyen);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilKhoiLuong = findViewById(R.id.tilKhoiLuong);
        tilGhiChu = findViewById(R.id.tilGhiChu);
        etKhoiLuong = findViewById(R.id.etKhoiLuong);
        etGhiChu = findViewById(R.id.etGhiChu);
        ivPreview = findViewById(R.id.ivPreview);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        btnXacNhan = findViewById(R.id.btnXacNhan);

        ((TextView) findViewById(R.id.tvKhoiLuongChuyen)).setText(khoiLuongChuyen == null
                ? "Khối lượng của chuyến: —"
                : String.format(Locale.getDefault(), "Khối lượng của chuyến: %.1f m³", khoiLuongChuyen));
        ((TextView) findViewById(R.id.tvThoiDiem)).setText("Thời điểm kết thúc: "
                + new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date()));
        if (khoiLuongChuyen != null) {
            etKhoiLuong.setText(String.format(Locale.US, "%.1f", khoiLuongChuyen));
        }

        btnChonAnh.setOnClickListener(v -> chonAnh.launch("image/*"));
        findViewById(R.id.btnHuy).setOnClickListener(v -> finish());
        btnXacNhan.setOnClickListener(v -> gui());
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void gui() {
        tilKhoiLuong.setError(null);
        tilGhiChu.setError(null);

        // Luồng 4.a: thiếu khối lượng thực giao
        Double khoiLuong;
        try {
            khoiLuong = Double.parseDouble(text(etKhoiLuong).replace(',', '.'));
        } catch (NumberFormatException e) {
            khoiLuong = null;
        }
        if (khoiLuong == null || khoiLuong <= 0) {
            tilKhoiLuong.setError("Vui lòng nhập khối lượng thực giao lớn hơn 0");
            return;
        }
        String ghiChu = text(etGhiChu);
        // Luồng 4.b: vượt khối lượng của chuyến -> bắt buộc ghi chú lý do
        if (khoiLuongChuyen != null && khoiLuong > khoiLuongChuyen + SAI_SO && ghiChu.isEmpty()) {
            tilGhiChu.setError(String.format(Locale.getDefault(),
                    "Khối lượng vượt %.1f m³ của chuyến, vui lòng nhập lý do", khoiLuongChuyen));
            return;
        }

        MultipartBody.Part anh;
        try {
            anh = FormParts.image(this, anhUri, "anhMinhChung");
        } catch (Exception e) {
            thongBao(e.getMessage() == null ? "Không đọc được ảnh, vui lòng chọn ảnh khác" : e.getMessage());
            return;
        }

        final double kl = khoiLuong;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận giao hàng")
                .setMessage(String.format(Locale.getDefault(),
                        "Xác nhận đã giao %.1f m³ cho chuyến #%d?", kl, idChuyen))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> goiApi(kl, ghiChu, anh))
                .show();
    }

    private void goiApi(double khoiLuong, String ghiChu, MultipartBody.Part anh) {
        btnXacNhan.setEnabled(false);
        btnXacNhan.setText("Đang gửi...");
        ApiClient.getService(this).xacNhanGiaoHang(idChuyen,
                FormParts.text(khoiLuong),
                FormParts.text(ghiChu.isEmpty() ? null : ghiChu),
                anh).enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                khoiPhucNut();
                String msg = ApiError.message(response);
                if (response.code() == 422) tilGhiChu.setError(msg);
                else thongBao(msg);
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                khoiPhucNut();
                thongBao("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void khoiPhucNut() {
        btnXacNhan.setEnabled(true);
        btnXacNhan.setText("Xác nhận giao hàng");
    }

    private void thongBao(String msg) {
        Snackbar.make(btnXacNhan, msg, Snackbar.LENGTH_LONG).show();
    }
}
