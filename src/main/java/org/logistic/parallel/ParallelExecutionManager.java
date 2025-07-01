package org.logistic.parallel;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.logistic.Main.Algorithm;
import org.logistic.algorithm.Optimizer;
import org.logistic.model.Location;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Quản lý thực thi song song các thuật toán tối ưu hóa
 */
public class ParallelExecutionManager {
    private static ParallelExecutionManager instance;
    
    private final GPUManager gpuManager;
    private final ExecutorService executorService;
    private final int maxThreads;
    
    // Progress tracking
    private final Map<Algorithm, ProgressBar> progressBars = new ConcurrentHashMap<>();
    private final Map<Algorithm, AtomicInteger> completedIterations = new ConcurrentHashMap<>();
    
    private ParallelExecutionManager() {
        this.gpuManager = GPUManager.getInstance();
        this.maxThreads = gpuManager.getOptimalThreadCount();
        
        // Tạo thread pool với số thread tối ưu
        this.executorService = Executors.newFixedThreadPool(maxThreads, r -> {
            Thread t = new Thread(r);
            t.setName("OptimizationWorker-" + t.getId());
            t.setDaemon(false);
            return t;
        });
        
        System.out.println("Parallel Execution Manager khởi tạo với " + maxThreads + " threads");
        if (gpuManager.isGpuAvailable()) {
            System.out.println("GPU acceleration: ENABLED (" + gpuManager.getDeviceName() + ")");
        } else {
            System.out.println("GPU acceleration: DISABLED (sử dụng CPU)");
        }
    }
    
    public static synchronized ParallelExecutionManager getInstance() {
        if (instance == null) {
            instance = new ParallelExecutionManager();
        }
        return instance;
    }
    
    /**
     * Chạy tất cả thuật toán với lựa chọn song song hoặc tuần tự
     */
    public Map<Algorithm, Solution> runAllAlgorithms(
            Algorithm[] algorithms,
            Solution[] initialSolutions,
            FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil,
            Location[] locations,
            int maxPayload,
            int iterations,
            OptimizerFactory optimizerFactory,
            boolean parallel) {
        
        if (parallel) {
            return runAllAlgorithmsParallel(algorithms, initialSolutions, fitnessUtil, 
                checkConditionUtil, locations, maxPayload, iterations, optimizerFactory);
        } else {
            return runAllAlgorithmsSequential(algorithms, initialSolutions, fitnessUtil, 
                checkConditionUtil, locations, maxPayload, iterations, optimizerFactory);
        }
    }

    /**
     * Chạy tất cả thuật toán song song với progress tracking
     */
    public Map<Algorithm, Solution> runAllAlgorithmsParallel(
            Algorithm[] algorithms,
            Solution[] initialSolutions,
            FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil,
            Location[] locations,
            int maxPayload,
            int iterations,
            OptimizerFactory optimizerFactory) {
        
        System.out.println("\n=== BẮT ĐẦU CHẠY SONG SONG " + algorithms.length + " THUẬT TOÁN ===");
        System.out.println("Số iterations cho mỗi thuật toán: " + iterations);
        System.out.println("Số threads: " + maxThreads);
        
        // Khởi tạo progress bars
        initializeProgressBars(algorithms, iterations);
        
        // Tạo CompletableFuture cho mỗi thuật toán
        List<CompletableFuture<AlgorithmResult>> futures = new ArrayList<>();
        
        for (Algorithm algorithm : algorithms) {
            CompletableFuture<AlgorithmResult> future = CompletableFuture.supplyAsync(() -> 
                runSingleAlgorithmWithIterations(
                    algorithm, 
                    initialSolutions, 
                    fitnessUtil, 
                    checkConditionUtil, 
                    locations, 
                    maxPayload, 
                    iterations, 
                    optimizerFactory
                ), executorService);
            
            futures.add(future);
        }
        
        // Đợi tất cả thuật toán hoàn thành
        Map<Algorithm, Solution> results = new HashMap<>();
        
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get(); // Đợi tất cả hoàn thành
            
            // Thu thập kết quả
            for (int i = 0; i < algorithms.length; i++) {
                AlgorithmResult result = futures.get(i).get();
                results.put(result.algorithm, result.bestSolution);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Lỗi khi chạy thuật toán song song: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng progress bars
            closeProgressBars();
        }
        
        System.out.println("\n=== HOÀN THÀNH CHẠY SONG SONG TẤT CẢ THUẬT TOÁN ===");
        return results;
    }

    /**
     * Chạy tất cả thuật toán tuần tự (không song song)
     */
    public Map<Algorithm, Solution> runAllAlgorithmsSequential(
            Algorithm[] algorithms,
            Solution[] initialSolutions,
            FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil,
            Location[] locations,
            int maxPayload,
            int iterations,
            OptimizerFactory optimizerFactory) {
        
        System.out.println("\n=== BẮT ĐẦU CHẠY TUẦN TỰ " + algorithms.length + " THUẬT TOÁN ===");
        System.out.println("Số iterations cho mỗi thuật toán: " + iterations);
        System.out.println("Chế độ: TUẦN TỰ (không song song)");
        
        Map<Algorithm, Solution> results = new HashMap<>();
        
        // Chạy từng thuật toán một cách tuần tự
        for (Algorithm algorithm : algorithms) {
            System.out.println("\n--- Bắt đầu thuật toán " + algorithm + " ---");
            
            Solution bestSolution = null;
            
            // Chạy từng iteration một cách tuần tự
            for (int iter = 0; iter < iterations; iter++) {
                try {
                    // Tạo bản sao của initial solutions
                    Solution[] solutionsCopy = new Solution[initialSolutions.length];
                    for (int i = 0; i < initialSolutions.length; i++) {
                        solutionsCopy[i] = initialSolutions[i].copy();
                    }
                    
                    // Tạo optimizer
                    Optimizer optimizer = optimizerFactory.createOptimizer(algorithm);
                    if (optimizer == null) {
                        System.err.println("Không thể tạo optimizer cho thuật toán " + algorithm);
                        continue;
                    }
                    
                    // Ghi thời gian bắt đầu
                    long startTime = System.currentTimeMillis();
                    
                    // Chạy optimization
                    Solution result = optimizer.run(solutionsCopy, fitnessUtil, 
                        checkConditionUtil, locations, maxPayload);
                    
                    // Ghi thời gian kết thúc và lưu vào performance monitor
                    long executionTime = System.currentTimeMillis() - startTime;
                    PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
                    performanceMonitor.recordIterationTime(algorithm, executionTime);
                    
                    if (result != null) {
                        performanceMonitor.recordFitness(algorithm, result.getFitness());
                        
                        // Ghi lại weights data từ FitnessUtil
                        int[] weights = fitnessUtil.getTempWeights();
                        performanceMonitor.recordWeights(algorithm, weights);
                        
                        System.out.println("[" + algorithm + "] Iteration " + (iter + 1) + 
                            " completed - Fitness: " + result.getFitness() + 
                            ", Time: " + executionTime + "ms");
                        
                        // Cập nhật best solution nếu cần
                        if (bestSolution == null || result.getFitness() < bestSolution.getFitness()) {
                            bestSolution = result.copy();
                        }
                    } else {
                        System.out.println("[" + algorithm + "] Iteration " + (iter + 1) + 
                            " - Không có kết quả");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Lỗi trong iteration " + (iter + 1) + 
                        " của thuật toán " + algorithm + ": " + e.getMessage());
                }
            }
            
            // Lưu kết quả tốt nhất của thuật toán
            results.put(algorithm, bestSolution);
            
            // Ghi lại kết quả cuối cùng vào performance monitor
            if (bestSolution != null) {
                PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
                performanceMonitor.recordAlgorithmResult(algorithm, bestSolution, 0);
                System.out.println("--- Hoàn thành thuật toán " + algorithm + 
                    " với fitness tốt nhất: " + bestSolution.getFitness() + " ---");
            } else {
                System.out.println("--- Thuật toán " + algorithm + " không có kết quả hợp lệ ---");
            }
        }
        
        System.out.println("\n=== HOÀN THÀNH CHẠY TUẦN TỰ TẤT CẢ THUẬT TOÁN ===");
        return results;
    }
    
    /**
     * Chạy một thuật toán với nhiều iterations song song
     */
    private AlgorithmResult runSingleAlgorithmWithIterations(
            Algorithm algorithm,
            Solution[] initialSolutions,
            FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil,
            Location[] locations,
            int maxPayload,
            int iterations,
            OptimizerFactory optimizerFactory) {
        
        System.out.println("Bắt đầu thuật toán " + algorithm + " với " + iterations + " iterations");
        
        AtomicReference<Solution> bestSolution = new AtomicReference<>();
        List<CompletableFuture<Solution>> iterationFutures = new ArrayList<>();
        
        // Tạo futures cho từng iteration
        for (int iter = 0; iter < iterations; iter++) {
            final int iterationNumber = iter + 1;
            
            CompletableFuture<Solution> iterationFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // Tạo bản sao của initial solutions
                    Solution[] solutionsCopy = Arrays.stream(initialSolutions)
                        .map(Solution::copy)
                        .toArray(Solution[]::new);
                    
                    // Tạo optimizer
                    Optimizer optimizer = optimizerFactory.createOptimizer(algorithm);
                    if (optimizer == null) {
                        return null;
                    }
                    
                    // Ghi thời gian bắt đầu
                    long startTime = System.currentTimeMillis();
                    
                    // Chạy optimization
                    Solution result = optimizer.run(solutionsCopy, fitnessUtil, 
                        checkConditionUtil, locations, maxPayload);
                    
                    // Ghi thời gian kết thúc và lưu vào performance monitor
                    long executionTime = System.currentTimeMillis() - startTime;
                    PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
                    performanceMonitor.recordIterationTime(algorithm, executionTime);
                    
                    if (result != null) {
                        System.out.println("[DEBUG] " + algorithm + " optimizer returned result with fitness: " + result.getFitness());
                        performanceMonitor.recordFitness(algorithm, result.getFitness());
                        
                        // Ghi lại weights data từ FitnessUtil
                        int[] weights = fitnessUtil.getTempWeights();
                        performanceMonitor.recordWeights(algorithm, weights);
                        
                        System.out.println("[" + algorithm + "] Iteration " + iterationNumber + 
                            " completed - Fitness: " + result.getFitness() + 
                            ", Time: " + executionTime + "ms");
                    } else {
                        System.out.println("[DEBUG] " + algorithm + " optimizer returned null result");
                    }
                    
                    // Cập nhật progress
                    updateProgress(algorithm);
                    
                    // Cập nhật best solution nếu cần
                    synchronized (bestSolution) {
                        Solution current = bestSolution.get();
                        if (current == null || (result != null && result.getFitness() < current.getFitness())) {
                            bestSolution.set(result.copy());
                        }
                    }
                    
                    return result;
                    
                } catch (Exception e) {
                    System.err.println("Lỗi trong iteration " + iterationNumber + 
                        " của thuật toán " + algorithm + ": " + e.getMessage());
                    return null;
                }
            }, executorService);
            
            iterationFutures.add(iterationFuture);
        }
        
        // Đợi tất cả iterations hoàn thành
        try {
            CompletableFuture.allOf(iterationFutures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Lỗi khi đợi iterations của " + algorithm + ": " + e.getMessage());
        }
        
        // Ghi lại kết quả cuối cùng vào performance monitor
        PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
        Solution finalSolution = bestSolution.get();
        
        if (finalSolution != null) {
            performanceMonitor.recordAlgorithmResult(algorithm, finalSolution, 0); // Thời gian tổng sẽ được tính trong monitor
            System.out.println("Hoàn thành thuật toán " + algorithm + 
                " với fitness tốt nhất: " + finalSolution.getFitness());
        } else {
            System.out.println("Thuật toán " + algorithm + " không có kết quả hợp lệ");
        }
        
        return new AlgorithmResult(algorithm, finalSolution);
    }
    
    /**
     * Khởi tạo progress bars cho các thuật toán
     */
    private void initializeProgressBars(Algorithm[] algorithms, int iterations) {
        for (Algorithm algorithm : algorithms) {
            ProgressBar pb = new ProgressBarBuilder()
                .setTaskName(algorithm.toString())
                .setInitialMax(iterations)
                .setStyle(ProgressBarStyle.ASCII)
                .setUpdateIntervalMillis(100)
                .build();
            
            progressBars.put(algorithm, pb);
            completedIterations.put(algorithm, new AtomicInteger(0));
        }
    }
    
    /**
     * Cập nhật progress cho một thuật toán
     */
    private void updateProgress(Algorithm algorithm) {
        AtomicInteger completed = completedIterations.get(algorithm);
        ProgressBar pb = progressBars.get(algorithm);
        
        if (completed != null && pb != null) {
            int newValue = completed.incrementAndGet();
            pb.stepTo(newValue);
        }
    }
    
    /**
     * Đóng tất cả progress bars
     */
    private void closeProgressBars() {
        for (ProgressBar pb : progressBars.values()) {
            pb.close();
        }
        progressBars.clear();
        completedIterations.clear();
    }
    
    /**
     * Lấy thông tin về khả năng xử lý
     */
    public String getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== THÔNG TIN HỆ THỐNG ===\n");
        info.append("CPU Cores: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        info.append("Max Threads: ").append(maxThreads).append("\n");
        info.append("GPU Available: ").append(gpuManager.isGpuAvailable()).append("\n");
        
        if (gpuManager.isGpuAvailable()) {
            info.append("GPU Device: ").append(gpuManager.getDeviceName()).append("\n");
            info.append("GPU Compute Units: ").append(gpuManager.getMaxComputeUnits()).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * Dọn dẹp tài nguyên
     */
    public void shutdown() {
        System.out.println("Đang dọn dẹp Parallel Execution Manager...");
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        gpuManager.cleanup();
        closeProgressBars();
        
        System.out.println("Parallel Execution Manager đã được dọn dẹp.");
    }
    
    /**
     * Interface để tạo optimizer
     */
    public interface OptimizerFactory {
        Optimizer createOptimizer(Algorithm algorithm);
    }
    
    /**
     * Kết quả của một thuật toán
     */
    private static class AlgorithmResult {
        final Algorithm algorithm;
        final Solution bestSolution;
        
        AlgorithmResult(Algorithm algorithm, Solution bestSolution) {
            this.algorithm = algorithm;
            this.bestSolution = bestSolution;
        }
    }
}