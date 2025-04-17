package org.logistic.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.logistic.annotation.LogLevel;
import org.logistic.annotation.LogMethod;
import org.logistic.util.WriteLogUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect để xử lý việc ghi log tự động
 */
@Aspect
@Component
public class LoggingAspect {
    
    private final WriteLogUtil writeLogUtil;
    
    public LoggingAspect() {
        this.writeLogUtil = WriteLogUtil.getInstance();
    }
    
    /**
     * Pointcut cho tất cả các phương thức được đánh dấu bằng @LogMethod
     */
    @Pointcut("@annotation(org.logistic.annotation.LogMethod)")
    public void logMethodPointcut() {
    }
    
    /**
     * Advice được thực thi trước khi phương thức được gọi
     */
    @Before("logMethodPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogMethod logMethod = method.getAnnotation(LogMethod.class);
        
        if (logMethod != null) {
            String message = logMethod.message().isEmpty() 
                ? "Bắt đầu thực thi phương thức: " + method.getName()
                : logMethod.message();
                
            if (logMethod.logParams()) {
                message += " với tham số: " + Arrays.toString(joinPoint.getArgs());
            }
            
            logMessage(logMethod.level(), message, joinPoint);
        }
    }
    
    /**
     * Advice được thực thi sau khi phương thức hoàn thành thành công
     */
    @AfterReturning(pointcut = "logMethodPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogMethod logMethod = method.getAnnotation(LogMethod.class);
        
        if (logMethod != null) {
            String message = "Hoàn thành thực thi phương thức: " + method.getName();
            
            if (logMethod.logReturn() && result != null) {
                message += " với kết quả: " + result;
            }
            
            logMessage(logMethod.level(), message, joinPoint);
        }
    }
    
    /**
     * Advice được thực thi khi phương thức ném ra ngoại lệ
     */
    @AfterThrowing(pointcut = "logMethodPointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String message = "Ngoại lệ trong phương thức: " + method.getName() + 
                         " - " + exception.getMessage();
        
        logMessage(LogLevel.ERROR, message, joinPoint);
    }
    
    /**
     * Advice được thực thi bao quanh phương thức (có thể đo thời gian thực thi)
     */
    @Around("logMethodPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogMethod logMethod = method.getAnnotation(LogMethod.class);
        
        if (logMethod != null) {
            String message = "Phương thức: " + method.getName() + 
                             " thực thi trong " + (endTime - startTime) + "ms";
            
            logMessage(logMethod.level(), message, joinPoint);
        }
        
        return result;
    }
    
    /**
     * Ghi log với mức độ được chỉ định
     */
    private void logMessage(LogLevel level, String message, JoinPoint joinPoint) {
        // Xác định thuật toán đang chạy từ tên lớp
        String className = joinPoint.getTarget().getClass().getSimpleName();
        setLogFilePathBasedOnClass(className);
        
        switch (level) {
            case INFO -> writeLogUtil.info(message);
            case DEBUG -> writeLogUtil.debug(message);
            case WARN -> writeLogUtil.warn(message);
            case ERROR -> writeLogUtil.error(message);
        }
    }
    
    /**
     * Thiết lập đường dẫn file log dựa trên tên lớp
     */
    private void setLogFilePathBasedOnClass(String className) {
        if (className.contains("SimulatedAnnealing")) {
            writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.SA.getPath());
        } else if (className.contains("SpottedHyena")) {
            writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.SHO.getPath());
        } else if (className.contains("AntColony")) {
            writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.ACO.getPath());
        } else if (className.contains("GreyWolf")) {
            writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.GWO.getPath());
        } else {
            // Mặc định sử dụng log của GWO
            writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.GWO.getPath());
        }
    }
}