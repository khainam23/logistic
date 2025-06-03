package org.logistic.parallel;

import org.logistic.Main.Algorithm;
import org.logistic.model.Solution;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Theo dõi hiệu suất và metrics của các thuật toán
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;
    
    // Tracking execution times
    private final Map<Algorithm, List<Long>> executionTimes = new ConcurrentHashMap<>();
    private final Map<Algorithm, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private final Map<Algorithm, AtomicLong> iterationCount = new ConcurrentHashMap<>();
    
    // Tracking fitness values
    private final Map<Algorithm, List<Double>> fitnessValues = new ConcurrentHashMap<>();
    private final Map<Algorithm, Double> bestFitness = new ConcurrentHashMap<>();
    
    // Tracking weights data for Excel export
    private final Map<Algorithm, List<int[]>> weightsData = new ConcurrentHashMap<>();
    
    // System metrics
    private long startTime;
    private long endTime;
    private final Runtime runtime = Runtime.getRuntime();
    
    private PerformanceMonitor() {
        // Initialize for all algorithms
        for (Algorithm algorithm : Algorithm.values()) {
            executionTimes.put(algorithm, Collections.synchronizedList(new ArrayList<>()));
            totalExecutionTime.put(algorithm, new AtomicLong(0));
            iterationCount.put(algorithm, new AtomicLong(0));
            fitnessValues.put(algorithm, Collections.synchronizedList(new ArrayList<>()));
            bestFitness.put(algorithm, Double.MAX_VALUE);
            weightsData.put(algorithm, Collections.synchronizedList(new ArrayList<>()));
        }
    }
    
    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }
    
    /**
     * Bắt đầu theo dõi
     */
    public void startMonitoring() {
        startTime = System.currentTimeMillis();
        System.out.println("=== BẮT ĐẦU THEO DÕI HIỆU SUẤT ===");
        printSystemInfo();
    }
    
    /**
     * Kết thúc theo dõi
     */
    public void stopMonitoring() {
        endTime = System.currentTimeMillis();
        System.out.println("\n=== KẾT THÚC THEO DÕI HIỆU SUẤT ===");
        printPerformanceReport();
    }
    
    /**
     * Ghi lại thời gian thực thi của một iteration
     */
    public void recordIterationTime(Algorithm algorithm, long executionTimeMs) {
        executionTimes.get(algorithm).add(executionTimeMs);
        totalExecutionTime.get(algorithm).addAndGet(executionTimeMs);
        iterationCount.get(algorithm).incrementAndGet();
    }
    
    /**
     * Ghi lại fitness value
     */
    public void recordFitness(Algorithm algorithm, double fitness) {
        fitnessValues.get(algorithm).add(fitness);
        System.out.println("[DEBUG] Recording fitness for " + algorithm + ": " + fitness);
        
        // Cập nhật best fitness
        synchronized (bestFitness) {
            Double current = bestFitness.get(algorithm);
            if (fitness < current) {
                bestFitness.put(algorithm, fitness);
                System.out.println("[DEBUG] Updated best fitness for " + algorithm + ": " + fitness);
            }
        }
    }
    
    /**
     * Ghi lại weights data từ FitnessUtil
     */
    public void recordWeights(Algorithm algorithm, int[] weights) {
        if (weights != null && weights.length == 4) {
            weightsData.get(algorithm).add(weights.clone());
            System.out.println("[DEBUG] Recording weights for " + algorithm + ": " + 
                Arrays.toString(weights));
        }
    }
    
    /**
     * Ghi lại kết quả cuối cùng của thuật toán
     */
    public void recordAlgorithmResult(Algorithm algorithm, Solution solution, long totalTimeMs) {
        if (solution != null) {
            recordFitness(algorithm, solution.getFitness());
        }
        
        System.out.printf("[%s] Hoàn thành - Best Fitness: %.2f%n", 
            algorithm, bestFitness.get(algorithm));
    }
    
    /**
     * In thông tin hệ thống
     */
    private void printSystemInfo() {
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        System.out.println("Thông tin hệ thống:");
        System.out.printf("  CPU Cores: %d%n", runtime.availableProcessors());
        System.out.printf("  Max Memory: %.2f MB%n", maxMemory / (1024.0 * 1024.0));
        System.out.printf("  Total Memory: %.2f MB%n", totalMemory / (1024.0 * 1024.0));
        System.out.printf("  Used Memory: %.2f MB%n", usedMemory / (1024.0 * 1024.0));
        System.out.printf("  Free Memory: %.2f MB%n", freeMemory / (1024.0 * 1024.0));
        
        GPUManager gpuManager = GPUManager.getInstance();
        if (gpuManager.isGpuAvailable()) {
            System.out.printf("  GPU: %s (%d compute units)%n", 
                gpuManager.getDeviceName(), gpuManager.getMaxComputeUnits());
        }
    }
    
    /**
     * In báo cáo hiệu suất chi tiết
     */
    private void printPerformanceReport() {
        long totalTime = endTime - startTime;
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    BÁO CÁO HIỆU SUẤT CHI TIẾT");
        System.out.println("=".repeat(80));
        
        System.out.printf("Tổng thời gian thực thi: %.2f giây%n", totalTime / 1000.0);
        System.out.println();
        
        // Báo cáo cho từng thuật toán
        System.out.printf("%-10s %-12s %-15s %-15s %-15s%n", 
            "Thuật toán", "Iterations", "Tổng thời gian", "TB/Iteration", "Best Fitness");
        System.out.println("-".repeat(75));
        
        for (Algorithm algorithm : Algorithm.values()) {
            long totalAlgTime = totalExecutionTime.get(algorithm).get();
            long iterations = iterationCount.get(algorithm).get();
            double avgTime = iterations > 0 ? totalAlgTime / (double) iterations : 0;
            double bestFit = bestFitness.get(algorithm);
            
            System.out.printf("%-10s %-12d %-15.2f %-15.2f %-15.2f%n",
                algorithm,
                iterations,
                totalAlgTime / 1000.0,
                avgTime,
                bestFit == Double.MAX_VALUE ? 0.0 : bestFit
            );
        }
        
        System.out.println("-".repeat(75));
        
        // Thống kê chi tiết
        printDetailedStatistics();
        
        // Memory usage cuối
        printMemoryUsage();
        
        // Recommendations
        printRecommendations();
    }
    
    /**
     * In thống kê chi tiết
     */
    private void printDetailedStatistics() {
        System.out.println("\nTHỐNG KÊ CHI TIẾT:");
        System.out.println("-".repeat(50));
        
        for (Algorithm algorithm : Algorithm.values()) {
            List<Long> times = executionTimes.get(algorithm);
            List<Double> fitness = fitnessValues.get(algorithm);
            
            if (times.isEmpty()) continue;
            
            System.out.println("\n" + algorithm + ":");
            
            // Time statistics
            long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
            double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0);
            
            System.out.printf("  Thời gian - Min: %d ms, Max: %d ms, Avg: %.2f ms%n", 
                minTime, maxTime, avgTime);
            
            // Fitness statistics
            if (!fitness.isEmpty()) {
                double minFitness = fitness.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                double maxFitness = fitness.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                double avgFitness = fitness.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                
                System.out.printf("  Fitness - Min: %.2f, Max: %.2f, Avg: %.2f%n", 
                    minFitness, maxFitness, avgFitness);
                
                // Improvement rate
                if (fitness.size() > 1) {
                    double firstFitness = fitness.get(0);
                    double lastFitness = fitness.get(fitness.size() - 1);
                    double improvement = ((firstFitness - lastFitness) / firstFitness) * 100;
                    System.out.printf("  Cải thiện: %.2f%%%n", improvement);
                }
            }
        }
    }
    
    /**
     * In thông tin sử dụng memory
     */
    private void printMemoryUsage() {
        System.out.println("\nSỬ DỤNG BỘ NHỚ:");
        System.out.println("-".repeat(30));
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        System.out.printf("Đã sử dụng: %.2f MB (%.1f%%)%n", 
            usedMemory / (1024.0 * 1024.0), 
            (usedMemory * 100.0) / maxMemory);
        System.out.printf("Còn trống: %.2f MB%n", freeMemory / (1024.0 * 1024.0));
        System.out.printf("Tối đa: %.2f MB%n", maxMemory / (1024.0 * 1024.0));
    }
    
    /**
     * In khuyến nghị tối ưu hóa
     */
    private void printRecommendations() {
        System.out.println("\nKHUYẾN NGHỊ TỐI ƯU HÓA:");
        System.out.println("-".repeat(40));
        
        // Tìm thuật toán tốt nhất
        Algorithm bestAlgorithm = null;
        double bestFit = Double.MAX_VALUE;
        
        for (Algorithm algorithm : Algorithm.values()) {
            double fitness = bestFitness.get(algorithm);
            if (fitness < bestFit && fitness != Double.MAX_VALUE) {
                bestFit = fitness;
                bestAlgorithm = algorithm;
            }
        }
        
        if (bestAlgorithm != null) {
            System.out.printf("• Thuật toán tốt nhất: %s (fitness: %.2f)%n", bestAlgorithm, bestFit);
        }
        
        // Kiểm tra memory usage
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (usedMemory * 100.0) / maxMemory;
        
        if (memoryUsagePercent > 80) {
            System.out.println("• Cân nhắc tăng heap size (-Xmx)");
        }
        
        // Parallel efficiency
        int availableCores = runtime.availableProcessors();
        System.out.printf("• Hệ thống có %d cores, đang sử dụng parallel execution%n", availableCores);
        
        // Speedup calculation
        long totalSequentialTime = totalExecutionTime.values().stream()
            .mapToLong(AtomicLong::get).sum();
        long actualTime = endTime - startTime;
        
        if (actualTime > 0 && totalSequentialTime > actualTime) {
            double speedup = (double) totalSequentialTime / actualTime;
            System.out.printf("• Speedup đạt được: %.2fx%n", speedup);
        }
    }
    
    /**
     * Reset tất cả metrics
     */
    public void reset() {
        for (Algorithm algorithm : Algorithm.values()) {
            executionTimes.get(algorithm).clear();
            totalExecutionTime.get(algorithm).set(0);
            iterationCount.get(algorithm).set(0);
            fitnessValues.get(algorithm).clear();
            bestFitness.put(algorithm, Double.MAX_VALUE);
            weightsData.get(algorithm).clear();
        }
        startTime = 0;
        endTime = 0;
    }
    
    /**
     * Lấy best fitness của một thuật toán
     */
    public double getBestFitness(Algorithm algorithm) {
        return bestFitness.getOrDefault(algorithm, Double.MAX_VALUE);
    }
    
    /**
     * Lấy average execution time của một thuật toán
     */
    public double getAverageExecutionTime(Algorithm algorithm) {
        List<Long> times = executionTimes.get(algorithm);
        if (times.isEmpty()) return 0;
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }
    
    /**
     * Tính toán totalWeights cho Excel export
     */
    public double[][][] calculateTotalWeights() {
        double[][][] totalWeights = new double[Algorithm.values().length][4][3];
        
        for (Algorithm algorithm : Algorithm.values()) {
            int algorithmIndex = algorithm.ordinal();
            List<int[]> weights = weightsData.get(algorithm);
            
            if (weights.isEmpty()) {
                // Nếu không có dữ liệu, điền 0
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 3; j++) {
                        totalWeights[algorithmIndex][i][j] = 0.0;
                    }
                }
                continue;
            }
            
            // Tính toán cho từng loại weight (NV, TC, SD, WT)
            for (int weightType = 0; weightType < 4; weightType++) {
                List<Integer> values = new ArrayList<>();
                for (int[] weightArray : weights) {
                    values.add(weightArray[weightType]);
                }
                
                if (!values.isEmpty()) {
                    // Min
                    totalWeights[algorithmIndex][weightType][0] = values.stream().mapToInt(Integer::intValue).min().orElse(0);
                    
                    // Mean
                    double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
                    totalWeights[algorithmIndex][weightType][2] = mean;
                    
                    // Std (Standard Deviation)
                    if (values.size() > 1) {
                        double variance = values.stream()
                            .mapToDouble(v -> Math.pow(v - mean, 2))
                            .average().orElse(0);
                        totalWeights[algorithmIndex][weightType][1] = Math.sqrt(variance);
                    } else {
                        totalWeights[algorithmIndex][weightType][1] = 0.0;
                    }
                }
            }
        }
        
        return totalWeights;
    }
}