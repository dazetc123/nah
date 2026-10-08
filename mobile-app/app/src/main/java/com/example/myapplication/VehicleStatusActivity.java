package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.FormParts;
import com.example.myapplication.utils.LocationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Báo cáo tình trạng xe (tài xế): báo xe hỏng / cần bảo trì, hoặc báo đã sửa xong.
 * - Bảo trì / Hỏng: xe bị khoá, điều phối không giao chuyến cho xe này nữa.
 *   Bắt buộc vị trí, nguyên nhân, mô tả và ảnh. Không cho báo khi xe đang có
 *   chuyến chưa hoàn thành (khi đó dùng Báo cáo sự cố để điều phối xử lý chuyến).
 * - Sẵn sàng hoạt động: mở khoá xe, không cần ảnh.
 * Quản lý xem lịch sử báo cáo ở trang "Báo cáo sự cố" trên web.
 */
public class VehicleStatusActivity extends AppCompatActivity {
    private static final int SAN_SANG = 0, BAO_TRI = 1, HONG = 2;

    private MaterialCardView[] cards;
    private String[] titles;
    private int selected = -1;
    private MaterialButton btnSave, btnChonAnh;
    private TextView tvXeInfo, tvCanhBao;
    private View formChiTiet;
    private TextInputLayout tilViTri, tilNguyenNhan, tilMoTa;
    private TextInputEditText etViTri, etNguyenNhan, etMoTa;
    private ImageView ivPreview;

    private Long idXe;
    private Integer trangThaiHienTai;
    private boolean dangCoChuyen;
    private Uri anhUri;

    private final ActivityResultLauncher<String> chonAnh =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    anhUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    btnChonAnh.setText("Đổi ảnh khác");
                }
            });

    private final ActivityResultLauncher<String> quyenViTri =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) dienViTri();
                else thongBao("Cần cấp quyền vị trí để lấy vị trí hiện tại");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_status);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        btnSave = findViewById(R.id.btnSaveStatus);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        tvXeInfo = findViewById(R.id.tvXeInfo);
        tvCanhBao = findViewById(R.id.tvCanhBao);
        formChiTiet = findViewById(R.id.formChiTiet);
        tilViTri = findViewById(R.id.tilViTri);
        tilNguyenNhan = findViewById(R.id.tilNguyenNhan);
        tilMoTa = findViewById(R.id.tilMoTa);
        etViTri = findViewById(R.id.etViTri);
        etNguyenNhan = findViewById(R.id.etNguyenNhan);
        etMoTa = findViewById(R.id.etMoTa);
        ivPreview = findViewById(R.id.ivPreview);

        titles = new String[]{"Sẵn sàng hoạt động", "Đang bảo trì", "Hỏng – không chạy được"};
        cards = new MaterialCardView[]{
                setup(R.id.optReady, SAN_SANG, R.drawable.ic_check_circle, R.drawable.bg_icon_green, R.color.colorSuccess,
                        "Xe đã sửa xong, có thể nhận chuyến"),
                setup(R.id.optMaintenance, BAO_TRI, R.drawable.ic_build, R.drawable.bg_icon_amber, R.color.colorWarning,
                        "Đang bảo dưỡng định kỳ hoặc sửa chữa nhỏ"),
                setup(R.id.optBroken, HONG, R.drawable.ic_block, R.drawable.bg_icon_red, R.color.colorError,
                        "Xe gặp sự cố, không chạy được")
        };

        btnChonAnh.setOnClickListener(v -> chonAnh.launch("image/*"));
        findViewById(R.id.btnGps).setOnClickListener(v -> {
            if (LocationUtil.coQuyen(this)) dienViTri();
            else quyenViTri.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        });
        btnSave.setOnClickListener(v -> gui());
        taiXe();
    }

    /** Lấy xe đang được gán cho tài xế, trạng thái hiện tại và xe có đang chạy chuyến không. */
    private void taiXe() {
        ApiClient.getService(this).getXeCuaToi().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvXeInfo.setText(ApiError.message(response));
                    return;
                }
                Map<String, Object> xe = response.body();
                idXe = ((Number) xe.get("idXe")).longValue();
                trangThaiHienTai = xe.get("trangThai") == null ? null : ((Number) xe.get("trangThai")).intValue();
                dangCoChuyen = Boolean.TRUE.equals(xe.get("dangCoChuyen"));
                tvXeInfo.setText("Xe " + xe.get("bienSo") + " · Hiện tại: " + xe.get("tenTrangThai"));
                if (dangCoChuyen) {
                    tvCanhBao.setText("Xe đang có chuyến chưa hoàn thành nên chưa báo bảo trì được. "
                            + "Nếu xe gặp sự cố trong lúc chạy chuyến, hãy dùng \"Báo cáo sự cố\" để điều phối xử lý.");
                    tvCanhBao.setVisibility(View.VISIBLE);
                }
                if (selected >= 0) btnSave.setEnabled(true);
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                tvXeInfo.setText("Không kết nối được máy chủ. Thoát ra vào lại để thử lại.");
            }
        });
    }

    private MaterialCardView setup(int includeId, int index, int icon, int iconBg, int tint, String subtitle) {
        MaterialCardView card = findViewById(includeId);
        ImageView iv = card.findViewById(R.id.ivIcon);
        iv.setImageResource(icon);
        iv.setColorFilter(ContextCompat.getColor(this, tint));
        card.findViewById(R.id.iconBg).setBackgroundResource(iconBg);
        ((TextView) card.findViewById(R.id.tvTitle)).setText(titles[index]);
        ((TextView) card.findViewById(R.id.tvSubtitle)).setText(subtitle);
        card.setOnClickListener(v -> select(index));
        return card;
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < cards.length; i++) {
            boolean on = i == index;
            cards[i].setChecked(on);
            cards[i].setStrokeColor(ContextCompat.getColor(this, on ? R.color.colorPrimary : R.color.colorCardStroke));
        }
        formChiTiet.setVisibility(index == SAN_SANG ? View.GONE : View.VISIBLE);
        if (index != SAN_SANG && LocationUtil.coQuyen(this) && text(etViTri).isEmpty()) dienViTri();
        btnSave.setEnabled(idXe != null);
        btnSave.setText("Cập nhật: " + titles[index]);
    }

    private void dienViTri() {
        Location l = LocationUtil.viTriGanNhat(this);
        if (l == null) { thongBao("Chưa xác định được vị trí. Hãy bật GPS và thử lại"); return; }
        etViTri.setText(String.format(Locale.US, "%.6f, %.6f", l.getLatitude(), l.getLongitude()));
        tilViTri.setError(null);
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void gui() {
        if (selected < 0 || idXe == null) return;
        boolean baoTri = selected != SAN_SANG;
        if (baoTri && dangCoChuyen) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Xe đang có chuyến")
                    .setMessage("Xe đang có chuyến chưa hoàn thành. Hãy dùng Báo cáo sự cố để điều phối xử lý chuyến, "
                            + "rồi báo bảo trì sau khi kết thúc chuyến.")
                    .setNegativeButton("Đóng", null)
                    .setPositiveButton("Mở Báo cáo sự cố", (d, w) -> {
                        startActivity(new Intent(this, FaultReportActivity.class));
                        finish();
                    })
                    .show();
            return;
        }

        MultipartBody.Part anh = null;
        if (baoTri) {
            tilViTri.setError(null); tilNguyenNhan.setError(null); tilMoTa.setError(null);
            boolean ok = true;
            if (text(etViTri).isEmpty()) { tilViTri.setError("Vui lòng nhập vị trí xe hoặc lấy vị trí hiện tại"); ok = false; }
            if (text(etNguyenNhan).isEmpty()) { tilNguyenNhan.setError("Vui lòng nhập nguyên nhân"); ok = false; }
            if (text(etMoTa).isEmpty()) { tilMoTa.setError("Vui lòng mô tả tình trạng xe"); ok = false; }
            if (anhUri == null) { thongBao("Vui lòng chọn ảnh tình trạng xe"); ok = false; }
            if (!ok) return;
            try {
                anh = FormParts.image(this, anhUri, "anh");
            } catch (Exception e) {
                thongBao(e.getMessage() == null ? "Không đọc được ảnh, vui lòng chọn ảnh khác" : e.getMessage());
                return;
            }
        }

        String nguyenNhan = baoTri ? (selected == HONG ? "Hỏng: " : "Bảo trì: ") + text(etNguyenNhan) : null;
        btnSave.setEnabled(false);
        ApiClient.getService(this).baoCaoTinhTrangXe(idXe,
                FormParts.text(baoTri ? 0 : 1),
                FormParts.text(baoTri ? text(etViTri) : null),
                FormParts.text(nguyenNhan),
                FormParts.text(baoTri ? text(etMoTa) : null),
                anh).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSave.setEnabled(true);
                if (!response.isSuccessful()) { thongBao(ApiError.message(response)); return; }
                new MaterialAlertDialogBuilder(VehicleStatusActivity.this)
                        .setTitle("Đã cập nhật trạng thái xe")
                        .setMessage(baoTri
                                ? "Xe đã chuyển sang Bảo trì. Điều phối sẽ không giao chuyến cho xe này cho tới khi bạn báo sửa xong."
                                : "Xe đã sẵn sàng hoạt động, điều phối có thể giao chuyến cho xe này.")
                        .setCancelable(false)
                        .setPositiveButton("Xong", (d, w) -> finish())
                        .show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSave.setEnabled(true);
                thongBao("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void thongBao(String msg) {
        Snackbar.make(btnSave, msg, Snackbar.LENGTH_LONG).show();
    }
}
