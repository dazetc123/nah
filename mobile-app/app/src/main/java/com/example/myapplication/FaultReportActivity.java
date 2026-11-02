package com.example.myapplication;

import android.Manifest;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.trip.SuCo;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.FormParts;
import com.example.myapplication.utils.LocationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.3 - Usecase "Báo cáo sự cố": loại sự cố, mô tả, ảnh đính kèm,
 * mức độ ưu tiên và vị trí hiện tại, gắn với chuyến đang thực hiện.
 * Mở từ màn chi tiết chuyến (có EXTRA_ID_CHUYEN) hoặc từ trang chủ (không có
 * id - server tự lấy chuyến đang thực hiện của tài xế).
 */
public class FaultReportActivity extends AppCompatActivity {

    public static final String EXTRA_ID_CHUYEN = "extra_id_chuyen";

    private ChipGroup chipGroupType, chipGroupPriority;
    private TextView tvTypeError;
    private TextInputLayout tilLocation, tilDescription;
    private TextInputEditText etLocation, etDescription;
    private ImageView ivPreview;
    private MaterialButton btnSubmit, btnUpload;
    private Uri imageUri;
    private Long idChuyen;
    private Double viDo, kinhDo;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setVisibility(View.VISIBLE);
                    btnUpload.setText("Đổi ảnh khác");
                }
            });

    private final ActivityResultLauncher<String> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) fillLocation(true);
                else toast("Cần cấp quyền vị trí để đính kèm vị trí vào báo cáo");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_report);

        long id = getIntent().getLongExtra(EXTRA_ID_CHUYEN, -1);
        idChuyen = id > 0 ? id : null;

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> attemptExit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { attemptExit(); }
        });

        chipGroupType = findViewById(R.id.chipGroupType);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        tvTypeError = findViewById(R.id.tvTypeError);
        tilLocation = findViewById(R.id.tilLocation);
        tilDescription = findViewById(R.id.tilDescription);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        ivPreview = findViewById(R.id.ivPreview);
        btnUpload = findViewById(R.id.btnUploadImage);
        btnSubmit = findViewById(R.id.btnSubmitFault);

        ((TextView) findViewById(R.id.tvChuyen)).setText(idChuyen != null
                ? "Chuyến #" + idChuyen
                : "Chuyến đang thực hiện (hệ thống tự xác định)");

        chipGroupType.setOnCheckedStateChangeListener((group, ids) -> {
            tvTypeError.setVisibility(View.GONE);
            // Luồng 6.a: sự cố nghiêm trọng -> tự đặt mức ưu tiên Cao
            String loai = loaiDaChon();
            if ("Hỏng xe".equals(loai) || "Tai nạn".equals(loai)) chipGroupPriority.check(R.id.chipCao);
        });

        btnUpload.setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnGps).setOnClickListener(v -> {
            if (LocationUtil.coQuyen(this)) fillLocation(true);
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> attemptExit());
        btnSubmit.setOnClickListener(v -> submit());

        // Bước 4: hệ thống tự lấy vị trí hiện tại kèm theo báo cáo (nếu đã có quyền)
        if (LocationUtil.coQuyen(this)) fillLocation(false);
    }

    private void fillLocation(boolean baoLoi) {
        Location best = LocationUtil.viTriGanNhat(this);
        if (best == null) {
            if (baoLoi) toast("Chưa xác định được vị trí. Hãy bật GPS và thử lại");
            return;
        }
        viDo = best.getLatitude();
        kinhDo = best.getLongitude();
        etLocation.setText(String.format(Locale.US, "%.6f, %.6f", viDo, kinhDo));
        tilLocation.setError(null);
    }

    private String loaiDaChon() {
        int id = chipGroupType.getCheckedChipId();
        if (id == View.NO_ID) return null;
        Chip chip = findViewById(id);
        return chip == null ? null : chip.getText().toString();
    }

    private int mucDoDaChon() {
        int id = chipGroupPriority.getCheckedChipId();
        if (id == R.id.chipThap) return 1;
        if (id == R.id.chipCao) return 3;
        return 2;
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private boolean hasInput() {
        return loaiDaChon() != null || !text(etDescription).isEmpty() || imageUri != null;
    }

    private void attemptExit() {
        if (!hasInput()) { finish(); return; }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Thoát báo cáo?")
                .setMessage("Thông tin bạn đã nhập sẽ không được lưu.")
                .setNegativeButton("Ở lại", null)
                .setPositiveButton("Thoát", (d, w) -> finish())
                .show();
    }

    private void submit() {
        tilDescription.setError(null);
        tvTypeError.setVisibility(View.GONE);

        // Luồng 4.a: chưa chọn loại sự cố hoặc chưa nhập mô tả
        String loai = loaiDaChon();
        boolean ok = true;
        if (loai == null) { tvTypeError.setVisibility(View.VISIBLE); ok = false; }
        if (text(etDescription).isEmpty()) { tilDescription.setError("Vui lòng mô tả sự cố"); ok = false; }
        if (!ok) return;

        MultipartBody.Part anh;
        try {
            anh = FormParts.image(this, imageUri, "anh");
        } catch (Exception e) {
            toast(e.getMessage() == null ? "Không đọc được ảnh, vui lòng chọn ảnh khác" : e.getMessage());
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");
        ApiClient.getService(this).baoCaoSuCo(
                FormParts.text(idChuyen),
                FormParts.text(loai),
                FormParts.text(text(etDescription)),
                FormParts.text(mucDoDaChon()),
                FormParts.text(viDo),
                FormParts.text(kinhDo),
                anh).enqueue(new Callback<SuCo>() {
            @Override
            public void onResponse(Call<SuCo> call, Response<SuCo> response) {
                khoiPhucNut();
                if (response.isSuccessful() && response.body() != null) {
                    hienKetQua(response.body());
                } else {
                    toast(ApiError.message(response));
                }
            }

            @Override
            public void onFailure(Call<SuCo> call, Throwable t) {
                khoiPhucNut();
                toast("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    /** Bước 7: thông báo đã gửi báo cáo kèm trạng thái xử lý. */
    private void hienKetQua(SuCo s) {
        String msg = "Mã sự cố: #" + s.getIdSuCo()
                + (s.getIdChuyen() != null ? "\nChuyến: #" + s.getIdChuyen() : "")
                + "\nLoại: " + s.getLoaiSuCo()
                + "\nMức độ ưu tiên: " + s.getTenMucDoUuTien()
                + "\nTrạng thái xử lý: " + s.getTenTrangThai()
                + "\n\nNhân viên điều phối đã được thông báo.";
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đã gửi báo cáo sự cố")
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("Xem trạng thái xử lý", (d, w) -> {
                    startActivity(new android.content.Intent(this, MyIncidentsActivity.class));
                    finish();
                })
                .setPositiveButton("Xong", (d, w) -> finish())
                .show();
    }

    private void khoiPhucNut() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Gửi báo cáo");
    }

    private void toast(String msg) {
        Snackbar.make(btnSubmit, msg, Snackbar.LENGTH_LONG).show();
    }
}
