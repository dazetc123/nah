package com.example.betong.taixe;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis GIẢ cho kiểm thử: lưu dữ liệu vào một Map trong bộ nhớ thay vì kết nối Redis thật.
 * Nhờ vậy chạy test không cần bật redis-server, mà vẫn kiểm tra được "vị trí xe đã được lưu vào Redis".
 */
@TestConfiguration
public class RedisGiaConfig {

    /** RedisTemplate chỉ hỗ trợ opsForValue().set/get - đủ cho GpsController và TaiXeChuyenServiceImpl. */
    public static class RedisGia extends RedisTemplate<String, Object> {
        public final Map<String, Object> duLieu = new ConcurrentHashMap<>();

        /** Bỏ qua bước kiểm tra kết nối Redis thật khi Spring khởi tạo bean. */
        @Override
        public void afterPropertiesSet() {
        }

        @Override
        @SuppressWarnings("unchecked")
        public ValueOperations<String, Object> opsForValue() {
            return (ValueOperations<String, Object>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(), new Class<?>[]{ValueOperations.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "set" -> { duLieu.put((String) args[0], args[1]); yield null; }
                        case "get" -> duLieu.get(args[0]);
                        default -> throw new UnsupportedOperationException("RedisGia chưa hỗ trợ " + method.getName());
                    });
        }
    }

    @Bean
    @Primary
    public RedisGia redisGia() {
        return new RedisGia();
    }
}
