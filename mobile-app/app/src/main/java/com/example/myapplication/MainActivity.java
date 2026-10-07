package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.ProfileResponse;
import com.example.myapplication.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextView tvDriverName, tvAvatar, tvGreeting;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);

        tvDriverName = findViewById(R.id.tvDriverName);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvGreeting = findViewById(R.id.tvGreeting);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this::loadProfile);

        // Báo cáo sự cố: cả thẻ và nút đều mở được
        View.OnClickListener openReport = v -> open(FaultReportActivity.class);
        findViewById(R.id.btnReportFault).setOnClickListener(openReport);
        findViewById(R.id.btnReportFaultAction).setOnClickListener(openReport);

        setupRow(R.id.rowVehicle, R.drawable.ic_truck, R.drawable.bg_icon_amber, R.color.colorWarning,
                "Trạng thái xe", "Cập nhật tình trạng xe đang chạy", VehicleStatusActivity.class);
        setupRow(R.id.rowTrips, R.drawable.ic_route, R.drawable.bg_icon_green, R.color.colorSuccess,
                "Lịch trình", "Chuyến đi được giao hôm nay", TripsActivity.class);
        setupRow(R.id.rowGps, R.drawable.ic_location, R.drawable.bg_icon_blue, R.color.colorSecondary,
                "Gửi vị trí GPS", "Bật/tắt định vị, theo dõi hành trình", GpsLocationActivity.class);
        setupRow(R.id.rowProfile, R.drawable.ic_person, R.drawable.bg_icon_indigo, R.color.colorPrimary,
                "Hồ sơ & mật khẩu", "Thông tin cá nhân, đổi mật khẩu", ProfileActivity.class);

        findViewById(R.id.btnAvatar).setOnClickListener(v -> open(ProfileActivity.class));
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        loadProfile();
    }

    private void setupRow(int rowId, int icon, int iconBg, int tint, String title, String subtitle, Class<?> target) {
        View row = findViewById(rowId);
        ImageView iv = row.findViewById(R.id.ivIcon);
        iv.setImageResource(icon);
        iv.setColorFilter(ContextCompat.getColor(this, tint));
        row.findViewById(R.id.iconBg).setBackgroundResource(iconBg);
        ((TextView) row.findViewById(R.id.tvTitle)).setText(title);
        ((TextView) row.findViewById(R.id.tvSubtitle)).setText(subtitle);
        View.OnClickListener l = v -> open(target);
        row.setOnClickListener(l);
        row.findViewById(R.id.btnOpen).setOnClickListener(l);
    }

    private void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    private void loadProfile() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int h = c.get(java.util.Calendar.HOUR_OF_DAY);
        tvGreeting.setText(h < 11 ? "Chào buổi sáng," : h < 14 ? "Chào buổi trưa," : h < 18 ? "Chào buổi chiều," : "Chào buổi tối,");

        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.code() == 401) { logout(); return; }
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getHoTen();
                    if (name == null || name.trim().isEmpty()) name = response.body().getTenDangNhap();
                    if (name != null) {
                        tvDriverName.setText(name);
                        tvAvatar.setText(initials(name));
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "TX";
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi ứng dụng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> logout())
                .show();
    }

    private void logout() {
        sessionManager.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
