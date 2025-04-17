package org.logistic.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Lớp khởi tạo Spring Context cho ứng dụng
 */
public class SpringContextInitializer {
    private static AnnotationConfigApplicationContext context;
    
    /**
     * Khởi tạo Spring Context
     */
    public static void initialize() {
        if (context == null) {
            context = new AnnotationConfigApplicationContext();
            context.register(AppConfig.class);
            context.refresh();
        }
    }
    
    /**
     * Lấy bean từ Spring Context
     * 
     * @param beanClass Lớp của bean cần lấy
     * @param <T> Kiểu của bean
     * @return Bean được quản lý bởi Spring
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            initialize();
        }
        return context.getBean(beanClass);
    }
    
    /**
     * Đóng Spring Context
     */
    public static void close() {
        if (context != null) {
            context.close();
            context = null;
        }
    }
}