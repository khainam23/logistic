package org.logistic.parallel;

import lombok.Getter;

/**
 * Quản lý GPU và tính toán số thread tối ưu
 */
public class GPUManager {
    private static GPUManager instance;
    
    @Getter
    private boolean gpuAvailable = false;
    
    @Getter
    private int maxComputeUnits = 0;
    
    @Getter
    private String deviceName = "CPU";
    
    private GPUManager() {
        initializeGPU();
    }
    
    public static synchronized GPUManager getInstance() {
        if (instance == null) {
            instance = new GPUManager();
        }
        return instance;
    }
    
    /**
     * Khởi tạo GPU (đơn giản hóa, không dùng OpenCL)
     */
    private void initializeGPU() {
        try {
            // Kiểm tra xem có GPU không bằng cách check system properties
            String osName = System.getProperty("os.name").toLowerCase();
            
            // Giả lập việc detect GPU
            if (osName.contains("windows") || osName.contains("linux")) {
                // Giả sử có GPU với 8 compute units
                maxComputeUnits = 8;
                deviceName = "Simulated GPU";
                gpuAvailable = true;
                
                System.out.println("GPU simulation enabled!");
                System.out.println("Device: " + deviceName);
                System.out.println("Max Compute Units: " + maxComputeUnits);
            }
            
        } catch (Exception e) {
            System.out.println("Không thể khởi tạo GPU: " + e.getMessage());
            System.out.println("Sẽ sử dụng CPU với Java threading.");
            gpuAvailable = false;
        }
    }
    
    /**
     * Tính toán số thread tối ưu dựa trên khả năng của hệ thống
     */
    public int getOptimalThreadCount() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        if (gpuAvailable) {
            // Sử dụng số compute units * 2 cho GPU simulation
            return Math.max(maxComputeUnits * 2, cpuCores);
        } else {
            // Sử dụng số CPU cores * 2 cho CPU
            return cpuCores * 2;
        }
    }
    
    /**
     * Tính toán số work groups tối ưu
     */
    public int getOptimalWorkGroups(int totalWork) {
        if (!gpuAvailable) {
            return Math.min(totalWork, getOptimalThreadCount());
        }
        
        int workGroupSize = 64; // Giả định work group size
        return Math.min((totalWork + workGroupSize - 1) / workGroupSize, maxComputeUnits);
    }
    
    /**
     * Kiểm tra xem có nên sử dụng GPU cho tác vụ này không
     */
    public boolean shouldUseGPU(int workSize) {
        return gpuAvailable && workSize > 100; // Chỉ sử dụng GPU cho tác vụ lớn
    }
    
    /**
     * Dọn dẹp tài nguyên
     */
    public void cleanup() {
        System.out.println("GPU Manager đã được dọn dẹp.");
    }
}