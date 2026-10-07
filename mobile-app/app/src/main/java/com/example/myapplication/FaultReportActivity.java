package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.ProfileResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaultReportActivity extends AppCompatActivity {
    private TextInputLayout tilPlate, tilPhone, tilLocation, tilDescription;
    private TextInputEditText etPlate, etPhone, etLocation, etDescription;
    private ImageView ivPreview;
    private MaterialButton btnSubmit, btnUpload;
    private Uri imageUri;

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
                if (granted) fillLocation();
                else toast("Cần cấp quyền vị trí để dùng GPS");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fault_report);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> attemptExit());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { attemptExit(); }
        });

        tilPlate = findViewById(R.id.tilLicensePlate);
        tilPhone = findViewById(R.id.tilPhone);
        tilLocation = findViewById(R.id.tilLocation);
        tilDescription = findViewById(R.id.tilDescription);
        etPlate = findViewById(R.id.etLicensePlate);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        ivPreview = findViewById(R.id.ivPreview);
        btnUpload = findViewById(R.id.btnUploadImage);
        btnSubmit = findViewById(R.id.btnSubmitFault);

        btnUpload.setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnGps).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) fillLocation();
            else locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> attemptExit());
        btnSubmit.setOnClickListener(v -> submit());

        prefillPhone();
    }

    private void prefillPhone() {
        ApiClient.getService(this).getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override public void onResponse(Call<ProfileResponse> c, Response<ProfileResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().getSdt() != null && text(etPhone).isEmpty())
                    etPhone.setText(r.body().getSdt());
            }
            @Override public void onFailure(Call<ProfileResponse> c, Throwable t) { }
        });
    }

    @SuppressLint("MissingPermission")
    private void fillLocation() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location best = null;
        for (String p : lm.getProviders(true)) {
            Location l = lm.getLastKnownLocation(p);
            if (l != null && (best == null || l.getAccuracy() < best.getAccuracy())) best = l;
        }
        if (best == null) {
            toast("Chưa xác định được vị trí. Hãy bật GPS và thử lại");
            return;
        }
        etLocation.setText(String.format(Locale.US, "%.6f, %.6f", best.getLatitude(), best.getLongitude()));
        tilLocation.setError(null);
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private boolean hasInput() {
        return !text(etPlate).isEmpty() || !text(etLocation).isEmpty()
                || !text(etDescription).isEmpty() || imageUri != null;
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
        tilPlate.setError(null); tilPhone.setError(null);
        tilLocation.setError(null); tilDescription.setError(null);
        boolean ok = true;
        if (text(etPlate).isEmpty()) { tilPlate.setError("Vui lòng nhập biển số xe"); ok = false; }
        if (text(etPhone).length() < 9) { tilPhone.setError("Số điện thoại không hợp lệ"); ok = false; }
        if (text(etLocation).isEmpty()) { tilLocation.setError("Vui lòng nhập vị trí hoặc dùng GPS"); ok = false; }
        if (text(etDescription).isEmpty()) { tilDescription.setError("Vui lòng mô tả sự cố"); ok = false; }
        if (!ok) return;

        // TODO: nối API gửi báo cáo khi backend sẵn sàng
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đã ghi nhận báo cáo")
                .setMessage("Quản lý sẽ liên hệ với bạn qua số " + text(etPhone) + " trong thời gian sớm nhất.")
                .setCancelable(false)
                .setPositiveButton("Về trang chủ", (d, w) -> finish())
                .show();
    }

    private void toast(String msg) {
        Snackbar.make(btnSubmit, msg, Snackbar.LENGTH_LONG).show();
    }
}
