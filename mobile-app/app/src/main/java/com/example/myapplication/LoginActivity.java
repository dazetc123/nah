package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.models.AuthResponse;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private CircularProgressIndicator progress;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.getToken() != null) {
            goToMain();
            return;
        }
        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progressLogin);

        btnLogin.setOnClickListener(v -> login());
        etPassword.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { login(); return true; }
            return false;
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "" : "Đăng nhập");
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String msg) {
        Snackbar.make(btnLogin, msg, Snackbar.LENGTH_LONG).show();
    }

    private void login() {
        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        tilUsername.setError(null);
        tilPassword.setError(null);
        boolean ok = true;
        if (username.isEmpty()) { tilUsername.setError("Vui lòng nhập tên đăng nhập"); ok = false; }
        if (password.isEmpty()) { tilPassword.setError("Vui lòng nhập mật khẩu"); ok = false; }
        else if (password.length() < 8) { tilPassword.setError("Mật khẩu phải từ 8 ký tự trở lên"); ok = false; }
        if (!ok) return;

        setLoading(true);
        ApiService apiService = ApiClient.getService(this);
        apiService.login(new LoginRequest(username, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (!"Tài xế".equalsIgnoreCase(response.body().getTenVaiTro())) {
                        showMessage("Ứng dụng này chỉ dành cho tài khoản Tài xế");
                        return;
                    }
                    sessionManager.saveToken(response.body().getAccessToken());
                    goToMain();
                    return;
                }
                String err = "";
                try { if (response.errorBody() != null) err = response.errorBody().string(); } catch (Exception ignored) {}
                Log.e("LOGIN_ERR", "Code: " + response.code() + ", Body: " + err);
                if (response.code() == 400 || response.code() == 401 || response.code() == 403) {
                    tilPassword.setError("Sai tài khoản hoặc mật khẩu");
                } else {
                    showMessage("Lỗi máy chủ (" + response.code() + "). Vui lòng thử lại sau");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                showMessage("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
