package com.example.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.utils.SessionManager;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Mục 2.2.2 báo cáo - "Bật định vị" / "Tắt định vị" / "Gửi tọa độ vị trí
 * theo thời gian thực".
 *
 * Dịch vụ nền (foreground service) chạy khi tài xế bật định vị, lấy tọa độ
 * từ LocationManager rồi gửi lên máy chủ qua WebSocket mỗi 5-10 giây — CHỈ
 * khi đang có một chuyến ở trạng thái "Đang giao" (idChuyen != null), đúng
 * tiền điều kiện của usecase "Gửi tọa độ...". Nếu tài xế chỉ bật định vị mà
 * chưa có chuyến nào đang giao, dịch vụ vẫn chạy và hiển thị tọa độ hiện tại
 * nhưng không gửi lên server (không có gì để gắn tọa độ vào).
 *
 * Mất kết nối Internet (3.a của "Gửi tọa độ..."): các điểm chưa gửi được lưu
 * tạm vào {@link #hangDoiChoGui} và gửi bù ngay khi WebSocket kết nối lại.
 */
public class LocationService extends Service {

    public static final String ACTION_START = "com.example.myapplication.action.START_LOCATION";
    public static final String ACTION_STOP = "com.example.myapplication.action.STOP_LOCATION";
    public static final String EXTRA_ID_CHUYEN = "extra_id_chuyen";

    /** Phát mỗi khi có tọa độ mới (dùng để cập nhật màn hình Gửi vị trí GPS). */
    public static final String ACTION_VI_TRI_CAP_NHAT = "com.example.myapplication.VI_TRI_CAP_NHAT";
    public static final String EXTRA_VI_DO = "vi_do";
    public static final String EXTRA_KINH_DO = "kinh_do";
    public static final String EXTRA_TOC_DO = "toc_do";
    public static final String EXTRA_DANG_GUI = "dang_gui";

    /** Phát khi dịch vụ bắt đầu/kết thúc (dùng để đồng bộ trạng thái công tắc trên UI). */
    public static final String ACTION_TRANG_THAI = "com.example.myapplication.VI_TRI_TRANG_THAI";
    public static final String EXTRA_DANG_CHAY = "dang_chay";

    private static final String CHANNEL_ID = "dinh_vi_gps";
    private static final int NOTIF_ID = 2001;
    private static final long CHU_KY_GUI_MS = 7000L; // giữa khoảng 5-10 giây theo mô tả usecase
    private static final int SO_LUONG_HANG_DOI_TOI_DA = 50;

    /** Trạng thái tĩnh để các Activity khác kiểm tra nhanh mà không cần bind Service. */
    private static volatile boolean dangChayTinhTrang = false;
    private static volatile Long idChuyenDangStream = null;

    public static boolean isRunning() {
        return dangChayTinhTrang;
    }

    public static Long chuyenDangStream() {
        return idChuyenDangStream;
    }

    public static void start(Context context, Long idChuyenHoacNull) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_START);
        if (idChuyenHoacNull != null) intent.putExtra(EXTRA_ID_CHUYEN, idChuyenHoacNull);
        // startForegroundService() chỉ có từ Android 8 (API 26); ContextCompat tự dùng
        // startService() trên Android 7 (minSdk 24) để không bị crash.
        ContextCompat.startForegroundService(context, intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    // ===================== Nội bộ Service =====================

    private LocationManager locationManager;
    private OkHttpClient httpClient;
    private WebSocket webSocket;
    private Handler handler;
    private Location viTriGanNhat;
    private Long idChuyen; // null nếu chưa có chuyến nào đang giao
    private final ArrayDeque<String> hangDoiChoGui = new ArrayDeque<>();
    private boolean dangKetNoi = false;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            viTriGanNhat = location;
            phatCapNhatViTri(false);
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onProviderDisabled(String provider) {}
    };

    private final Runnable guiDinhKy = new Runnable() {
        @Override
        public void run() {
            guiViTriHienTaiNeuCoTheGui();
            handler.postDelayed(this, CHU_KY_GUI_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        httpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // giữ kết nối WebSocket mở
                .build();
        taoKenhThongBao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            dungDichVu();
            return START_NOT_STICKY;
        }

        if (intent == null) {
            // Hệ thống khởi động lại dịch vụ (START_STICKY) sau khi bị dừng -> lấy lại chuyến đang gửi
            long daLuu = getSharedPreferences(PREF, MODE_PRIVATE).getLong(KEY_ID_CHUYEN, -1);
            idChuyen = daLuu > 0 ? daLuu : null;
        } else if (intent.hasExtra(EXTRA_ID_CHUYEN)) {
            idChuyen = intent.getLongExtra(EXTRA_ID_CHUYEN, -1);
            if (idChuyen == -1) idChuyen = null;
        } else {
            idChuyen = null;
        }
        idChuyenDangStream = idChuyen;
        luuChuyenDangGui(idChuyen);

        Notification notification = taoThongBao("Đang gửi vị trí GPS",
                idChuyen != null ? "Đang cập nhật hành trình chuyến #" + idChuyen : "Chưa có chuyến nào đang giao");
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIF_ID, notification);
        }

        batDauLayViTri();
        if (idChuyen != null) moKetNoi();
        handler.removeCallbacks(guiDinhKy);
        handler.postDelayed(guiDinhKy, CHU_KY_GUI_MS);

        dangChayTinhTrang = true;
        phatTrangThai();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception ignored) {
        }
        dongKetNoi();
        handler.removeCallbacks(guiDinhKy);
        dangChayTinhTrang = false;
        idChuyenDangStream = null;
        phatTrangThai();
        super.onDestroy();
    }

    private static final String PREF = "LocationServiceState";
    private static final String KEY_ID_CHUYEN = "id_chuyen";

    private void luuChuyenDangGui(Long id) {
        getSharedPreferences(PREF, MODE_PRIVATE).edit()
                .putLong(KEY_ID_CHUYEN, id == null ? -1 : id).apply();
    }

    private void dungDichVu() {
        luuChuyenDangGui(null);
        // Hậu điều kiện "Tắt định vị": gửi bản ghi vị trí cuối cùng trước khi dừng hẳn.
        guiViTriHienTaiNeuCoTheGui();
        handler.postDelayed(() -> {
            stopForeground(true);
            stopSelf();
        }, 400L); // chờ 1 nhịp ngắn để kịp gửi điểm cuối trước khi đóng socket
    }

    private void batDauLayViTri() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return; // Activity gọi start() phải đảm bảo đã có quyền trước
        }
        try {
            locationManager.removeUpdates(locationListener);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 0f, locationListener);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 0f, locationListener);
            }
        } catch (SecurityException ignored) {
            // Quyền có thể bị thu hồi giữa lúc chạy — bỏ qua, lần gửi tiếp theo sẽ không có tọa độ mới.
        }
    }

    private void moKetNoi() {
        if (webSocket != null || dangKetNoi) return;
        String token = new SessionManager(getApplicationContext()).getToken();
        if (token == null || token.isEmpty()) return;
        dangKetNoi = true;
        Request request = new Request.Builder()
                .url(ApiClient.getWsUrl())
                .addHeader("Authorization", "Bearer " + token)
                .build();
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                dangKetNoi = false;
                guiBuHangDoi();
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                dangKetNoi = false;
                webSocket = null; // sẽ thử kết nối lại ở chu kỳ gửi tiếp theo
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                dangKetNoi = false;
                webSocket = null;
            }
        });
    }

    private void dongKetNoi() {
        if (webSocket != null) {
            try {
                webSocket.close(1000, "Tài xế tắt định vị");
            } catch (Exception ignored) {
            }
            webSocket = null;
        }
    }

    private void guiViTriHienTaiNeuCoTheGui() {
        if (idChuyen == null || viTriGanNhat == null) return;
        String json = diemGpsJson();
        if (webSocket == null) {
            themVaoHangDoi(json);
            moKetNoi();
            return;
        }
        boolean guiThanhCong = webSocket.send(json);
        if (!guiThanhCong) themVaoHangDoi(json);
        phatCapNhatViTri(guiThanhCong);
    }

    private void guiBuHangDoi() {
        while (!hangDoiChoGui.isEmpty() && webSocket != null) {
            String json = hangDoiChoGui.peek();
            if (webSocket.send(json)) {
                hangDoiChoGui.poll();
            } else {
                break;
            }
        }
    }

    private void themVaoHangDoi(String json) {
        if (hangDoiChoGui.size() >= SO_LUONG_HANG_DOI_TOI_DA) hangDoiChoGui.poll();
        hangDoiChoGui.offer(json);
    }

    private String diemGpsJson() {
        GpsDiemGui diem = new GpsDiemGui();
        diem.idChuyen = idChuyen;
        diem.viDo = viTriGanNhat.getLatitude();
        diem.kinhDo = viTriGanNhat.getLongitude();
        diem.tocDo = (double) viTriGanNhat.getSpeed();
        diem.thoiDiem = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());
        return new Gson().toJson(diem);
    }

    private void phatCapNhatViTri(boolean dangGui) {
        if (viTriGanNhat == null) return;
        Intent intent = new Intent(ACTION_VI_TRI_CAP_NHAT);
        intent.putExtra(EXTRA_VI_DO, viTriGanNhat.getLatitude());
        intent.putExtra(EXTRA_KINH_DO, viTriGanNhat.getLongitude());
        intent.putExtra(EXTRA_TOC_DO, (double) viTriGanNhat.getSpeed());
        intent.putExtra(EXTRA_DANG_GUI, dangGui);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        updateNotification();
    }

    private void phatTrangThai() {
        Intent intent = new Intent(ACTION_TRANG_THAI);
        intent.putExtra(EXTRA_DANG_CHAY, dangChayTinhTrang);
        if (idChuyen != null) intent.putExtra(EXTRA_ID_CHUYEN, idChuyen);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void taoKenhThongBao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Định vị GPS", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Thông báo khi ứng dụng đang gửi vị trí xe theo thời gian thực");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification taoThongBao(String title, String content) {
        Intent openIntent = new Intent(this, GpsLocationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_location)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification() {
        String content = idChuyen != null
                ? String.format(Locale.US, "Chuyến #%d · %.5f, %.5f", idChuyen,
                        viTriGanNhat.getLatitude(), viTriGanNhat.getLongitude())
                : String.format(Locale.US, "Chưa có chuyến đang giao · %.5f, %.5f",
                        viTriGanNhat.getLatitude(), viTriGanNhat.getLongitude());
        Notification notification = taoThongBao("Đang gửi vị trí GPS", content);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.notify(NOTIF_ID, notification);
    }

    /** Payload gửi lên server — tên field phải khớp DTO GpsMessage bên backend. */
    private static class GpsDiemGui {
        Long idChuyen;
        double viDo;
        double kinhDo;
        double tocDo;
        String thoiDiem;
    }
}
