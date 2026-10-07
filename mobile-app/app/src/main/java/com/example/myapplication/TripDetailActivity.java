package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.trip.ChuyenChiTiet;
import com.example.myapplication.utils.TrangThaiChuyenUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.1 - Usecase "Xem thông tin chi tiết chuyến", "Nhận chuyến",
 * "Bắt đầu chuyến", "Hoàn thành chuyến".
 */
public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID_CHUYEN = "extra_id_chuyen";

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvMaChuyen, tvTrangThai, tvTenCongTrinh, tvDiaChiCongTrinh;
    private MaterialButton btnAction, btnMap, btnCall;
    private View actionBar;
    private View rowMac, rowKhoiLuong, rowTram, rowBienSo, rowThoiGianGiao, rowGhiChu;

    private long idChuyen;
    private ChuyenChiTiet chuyen;
    private boolean dangXuLy = false;

    /** Mục 2.2.2, luồng phụ 4.a "Bắt đầu chuyến": từ chối quyền vị trí -> không cho bắt đầu chuyến. */
    private final ActivityResultLauncher<String> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) kiemTraGpsRoiXacNhanBatDau();
                else showError("Cần cấp quyền vị trí để bắt đầu chuyến và gửi GPS");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        idChuyen = getIntent().getLongExtra(EXTRA_ID_CHUYEN, -1);
        if (idChuyen <= 0) { finish(); return; }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvMaChuyen = findViewById(R.id.tvMaChuyen);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        tvTenCongTrinh = findViewById(R.id.tvTenCongTrinh);
        tvDiaChiCongTrinh = findViewById(R.id.tvDiaChiCongTrinh);
        btnAction = findViewById(R.id.btnAction);
        btnMap = findViewById(R.id.btnMap);
        btnCall = findViewById(R.id.btnCall);
        actionBar = findViewById(R.id.actionBar);
        rowMac = findViewById(R.id.rowMac);
        rowKhoiLuong = findViewById(R.id.rowKhoiLuong);
        rowTram = findViewById(R.id.rowTram);
        rowBienSo = findViewById(R.id.rowBienSo);
        rowThoiGianGiao = findViewById(R.id.rowThoiGianGiao);
        rowGhiChu = findViewById(R.id.rowGhiChu);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this::load);
        btnAction.setOnClickListener(v -> confirmAction());
        btnMap.setOnClickListener(v -> openMap());
        btnCall.setOnClickListener(v -> callCongTrinh());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getChiTietChuyen(idChuyen).enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                swipeRefresh.setRefreshing(false);
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Không tải được chuyến (mã lỗi " + response.code() + ")");
                    return;
                }
                chuyen = response.body();
                bind(chuyen);
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void bind(ChuyenChiTiet c) {
        tvMaChuyen.setText(String.format(Locale.getDefault(), "Chuyến #%d", c.getIdChuyen()));
        tvTrangThai.setText(c.getTenTrangThai());
        TrangThaiChuyenUtil.toMauTrangThai(tvTrangThai, c.getTrangThai());
        tvTenCongTrinh.setText(c.getTenCongTrinh() == null ? "Công trình" : c.getTenCongTrinh());
        tvDiaChiCongTrinh.setText(c.getDiaChiCongTrinh() == null ? "" : c.getDiaChiCongTrinh());

        setRow(rowMac, "Mác bê tông", c.getMacBeTong());
        setRow(rowKhoiLuong, "Khối lượng", c.getKhoiLuong() == null ? "—"
                : String.format(Locale.getDefault(), "%.1f m³", c.getKhoiLuong()));
        setRow(rowTram, "Trạm trộn xuất phát", c.getTenTram());
        setRow(rowBienSo, "Biển số xe", c.getBienSo());
        setRow(rowThoiGianGiao, "Thời gian xuất phát", TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianXuatPhat()));
        setRow(rowGhiChu, "Ghi chú điều phối", c.getGhiChu() == null || c.getGhiChu().isEmpty()
                ? "Không có" : c.getGhiChu());

        btnCall.setEnabled(c.getSdtCongTrinh() != null && !c.getSdtCongTrinh().isEmpty());
        btnMap.setEnabled(c.getViDoCongTrinh() != null && c.getKinhDoCongTrinh() != null);

        int t = c.getTrangThai();
        if (t == TrangThaiChuyenUtil.CHO_NHAN) {
            showAction("Nhận chuyến");
        } else if (t == TrangThaiChuyenUtil.DA_NHAN) {
            showAction("Bắt đầu chuyến");
        } else if (t >= TrangThaiChuyenUtil.DANG_GIAO && t < TrangThaiChuyenUtil.HOAN_THANH) {
            showAction("Hoàn thành chuyến");
        } else {
            actionBar.setVisibility(View.GONE);
        }
    }

    private void showAction(String text) {
        actionBar.setVisibility(View.VISIBLE);
        btnAction.setText(text);
    }

    private void setRow(View row, String label, String value) {
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value == null || value.isEmpty() ? "—" : value);
    }

    private void confirmAction() {
        if (chuyen == null || dangXuLy) return;
        String action = btnAction.getText().toString();

        // Mục 2.2.2 - "Bắt đầu chuyến" phải kiểm tra quyền vị trí + GPS trước khi xác nhận,
        // vì bước này kích hoạt luôn dịch vụ gửi tọa độ GPS theo thời gian thực.
        if (action.equals("Bắt đầu chuyến")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }
            kiemTraGpsRoiXacNhanBatDau();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(action)
                .setMessage("Bạn có chắc muốn " + action.toLowerCase(Locale.getDefault()) + " #" + idChuyen + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> performAction())
                .show();
    }

    private void kiemTraGpsRoiXacNhanBatDau() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsBat = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        if (!gpsBat) {
            showError("Vui lòng bật GPS của thiết bị trước khi bắt đầu chuyến");
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Bắt đầu chuyến")
                .setMessage("Bạn có chắc muốn bắt đầu chuyến #" + idChuyen
                        + "? Hệ thống sẽ bắt đầu gửi vị trí GPS theo thời gian thực.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> performAction())
                .show();
    }

    private void performAction() {
        int t = chuyen.getTrangThai();
        final int trangThaiTruoc = t;
        dangXuLy = true;
        btnAction.setEnabled(false);

        Call<ChuyenChiTiet> call;
        if (t == TrangThaiChuyenUtil.CHO_NHAN) {
            call = ApiClient.getService(this).nhanChuyen(idChuyen);
        } else if (t == TrangThaiChuyenUtil.DA_NHAN) {
            call = ApiClient.getService(this).batDauChuyen(idChuyen);
        } else {
            call = ApiClient.getService(this).hoanThanhChuyen(idChuyen);
        }

        call.enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                dangXuLy = false;
                btnAction.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    chuyen = response.body();
                    bind(chuyen);
                    showError("Cập nhật trạng thái thành công");

                    // Mục 2.2.2: Bắt đầu chuyến -> bật dịch vụ gửi GPS cho đúng chuyến này;
                    // Hoàn thành chuyến -> dừng dịch vụ (gửi bản ghi vị trí cuối cùng rồi tắt).
                    if (trangThaiTruoc == TrangThaiChuyenUtil.DA_NHAN) {
                        LocationService.start(TripDetailActivity.this, idChuyen);
                    } else if (trangThaiTruoc >= TrangThaiChuyenUtil.DANG_GIAO
                            && trangThaiTruoc < TrangThaiChuyenUtil.HOAN_THANH) {
                        LocationService.stop(TripDetailActivity.this);
                    }
                } else {
                    showError("Không thực hiện được thao tác (mã lỗi " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                dangXuLy = false;
                btnAction.setEnabled(true);
                showError("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void openMap() {
        if (chuyen == null || chuyen.getViDoCongTrinh() == null || chuyen.getKinhDoCongTrinh() == null) return;
        Uri uri = Uri.parse(String.format(Locale.US, "geo:0,0?q=%f,%f(%s)",
                chuyen.getViDoCongTrinh(), chuyen.getKinhDoCongTrinh(),
                Uri.encode(chuyen.getTenCongTrinh() == null ? "Công trình" : chuyen.getTenCongTrinh())));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showError("Không tìm thấy ứng dụng bản đồ trên thiết bị");
        }
    }

    private void callCongTrinh() {
        if (chuyen == null || chuyen.getSdtCongTrinh() == null) return;
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + chuyen.getSdtCongTrinh())));
    }

    private void showError(String msg) {
        Snackbar.make(btnAction, msg, Snackbar.LENGTH_LONG).show();
    }
}
