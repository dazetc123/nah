package com.example.myapplication.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.content.ContextCompat;

/** Lấy vị trí gần nhất mà thiết bị đã ghi nhận (ưu tiên độ chính xác cao nhất). */
public final class LocationUtil {

    private LocationUtil() {}

    public static boolean coQuyen(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Null nếu chưa có quyền hoặc thiết bị chưa ghi nhận được vị trí nào. */
    public static Location viTriGanNhat(Context context) {
        if (!coQuyen(context)) return null;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return null;
        Location best = null;
        try {
            for (String p : lm.getProviders(true)) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && (best == null || l.getAccuracy() < best.getAccuracy())) best = l;
            }
        } catch (SecurityException ignored) {
            return null;
        }
        return best;
    }
}
