package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.adapters.TripAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.common.PageResponse;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.google.android.material.snackbar.Snackbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Mục 2.2.1 - Usecase "Xem danh sách chuyến được phân công". */
public class TripsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTrips;
    private View emptyState;
    private TripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTrips = findViewById(R.id.rvTrips);
        emptyState = findViewById(R.id.emptyState);

        adapter = new TripAdapter(chuyen -> {
            Intent i = new Intent(this, TripDetailActivity.class);
            i.putExtra(TripDetailActivity.EXTRA_ID_CHUYEN, chuyen.getIdChuyen());
            startActivity(i);
        });
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        rvTrips.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(this::load);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getService(this).getDanhSachChuyen(null, 1, 50)
                .enqueue(new Callback<PageResponse<ChuyenDanhSach>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ChuyenDanhSach>> call,
                                           Response<PageResponse<ChuyenDanhSach>> response) {
                        swipeRefresh.setRefreshing(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            showError("Không tải được danh sách chuyến (mã lỗi " + response.code() + ")");
                            return;
                        }
                        adapter.submit(response.body().getDanhSach());
                        toggleEmpty();
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ChuyenDanhSach>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        showError("Không kết nối được máy chủ. Kiểm tra mạng và thử lại");
                        toggleEmpty();
                    }
                });
    }

    private void toggleEmpty() {
        boolean empty = adapter.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTrips.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        Snackbar.make(rvTrips, msg, Snackbar.LENGTH_LONG).show();
    }
}
