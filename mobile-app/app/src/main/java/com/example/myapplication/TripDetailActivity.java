package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.trip.ChuyenChiTiet;
import com.example.myapplication.models.trip.DaDenRequest;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.LocationUtil;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.utils.TrangThaiChuyenUtil;
import com.example.myapplication.utils.TripMapController;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.1 - "Xem thông tin chi tiết chuyến", "Nhận chuyến", "Bắt đầu chuyến",
 * "Hoàn thành chuyến"; mục 2.2.3 - "Xác nhận đã đến công trình",
 * "Xác nhận giao hàng thành công" và lối vào "Báo cáo sự cố".
 *
 * Nút hành động chính đổi theo trạng thái chuyến:
 * Chờ nhận -> Nhận chuyến -> Bắt đầu chuyến -> Đã đến công trình
 * -> Xác nhận giao hàng -> Hoàn thành chuyến.
 */
public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID_CHUYEN = "extra_id_chuyen";

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvMaChuyen, tvTrangThai, tvTenCongTrinh, tvDiaChiCongTrinh;
    private MaterialButton btnAction, btnMap, btnCall, btnReport;
    private View actionBar, cardTienDo;
    private View rowMac, rowKhoiLuong, rowTram, rowBienSo, rowThoiGianDuKien, rowThoiGianGiao, rowGhiChu;
    private View rowDaDen, rowGhiChuDen, rowKhoiLuongThucGiao, rowGhiChuGiaoHang, rowDonHangDaGiao, rowHoanThanh;

    private TripMapController mapController;

    private long idChuyen;
    private ChuyenChiTiet chuyen;
    private boolean dangXuLy = false;

    /** Hành động đang chờ quyền vị trí: bắt đầu chuyến hoặc xác nhận đã đến. */
    private Runnable sauKhiCoQuyen;

    /** Luồng phụ 4.a "Bắt đầu chuyến": từ chối quyền vị trí -> không cho bắt đầu chuyến. */
    private final ActivityResultLauncher<String> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                Runnable r = sauKhiCoQuyen;
                sauKhiCoQuyen = null;
                if (granted) {
                    if (r != null) r.run();
                } else if (chuyen != null && chuyen.getTrangThai() == TrangThaiChuyenUtil.DANG_GIAO) {
                    // Luồng 2.b "Đã đến công trình": không có vị trí -> cho xác nhận thủ công
                    hoiGhiChuRoiGuiDaDen(null, "Không lấy được vị trí hiện tại. Nhập ghi chú để xác nhận thủ công.");
                } else {
                    showError("Cần cấp quyền vị trí để bắt đầu chuyến và gửi GPS");
                }
            });

    private final ActivityResultLauncher<Intent> deliveryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) showError("Xác nhận giao hàng thành công");
                // onResume() sẽ tải lại chuyến
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TripMapController.init(this);
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
        btnReport = findViewById(R.id.btnReport);
        actionBar = findViewById(R.id.actionBar);
        cardTienDo = findViewById(R.id.cardTienDo);
        rowMac = findViewById(R.id.rowMac);
        rowKhoiLuong = findViewById(R.id.rowKhoiLuong);
        rowTram = findViewById(R.id.rowTram);
        rowBienSo = findViewById(R.id.rowBienSo);
        rowThoiGianDuKien = findViewById(R.id.rowThoiGianDuKien);
        rowThoiGianGiao = findViewById(R.id.rowThoiGianGiao);
        rowGhiChu = findViewById(R.id.rowGhiChu);
        rowDaDen = findViewById(R.id.rowDaDen);
        rowGhiChuDen = findViewById(R.id.rowGhiChuDen);
        rowKhoiLuongThucGiao = findViewById(R.id.rowKhoiLuongThucGiao);
        rowGhiChuGiaoHang = findViewById(R.id.rowGhiChuGiaoHang);
        rowDonHangDaGiao = findViewById(R.id.rowDonHangDaGiao);
        rowHoanThanh = findViewById(R.id.rowHoanThanh);

        mapController = new TripMapController(findViewById(R.id.mapView), findViewById(R.id.tvMapInfo));

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this::load);
        btnAction.setOnClickListener(v -> confirmAction());
        btnMap.setOnClickListener(v -> openMap());
        btnCall.setOnClickListener(v -> callCongTrinh());
        btnReport.setOnClickListener(v -> {
            Intent i = new Intent(this, FaultReportActivity.class);
            i.putExtra(FaultReportActivity.EXTRA_ID_CHUYEN, idChuyen);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapController != null) mapController.onResume();
        load();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapController != null) mapController.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mapController != null) mapController.onDestroy();
        super.onDestroy();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getChiTietChuyen(idChuyen).enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                swipeRefresh.setRefreshing(false);
                if (!response.isSuccessful() || response.body() == null) {
                    xuLyLoi(response);
                    return;
                }
                chuyen = response.body();
                bind(chuyen);
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Không kết nối được máy chủ. Vuốt xuống để thử lại");
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
        setRow(rowKhoiLuong, "Khối lượng chuyến", m3(c.getKhoiLuong()));
        setRow(rowTram, "Trạm trộn xuất phát", c.getTenTram() == null ? null
                : c.getDiaChiTram() == null ? c.getTenTram() : c.getTenTram() + "\n" + c.getDiaChiTram());
        setRow(rowBienSo, "Biển số xe", c.getBienSo());
        setRow(rowThoiGianDuKien, "Giao dự kiến", TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianGiao()));
        setRow(rowThoiGianGiao, "Thời gian xuất phát", TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianXuatPhat()));
        setRow(rowGhiChu, "Ghi chú điều phối", c.getGhiChu() == null || c.getGhiChu().isEmpty()
                ? "Không có" : c.getGhiChu());

        bindTienDo(c);
        mapController.show(c, LocationUtil.viTriGanNhat(this));

        btnCall.setEnabled(c.getSdtCongTrinh() != null && !c.getSdtCongTrinh().isEmpty());
        btnMap.setEnabled(c.getViDoCongTrinh() != null && c.getKinhDoCongTrinh() != null);

        int t = c.getTrangThai();
        if (t == TrangThaiChuyenUtil.CHO_NHAN) {
            showAction("Nhận chuyến");
        } else if (t == TrangThaiChuyenUtil.DA_NHAN) {
            showAction("Bắt đầu chuyến");
        } else if (t == TrangThaiChuyenUtil.DANG_GIAO) {
            showAction("Đã đến công trình");
        } else if (t == TrangThaiChuyenUtil.DA_DEN) {
            showAction("Xác nhận giao hàng");
        } else if (t == TrangThaiChuyenUtil.DA_GIAO_HANG) {
            showAction("Hoàn thành chuyến");
        } else {
            actionBar.setVisibility(View.GONE);
        }
        // "Báo cáo sự cố" chỉ khi đang thực hiện chuyến (Đã nhận -> Đã giao hàng)
        btnReport.setVisibility(t >= TrangThaiChuyenUtil.DA_NHAN && t <= TrangThaiChuyenUtil.DA_GIAO_HANG
                ? View.VISIBLE : View.GONE);
    }

    private void bindTienDo(ChuyenChiTiet c) {
        boolean daDen = c.getThoiGianDen() != null && c.getTrangThai() >= TrangThaiChuyenUtil.DA_DEN;
        boolean daGiao = c.getKhoiLuongThucGiao() != null;
        cardTienDo.setVisibility(daDen || daGiao ? View.VISIBLE : View.GONE);

        String daDenText = TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianDen());
        if (c.getKhoangCachDen() != null) {
            daDenText += String.format(Locale.getDefault(), " · cách %.0f m", c.getKhoangCachDen());
        }
        if (c.isCanKiemTraDen()) daDenText += " · cần kiểm tra";
        showRow(rowDaDen, daDen, "Đã đến công trình", daDenText);
        showRow(rowGhiChuDen, daDen && c.getGhiChuDen() != null, "Ghi chú khi đến", c.getGhiChuDen());

        showRow(rowKhoiLuongThucGiao, daGiao, "Khối lượng thực giao", m3(c.getKhoiLuongThucGiao())
                + " lúc " + TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianGiaoXong()));
        showRow(rowGhiChuGiaoHang, daGiao && c.getGhiChuGiaoHang() != null, "Ghi chú giao hàng", c.getGhiChuGiaoHang());
        showRow(rowDonHangDaGiao, daGiao, "Đơn hàng đã giao", m3(c.getTongKhoiLuongDaGiao())
                + " / " + m3(c.getTongKhoiLuongDonHang()));
        showRow(rowHoanThanh, c.getThoiGianHoanThanh() != null, "Hoàn thành lúc",
                TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianHoanThanh()));
    }

    private static String m3(Double v) {
        return v == null ? "—" : String.format(Locale.getDefault(), "%.1f m³", v);
    }

    private void showRow(View row, boolean visible, String label, String value) {
        row.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) setRow(row, label, value);
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
        int t = chuyen.getTrangThai();

        if (t == TrangThaiChuyenUtil.DA_NHAN) {
            // Mục 2.2.2 - "Bắt đầu chuyến" phải kiểm tra quyền vị trí + GPS trước khi xác nhận,
            // vì bước này kích hoạt luôn dịch vụ gửi tọa độ GPS theo thời gian thực.
            canQuyenViTriRoi(this::kiemTraGpsRoiXacNhanBatDau);
            return;
        }
        if (t == TrangThaiChuyenUtil.DANG_GIAO) {
            canQuyenViTriRoi(this::xacNhanDaDen);
            return;
        }
        if (t == TrangThaiChuyenUtil.DA_DEN) {
            Intent i = new Intent(this, DeliveryConfirmActivity.class);
            i.putExtra(DeliveryConfirmActivity.EXTRA_ID_CHUYEN, idChuyen);
            if (chuyen.getKhoiLuong() != null) {
                i.putExtra(DeliveryConfirmActivity.EXTRA_KHOI_LUONG, chuyen.getKhoiLuong().doubleValue());
            }
            deliveryLauncher.launch(i);
            return;
        }
        if (t == TrangThaiChuyenUtil.DA_GIAO_HANG) {
            xacNhanHoanThanh();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nhận chuyến")
                .setMessage("Bạn có chắc muốn nhận chuyến #" + idChuyen + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> goiApi(ApiClient.getService(this).nhanChuyen(idChuyen), t))
                .show();
    }

    private void canQuyenViTriRoi(Runnable next) {
        if (LocationUtil.coQuyen(this)) {
            next.run();
        } else {
            sauKhiCoQuyen = next;
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
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
                .setPositiveButton("Xác nhận", (d, w) ->
                        goiApi(ApiClient.getService(this).batDauChuyen(idChuyen), TrangThaiChuyenUtil.DA_NHAN))
                .show();
    }

    // ===================== Mục 2.2.3 - Xác nhận đã đến công trình =====================

    private void xacNhanDaDen() {
        Location viTri = LocationUtil.viTriGanNhat(this);
        if (viTri == null) {
            // Luồng 2.b: không lấy được vị trí -> xác nhận thủ công kèm ghi chú
            hoiGhiChuRoiGuiDaDen(null, "Không lấy được vị trí hiện tại. Nhập ghi chú để xác nhận thủ công.");
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đã đến công trình")
                .setMessage("Xác nhận xe đã đến công trình của chuyến #" + idChuyen + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (d, w) -> guiDaDen(viTri, null))
                .show();
    }

    private void guiDaDen(Location viTri, String ghiChu) {
        DaDenRequest body = new DaDenRequest(
                viTri == null ? null : viTri.getLatitude(),
                viTri == null ? null : viTri.getLongitude(),
                ghiChu);
        batDauXuLy();
        ApiClient.getService(this).xacNhanDaDen(idChuyen, body).enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                ketThucXuLy();
                if (response.isSuccessful() && response.body() != null) {
                    chuyen = response.body();
                    bind(chuyen);
                    showError(chuyen.isCanKiemTraDen()
                            ? "Đã xác nhận đến công trình (điều phối sẽ kiểm tra lại vị trí)"
                            : "Đã xác nhận đến công trình");
                } else if (response.code() == 422) {
                    // Luồng 2.a / 2.b: ngoài bán kính hoặc thiếu vị trí -> bắt buộc ghi chú
                    hoiGhiChuRoiGuiDaDen(viTri, ApiError.message(response));
                } else {
                    xuLyLoi(response);
                }
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                ketThucXuLy();
                showError("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void hoiGhiChuRoiGuiDaDen(Location viTri, String thongBao) {
        TextInputLayout til = new TextInputLayout(this);
        til.setHint("Ghi chú lý do");
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        til.addView(et);
        FrameLayout box = new FrameLayout(this);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(til);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Cần ghi chú để xác nhận")
                .setMessage(thongBao)
                .setView(box)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi xác nhận", null)
                .show();
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ghiChu = et.getText() == null ? "" : et.getText().toString().trim();
            if (ghiChu.isEmpty()) {
                til.setError("Vui lòng nhập ghi chú");
                return;
            }
            dialog.dismiss();
            guiDaDen(viTri, ghiChu);
        });
    }

    // ===================== Hoàn thành chuyến =====================

    private void xacNhanHoanThanh() {
        // Bước 3: hộp thoại xác nhận kèm tóm tắt khối lượng đã giao và thời gian thực hiện
        String tomTat = "Khối lượng đã giao: " + m3(chuyen.getKhoiLuongThucGiao())
                + "\nXuất phát: " + TrangThaiChuyenUtil.dinhDangNgayGio(chuyen.getThoiGianXuatPhat())
                + "\nGiao xong: " + TrangThaiChuyenUtil.dinhDangNgayGio(chuyen.getThoiGianGiaoXong())
                + TrangThaiChuyenUtil.thoiLuong(chuyen.getThoiGianXuatPhat(), chuyen.getThoiGianGiaoXong())
                + "\n\nHoàn thành chuyến #" + idChuyen + " và dừng gửi vị trí GPS?";
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hoàn thành chuyến")
                .setMessage(tomTat)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Hoàn thành", (d, w) ->
                        goiApi(ApiClient.getService(this).hoanThanhChuyen(idChuyen), TrangThaiChuyenUtil.DA_GIAO_HANG))
                .show();
    }

    // ===================== Gọi API chung =====================

    private void goiApi(Call<ChuyenChiTiet> call, int trangThaiTruoc) {
        batDauXuLy();
        call.enqueue(new Callback<ChuyenChiTiet>() {
            @Override
            public void onResponse(Call<ChuyenChiTiet> call, Response<ChuyenChiTiet> response) {
                ketThucXuLy();
                if (response.isSuccessful() && response.body() != null) {
                    chuyen = response.body();
                    bind(chuyen);
                    showError("Cập nhật trạng thái thành công");

                    // Mục 2.2.2: Bắt đầu chuyến -> bật dịch vụ gửi GPS cho đúng chuyến này;
                    // Hoàn thành chuyến -> dừng dịch vụ (gửi bản ghi vị trí cuối cùng rồi tắt).
                    if (trangThaiTruoc == TrangThaiChuyenUtil.DA_NHAN) {
                        LocationService.start(TripDetailActivity.this, idChuyen);
                    } else if (trangThaiTruoc == TrangThaiChuyenUtil.DA_GIAO_HANG) {
                        LocationService.stop(TripDetailActivity.this);
                    }
                } else {
                    xuLyLoi(response);
                }
            }

            @Override
            public void onFailure(Call<ChuyenChiTiet> call, Throwable t) {
                ketThucXuLy();
                showError("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
            }
        });
    }

    private void batDauXuLy() {
        dangXuLy = true;
        btnAction.setEnabled(false);
    }

    private void ketThucXuLy() {
        dangXuLy = false;
        btnAction.setEnabled(true);
    }

    /** 401 -> đăng nhập lại; 404 -> chuyến không còn hiệu lực; 409 -> trạng thái đổi, tải lại. */
    private void xuLyLoi(Response<?> response) {
        String msg = ApiError.message(response);
        if (response.code() == 401) {
            new SessionManager(this).clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else if (response.code() == 404) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Chuyến không còn hiệu lực")
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("Về danh sách", (d, w) -> finish())
                    .show();
        } else {
            showError(msg);
            if (response.code() == 409) load();
        }
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
