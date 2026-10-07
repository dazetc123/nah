package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.adapters.TripAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.common.PageResponse;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.SessionManager;
import com.example.myapplication.utils.TrangThaiChuyenUtil;
import com.example.myapplication.utils.TripCache;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.1 - Usecase "Xem danh sách chuyến được phân công":
 * bước 5 lọc theo trạng thái / ngày giao; luồng 3.a mất mạng -> hiển thị dữ
 * liệu đã lưu ở lần đồng bộ gần nhất; 4.a không có chuyến; 4.b phiên hết hạn.
 */
public class TripsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTrips;
    private View emptyState, errorState;
    private TextView tvOfflineBanner, tvEmptyTitle, tvEmptySubtitle, tvErrorDetail;
    private ChipGroup chipGroupFilter;
    private Chip chipNgay;
    private TripAdapter adapter;

    /** null = tất cả trạng thái. */
    private Integer trangThaiLoc = null;
    /** "yyyy-MM-dd" hoặc null = mọi ngày. */
    private String ngayLoc = null;
    /** Tránh hiển thị kết quả cũ khi người dùng đổi bộ lọc liên tục. */
    private int lanTai = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTrips = findViewById(R.id.rvTrips);
        emptyState = findViewById(R.id.emptyState);
        errorState = findViewById(R.id.errorState);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        tvErrorDetail = findViewById(R.id.tvErrorDetail);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipNgay = findViewById(R.id.chipNgay);

        adapter = new TripAdapter(chuyen -> {
            Intent i = new Intent(this, TripDetailActivity.class);
            i.putExtra(TripDetailActivity.EXTRA_ID_CHUYEN, chuyen.getIdChuyen());
            startActivity(i);
        });
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this::load);
        findViewById(R.id.btnRetry).setOnClickListener(v -> load());

        chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            trangThaiLoc = trangThaiTuChip(group.getCheckedChipId());
            load();
        });
        chipNgay.setOnClickListener(v -> chonNgay());
        chipNgay.setOnCloseIconClickListener(v -> {
            ngayLoc = null;
            capNhatChipNgay();
            load();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private Integer trangThaiTuChip(int id) {
        if (id == R.id.chipChoNhan) return TrangThaiChuyenUtil.CHO_NHAN;
        if (id == R.id.chipDaNhan) return TrangThaiChuyenUtil.DA_NHAN;
        if (id == R.id.chipDangGiao) return TrangThaiChuyenUtil.DANG_GIAO;
        if (id == R.id.chipDaDen) return TrangThaiChuyenUtil.DA_DEN;
        if (id == R.id.chipDaGiaoHang) return TrangThaiChuyenUtil.DA_GIAO_HANG;
        if (id == R.id.chipHoanThanh) return TrangThaiChuyenUtil.HOAN_THANH;
        return null;
    }

    private void chonNgay() {
        Calendar c = Calendar.getInstance();
        if (ngayLoc != null) {
            try {
                c.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(ngayLoc));
            } catch (Exception ignored) {
                // giữ ngày hôm nay
            }
        }
        new DatePickerDialog(this, (picker, y, m, d) -> {
            ngayLoc = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            capNhatChipNgay();
            load();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void capNhatChipNgay() {
        if (ngayLoc == null) {
            chipNgay.setText("Ngày giao: tất cả");
            chipNgay.setCloseIconVisible(false);
        } else {
            chipNgay.setText("Ngày giao: " + ngayLoc.substring(8, 10) + "/" + ngayLoc.substring(5, 7)
                    + "/" + ngayLoc.substring(0, 4));
            chipNgay.setCloseIconVisible(true);
        }
    }

    private boolean dangLoc() {
        return trangThaiLoc != null || ngayLoc != null;
    }

    private void load() {
        final int lan = ++lanTai;
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getDanhSachChuyen(trangThaiLoc, ngayLoc, 1, 50)
                .enqueue(new Callback<PageResponse<ChuyenDanhSach>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ChuyenDanhSach>> call,
                                           Response<PageResponse<ChuyenDanhSach>> response) {
                        if (lan != lanTai) return;
                        swipeRefresh.setRefreshing(false);
                        if (response.code() == 401) {
                            // Luồng 4.b: phiên đăng nhập hết hạn
                            dangXuat();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            hienLoi(ApiError.message(response));
                            return;
                        }
                        List<ChuyenDanhSach> ds = response.body().getDanhSach();
                        if (!dangLoc()) TripCache.save(TripsActivity.this, ds);
                        tvOfflineBanner.setVisibility(View.GONE);
                        hienDanhSach(ds);
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ChuyenDanhSach>> call, Throwable t) {
                        if (lan != lanTai) return;
                        swipeRefresh.setRefreshing(false);
                        hienDuLieuDaLuuHoacLoi();
                    }
                });
    }

    /** Luồng 3.a: mất kết nối -> dùng dữ liệu lần đồng bộ gần nhất (lọc tại máy). */
    private void hienDuLieuDaLuuHoacLoi() {
        List<ChuyenDanhSach> cache = TripCache.load(this);
        if (cache == null) {
            hienLoi("Kiểm tra kết nối mạng rồi thử lại.");
            return;
        }
        List<ChuyenDanhSach> ketQua = new ArrayList<>();
        for (ChuyenDanhSach c : cache) {
            if (trangThaiLoc != null && c.getTrangThai() != trangThaiLoc) continue;
            if (ngayLoc != null && (c.getThoiGianGiao() == null || !c.getThoiGianGiao().startsWith(ngayLoc))) continue;
            ketQua.add(c);
        }
        String luc = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(new Date(TripCache.savedAt(this)));
        tvOfflineBanner.setText("Không có kết nối mạng. Đang hiển thị dữ liệu đã lưu lúc " + luc);
        tvOfflineBanner.setVisibility(View.VISIBLE);
        hienDanhSach(ketQua);
    }

    private void hienDanhSach(List<ChuyenDanhSach> ds) {
        adapter.submit(ds);
        boolean empty = adapter.isEmpty();
        errorState.setVisibility(View.GONE);
        rvTrips.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (dangLoc()) {
            tvEmptyTitle.setText("Không có chuyến phù hợp");
            tvEmptySubtitle.setText("Thử bỏ bộ lọc trạng thái hoặc ngày giao.");
        } else {
            tvEmptyTitle.setText("Chưa có chuyến nào được phân công");
            tvEmptySubtitle.setText("Khi điều phối giao chuyến, lịch trình sẽ hiển thị tại đây.");
        }
    }

    private void hienLoi(String chiTiet) {
        adapter.submit(null);
        tvOfflineBanner.setVisibility(View.GONE);
        rvTrips.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        errorState.setVisibility(View.VISIBLE);
        tvErrorDetail.setText(chiTiet);
    }

    private void dangXuat() {
        new SessionManager(this).clear();
        Snackbar.make(rvTrips, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", Snackbar.LENGTH_LONG).show();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
