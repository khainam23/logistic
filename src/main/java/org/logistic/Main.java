package org.logistic;

import org.logistic.algorithm.Optimizer;
import org.logistic.algorithm.aco.AntColonyOptimization;
import org.logistic.algorithm.gwo.GreyWolfOptimizer;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lớp chính của ứng dụng tối ưu hóa hậu cần
 */
public class Main {
    /**
     * Các thuật toán tối ưu hóa được hỗ trợ
     */
    enum Algorithm {SHO, ACO, GWO, SA}
    
    /**
     * Các định dạng xuất dữ liệu được hỗ trợ
     */
    enum ExportType {NONE, EXCEL, CSV, TXT, ALL}
    
    /**
     * Các chế độ chạy
     */
    enum RunMode {SINGLE_FILE, DIRECTORY}

    // Các đường dẫn mặc định
    private static final String DEFAULT_DATA_LOCATION = "data/pdptw/src/lc101.txt";
    private static final String DEFAULT_DATA_SOLUTION = "data/pdptw/solution/lc101.txt";
    private static final String DEFAULT_SRC_DIRECTORY = "data/pdptw/src";
    private static final String DEFAULT_SOLUTION_DIRECTORY = "data/pdptw/solution";
    private static final String EXPORT_DIRECTORY = "exports";
    private static final String RESULTS_SUMMARY_FILE = "results_summary.csv";
    
    // Đường dẫn tuyệt đối đến thư mục resources
    private static String getResourcesPath() {
        try {
            java.net.URL url = Main.class.getClassLoader().getResource("data");
            if (url != null) {
                return new File(url.toURI()).getParentFile().getAbsolutePath();
            }
        } catch (Exception e) {
            System.err.println("Error getting resources path: " + e.getMessage());
        }
        return "src/main/resources";
    }

    /**
     * Phương thức chính của ứng dụng
     * 
     * @param args Tham số dòng lệnh
     */
    public static void main(String[] args) {
        // Phân tích tham số dòng lệnh
        CommandLineParams params = parseCommandLineArgs(args);
        
        // Khởi tạo các tiện ích
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();
        ExportDataUtil exportData = ExportDataUtil.getInstance();

        // Tạo thư mục exports nếu chưa tồn tại
        new File(EXPORT_DIRECTORY).mkdirs();
        
        // Tạo file tổng hợp kết quả nếu chạy theo thư mục
        if (params.runMode == RunMode.DIRECTORY) {
            createResultsSummaryFile();
            
            // Xử lý tất cả các file trong thư mục
            processAllFilesInDirectory(params, rdff, fitnessUtil, printUtil, 
                                      checkConditionUtil, writeLogUtil, exportData);
        } else {
            // Chạy với một file duy nhất
            processSingleFile(params, rdff, fitnessUtil, printUtil, 
                             checkConditionUtil, writeLogUtil, exportData);
        }

        // Đóng các tiện ích
        writeLogUtil.close();
    }

    /**
     * Phân tích tham số dòng lệnh
     * 
     * @param args Mảng tham số dòng lệnh
     * @return Đối tượng chứa các tham số đã phân tích
     */
    private static CommandLineParams parseCommandLineArgs(String[] args) {
        CommandLineParams params = new CommandLineParams();
        
        // Phân tích thuật toán
        if (args.length > 0) {
            try {
                params.algorithm = Algorithm.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Thuật toán không hợp lệ. Sử dụng SHO, ACO, GWO hoặc SA.");
                System.out.println("Sử dụng thuật toán mặc định: " + params.algorithm);
            }
        }
        
        // Phân tích định dạng xuất
        if (args.length > 1) {
            try {
                params.exportType = ExportType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Định dạng xuất không hợp lệ. Sử dụng NONE, EXCEL, CSV, TXT hoặc ALL.");
                System.out.println("Sử dụng định dạng xuất mặc định: " + params.exportType);
            }
        }
        
        // Phân tích chế độ chạy
        if (args.length > 2) {
            if (args[2].equalsIgnoreCase("dir") || args[2].equalsIgnoreCase("directory")) {
                params.runMode = RunMode.DIRECTORY;
                System.out.println("Chế độ chạy: Xử lý tất cả các file trong thư mục");
                
                // Phân tích thư mục src
                if (args.length > 3) {
                    params.srcDirectory = args[3];
                    // Kiểm tra xem có phải đường dẫn tương đối không
                    if (!new File(params.srcDirectory).isAbsolute()) {
                        // Thử tìm trong resources
                        String resourcePath = getResourcesPath() + "/" + params.srcDirectory;
                        if (new File(resourcePath).exists()) {
                            params.srcDirectory = resourcePath;
                        }
                    }
                }
                
                // Phân tích thư mục solution
                if (args.length > 4) {
                    params.solutionDirectory = args[4];
                    // Kiểm tra xem có phải đường dẫn tương đối không
                    if (!new File(params.solutionDirectory).isAbsolute()) {
                        // Thử tìm trong resources
                        String resourcePath = getResourcesPath() + "/" + params.solutionDirectory;
                        if (new File(resourcePath).exists()) {
                            params.solutionDirectory = resourcePath;
                        }
                    }
                }
                
                System.out.println("Thư mục src: " + params.srcDirectory);
                System.out.println("Thư mục solution: " + params.solutionDirectory);
                
                // Kiểm tra xem thư mục có tồn tại không
                File srcDir = new File(params.srcDirectory);
                File solutionDir = new File(params.solutionDirectory);
                
                if (!srcDir.exists() || !srcDir.isDirectory()) {
                    System.err.println("CẢNH BÁO: Thư mục src không tồn tại hoặc không phải là thư mục: " + params.srcDirectory);
                    System.err.println("Đường dẫn tuyệt đối: " + srcDir.getAbsolutePath());
                }
                
                if (!solutionDir.exists() || !solutionDir.isDirectory()) {
                    System.err.println("CẢNH BÁO: Thư mục solution không tồn tại hoặc không phải là thư mục: " + params.solutionDirectory);
                    System.err.println("Đường dẫn tuyệt đối: " + solutionDir.getAbsolutePath());
                }
                
            } else {
                // Chế độ file đơn
                params.dataLocation = args[2];
                System.out.println("Chế độ chạy: Xử lý file đơn");
                System.out.println("File dữ liệu: " + params.dataLocation);
                
                // Phân tích đường dẫn file giải pháp
                if (args.length > 3) {
                    params.dataSolution = args[3];
                    System.out.println("File giải pháp: " + params.dataSolution);
                }
            }
        }
        
        return params;
    }
    
    /**
     * Tạo file tổng hợp kết quả
     */
    private static void createResultsSummaryFile() {
        String filePath = EXPORT_DIRECTORY + "/" + RESULTS_SUMMARY_FILE;
        File file = new File(filePath);
        
        // Nếu file chưa tồn tại, tạo file mới với header
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp,FileName,Algorithm,Fitness,NumberOfVehicles,TotalDistance,TotalServiceTime,TotalWaitingTime\n");
                System.out.println("Đã tạo file tổng hợp kết quả: " + filePath);
            } catch (IOException e) {
                System.err.println("Lỗi khi tạo file tổng hợp kết quả: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ghi kết quả vào file tổng hợp
     * 
     * @param fileName Tên file dữ liệu
     * @param algorithm Thuật toán sử dụng
     * @param solution Giải pháp tối ưu
     * @param locations Mảng các vị trí
     */
    private static void appendResultToSummary(String fileName, Algorithm algorithm, Solution solution, Location[] locations) {
        String filePath = EXPORT_DIRECTORY + "/" + RESULTS_SUMMARY_FILE;
        
        // Tính toán các thông số
        int totalDistance = 0;
        int totalServiceTime = 0;
        int totalWaitingTime = 0;
        
        for (Route route : solution.getRoutes()) {
            int[] indLocs = route.getIndLocations();
            for (int j = 0; j < indLocs.length - 1; j++) {
                Location currLoc = locations[indLocs[j]];
                Location nextLoc = locations[indLocs[j + 1]];
                
                // Tính khoảng cách
                totalDistance += currLoc.distance(nextLoc);
                
                // Tính thời gian phục vụ
                totalServiceTime += nextLoc.totalServiceTime();
                
                // Tính thời gian chờ
                int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - currLoc.distance(nextLoc);
                if (waitingTime > 0) {
                    totalWaitingTime += waitingTime;
                }
            }
        }
        
        // Định dạng timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        
        // Ghi vào file
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(String.format("%s,%s,%s,%.2f,%d,%d,%d,%d\n",
                    timestamp, fileName, algorithm, solution.getFitness(),
                    solution.getRoutes().length, totalDistance, totalServiceTime, totalWaitingTime));
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi kết quả vào file tổng hợp: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý tất cả các file trong thư mục
     */
    private static void processAllFilesInDirectory(CommandLineParams params, ReadDataFromFile rdff,
                                                 FitnessUtil fitnessUtil, PrintUtil printUtil,
                                                 CheckConditionUtil checkConditionUtil, WriteLogUtil writeLogUtil,
                                                 ExportDataUtil exportData) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC ===");
        System.out.println("Thư mục src: " + params.srcDirectory);
        System.out.println("Thư mục solution: " + params.solutionDirectory);
        
        // Xác định loại bài toán dựa trên đường dẫn
        ReadDataFromFile.ProblemType problemType = params.srcDirectory.contains("pdptw") ? 
                ReadDataFromFile.ProblemType.PDPTW : ReadDataFromFile.ProblemType.VRPTW;
        
        System.out.println("Loại bài toán: " + problemType);
        
        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(params.srcDirectory, params.solutionDirectory, problemType,
                (locations, routes, fileName) -> {
                    try {
                        System.out.println("\n=== XỬ LÝ FILE: " + fileName + " ===");
                        
                        // Tạo giải pháp ban đầu và tập giải pháp
                        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
                        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
                        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, 
                                                                          locations, routes[0].getMaxPayload());
                        
                        // Chọn và chạy thuật toán tối ưu hóa
                        Optimizer optimizer = createOptimizer(params.algorithm, sa, writeLogUtil);
                        Solution optimizedSolution = runOptimization(optimizer, initialSolutions, fitnessUtil, 
                                                                   checkConditionUtil, locations, routes[0].getMaxPayload());
                        
                        // In kết quả
                        printResults(printUtil, writeLogUtil, optimizedSolution, params.algorithm);
                        
                        // Ghi kết quả vào file tổng hợp
                        appendResultToSummary(fileName, params.algorithm, optimizedSolution, locations);
                        
                        // Xuất dữ liệu nếu cần
                        if (params.exportType != ExportType.NONE) {
                            // Tạo thư mục con cho file này
                            String filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));
                            String subDir = EXPORT_DIRECTORY + "/" + params.algorithm.toString().toLowerCase() + "/" + filePrefix;
                            new File(subDir).mkdirs();
                            
                            exportResults(exportData, optimizedSolution, locations, params.exportType, 
                                        params.algorithm, subDir, filePrefix);
                        }
                        
                        System.out.println("=== HOÀN THÀNH XỬ LÝ FILE: " + fileName + " ===\n");
                        
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý file " + fileName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
        
        System.out.println("\n=== HOÀN THÀNH XỬ LÝ TẤT CẢ CÁC FILE ===");
        System.out.println("Kết quả tổng hợp được lưu trong file: " + EXPORT_DIRECTORY + "/" + RESULTS_SUMMARY_FILE);
    }
    
    /**
     * Xử lý một file duy nhất
     */
    private static void processSingleFile(CommandLineParams params, ReadDataFromFile rdff,
                                        FitnessUtil fitnessUtil, PrintUtil printUtil,
                                        CheckConditionUtil checkConditionUtil, WriteLogUtil writeLogUtil,
                                        ExportDataUtil exportData) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ FILE ĐƠN ===");
        
        // Đọc dữ liệu đầu vào
        Location[] locations = readInputData(rdff, params.dataLocation);
        if (locations.length == 0) {
            return;
        }
        
        Route[] routes = readSolutionData(rdff, params.dataSolution);
        if (routes.length == 0) {
            return;
        }

        // Tạo giải pháp ban đầu và tập giải pháp
        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations, routes[0].getMaxPayload());
        
        // Chọn và chạy thuật toán tối ưu hóa
        Optimizer optimizer = createOptimizer(params.algorithm, sa, writeLogUtil);
        Solution optimizedSolution = runOptimization(optimizer, initialSolutions, fitnessUtil, 
                                                   checkConditionUtil, locations, routes[0].getMaxPayload());
        
        // In kết quả
        printResults(printUtil, writeLogUtil, optimizedSolution, params.algorithm);
        
        // Xuất dữ liệu nếu cần
        if (params.exportType != ExportType.NONE) {
            exportResults(exportData, optimizedSolution, locations, params.exportType, params.algorithm);
        }
        
        System.out.println("\n=== HOÀN THÀNH XỬ LÝ FILE ĐƠN ===");
    }

    /**
     * Đọc dữ liệu đầu vào từ file
     * 
     * @param rdff Đối tượng đọc dữ liệu
     * @param filePath Đường dẫn file
     * @return Mảng các vị trí
     */
    private static Location[] readInputData(ReadDataFromFile rdff, String filePath) {
        System.out.println("Đang đọc dữ liệu từ file: " + filePath);
        Location[] locations = readData(rdff, filePath, ReadDataFromFile.ProblemType.PDPTW);
        
        if (locations.length == 0) {
            System.err.println("Không thể đọc dữ liệu từ file: " + filePath);
        }
        
        return locations;
    }

    /**
     * Đọc dữ liệu giải pháp từ file
     * 
     * @param rdff Đối tượng đọc dữ liệu
     * @param filePath Đường dẫn file
     * @return Mảng các tuyến đường
     */
    private static Route[] readSolutionData(ReadDataFromFile rdff, String filePath) {
        System.out.println("Đang đọc giải pháp từ file: " + filePath);
        Route[] routes = readSolution(rdff, filePath);
        
        if (routes.length == 0) {
            System.err.println("Không thể đọc giải pháp từ file: " + filePath);
        }
        
        return routes;
    }

    /**
     * Tạo đối tượng tối ưu hóa dựa trên thuật toán được chọn
     * 
     * @param algorithm Thuật toán được chọn
     * @param sa Đối tượng SimulatedAnnealing đã tạo
     * @param writeLogUtil Tiện ích ghi log
     * @return Đối tượng tối ưu hóa
     */
    private static Optimizer createOptimizer(Algorithm algorithm, SimulatedAnnealing sa, WriteLogUtil writeLogUtil) {
        switch (algorithm) {
            case ACO:
                System.out.println("Đang chạy thuật toán Ant Colony Optimization (ACO)...");
                return new AntColonyOptimization(writeLogUtil);

            case GWO:
                System.out.println("Đang chạy thuật toán Grey Wolf Optimizer (GWO)...");
                return new GreyWolfOptimizer(writeLogUtil);
                
            case SA:
                System.out.println("Đang chạy thuật toán Simulated Annealing (SA)...");
                return sa;

            case SHO:
            default:
                System.out.println("Đang chạy thuật toán Spotted Hyena Optimizer (SHO)...");
                return new SpottedHyenaOptimizer(writeLogUtil);
        }
    }

    /**
     * Chạy thuật toán tối ưu hóa
     * 
     * @param optimizer Đối tượng tối ưu hóa
     * @param initialSolutions Tập giải pháp ban đầu
     * @param fitnessUtil Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations Mảng các vị trí
     * @param maxPayload Trọng tải tối đa
     * @return Giải pháp tối ưu
     */
    private static Solution runOptimization(Optimizer optimizer, Solution[] initialSolutions, 
                                          FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, 
                                          Location[] locations, int maxPayload) {
        return optimizer.run(initialSolutions, fitnessUtil, checkConditionUtil, locations, maxPayload);
    }

    /**
     * In kết quả tối ưu
     * 
     * @param printUtil Tiện ích in
     * @param writeLogUtil Tiện ích ghi log
     * @param solution Giải pháp tối ưu
     * @param algorithm Thuật toán đã sử dụng
     */
    private static void printResults(PrintUtil printUtil, WriteLogUtil writeLogUtil, 
                                   Solution solution, Algorithm algorithm) {
        writeLogUtil.info(algorithm + " completed with fitness: " + solution.getFitness());
        printUtil.printSolution(solution);
        writeLogUtil.info("Final optimized solution fitness: " + solution.getFitness());
    }

    /**
     * Xuất kết quả ra file
     * 
     * @param exportData Tiện ích xuất dữ liệu
     * @param solution Giải pháp tối ưu
     * @param locations Mảng các vị trí
     * @param exportType Định dạng xuất
     * @param algorithm Thuật toán đã sử dụng
     */
    private static void exportResults(ExportDataUtil exportData, Solution solution, 
                                    Location[] locations, ExportType exportType, Algorithm algorithm) {
        exportResults(exportData, solution, locations, exportType, algorithm, EXPORT_DIRECTORY, 
                     algorithm.toString().toLowerCase());
    }
    
    /**
     * Xuất kết quả ra file với thư mục và tiền tố tùy chỉnh
     * 
     * @param exportData Tiện ích xuất dữ liệu
     * @param solution Giải pháp tối ưu
     * @param locations Mảng các vị trí
     * @param exportType Định dạng xuất
     * @param algorithm Thuật toán đã sử dụng
     * @param directory Thư mục xuất
     * @param filePrefix Tiền tố tên file
     */
    private static void exportResults(ExportDataUtil exportData, Solution solution, 
                                    Location[] locations, ExportType exportType, Algorithm algorithm,
                                    String directory, String filePrefix) {
        // Tạo thư mục nếu chưa tồn tại
        new File(directory).mkdirs();
        
        System.out.println("Đang xuất dữ liệu...");
        
        // Xuất dữ liệu theo định dạng được chọn
        if (exportType == ExportType.ALL || exportType == ExportType.EXCEL) {
            System.out.println("Xuất ra file Excel...");
            exportData.exportSolution(solution, locations, ExportDataUtil.ExportFormat.EXCEL, 
                    directory + "/" + filePrefix, filePrefix);
        }
        
        if (exportType == ExportType.ALL || exportType == ExportType.CSV) {
            System.out.println("Xuất ra file CSV...");
            exportData.exportSolution(solution, locations, ExportDataUtil.ExportFormat.CSV, 
                    directory + "/" + filePrefix, filePrefix);
        }
        
        if (exportType == ExportType.ALL || exportType == ExportType.TXT) {
            System.out.println("Xuất ra file TXT...");
            exportData.exportSolution(solution, locations, ExportDataUtil.ExportFormat.TXT, 
                    directory + "/" + filePrefix, filePrefix);
        }
        
        // Xuất dữ liệu vị trí và tuyến đường nếu xuất tất cả
        if (exportType == ExportType.ALL) {
            System.out.println("Xuất dữ liệu vị trí...");
            exportData.exportLocations(locations, ExportDataUtil.ExportFormat.EXCEL, 
                    directory + "/locations");
            
            System.out.println("Xuất dữ liệu tuyến đường...");
            exportData.exportRoutes(solution.getRoutes(), locations, ExportDataUtil.ExportFormat.EXCEL, 
                    directory + "/routes");
        }
        
        System.out.println("Xuất dữ liệu hoàn tất. Các file được lưu trong thư mục: " + directory);
    }

    /**
     * Đọc dữ liệu từ file
     * 
     * @param rdff Đối tượng ReadDataFromFile
     * @param filePath Đường dẫn file
     * @param problemType Loại bài toán
     * @return Mảng các địa điểm
     */
    public static Location[] readData(ReadDataFromFile rdff, String filePath, ReadDataFromFile.ProblemType problemType) {
        try {
            // Thử đọc từ classpath
            rdff.dataOfProblem(filePath, problemType);
            
            // Nếu không có dữ liệu, thử đọc từ đường dẫn tuyệt đối
            if (rdff.getLocations() == null || rdff.getLocations().length == 0) {
                rdff.dataOfProblemFromAbsolutePath(filePath, problemType);
            }
            
            return rdff.getLocations();
        } catch (Exception e) {
            System.err.println("Error reading data: " + e.getMessage());
            e.printStackTrace();
            return new Location[0];
        }
    }

    /**
     * Đọc giải pháp từ file
     * 
     * @param rdff Đối tượng ReadDataFromFile
     * @param filePath Đường dẫn file
     * @return Mảng các tuyến đường
     */
    public static Route[] readSolution(ReadDataFromFile rdff, String filePath) {
        try {
            // Thử đọc từ classpath
            rdff.readSolution(filePath);
            
            // Nếu không có dữ liệu, thử đọc từ đường dẫn tuyệt đối
            if (rdff.getRoutes() == null || rdff.getRoutes().length == 0) {
                rdff.readSolutionFromAbsolutePath(filePath);
            }
            
            // Tính khoảng cách cho mỗi tuyến đường
            if (rdff.getRoutes() != null && rdff.getLocations() != null) {
                for (Route route : rdff.getRoutes()) {
                    route.calculateDistance(rdff.getLocations());
                }
            }
            
            return rdff.getRoutes();
        } catch (Exception e) {
            System.err.println("Error reading solution: " + e.getMessage());
            e.printStackTrace();
            return new Route[0];
        }
    }
    
    /**
     * Lớp nội bộ để lưu trữ các tham số dòng lệnh
     */
    private static class CommandLineParams {
        Algorithm algorithm = Algorithm.SHO;
        ExportType exportType = ExportType.CSV;
        RunMode runMode = RunMode.DIRECTORY;
        String dataLocation = DEFAULT_DATA_LOCATION;
        String dataSolution = DEFAULT_DATA_SOLUTION;
        String srcDirectory = DEFAULT_SRC_DIRECTORY;
        String solutionDirectory = DEFAULT_SOLUTION_DIRECTORY;
    }
}