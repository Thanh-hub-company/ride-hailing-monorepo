package com.example.commonsecurity.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình các thông số JWT dùng chung cho toàn hệ thống Microservices.
 * Tự động ánh xạ từ prefix 'jwt' trong file application.yml của các ứng dụng downstream.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Khóa bí mật dùng để ký và giải mã Token (HMAC-SHA256).
     * Mặc định là chuỗi 256-bit (32 bytes) hợp chuẩn thư viện JJWT 0.12.x để tránh lỗi WeakKeyException.
     */
    @Default
    private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    /**
     * Thời gian hết hạn của Access Token tính bằng millisecond (Mặc định: 24 giờ = 86,400,000 ms).
     */
    @Default
    private long expirationMs = 86400000L;

    /**
     * Thời gian hết hạn của Refresh Token tính bằng millisecond (Mặc định: 7 ngày = 604,800,000 ms).
     */
    @Default
    private long refreshExpirationMs = 604800000L;
}