package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.models.ChangePasswordRequest;
import com.example.myapplication.models.ProfileResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvUsername, tvPhone, tvEmail, tvLicense, tvAvatar;
    private TextInputLayout tilOld, tilNew, tilConfirm;
    private TextInputEditText etOld, etNew, etConfirm;
    private MaterialButton btnChangePassword;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvLicense = findViewById(R.id.tvLicense);
        tvAvatar = findViewById(R.id.tvAvatar);
        tilOld = findViewById(R.id.tilOldPassword);
        tilNew = findViewById(R.id.tilNewPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);
        etOld = findViewById(R.id.etOldPassword);
        etNew = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        apiService = ApiClient.getService(this);
        loadProfile();
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private static String orDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "Chưa cập nhật" : s;
    }

    private void loadProfile() {
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse p = response.body();
                    String name = p.getHoTen() != null ? p.getHoTen() : p.getTenDangNhap();
                    tvName.setText(orDash(name));
                    tvUsername.setText("@" + (p.getTenDangNhap() == null ? "" : p.getTenDangNhap()));
                    tvPhone.setText(orDash(p.getSdt()));
                    tvEmail.setText(orDash(p.getEmail()));
                    tvLicense.setText(orDash(p.getSoGPLX()));
                    if (name != null) tvAvatar.setText(MainActivity.initials(name));
                } else {
                    tvName.setText("Không tải được hồ sơ");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                tvName.setText("Không tải được hồ sơ");
                Snackbar.make(tvName, "Lỗi kết nối máy chủ", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }

    private void changePassword() {
        String oldPw = text(etOld), newPw = text(etNew), confirm = text(etConfirm);
        tilOld.setError(null); tilNew.setError(null); tilConfirm.setError(null);
        boolean ok = true;
        if (oldPw.isEmpty()) { tilOld.setError("Nhập mật khẩu hiện tại"); ok = false; }
        if (newPw.length() < 8) { tilNew.setError("Mật khẩu mới phải từ 8 ký tự"); ok = false; }
        else if (newPw.equals(oldPw)) { tilNew.setError("Mật khẩu mới phải khác mật khẩu cũ"); ok = false; }
        if (!confirm.equals(newPw)) { tilConfirm.setError("Mật khẩu nhập lại không khớp"); ok = false; }
        if (!ok) return;

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Đang cập nhật...");
        apiService.changePassword(new ChangePasswordRequest(oldPw, newPw)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resetButton();
                if (response.isSuccessful()) {
                    etOld.setText(""); etNew.setText(""); etConfirm.setText("");
                    Snackbar.make(btnChangePassword, "Đổi mật khẩu thành công", Snackbar.LENGTH_LONG).show();
                } else if (response.code() == 400 || response.code() == 401) {
                    tilOld.setError("Mật khẩu hiện tại không đúng");
                } else {
                    Snackbar.make(btnChangePassword, "Lỗi máy chủ (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resetButton();
                Snackbar.make(btnChangePassword, "Lỗi kết nối máy chủ", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void resetButton() {
        btnChangePassword.setEnabled(true);
        btnChangePassword.setText("Cập nhật mật khẩu");
    }
}
