package org.logistic;

import java.util.Arrays;
import java.util.Map;

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
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.ExcelUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.PrintUtil;

/**
 * Lớp chính của ứng dụng tối ưu hóa hậu cần
 */
public class Main {
    /**
     * Các thuật toán tối ưu hóa được hỗ trợ, không cần thêm SA vì chỉ dùng nó để
     * tạo nhiều giải pháp ban đầu
     */
    public enum Algorithm {
        SHO, ACO, GWO, WOA
    }

    /**
     * Các chế độ chạy
     */
    enum RunMode {
        SINGLE_FILE, DIRECTORY
    }

    /**
     * Các định dạng xuất dữ liệu
     */
    enum ExportType {
        NONE, EXCEL, CSV, TXT, ALL
    }

    /**
     * Lớp nội bộ để lưu trữ các tham số cấu hình
     */
    private static class ConfigParams {
        // Chế độ chạy mặc định là xử lý tất cả các file trong thư mục
        RunMode runMode = RunMode.SINGLE_FILE;
        String dataLocation = "data/vrptw/src/c101.txt";
        String dataSolution = "data/vrptw/solution/c101.txt";
        String srcDirectory = "data/vrptw/src";
        String solutionDirectory = "data/vrptw/solution";
        // Mặc định xuất dữ liệu ra Excel
        ExportType exportType = ExportType.EXCEL;
        // Số lần chạy lặp lại cho mỗi thuật toán (tăng để thấy hiệu quả parallel)
        int iterations = 5;
    }

    /**
     * Phương thức chính của ứng dụng
     *
     * @param args Tham số dòng lệnh (không sử dụng)
     */
    public static void main(String[] args) {
        // Tạo cấu hình mặc định
        ConfigParams config = new ConfigParams();
        
        // Khởi tạo parallel execution manager và performance monitor
        ParallelExecutionManager parallelManager = ParallelExecutionManager.getInstance();
        PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
        
        // Bắt đầu monitoring
        performanceMonitor.startMonitoring();
        
        // In thông tin hệ thống
        System.out.println(parallelManager.getSystemInfo());

        // Khởi tạo các tiện ích
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        System.out.println("Chế độ chạy: Tất cả các thuật toán (SHO, ACO, GWO, WOA) sẽ được chạy song song");
        System.out.println("Số lần chạy lặp lại cho mỗi thuật toán: " + config.iterations);

        // Khởi tạo ExcelUtil và file Excel nếu cần
        ExcelUtil excelUtil = ExcelUtil.getInstance();
        if (config.exportType == ExportType.EXCEL || config.exportType == ExportType.ALL) {
            excelUtil.initializeExcelWorkbook();
        }

        // Xác định loại bài toán dựa trên đường dẫn
        ReadDataFromFile.ProblemType problemType = Arrays.stream(ReadDataFromFile.ProblemType.values())
                .filter(type -> config.srcDirectory.toLowerCase().contains(type.name().toLowerCase()))
                .findFirst()
                .orElse(null);

        // Không làm nếu không có chỉ định loại bài toán
        if(problemType == null) {
            System.err.println("Không xác định được loại bài toán từ thư mục: " + config.srcDirectory);
            return;
        }

        System.out.println("Loại bài toán: " + problemType);

        if (config.runMode == RunMode.DIRECTORY) {
            // Xử lý tất cả các file trong thư mục
            processAllFilesInDirectory(config, rdff, fitnessUtil, printUtil, checkConditionUtil, problemType);
        } else {
            // Chạy với một file duy nhất
            processSingleFile(config, rdff, fitnessUtil, printUtil, checkConditionUtil, problemType);
        }

        // Lưu file Excel nếu đã được khởi tạo
        if (config.exportType == ExportType.EXCEL || config.exportType == ExportType.ALL) {
            excelUtil.saveExcelWorkbook();
        }
        
        // Kết thúc monitoring và in báo cáo
        performanceMonitor.stopMonitoring();
        
        // Dọn dẹp tài nguyên
        parallelManager.shutdown();
    }

    /**
     * Xử lý tất cả các file trong thư mục
     */
    private static void processAllFilesInDirectory(ConfigParams config, ReadDataFromFile rdff,
            FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil,
            ReadDataFromFile.ProblemType problemType) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC ===");
        System.out.println("Thư mục src: " + config.srcDirectory);
        System.out.println("Thư mục solution: " + config.solutionDirectory);

        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(config.srcDirectory, config.solutionDirectory, problemType,
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
                                config.exportType, config.iterations);

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
    private static void processSingleFile(ConfigParams config, ReadDataFromFile rdff,
            FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil,
            ReadDataFromFile.ProblemType problemType) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ FILE ĐƠN ===");

        try {
            // Đọc dữ liệu đầu vào
            System.out.println("Đang đọc dữ liệu từ file: " + config.dataLocation);
            rdff.readProblemData(config.dataLocation, problemType);
            Location[] locations = rdff.getLocations();

            if (locations == null || locations.length == 0) {
                System.err.println("Không thể đọc dữ liệu từ file: " + config.dataLocation);
                return;
            }

            // Đọc giải pháp
            System.out.println("Đang đọc giải pháp từ file: " + config.dataSolution);
            rdff.readSolution(config.dataSolution);
            Route[] routes = rdff.getRoutes();

            if (routes == null || routes.length == 0) {
                System.err.println("Không thể đọc giải pháp từ file: " + config.dataSolution);
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
                    config.exportType, config.iterations);

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
    private static Optimizer createOptimizer(Algorithm algorithm) {
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
     */
    private static void runAllOptimizers(Solution[] initialSolutions,
            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
            Location[] locations, int maxPayload, PrintUtil printUtil,
            String fileName, ExportType exportType, int iterations) {
        
        // Khởi tạo parallel execution manager và performance monitor
        ParallelExecutionManager parallelManager = ParallelExecutionManager.getInstance();
        PerformanceMonitor performanceMonitor = PerformanceMonitor.getInstance();
        
        // Tạo optimizer factory
        ParallelExecutionManager.OptimizerFactory optimizerFactory = algorithm -> createOptimizer(algorithm);
        
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
                excelUtil.exportResultsToExcel(totalWeights, timeAvgs, fileName);
                System.out.println("Đã xuất kết quả ra Excel với dữ liệu thực tế");
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
    private static void printResults(PrintUtil printUtil, Solution solution, Algorithm algorithm) {
        System.out.println(algorithm + " completed with fitness: " + solution.getFitness());
        printUtil.printSolution(solution);
        System.out.println("Final optimized solution fitness: " + solution.getFitness());
    }
}