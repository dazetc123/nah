package com.example.betong.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger/OpenAPI cho hệ thống quản lý & đặt hàng bê tông thương phẩm.
 * Truy cập giao diện tại: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    /** Mọi API /api/** đều bắt buộc header ClientKey (xem ClientKeyFilter) — khai báo để nhập được trong nút Authorize. */
    private static final String CLIENT_KEY_SCHEME = "clientKey";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Nền tảng quản lý và đặt hàng bê tông thương phẩm")
                        .description("Tài liệu API cho hệ thống quản lý, đặt hàng bê tông "
                                + "và điều phối vận chuyển theo thời gian thực.")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME).addList(CLIENT_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSecuritySchemes(CLIENT_KEY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("ClientKey")
                                        .description("Web: betong-web-local · App: betongmobile")));
    }
}
