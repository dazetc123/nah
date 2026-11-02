package com.example.myapplication;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Kiểm thử GIAO DIỆN màn Đăng nhập của app Tài xế bằng Espresso (chạy trên máy ảo / điện thoại thật).
 * Espresso tự gõ chữ, bấm nút như người dùng rồi kiểm tra màn hình hiển thị đúng chưa.
 *
 * Chạy: trong Android Studio, chuột phải file này -> Run 'DangNhapGiaoDienTest' (cần bật máy ảo trước).
 * Các test dưới đây KHÔNG cần backend vì lỗi được kiểm tra ngay trên app trước khi gọi API.
 */
@RunWith(AndroidJUnit4.class)
public class DangNhapGiaoDienTest {

    @Before
    public void xoaPhienDangNhapCu() {
        // Nếu đã đăng nhập từ trước, LoginActivity sẽ tự chuyển sang trang chủ -> xoá phiên để luôn thấy form đăng nhập
        Context context = ApplicationProvider.getApplicationContext();
        new SessionManager(context).clear();
    }

    @Test
    public void dn03_boTrongTenDangNhapVaMatKhau() {
        try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.tilUsername)).check(matches(coThongBaoLoi("Vui lòng nhập tên đăng nhập")));
            onView(withId(R.id.tilPassword)).check(matches(coThongBaoLoi("Vui lòng nhập mật khẩu")));
        }
    }

    @Test
    public void dn03_matKhauDuoi8KyTu() {
        try (ActivityScenario<LoginActivity> ignored = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etUsername)).perform(typeText("taixe01"), closeSoftKeyboard());
            onView(withId(R.id.etPassword)).perform(typeText("1234567"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.tilPassword)).check(matches(coThongBaoLoi("Mật khẩu phải từ 8 ký tự trở lên")));
        }
    }

    /** So khớp ô nhập (TextInputLayout) đang hiện đúng dòng báo lỗi màu đỏ. */
    private static Matcher<View> coThongBaoLoi(String loiMongDoi) {
        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("ô nhập có thông báo lỗi: " + loiMongDoi);
            }

            @Override
            protected boolean matchesSafely(TextInputLayout o) {
                return o.getError() != null && loiMongDoi.contentEquals(o.getError());
            }
        };
    }
}
