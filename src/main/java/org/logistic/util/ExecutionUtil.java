package org.logistic.util;

import org.logistic.Main.Algorithm;
import org.logistic.Main.ExportType;
import org.logistic.algorithm.Optimizer;
import org.logistic.algorithm.aco.AntColonyOptimization;
import org.logistic.algorithm.gwo.GreyWolfOptimizer;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.algorithm.woa.WhaleOptimizationAlgorithm;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.parallel.ParallelExecutionManager;
import org.logistic.parallel.PerformanceMonitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích thực thi các thuật toán tối ưu hóa
 */
public class ExecutionUtil {

    /**
     * Xử lý tất cả các file trong thư mục
     */
    public static void processAllFilesInDirectory(String srcDirectory, String solutionDirectory,
            ReadDataFromFile rdff, FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil, ReadDataFromFile.ProblemType problemType, 
            ExportType exportType, int iterations, boolean parallelEnabled) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC ===");
        System.out.println("Thư mục src: " + srcDirectory);
        System.out.println("Thư mục solution: " + solutionDirectory);

        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(srcDirectory, solutionDirectory, problemType,
                (locations, routes, fileName) -> {
                    try {
                        System.out.println("\n=== XỬ LÝ FILE: " + fileName + " ===");

                        // Tạo giải pháp ban đầu và tập giải pháp
                        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations, parallelEnabled));
                        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution);
                        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil,
                                locations, routes[0].getMaxPayload());

                        // Chạy tất cả các thuật toán tối ưu hóa
                        runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                                routes[0].getMaxPayload(), printUtil, fileName,
                                exportType, iterations, parallelEnabled);

                        System.out.println("=== HOÀN THÀNH XỬ LÝ FILE: " + fileName + " ===\n");

                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý file " + fileName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });

        // Lưu ý: Không lưu Excel ở đây vì sẽ được lưu trong Main.java

        System.out.println("\n=== HOÀN THÀNH XỬ LÝ TẤT CẢ CÁC FILE ===");
    }

    /**
     * Xử lý một file duy nhất
     */
    public static void processSingleFile(String dataLocation, String dataSolution,
            ReadDataFromFile rdff, FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil, ReadDataFromFile.ProblemType problemType, 
            ExportType exportType, int iterations, boolean parallelEnabled) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ FILE ĐƠN ===");

        try {
            // Đọc dữ liệu đầu vào
            System.out.println("Đang đọc dữ liệu từ file: " + dataLocation);
            rdff.readProblemData(dataLocation, problemType);
            Location[] locations = rdff.getLocations();

            if (locations == null || locations.length == 0) {
                System.err.println("Không thể đọc dữ liệu từ file: " + dataLocation);
                return;
            }

            // Đọc giải pháp
            System.out.println("Đang đọc giải pháp từ file: " + dataSolution);
            rdff.readSolution(dataSolution);
            Route[] routes = rdff.getRoutes();

            if (routes == null || routes.length == 0) {
                System.err.println("Không thể đọc giải pháp từ file: " + dataSolution);
                return;
            }

            // Tạo giải pháp ban đầu và tập giải pháp
            Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations, parallelEnabled));
            SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution);
            Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations,
                    routes[0].getMaxPayload());

            // Chạy tất cả các thuật toán tối ưu hóa
            runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                    routes[0].getMaxPayload(), printUtil, null,
                    exportType, iterations, parallelEnabled);

        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý file đơn: " + e.getMessage());
            e.printStackTrace();
        }

        // Lưu ý: Không lưu Excel ở đây vì sẽ được lưu trong Main.java

        System.out.println("\n=== HOÀN THÀNH XỬ LÝ FILE ĐƠN ===");
    }

    /**
     * Tạo đối tượng tối ưu hóa dựa trên thuật toán được chọn
     *
     * @param algorithm Thuật toán được chọn
     * @return Đối tượng tối ưu hóa
     */
    public static Optimizer createOptimizer(Algorithm algorithm) {
        return switch (algorithm) {
            case ACO -> {
                System.out.println("Đang chạy thuật toán Ant Colony Optimization (ACO)...");
                yield new AntColonyOptimization();
            }
            case GWO -> {
                System.out.println("Đang chạy thuật toán Grey Wolf Optimizer (GWO)...");
                yield new GreyWolfOptimizer();
            }
            case WOA -> {
                System.out.println("Đang chạy thuật toán Whale Optimization Algorithm (WOA)...");
                yield new WhaleOptimizationAlgorithm();
            }
            case SHO -> {
                System.out.println("Đang chạy thuật toán Spotted Hyena Optimizer (SHO)...");
                yield new SpottedHyenaOptimizer();
            }
            default -> {
                System.out.println("Thuật toán chưa được định nghĩa");
                yield null;
            }
        };
    }

    /**
     * Chạy các thuật toán tối ưu hóa theo cách tuần tự thông thường
     *
     * @param initialSolutions   Tập giải pháp ban đầu
     * @param fitnessUtil        Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations          Mảng các vị trí
     * @param maxPayload         Trọng tải tối đa
     * @param iterations         Số lần chạy lặp lại cho mỗi thuật toán
     * @return SequentialResults chứa kết quả và thống kê của từng thuật toán
     */
    private static SequentialResults runSequentialOptimizers(Solution[] initialSolutions,
            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
            Location[] locations, int maxPayload, int iterations) {
        
        Map<Algorithm, Solution> bestResults = new HashMap<>();
        Map<Algorithm, Long> executionTimes = new HashMap<>();
        Map<Algorithm, double[][]> algorithmStats = new HashMap<>(); // [weight_type][all_values]
        Algorithm[] algorithms = Algorithm.values();
        
        // Chạy từng thuật toán một cách tuần tự
        for (Algorithm algorithm : algorithms) {
            try {
                System.out.println("\n=== Bắt đầu chạy thuật toán: " + algorithm + " ===");
                
                Optimizer optimizer = createOptimizer(algorithm);
                if (optimizer == null) {
                    System.err.println("Không thể tạo optimizer cho thuật toán: " + algorithm);
                    bestResults.put(algorithm, null);
                    executionTimes.put(algorithm, 0L);
                    continue;
                }
                
                // Thu thập dữ liệu từ tất cả các lần chạy
                Solution bestSolution = null;
                double[][] allWeights = new double[4][iterations]; // [weight_type][iteration]
                long totalExecutionTime = 0;
                
                for (int i = 0; i < iterations; i++) {
                    System.out.println("Lần chạy " + (i + 1) + "/" + iterations + " cho " + algorithm);
                    
                    long startTime = System.currentTimeMillis();
                    Solution currentSolution = optimizer.run(initialSolutions, fitnessUtil, 
                            checkConditionUtil, locations, maxPayload);
                    long endTime = System.currentTimeMillis();
                    
                    totalExecutionTime += (endTime - startTime);
                    
                    if (currentSolution != null) {
                        // Thu thập dữ liệu weights cho lần chạy này
                        int[] weights = fitnessUtil.calculateWeightsFromSolution(currentSolution, locations);
                        for (int j = 0; j < 4; j++) {
                            allWeights[j][i] = weights[j];
                        }
                        
                        // Cập nhật best solution
                        if (bestSolution == null || currentSolution.getFitness() < bestSolution.getFitness()) {
                            bestSolution = currentSolution;
                        }
                    } else {
                        // Nếu không có solution, điền 0
                        for (int j = 0; j < 4; j++) {
                            allWeights[j][i] = 0.0;
                        }
                    }
                }
                
                bestResults.put(algorithm, bestSolution);
                executionTimes.put(algorithm, totalExecutionTime / iterations); // Thời gian trung bình
                algorithmStats.put(algorithm, allWeights);
                
                if (bestSolution != null) {
                    System.out.println("Hoàn thành " + algorithm + " với fitness tốt nhất: " + bestSolution.getFitness());
                } else {
                    System.out.println("Thuật toán " + algorithm + " không trả về kết quả");
                }
                
            } catch (Exception e) {
                System.err.println("Lỗi khi chạy thuật toán " + algorithm + ": " + e.getMessage());
                e.printStackTrace();
                bestResults.put(algorithm, null);
                executionTimes.put(algorithm, 0L);
            }
        }
        
        return new SequentialResults(bestResults, executionTimes, algorithmStats);
    }
    
    /**
     * Class để lưu trữ kết quả của việc chạy tuần tự
     */
    private static class SequentialResults {
        private final Map<Algorithm, Solution> bestResults;
        private final Map<Algorithm, Long> executionTimes;
        private final Map<Algorithm, double[][]> algorithmStats;
        
        public SequentialResults(Map<Algorithm, Solution> bestResults, 
                               Map<Algorithm, Long> executionTimes,
                               Map<Algorithm, double[][]> algorithmStats) {
            this.bestResults = bestResults;
            this.executionTimes = executionTimes;
            this.algorithmStats = algorithmStats;
        }
        
        public Map<Algorithm, Solution> getBestResults() { return bestResults; }
        public Map<Algorithm, Long> getExecutionTimes() { return executionTimes; }
        public Map<Algorithm, double[][]> getAlgorithmStats() { return algorithmStats; }
    }

    /**
     * Chạy tất cả các thuật toán tối ưu hóa và trả về kết quả tốt nhất
     *
     * @param initialSolutions   Tập giải pháp ban đầu
     * @param fitnessUtil        Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations          Mảng các vị trí
     * @param maxPayload         Trọng tải tối đa
     * @param printUtil          Tiện ích in
     * @param fileName           Tên file dữ liệu (nếu có)
     * @param exportType         Loại xuất dữ liệu
     * @param iterations         Số lần chạy lặp lại cho mỗi thuật toán
     * @param parallelEnabled    Có sử dụng xử lý song song hay không
     */
    public static void runAllOptimizers(Solution[] initialSolutions,
            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
            Location[] locations, int maxPayload, PrintUtil printUtil,
            String fileName, ExportType exportType, int iterations,
            boolean parallelEnabled) {
        
        // Thiết lập chế độ parallel cho FitnessUtil
        fitnessUtil.setParallelMode(parallelEnabled);
        
        Map<Algorithm, Solution> results;
        SequentialResults sequentialResults = null;
        
        if (parallelEnabled) {
            // Sử dụng parallel execution manager khi chạy song song
            ParallelExecutionManager parallelManager = ParallelExecutionManager.getInstance();
            ParallelExecutionManager.OptimizerFactory optimizerFactory = ExecutionUtil::createOptimizer;
            
            results = parallelManager.runAllAlgorithms(
                Algorithm.values(),
                initialSolutions,
                fitnessUtil,
                checkConditionUtil,
                locations,
                maxPayload,
                iterations,
                optimizerFactory
            );
        } else {
            // Xử lý tuần tự thông thường
            sequentialResults = runSequentialOptimizers(initialSolutions, fitnessUtil, checkConditionUtil,
                    locations, maxPayload, iterations);
            results = sequentialResults.getBestResults();
        }

        // Tìm thuật toán tốt nhất
        Solution bestSolution = null;
        Algorithm bestAlgorithm = null;
        
        for (Map.Entry<Algorithm, Solution> entry : results.entrySet()) {
            Solution solution = entry.getValue();
            if (solution != null && (bestSolution == null || solution.getFitness() < bestSolution.getFitness())) {
                bestSolution = solution;
                bestAlgorithm = entry.getKey();
            }
        }

        System.out.println("\n=== KẾT QUẢ SO SÁNH CÁC THUẬT TOÁN ===");
        for (Algorithm algorithm : Algorithm.values()) {
            Solution solution = results.get(algorithm);
            if (solution != null) {
                System.out.printf("%-5s: Fitness = %.2f, Số phương tiện = %d\n",
                        algorithm, solution.getFitness(), solution.getRoutes().length);
                
                // In kết quả chi tiết
                printResults(printUtil, solution, algorithm);
            } else {
                System.out.printf("%-5s: Không có kết quả\n", algorithm);
            }
        }

        if (bestAlgorithm != null && bestSolution != null) {
            System.out.println("\n=== THUẬT TOÁN TỐT NHẤT: " + bestAlgorithm +
                    " với Fitness = " + bestSolution.getFitness() + " ===");
        }

        // Xuất dữ liệu ra Excel nếu được yêu cầu
        if (exportType == ExportType.EXCEL || exportType == ExportType.ALL) {
            try {
                if (parallelEnabled) {
                    // Lấy dữ liệu từ performance monitor khi chạy song song
                    PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
                    double[][][] totalWeights = performanceMonitor.calculateTotalWeights();
                    long[] timeAvgs = new long[Algorithm.values().length];
                    
                    for (Algorithm algorithm : Algorithm.values()) {
                        timeAvgs[algorithm.ordinal()] = (long) performanceMonitor.getAverageExecutionTime(algorithm);
                    }
                    
                    ExcelUtil excelUtil = ExcelUtil.getInstance();
                    if (!excelUtil.isWorkbookInitialized()) {
                        excelUtil.initializeExcelWorkbook(fitnessUtil.getFitnessStrategy());
                    }
                    excelUtil.exportResultsToExcel(totalWeights, timeAvgs, fileName, fitnessUtil.getFitnessStrategy());
                    System.out.println("Đã chuẩn bị dữ liệu Excel với dữ liệu được lọc theo cấu hình fitness (chế độ song song)");
                } else {
                    // Tạo dữ liệu cho chế độ tuần tự với cấu trúc phù hợp từ SequentialResults
                    // Cấu trúc: [algorithm][weight_type][statistic]
                    // weight_type: 0=NV, 1=TC, 2=SD, 3=WT
                    // statistic: 0=Min, 1=Std, 2=Mean
                    double[][][] totalWeights = new double[Algorithm.values().length][4][3];
                    long[] timeAvgs = new long[Algorithm.values().length];
                    
                    if (sequentialResults != null) {
                        Map<Algorithm, Long> executionTimes = sequentialResults.getExecutionTimes();
                        Map<Algorithm, double[][]> algorithmStats = sequentialResults.getAlgorithmStats();
                        
                        // Điền dữ liệu từ kết quả thống kê chi tiết
                        for (Algorithm algorithm : Algorithm.values()) {
                            int algIndex = algorithm.ordinal();
                            
                            // Lấy thời gian thực thi trung bình
                            timeAvgs[algIndex] = executionTimes.getOrDefault(algorithm, 0L);
                            
                            double[][] stats = algorithmStats.get(algorithm);
                            if (stats != null) {
                                // Tính toán thống kê cho từng loại weight
                                for (int weightType = 0; weightType < 4; weightType++) {
                                    double[] values = stats[weightType];
                                    
                                    if (values != null && values.length > 0) {
                                        // Tính Min, Mean, Std
                                        double min = Double.MAX_VALUE;
                                        double sum = 0.0;
                                        int validCount = 0;
                                        
                                        for (double value : values) {
                                            if (value >= 0) { // Chỉ tính các giá trị hợp lệ
                                                min = Math.min(min, value);
                                                sum += value;
                                                validCount++;
                                            }
                                        }
                                        
                                        if (validCount > 0) {
                                            double mean = sum / validCount;
                                            
                                            // Tính standard deviation
                                            double variance = 0.0;
                                            for (double value : values) {
                                                if (value >= 0) {
                                                    variance += Math.pow(value - mean, 2);
                                                }
                                            }
                                            double std = validCount > 1 ? Math.sqrt(variance / (validCount - 1)) : 0.0;
                                            
                                            totalWeights[algIndex][weightType][0] = min;  // Min
                                            totalWeights[algIndex][weightType][1] = std;  // Std
                                            totalWeights[algIndex][weightType][2] = mean; // Mean
                                        } else {
                                            // Không có dữ liệu hợp lệ
                                            totalWeights[algIndex][weightType][0] = 0.0; // Min
                                            totalWeights[algIndex][weightType][1] = 0.0; // Std
                                            totalWeights[algIndex][weightType][2] = 0.0; // Mean
                                        }
                                    } else {
                                        // Không có dữ liệu
                                        totalWeights[algIndex][weightType][0] = 0.0; // Min
                                        totalWeights[algIndex][weightType][1] = 0.0; // Std
                                        totalWeights[algIndex][weightType][2] = 0.0; // Mean
                                    }
                                }
                            } else {
                                // Không có thống kê cho thuật toán này
                                for (int i = 0; i < 4; i++) {
                                    totalWeights[algIndex][i][0] = 0.0; // Min
                                    totalWeights[algIndex][i][1] = 0.0; // Std
                                    totalWeights[algIndex][i][2] = 0.0; // Mean
                                }
                            }
                        }
                    } else {
                        // Fallback: không có sequentialResults, điền 0
                        for (Algorithm algorithm : Algorithm.values()) {
                            int algIndex = algorithm.ordinal();
                            timeAvgs[algIndex] = 0;
                            for (int i = 0; i < 4; i++) {
                                totalWeights[algIndex][i][0] = 0.0; // Min
                                totalWeights[algIndex][i][1] = 0.0; // Std
                                totalWeights[algIndex][i][2] = 0.0; // Mean
                            }
                        }
                    }
                    
                    ExcelUtil excelUtil = ExcelUtil.getInstance();
                    if (!excelUtil.isWorkbookInitialized()) {
                        excelUtil.initializeExcelWorkbook(fitnessUtil.getFitnessStrategy());
                    }
                    excelUtil.exportResultsToExcel(totalWeights, timeAvgs, fileName, fitnessUtil.getFitnessStrategy());
                    System.out.println("Đã chuẩn bị dữ liệu Excel với dữ liệu thống kê đầy đủ (chế độ tuần tự)");
                }
                
                // Lưu ý: Không gọi saveExcelWorkbook() ở đây để tránh tạo nhiều file Excel
                // File Excel sẽ được lưu một lần duy nhất ở cuối quá trình xử lý
            } catch (Exception e) {
                System.err.println("Lỗi khi chuẩn bị dữ liệu Excel: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * In kết quả tối ưu
     *
     * @param printUtil Tiện ích in
     * @param solution  Giải pháp tối ưu
     * @param algorithm Thuật toán đã sử dụng
     */
    public static void printResults(PrintUtil printUtil, Solution solution, Algorithm algorithm) {
        System.out.println(algorithm + " completed with fitness: " + solution.getFitness());
        printUtil.printSolution(solution);
        System.out.println("Final optimized solution fitness: " + solution.getFitness());
    }
}