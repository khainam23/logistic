package org.logistic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các phương thức cần ghi log
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogMethod {
    /**
     * Mức độ log (mặc định là INFO)
     */
    LogLevel level() default LogLevel.INFO;
    
    /**
     * Thông điệp tùy chỉnh (nếu để trống sẽ sử dụng tên phương thức)
     */
    String message() default "";
    
    /**
     * Có ghi log tham số đầu vào không
     */
    boolean logParams() default false;
    
    /**
     * Có ghi log giá trị trả về không
     */
    boolean logReturn() default false;
}