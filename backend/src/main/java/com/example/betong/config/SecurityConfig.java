package com.example.betong.config;

import com.example.betong.Security.JwtAuthenticationFilter;
import com.example.betong.Security.ClientKeyFilter;
import com.example.betong.Security.GoogleOAuth2SuccessHandler;
import com.example.betong.Security.GoogleOAuth2FailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ClientKeyFilter clientKeyFilter;
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    private final GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ClientKeyFilter clientKeyFilter,
                          @Lazy GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
                          @Lazy GoogleOAuth2FailureHandler googleOAuth2FailureHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.clientKeyFilter = clientKeyFilter;
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
        this.googleOAuth2FailureHandler = googleOAuth2FailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> oauth2AuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // QUAN TRỌNG: rule cụ thể này phải khai báo TRƯỚC "/api/auth/**",
                        // vì Spring Security so khớp theo đúng thứ tự, rule đầu tiên khớp sẽ thắng.
                        // API đổi mật khẩu lần đầu bắt buộc phải có JWT hợp lệ (dù JWT đó
                        // đang mang cờ phaiDoiMatKhau = true) — không được để public.
                        .requestMatchers("/api/auth/doi-mat-khau-lan-dau").authenticated()
                        .requestMatchers("/api/auth/dang-xuat").authenticated()

                        // Module Quản lý người dùng (Bảng 3.3-3.11): TẤT CẢ usecase có
                        // Tác nhân = Quản lý. Ràng buộc theo vai trò ngay ở tầng gateway
                        // này, không chỉ dựa vào kiemTraLaQuanLy() bên trong từng Service
                        // — 2 lớp phòng vệ độc lập (defense in depth), lỡ 1 Controller
                        // mới sau này quên gọi kiểm tra trong Service thì vẫn bị chặn ở đây.
                        // "Nhân viên điều phối" chứa khoảng trắng nên hasRole() vẫn khớp
                        // đúng vì JwtAuthenticationFilter gán authority y nguyên "ROLE_" + vaiTro.
                        .requestMatchers("/api/quan-ly/**", "/api/admin/**").hasRole("Quản lý")
                        .requestMatchers("/api/dieu-phoi/xe/**", "/api/dieu-phoi/theo-doi-xe/**")
                        .hasAnyRole("Tài xế", "Nhân viên điều phối")

                        // Mục 2.2 báo cáo - App di động Tài xế: Quản lý chuyến, Gửi vị trí
                        // GPS, Cập nhật trạng thái. Chỉ vai trò Tài xế được gọi.
                        .requestMatchers("/api/tai-xe/**").hasRole("Tài xế")

                        // Mục 2.2.2 - Endpoint WebSocket nhận tọa độ GPS từ app tài xế.
                        // GpsHandshakeInterceptor kiểm tra lại JWT lần 2 (phòng vệ 2 lớp).
                        .requestMatchers("/ws/**").hasRole("Tài xế")
                        .requestMatchers("/api/khach-hang/**").hasRole("Khách hàng")

                        // Hồ sơ cá nhân (Bảng 3.39, 3.40): mọi vai trò đã đăng nhập đều
                        // được tự xem/sửa hồ sơ và đổi mật khẩu của CHÍNH MÌNH.
                        .requestMatchers("/api/nguoi-dung/**").authenticated()

                        // Các API xác thực còn lại (đăng nhập, đăng ký): cho phép truy cập công khai
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Swagger/OpenAPI: cho phép truy cập công khai để xem tài liệu API
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Các request còn lại bắt buộc phải có JWT hợp lệ
                        .anyRequest().authenticated()
                )
                // API (app di động, web) chưa đăng nhập / token hết hạn -> trả 401 để client
                // tự đưa về màn hình đăng nhập, thay vì bị chuyển hướng 302 sang trang Google.
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new OrRequestMatcher(new AntPathRequestMatcher("/api/**"), new AntPathRequestMatcher("/ws/**"))))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(authorizationRequestRepository))
                        .failureHandler(googleOAuth2FailureHandler)
                        .successHandler(googleOAuth2SuccessHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(clientKeyFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}