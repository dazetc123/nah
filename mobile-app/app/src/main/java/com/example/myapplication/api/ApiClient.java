package com.example.myapplication.api;

import android.content.Context;
import com.example.myapplication.utils.SessionManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String CLIENT_KEY = "betongmobile";
    private static Retrofit retrofit = null;

    public static ApiService getService(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context.getApplicationContext());

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request.Builder builder = chain.request().newBuilder();
                        // Thêm Client Key (bắt buộc với backend)
                        builder.addHeader("ClientKey", CLIENT_KEY);
                        // Thêm Bearer Token nếu đã đăng nhập
                        String token = sessionManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            builder.addHeader("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    /** Mục 2.2.2 - địa chỉ WebSocket nhận vị trí GPS, suy ra từ cùng BASE_URL với REST API. */
    public static String getWsUrl() {
        return BASE_URL.replaceFirst("^http", "ws") + "ws/vi-tri-gps";
    }
}
