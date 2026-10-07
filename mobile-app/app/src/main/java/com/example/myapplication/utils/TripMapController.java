package com.example.myapplication.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.models.trip.ChuyenChiTiet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

/**
 * Mục 2.2.1 - "Xem thông tin chi tiết chuyến" bước 4: hiển thị vị trí công
 * trình trên bản đồ (OpenStreetMap, không cần API key) kèm tuyến đường gợi ý
 * từ trạm trộn lấy từ dịch vụ OSRM công khai. Luồng 4.a: không có tọa độ hoặc
 * không lấy được tuyến -> báo bằng chữ để tài xế dùng nút "Chỉ đường".
 */
public class TripMapController {

    private static final String OSRM_URL =
            "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson";

    private final MapView map;
    private final TextView tvInfo;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    private long daVeChuyen = -1;

    /** Gọi trước setContentView: osmdroid cần user-agent riêng để tải ô bản đồ. */
    public static void init(Context context) {
        Configuration.getInstance().load(context,
                context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }

    @SuppressLint("ClickableViewAccessibility")
    public TripMapController(MapView map, TextView tvInfo) {
        this.map = map;
        this.tvInfo = tvInfo;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(14.0);
        // Cho phép kéo bản đồ bên trong NestedScrollView/SwipeRefreshLayout
        map.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });
    }

    public void onResume() { map.onResume(); }

    public void onPause() { map.onPause(); }

    public void onDestroy() {
        io.shutdownNow();
        map.onDetach();
    }

    /** Vẽ lại khi chuyến đổi (mỗi chuyến chỉ gọi OSRM một lần). */
    public void show(ChuyenChiTiet c, Location viTriHienTai) {
        if (c.getViDoCongTrinh() == null || c.getKinhDoCongTrinh() == null) {
            map.setVisibility(View.GONE);
            tvInfo.setText("Công trình chưa có tọa độ. Dùng địa chỉ ở trên hoặc nút \"Chỉ đường\".");
            return;
        }
        if (daVeChuyen == c.getIdChuyen()) return;
        daVeChuyen = c.getIdChuyen();
        map.setVisibility(View.VISIBLE);
        map.getOverlays().clear();

        Context ctx = map.getContext();
        GeoPoint congTrinh = new GeoPoint(c.getViDoCongTrinh(), c.getKinhDoCongTrinh());
        List<GeoPoint> diem = new ArrayList<>();
        diem.add(congTrinh);
        themMarker(congTrinh, c.getTenCongTrinh() == null ? "Công trình" : c.getTenCongTrinh(),
                c.getDiaChiCongTrinh(), null);

        GeoPoint tram = null;
        if (c.getViDoTram() != null && c.getKinhDoTram() != null) {
            tram = new GeoPoint(c.getViDoTram(), c.getKinhDoTram());
            diem.add(tram);
            Marker m = themMarker(tram, "Trạm trộn: " + (c.getTenTram() == null ? "" : c.getTenTram()),
                    c.getDiaChiTram(), ContextCompat.getDrawable(ctx, R.drawable.ic_build));
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        }
        if (viTriHienTai != null) {
            GeoPoint xe = new GeoPoint(viTriHienTai.getLatitude(), viTriHienTai.getLongitude());
            diem.add(xe);
            Marker m = themMarker(xe, "Vị trí của bạn", null, ContextCompat.getDrawable(ctx, R.drawable.ic_truck));
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        }
        canhKhung(diem);
        map.invalidate();

        if (tram == null) {
            tvInfo.setText("Trạm trộn chưa có tọa độ nên chưa vẽ được tuyến đường. "
                    + "Nếu bản đồ không hiện, bấm \"Chỉ đường\" để mở ứng dụng bản đồ.");
            return;
        }
        tvInfo.setText("Đang tìm tuyến đường từ trạm trộn...");
        final GeoPoint tu = tram;
        final long idChuyen = c.getIdChuyen();
        io.execute(() -> {
            TuyenDuong tuyen = layTuyenDuong(tu, congTrinh);
            main.post(() -> {
                if (idChuyen != daVeChuyen) return;
                veTuyen(tuyen, tu, congTrinh);
            });
        });
    }

    private Marker themMarker(GeoPoint p, String title, String snippet, android.graphics.drawable.Drawable icon) {
        Marker m = new Marker(map);
        m.setPosition(p);
        m.setTitle(title);
        if (snippet != null) m.setSnippet(snippet);
        if (icon != null) m.setIcon(icon);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(m);
        return m;
    }

    private void canhKhung(List<GeoPoint> diem) {
        if (diem.size() == 1) {
            map.getController().setZoom(16.0);
            map.getController().setCenter(diem.get(0));
            return;
        }
        BoundingBox box = BoundingBox.fromGeoPoints(diem).increaseByScale(1.3f);
        // Phải chờ MapView đo xong kích thước mới zoom vừa khung được
        map.post(() -> map.zoomToBoundingBox(box, false, 48));
    }

    private void veTuyen(TuyenDuong tuyen, GeoPoint tu, GeoPoint den) {
        Polyline line = new Polyline(map);
        line.getOutlinePaint().setStrokeWidth(10f);
        if (tuyen != null) {
            line.setPoints(tuyen.diem);
            line.getOutlinePaint().setColor(ContextCompat.getColor(map.getContext(), R.color.colorPrimary));
            tvInfo.setText(String.format(Locale.getDefault(),
                    "Tuyến gợi ý từ trạm trộn: %.1f km, khoảng %d phút.", tuyen.met / 1000, Math.round(tuyen.giay / 60)));
        } else {
            // Không lấy được tuyến (mất mạng / dịch vụ lỗi): vẽ đường thẳng tham khảo
            List<GeoPoint> thang = new ArrayList<>();
            thang.add(tu);
            thang.add(den);
            line.setPoints(thang);
            line.getOutlinePaint().setColor(Color.GRAY);
            tvInfo.setText("Không lấy được tuyến đường, đang hiển thị đường thẳng tham khảo. "
                    + "Bấm \"Chỉ đường\" để dẫn đường bằng ứng dụng bản đồ.");
        }
        map.getOverlays().add(0, line);
        map.invalidate();
    }

    private static class TuyenDuong {
        List<GeoPoint> diem;
        double met;
        double giay;
    }

    /** Gọi OSRM (chạy trên luồng nền). Null nếu lỗi. */
    private TuyenDuong layTuyenDuong(GeoPoint tu, GeoPoint den) {
        String url = String.format(Locale.US, OSRM_URL,
                tu.getLongitude(), tu.getLatitude(), den.getLongitude(), den.getLatitude());
        try (Response r = http.newCall(new Request.Builder().url(url).build()).execute()) {
            if (!r.isSuccessful() || r.body() == null) return null;
            JSONObject route = new JSONObject(r.body().string()).getJSONArray("routes").getJSONObject(0);
            JSONArray coords = route.getJSONObject("geometry").getJSONArray("coordinates");
            TuyenDuong t = new TuyenDuong();
            t.diem = new ArrayList<>();
            for (int i = 0; i < coords.length(); i++) {
                JSONArray p = coords.getJSONArray(i);
                t.diem.add(new GeoPoint(p.getDouble(1), p.getDouble(0)));
            }
            t.met = route.getDouble("distance");
            t.giay = route.getDouble("duration");
            return t.diem.size() >= 2 ? t : null;
        } catch (Exception e) {
            return null;
        }
    }
}
