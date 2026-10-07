package com.example.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.common.PageResponse;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.example.myapplication.utils.TrangThaiChuyenUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.2 - Màn hình "Gửi vị trí GPS": usecase Bật định vị / Tắt định vị,
 * hiển thị tọa độ hiện tại và trạng thái gửi lên máy chủ theo thời gian thực.
 */
public class GpsLocationActivity extends AppCompatActivity {

    private SwitchMaterial switchDinhVi;
    private TextView tvTrangThai, tvToaDo, tvTocDo, tvChuyen;
    private boolean dangTuCapNhatSwitch = false;

    private final ActivityResultLauncher<String> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) kiemTraGpsRoiBat();
                else {
                    toast("Cần cấp quyền vị trí để bật định vị");
                    datSwitch(false);
                }
            });

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            if (intent.getAction().equals(LocationService.ACTION_VI_TRI_CAP_NHAT)) {
                double viDo = intent.getDoubleExtra(LocationService.EXTRA_VI_DO, 0);
                double kinhDo = intent.getDoubleExtra(LocationService.EXTRA_KINH_DO, 0);
                double tocDo = intent.getDoubleExtra(LocationService.EXTRA_TOC_DO, 0);
                boolean dangGui = intent.getBooleanExtra(LocationService.EXTRA_DANG_GUI, false);
                tvToaDo.setText(String.format(Locale.US, "%.5f, %.5f", viDo, kinhDo));
                tvTocDo.setText(String.format(Locale.US, "%.1f m/s", tocDo));
                if (LocationService.chuyenDangStream() != null) {
                    tvTrangThai.setText(dangGui ? "Đang gửi vị trí lên máy chủ" : "Đã lấy được tọa độ, đang chờ gửi");
                }
            } else if (intent.getAction().equals(LocationService.ACTION_TRANG_THAI)) {
                capNhatTrangThaiHienThi();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_location);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchDinhVi = findViewById(R.id.switchDinhVi);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        tvToaDo = findViewById(R.id.tvToaDo);
        tvTocDo = findViewById(R.id.tvTocDo);
        tvChuyen = findViewById(R.id.tvChuyen);

        switchDinhVi.setOnCheckedChangeListener((btn, checked) -> {
            if (dangTuCapNhatSwitch) return;
            if (checked) batDinhVi();
            else tatDinhVi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.ACTION_VI_TRI_CAP_NHAT);
        filter.addAction(LocationService.ACTION_TRANG_THAI);
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        capNhatTrangThaiHienThi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void batDinhVi() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        kiemTraGpsRoiBat();
    }

    private void kiemTraGpsRoiBat() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsBat = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        if (!gpsBat) {
            datSwitch(false);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("GPS đang tắt")
                    .setMessage("Vui lòng bật định vị (GPS) của thiết bị để tiếp tục")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Mở cài đặt", (d, w) ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .show();
            return;
        }
        timChuyenDangGiaoRoiBat();
    }

    /**
     * Tìm chuyến đang thực hiện (Đang giao / Đã đến / Đã giao hàng) của tài xế
     * (nếu có) để gắn vào dịch vụ GPS.
     */
    private void timChuyenDangGiaoRoiBat() {
        ApiClient.getService(this).getDanhSachChuyen(null, 1, 20)
                .enqueue(new Callback<PageResponse<ChuyenDanhSach>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ChuyenDanhSach>> call,
                                            Response<PageResponse<ChuyenDanhSach>> response) {
                        Long idChuyen = null;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getDanhSach() != null) {
                            for (ChuyenDanhSach c : response.body().getDanhSach()) {
                                if (c.getTrangThai() >= TrangThaiChuyenUtil.DANG_GIAO
                                        && c.getTrangThai() <= TrangThaiChuyenUtil.DA_GIAO_HANG) {
                                    idChuyen = c.getIdChuyen();
                                    break;
                                }
                            }
                        }
                        hoanTatBat(idChuyen);
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ChuyenDanhSach>> call, Throwable t) {
                        // Không lấy được danh sách chuyến: vẫn bật định vị, chỉ hiển thị tọa độ,
                        // chưa gửi lên server (không có chuyến để gắn).
                        hoanTatBat(null);
                    }
                });
    }

    private void hoanTatBat(Long idChuyen) {
        LocationService.start(this, idChuyen);
        datSwitch(true);
        capNhatTrangThaiHienThi();
    }

    private void tatDinhVi() {
        Long idChuyen = LocationService.chuyenDangStream();
        if (idChuyen != null) {
            // Luồng phụ 3.a usecase "Tắt định vị": đang có chuyến Đang giao -> không cho tắt.
            datSwitch(true);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Không thể tắt định vị")
                    .setMessage("Chuyến #" + idChuyen + " đang thực hiện, không thể tắt định vị cho tới khi hoàn thành chuyến.")
                    .setPositiveButton("Đã hiểu", null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tắt định vị")
                .setMessage("Bạn có chắc muốn tắt định vị?")
                .setNegativeButton("Hủy", (d, w) -> datSwitch(true))
                .setPositiveButton("Tắt định vị", (d, w) -> {
                    LocationService.stop(this);
                    datSwitch(false);
                    capNhatTrangThaiHienThi();
                })
                .show();
    }

    private void capNhatTrangThaiHienThi() {
        boolean dangChay = LocationService.isRunning();
        Long idChuyen = LocationService.chuyenDangStream();
        datSwitch(dangChay);
        tvTrangThai.setText(!dangChay ? "Đã tắt định vị"
                : idChuyen != null ? "Đang gửi vị trí cho chuyến #" + idChuyen
                : "Đã bật định vị, chưa có chuyến đang giao");
        tvChuyen.setText(idChuyen != null ? "Chuyến #" + idChuyen : "Không có chuyến đang giao");
    }

    private void datSwitch(boolean on) {
        dangTuCapNhatSwitch = true;
        switchDinhVi.setChecked(on);
        dangTuCapNhatSwitch = false;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
