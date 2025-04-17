package org.logistic.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Cấu hình Spring cho ứng dụng
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"org.logistic"})
public class AppConfig {
    // Các cấu hình bổ sung có thể được thêm vào đây
}