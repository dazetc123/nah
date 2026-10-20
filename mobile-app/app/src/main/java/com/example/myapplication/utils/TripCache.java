package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

/**
 * Mục 2.2.1 - luồng 3.a "Xem danh sách chuyến": lưu danh sách chuyến của lần
 * đồng bộ thành công gần nhất để hiển thị khi mất kết nối.
 * Lưu chung file SharedPreferences với SessionManager nên tự bị xóa khi đăng xuất.
 */
public final class TripCache {

    private static final String PREF_NAME = "BetongDriverSession";
    private static final String KEY_DATA = "trip_cache_json";
    private static final String KEY_TIME = "trip_cache_time";

    private TripCache() {}

    public static void save(Context context, List<ChuyenDanhSach> trips) {
        prefs(context).edit()
                .putString(KEY_DATA, new Gson().toJson(trips == null ? new ArrayList<>() : trips))
                .putLong(KEY_TIME, System.currentTimeMillis())
                .apply();
    }

    /** Null nếu chưa từng đồng bộ. */
    public static List<ChuyenDanhSach> load(Context context) {
        String json = prefs(context).getString(KEY_DATA, null);
        if (json == null) return null;
        try {
            return new Gson().fromJson(json, new TypeToken<List<ChuyenDanhSach>>() {}.getType());
        } catch (Exception e) {
            return null;
        }
    }

    public static long savedAt(Context context) {
        return prefs(context).getLong(KEY_TIME, 0);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
