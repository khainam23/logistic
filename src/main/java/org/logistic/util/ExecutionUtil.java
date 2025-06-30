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
            FitnessStrategy strategy, ExportType exportType, int iterations) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC ===");
        System.out.println("Thư mục src: " + srcDirectory);
        System.out.println("Thư mục solution: " + solutionDirectory);

        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(srcDirectory, solutionDirectory, problemType,
                (locations, routes, fileName) -> {
                    try {
                        System.out.println("\n=== XỬ LÝ FILE: " + fileName + " ===");

                        // Tạo giải pháp ban đầu và tập giải pháp
                        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
                        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution);
                        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil,
                                locations, routes[0].getMaxPayload());

                        // Chạy tất cả các thuật toán tối ưu hóa
                        runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                                routes[0].getMaxPayload(), printUtil, fileName,
                                exportType, iterations, strategy);

                        System.out.println("=== HOÀN THÀNH XỬ LÝ FILE: " + fileName + " ===\n");

                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý file " + fileName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });

        System.out.println("\n=== HOÀN THÀNH XỬ LÝ TẤT CẢ CÁC FILE ===");
    }

    /**
     * Xử lý một file duy nhất
     */
    public static void processSingleFile(String dataLocation, String dataSolution,
            ReadDataFromFile rdff, FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil, ReadDataFromFile.ProblemType problemType, 
            FitnessStrategy strategy, ExportType exportType, int iterations) {
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
            Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
            SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution);
            Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations,
                    routes[0].getMaxPayload());

            // Chạy tất cả các thuật toán tối ưu hóa
            runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                    routes[0].getMaxPayload(), printUtil, null,
                    exportType, iterations, strategy);

        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý file đơn: " + e.getMessage());
            e.printStackTrace();
        }

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
     * @param fitnessStrategy    Strategy fitness để xác định dữ liệu cần xuất
     */
    public static void runAllOptimizers(Solution[] initialSolutions,
            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
            Location[] locations, int maxPayload, PrintUtil printUtil,
            String fileName, ExportType exportType, int iterations, FitnessStrategy fitnessStrategy) {
        
        // Khởi tạo parallel execution manager và performance monitor
        ParallelExecutionManager parallelManager = ParallelExecutionManager.getInstance();
        PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
        
        // Tạo optimizer factory
        ParallelExecutionManager.OptimizerFactory optimizerFactory = ExecutionUtil::createOptimizer;
        
        // Chạy tất cả thuật toán song song
        Map<Algorithm, Solution> results = parallelManager.runAllAlgorithmsParallel(
            Algorithm.values(),
            initialSolutions,
            fitnessUtil,
            checkConditionUtil,
            locations,
            maxPayload,
            iterations,
            optimizerFactory
        );

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
                // Lấy dữ liệu từ performance monitor
                double[][][] totalWeights = performanceMonitor.calculateTotalWeights();
                long[] timeAvgs = new long[Algorithm.values().length];
                
                for (Algorithm algorithm : Algorithm.values()) {
                    timeAvgs[algorithm.ordinal()] = (long) performanceMonitor.getAverageExecutionTime(algorithm);
                }
                
                ExcelUtil excelUtil = ExcelUtil.getInstance();
                excelUtil.exportResultsToExcel(totalWeights, timeAvgs, fileName, fitnessStrategy);
                System.out.println("Đã xuất kết quả ra Excel với dữ liệu được lọc theo cấu hình fitness");
            } catch (Exception e) {
                System.err.println("Lỗi khi xuất dữ liệu ra Excel: " + e.getMessage());
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