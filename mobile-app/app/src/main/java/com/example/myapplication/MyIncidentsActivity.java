package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.common.PageResponse;
import com.example.myapplication.models.trip.SuCo;
import com.example.myapplication.utils.ApiError;
import com.example.myapplication.utils.TrangThaiChuyenUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mục 2.2.3 - "Báo cáo sự cố" bước 7: tài xế xem các sự cố đã gửi và trạng
 * thái xử lý (Mới tiếp nhận / Đang xử lý / Đã xử lý) do Nhân viên điều phối
 * cập nhật trên web. Tự làm mới mỗi 15 giây khi đang mở màn hình.
 */
public class MyIncidentsActivity extends AppCompatActivity {

    private static final long CHU_KY_MS = 15_000L;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rv;
    private TextView tvEmpty;
    private final IncidentAdapter adapter = new IncidentAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tuLamMoi = new Runnable() {
        @Override
        public void run() {
            load(false);
            handler.postDelayed(this, CHU_KY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_incidents);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rv = findViewById(R.id.rvIncidents);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(() -> load(true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        load(true);
        handler.postDelayed(tuLamMoi, CHU_KY_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tuLamMoi);
    }

    private void load(boolean hienVongXoay) {
        if (hienVongXoay) swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getSuCoCuaToi(1, 50).enqueue(new Callback<PageResponse<SuCo>>() {
            @Override
            public void onResponse(Call<PageResponse<SuCo>> call, Response<PageResponse<SuCo>> response) {
                swipeRefresh.setRefreshing(false);
                if (!response.isSuccessful() || response.body() == null) {
                    if (hienVongXoay) thongBao(ApiError.message(response));
                    return;
                }
                adapter.submit(response.body().getDanhSach());
                boolean empty = adapter.getItemCount() == 0;
                tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(Call<PageResponse<SuCo>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (hienVongXoay) thongBao("Không kết nối được máy chủ. Vuốt xuống để thử lại");
            }
        });
    }

    private void thongBao(String msg) {
        Snackbar.make(rv, msg, Snackbar.LENGTH_LONG).show();
    }

    private static class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.VH> {
        private final List<SuCo> items = new ArrayList<>();

        void submit(List<SuCo> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            SuCo s = items.get(position);
            String thoiDiem = TrangThaiChuyenUtil.dinhDangNgayGio(s.getThoiDiem());
            h.tvMa.setText("Sự cố #" + s.getIdSuCo()
                    + (s.getIdChuyen() != null ? " · Chuyến #" + s.getIdChuyen() : "")
                    + (thoiDiem.isEmpty() ? "" : " · " + thoiDiem));
            h.tvLoai.setText(s.getLoaiSuCo() + " · Ưu tiên "
                    + (s.getTenMucDoUuTien() == null ? "Trung bình" : s.getTenMucDoUuTien()));
            h.tvMoTa.setText(s.getMoTa());

            int t = s.getTrangThai() == null ? 0 : s.getTrangThai();
            h.tvTrangThai.setText(s.getTenTrangThai() == null ? "Mới tiếp nhận" : s.getTenTrangThai());
            int bg, mau;
            String ghiChu;
            if (t == 2) {
                bg = R.drawable.bg_icon_green; mau = R.color.colorSuccess;
                ghiChu = "Điều phối đã xử lý xong sự cố này.";
            } else if (t == 1) {
                bg = R.drawable.bg_icon_indigo; mau = R.color.colorPrimary;
                ghiChu = "Điều phối đang xử lý, vui lòng giữ máy để được liên hệ.";
            } else {
                bg = R.drawable.bg_icon_amber; mau = R.color.colorWarning;
                ghiChu = "Đã gửi tới điều phối, đang chờ tiếp nhận.";
            }
            h.tvTrangThai.setBackgroundResource(bg);
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), mau));
            h.tvGhiChu.setText(ghiChu);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvMa, tvTrangThai, tvLoai, tvMoTa, tvGhiChu;

            VH(@NonNull View v) {
                super(v);
                tvMa = v.findViewById(R.id.tvMaSuCo);
                tvTrangThai = v.findViewById(R.id.tvTrangThai);
                tvLoai = v.findViewById(R.id.tvLoai);
                tvMoTa = v.findViewById(R.id.tvMoTa);
                tvGhiChu = v.findViewById(R.id.tvGhiChu);
            }
        }
    }
}
