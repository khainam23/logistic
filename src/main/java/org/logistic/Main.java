package org.logistic;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.logistic.algorithm.Optimizer;
import org.logistic.algorithm.aco.AntColonyOptimization;
import org.logistic.algorithm.gwo.GreyWolfOptimizer;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.config.SpringContextInitializer;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.PrintUtil;
import org.logistic.util.WriteLogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lớp chính của ứng dụng tối ưu hóa hậu cần
 */
public class Main {
    /**
     * Các thuật toán tối ưu hóa được hỗ trợ, không cần thêm SA vì chỉ dùng nó để tạo nhiều giải pháp ban đầu
     */
    enum Algorithm {SHO, ACO, GWO}

    /**
     * Các chế độ chạy
     */
    enum RunMode {SINGLE_FILE, DIRECTORY}

    /**
     * Các định dạng xuất dữ liệu
     */
    enum ExportType {NONE, EXCEL, CSV, TXT, ALL}

    private static final String DEFAULT_SRC_DIRECTORY = "data/pdptw/src";
    private static final String DEFAULT_SOLUTION_DIRECTORY = "data/pdptw/solution";
    private static final String EXPORT_DIRECTORY = "exports";
    private static final String RESULTS_SUMMARY_FILE = "results_summary.txt";
    private static final String EXCEL_RESULTS_FILE = "optimization_results.xlsx";

    // Biến static để lưu trữ workbook Excel
    private static Workbook resultsWorkbook;
    private static Sheet resultsSheet;
    private static int currentExcelRow = 2; // Bắt đầu từ dòng 3 (sau 2 dòng header)

    /**
     * Lớp nội bộ để lưu trữ các tham số cấu hình
     */
    private static class ConfigParams {
        // Chế độ chạy mặc định là xử lý tất cả các file trong thư mục
        RunMode runMode = RunMode.DIRECTORY;
        String dataLocation = "data/pdptw/src/lc101.txt";
        String dataSolution = "data/pdptw/solution/lc101.txt";
        String srcDirectory = DEFAULT_SRC_DIRECTORY;
        String solutionDirectory = DEFAULT_SOLUTION_DIRECTORY;
        // Mặc định xuất dữ liệu ra Excel
        ExportType exportType = ExportType.EXCEL;
        // Số lần chạy lặp lại cho mỗi thuật toán
        int iterations = 2;
    }

    /**
     * Phương thức chính của ứng dụng
     *
     * @param args Tham số dòng lệnh (không sử dụng)
     */
    public static void main(String[] args) {
        // Khởi tạo Spring Context cho AOP
        SpringContextInitializer.initialize();

        // Tạo cấu hình mặc định
        ConfigParams config = new ConfigParams();

        // Khởi tạo các tiện ích
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        System.out.println("Chế độ chạy: Tất cả các thuật toán (SHO, ACO, GWO, SA) sẽ được chạy song song");
        System.out.println("Số lần chạy lặp lại cho mỗi thuật toán: " + config.iterations);

        // Kiểm tra thư mục
        checkDirectories(config);

        // Khởi tạo file Excel nếu cần
        if (config.exportType == ExportType.EXCEL || config.exportType == ExportType.ALL) {
            initializeExcelWorkbook();
        }

        if (config.runMode == RunMode.DIRECTORY) {
            // Xử lý tất cả các file trong thư mục
            processAllFilesInDirectory(config, rdff, fitnessUtil, printUtil,
                    checkConditionUtil, writeLogUtil);
        } else {
            // Chạy với một file duy nhất
            processSingleFile(config, rdff, fitnessUtil, printUtil,
                    checkConditionUtil, writeLogUtil);
        }

        // Lưu file Excel nếu đã được khởi tạo
        if (config.exportType == ExportType.EXCEL || config.exportType == ExportType.ALL) {
            saveExcelWorkbook();
        }

        // Đóng các tiện ích
        writeLogUtil.close();

        // Đóng Spring Context
        SpringContextInitializer.close();
    }

    /**
     * Khởi tạo workbook Excel với các tiêu đề cần thiết
     */
    private static void initializeExcelWorkbook() {
        try {
            resultsWorkbook = new XSSFWorkbook();
            resultsSheet = resultsWorkbook.createSheet("Optimization Results");

            // Định nghĩa các trọng số và thống kê
            String[] weights = {"NV", "TC", "SD", "WT"};
            String[] partWeights = {"Min", "Std", "Mean"};

            // Tạo dòng tiêu đề 1 (row 0)
            Row headerRow1 = resultsSheet.createRow(0);
            headerRow1.createCell(0).setCellValue("Instance");
            headerRow1.createCell(1).setCellValue("Algorithm");

            // Tạo dòng tiêu đề 2 (row 1)
            Row headerRow2 = resultsSheet.createRow(1);

            // Merge "Instance" và "Algorithm"
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

            // Thêm các cột dữ liệu cho từng trọng số và thống kê
            for (int w = 0; w < weights.length; w++) {
                int baseCol = 2 + w * partWeights.length;

                // Gộp 3 ô cho mỗi trọng số (VD: NV -> Min, Std, Mean)
                resultsSheet.addMergedRegion(new CellRangeAddress(0, 0, baseCol, baseCol + partWeights.length - 1));
                headerRow1.createCell(baseCol).setCellValue(weights[w]);

                for (int j = 0; j < partWeights.length; j++) {
                    headerRow2.createCell(baseCol + j).setCellValue(partWeights[j]);
                }
            }

            // Tạo thư mục exports nếu chưa tồn tại
            new File(EXPORT_DIRECTORY).mkdirs();

            System.out.println("Đã khởi tạo file Excel để lưu kết quả");
        } catch (Exception e) {
            System.err.println("Lỗi khi khởi tạo file Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lưu workbook Excel vào file
     */
    private static void saveExcelWorkbook() {
        if (resultsWorkbook == null) {
            return;
        }

        try {
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < 14; i++) { // 2 cột đầu + 4 trọng số * 3 thống kê
                resultsSheet.autoSizeColumn(i);
            }

            // Tạo tên file với timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String excelFilePath = EXPORT_DIRECTORY + "/" + timestamp + "_" + EXCEL_RESULTS_FILE;

            // Lưu workbook vào file
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                resultsWorkbook.write(fileOut);
            }

            resultsWorkbook.close();
            System.out.println("Đã lưu kết quả vào file Excel: " + excelFilePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Đường dẫn tuyệt đối đến thư mục resources
    private static String getResourcesPath() {
        try {
            URL url = Main.class.getClassLoader().getResource("data");
            if (url != null) {
                return new File(url.toURI()).getParentFile().getAbsolutePath();
            }
        } catch (Exception e) {
            System.err.println("Error getting resources path: " + e.getMessage());
        }
        return "src/main/resources";
    }

    /**
     * Kiểm tra các thư mục cấu hình
     *
     * @param config Đối tượng cấu hình
     */
    private static void checkDirectories(ConfigParams config) {
        // Kiểm tra thư mục src
        File srcDir = new File(config.srcDirectory);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            System.err.println("CẢNH BÁO: Thư mục src không tồn tại hoặc không phải là thư mục: " + config.srcDirectory);
            System.err.println("Đường dẫn tuyệt đối: " + srcDir.getAbsolutePath());

            // Thử tìm trong resources
            String resourcePath = getResourcesPath() + "/" + config.srcDirectory;
            if (new File(resourcePath).exists()) {
                config.srcDirectory = resourcePath;
                System.out.println("Đã tìm thấy thư mục src trong resources: " + config.srcDirectory);
            }
        }

        // Kiểm tra thư mục solution
        File solutionDir = new File(config.solutionDirectory);
        if (!solutionDir.exists() || !solutionDir.isDirectory()) {
            System.err.println("CẢNH BÁO: Thư mục solution không tồn tại hoặc không phải là thư mục: " + config.solutionDirectory);
            System.err.println("Đường dẫn tuyệt đối: " + solutionDir.getAbsolutePath());

            // Thử tìm trong resources
            String resourcePath = getResourcesPath() + "/" + config.solutionDirectory;
            if (new File(resourcePath).exists()) {
                config.solutionDirectory = resourcePath;
                System.out.println("Đã tìm thấy thư mục solution trong resources: " + config.solutionDirectory);
            }
        }

        // Tạo thư mục exports nếu chưa tồn tại
        new File(EXPORT_DIRECTORY).mkdirs();
    }

    /**
     * Xử lý tất cả các file trong thư mục
     */
    private static void processAllFilesInDirectory(ConfigParams config, ReadDataFromFile rdff,
                                                   FitnessUtil fitnessUtil, PrintUtil printUtil,
                                                   CheckConditionUtil checkConditionUtil, WriteLogUtil writeLogUtil) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TẤT CẢ CÁC FILE TRONG THƯ MỤC ===");
        System.out.println("Thư mục src: " + config.srcDirectory);
        System.out.println("Thư mục solution: " + config.solutionDirectory);

        // Xác định loại bài toán dựa trên đường dẫn
        ReadDataFromFile.ProblemType problemType = config.srcDirectory.contains("pdptw") ?
                ReadDataFromFile.ProblemType.PDPTW : ReadDataFromFile.ProblemType.VRPTW;

        System.out.println("Loại bài toán: " + problemType);

        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(config.srcDirectory, config.solutionDirectory, problemType,
                (locations, routes, fileName) -> {
                    try {
                        System.out.println("\n=== XỬ LÝ FILE: " + fileName + " ===");

                        // Tạo giải pháp ban đầu và tập giải pháp
                        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
                        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
                        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil,
                                locations, routes[0].getMaxPayload());

                        // Chạy tất cả các thuật toán tối ưu hóa
                        runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                                routes[0].getMaxPayload(), writeLogUtil, printUtil, fileName,
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
                                          CheckConditionUtil checkConditionUtil, WriteLogUtil writeLogUtil) {
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ FILE ĐƠN ===");

        // Đọc dữ liệu đầu vào
        Location[] locations = readInputData(rdff, config.dataLocation);
        if (locations.length == 0) {
            return;
        }

        Route[] routes = readSolutionData(rdff, config.dataSolution);
        if (routes.length == 0) {
            return;
        }

        // Tạo giải pháp ban đầu và tập giải pháp
        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));
        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations, routes[0].getMaxPayload());

        // Chạy tất cả các thuật toán tối ưu hóa
        runAllOptimizers(initialSolutions, fitnessUtil, checkConditionUtil, locations,
                routes[0].getMaxPayload(), writeLogUtil, printUtil, null,
                config.exportType, config.iterations);

        System.out.println("\n=== HOÀN THÀNH XỬ LÝ FILE ĐƠN ===");
    }

    /**
     * Đọc dữ liệu đầu vào từ file
     *
     * @param rdff     Đối tượng đọc dữ liệu
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
     * @param rdff     Đối tượng đọc dữ liệu
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
     * @param algorithm    Thuật toán được chọn
     * @param writeLogUtil Tiện ích ghi log
     * @return Đối tượng tối ưu hóa
     */
    private static Optimizer createOptimizer(Algorithm algorithm, WriteLogUtil writeLogUtil) {
        return switch (algorithm) {
            case ACO -> {
                System.out.println("Đang chạy thuật toán Ant Colony Optimization (ACO)...");
                yield new AntColonyOptimization(writeLogUtil);
            }
            case GWO -> {
                System.out.println("Đang chạy thuật toán Grey Wolf Optimizer (GWO)...");
                yield new GreyWolfOptimizer(writeLogUtil);
            }
            default -> {
                System.out.println("Đang chạy thuật toán Spotted Hyena Optimizer (SHO)...");
                yield new SpottedHyenaOptimizer(writeLogUtil);
            }
        };
    }

    /**
     * Chạy thuật toán tối ưu hóa
     *
     * @param optimizer          Đối tượng tối ưu hóa
     * @param initialSolutions   Tập giải pháp ban đầu
     * @param fitnessUtil        Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations          Mảng các vị trí
     * @param maxPayload         Trọng tải tối đa
     * @return Giải pháp tối ưu
     */
    private static Solution runOptimization(Optimizer optimizer, Solution[] initialSolutions,
                                            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
                                            Location[] locations, int maxPayload) {
        return optimizer.run(initialSolutions, fitnessUtil, checkConditionUtil, locations, maxPayload);
    }

    /**
     * Chạy tất cả các thuật toán tối ưu hóa và trả về kết quả tốt nhất
     *
     * @param initialSolutions   Tập giải pháp ban đầu
     * @param fitnessUtil        Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations          Mảng các vị trí
     * @param maxPayload         Trọng tải tối đa
     * @param writeLogUtil       Tiện ích ghi log
     * @param printUtil          Tiện ích in
     * @param fileName           Tên file dữ liệu (nếu có)
     * @param exportType         Loại xuất dữ liệu
     * @param iterations         Số lần chạy lặp lại cho mỗi thuật toán
     */
    private static void runAllOptimizers(Solution[] initialSolutions,
                                         FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
                                         Location[] locations, int maxPayload, WriteLogUtil writeLogUtil,
                                         PrintUtil printUtil, String fileName,
                                         ExportType exportType,
                                         int iterations) {
        Solution[] results = new Solution[Algorithm.values().length];

        System.out.println("\n=== CHẠY TẤT CẢ CÁC THUẬT TOÁN TỐI ƯU HÓA ===");
        System.out.println("Số lần chạy lặp lại cho mỗi thuật toán: " + iterations);
        double[][][] totalWeights = new double[Algorithm.values().length][4][3];

        // Tạo và chạy từng thuật toán
        for (Algorithm algorithm : Algorithm.values()) {
            double[][] partsWeights = new double[4][3]; // Lưu trữ tính toán giá trị min, std, mean của 4 trọng số
            System.out.println("\n--- THUẬT TOÁN: " + algorithm + " ---");

            // Biến lưu giải pháp tốt nhất sau nhiều lần chạy
            Solution bestSolutionForAlgorithm = null;

            // Chạy thuật toán nhiều lần theo số lần lặp lại được chỉ định
            for (int iter = 1; iter <= iterations; iter++) {
                System.out.println("Lần chạy thứ " + iter + "/" + iterations);

                // Tạo bản sao của tập giải pháp ban đầu để mỗi thuật toán có điểm bắt đầu giống nhau
                Solution[] initialSolutionsCopy = new Solution[initialSolutions.length];
                for (int i = 0; i < initialSolutions.length; i++) {
                    initialSolutionsCopy[i] = initialSolutions[i].copy();
                }

                // Tạo và chạy thuật toán
                Optimizer optimizer = createOptimizer(algorithm, writeLogUtil);
                Solution optimizedSolution = runOptimization(optimizer, initialSolutionsCopy, fitnessUtil,
                        checkConditionUtil, locations, maxPayload);

                // Cập nhật giải pháp tốt nhất nếu cần
                if (bestSolutionForAlgorithm == null ||
                        optimizedSolution.getFitness() < bestSolutionForAlgorithm.getFitness()) {
                    bestSolutionForAlgorithm = optimizedSolution.copy();
                    System.out.println("Cập nhật giải pháp tốt nhất với fitness = " + bestSolutionForAlgorithm.getFitness());
                }

                // Cập nhật trọng số trung bình
                for (int i = 0; i < partsWeights.length; i++) {
                    int[] tempWeights = fitnessUtil.getTempWeights();
                    if(partsWeights[i][0] > tempWeights[i] || partsWeights[i][0] == 0) { // Số lượng nhỏ nhất
                        partsWeights[i][0] = tempWeights[i];
                    }

                    // Tính phương sai, tạm thời tổng giá trị lại
                    partsWeights[i][1] += Math.pow(tempWeights[i], 2);

                    // Tính trung bình, tạm thời tổng giá trị lại
                    partsWeights[i][2] += tempWeights[i];
                }

                // In kết quả của lần chạy hiện tại
                System.out.println("Kết quả lần chạy " + iter + ": Fitness = " + optimizedSolution.getFitness());
            }

            // Cập nhật trọng số trung bình
            for (int i = 0; i < partsWeights.length; i++) {
                double totalX = partsWeights[i][2];

                // Tính trung bình
                partsWeights[i][2] = totalX / iterations;

                // Tính phương sai
                partsWeights[i][1] = (partsWeights[i][1] - 2 *  partsWeights[i][2] * totalX + iterations * Math.pow(partsWeights[i][2], 2)) / iterations; // Triển khai (a - b)^2 = a^2 + b^2 - 2ab
            }

            // Lưu các trọng số
            totalWeights[algorithm.ordinal()] = partsWeights;

            // Lưu kết quả tốt nhất sau nhiều lần chạy
            results[algorithm.ordinal()] = bestSolutionForAlgorithm;

            // In kết quả tốt nhất
            System.out.println("\nKết quả tốt nhất sau " + iterations + " lần chạy:");
            printResults(printUtil, writeLogUtil, bestSolutionForAlgorithm, algorithm);
        }

        // Tìm thuật toán tốt nhất
        Solution bestSolution = results[0];
        Algorithm bestAlgorithm = Algorithm.values()[0];

        for (int i = 1; i < results.length; i++) {
            if (results[i].getFitness() < bestSolution.getFitness()) {
                bestSolution = results[i];
                bestAlgorithm = Algorithm.values()[i];
            }
        }

        System.out.println("\n=== KẾT QUẢ SO SÁNH CÁC THUẬT TOÁN ===");
        for (Algorithm algorithm : Algorithm.values()) {
            System.out.printf("%-5s: Fitness = %.2f, Số phương tiện = %d\n",
                    algorithm, results[algorithm.ordinal()].getFitness(),
                    results[algorithm.ordinal()].getRoutes().length);
        }

        System.out.println("\n=== THUẬT TOÁN TỐT NHẤT: " + bestAlgorithm +
                " với Fitness = " + bestSolution.getFitness() + " ===");

        // Xuất dữ liệu ra Excel nếu được yêu cầu
        if (exportType == ExportType.EXCEL || exportType == ExportType.ALL) {
            try {
                // Tạo tên file Excel
                String excelFileName = EXPORT_DIRECTORY + "/results_";
                if (fileName != null) {
                    excelFileName += fileName.replace(".txt", "");
                } else {
                    excelFileName += "single_run";
                }
                excelFileName += "_" + System.currentTimeMillis() + ".xlsx";

                // Xuất dữ liệu ra Excel
                exportResultsToExcel(locations, totalWeights, fileName);
                System.out.println("Đã xuất kết quả ra Excel: " + excelFileName);
            } catch (Exception e) {
                System.err.println("Lỗi khi xuất dữ liệu ra Excel: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Ghi kết quả của thuật toán tốt nhất vào file tổng hợp
        if (fileName != null) {
            // Xóa các kết quả cũ trước khi ghi kết quả mới với thông tin thuật toán tốt nhất
            String filePath = EXPORT_DIRECTORY + "/" + RESULTS_SUMMARY_FILE;
            String tempFilePath = EXPORT_DIRECTORY + "/temp_" + RESULTS_SUMMARY_FILE;

            try {
                // Đọc file hiện tại và tạo file tạm thời
                Path inputPath = Paths.get(filePath);

                // Đọc tất cả các dòng từ file gốc
                List<String> lines = Files.readAllLines(inputPath);

                // Ghi lại file, bỏ qua các dòng liên quan đến file hiện tại
                try (FileWriter writer = new FileWriter(tempFilePath)) {
                    // Ghi header
                    writer.write(lines.get(0) + "\n");

                    // Ghi các dòng không liên quan đến file hiện tại
                    for (int i = 1; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (!line.contains("," + fileName + ",")) {
                            writer.write(line + "\n");
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Lỗi khi cập nhật file tổng hợp: " + e.getMessage());
            }
        }

    }

    /**
     * In kết quả tối ưu
     *
     * @param printUtil    Tiện ích in
     * @param writeLogUtil Tiện ích ghi log
     * @param solution     Giải pháp tối ưu
     * @param algorithm    Thuật toán đã sử dụng
     */
    private static void printResults(PrintUtil printUtil, WriteLogUtil writeLogUtil,
                                     Solution solution, Algorithm algorithm) {
        writeLogUtil.info(algorithm + " completed with fitness: " + solution.getFitness());
        printUtil.printSolution(solution);
        writeLogUtil.info("Final optimized solution fitness: " + solution.getFitness());
    }

    /**
     * Đọc dữ liệu từ file
     *
     * @param rdff        Đối tượng ReadDataFromFile
     * @param filePath    Đường dẫn file
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
     * @param rdff     Đối tượng ReadDataFromFile
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
     * Xuất kết quả ra file Excel
     *
     * @param fileName Tên file dữ liệu (nếu có)
     * @throws IOException Nếu có lỗi khi xuất dữ liệu
     */
    private static void exportResultsToExcel(Location[] locations, double[][][] totalWeights, String fileName) {
        if (resultsWorkbook == null || resultsSheet == null) {
            System.err.println("Excel workbook chưa được khởi tạo");
            return;
        }

        // Thêm dữ liệu cho từng thuật toán
        for (Algorithm algorithm : Algorithm.values()) {
            Row row = resultsSheet.createRow(currentExcelRow++);
            double[][] partsWeights = totalWeights[algorithm.ordinal()];

            // Tên instance và thuật toán
            row.createCell(0).setCellValue(fileName != null ? fileName : "Single Run");
            row.createCell(1).setCellValue(algorithm.toString());

            // Tính các giá trị thống kê
            // 1. NV (Number of Vehicles)
            row.createCell(2).setCellValue(partsWeights[0][0]); // Min
            row.createCell(3).setCellValue(partsWeights[0][1]); // Std (không áp dụng cho số lượng xe)
            row.createCell(4).setCellValue(partsWeights[0][2]); // Mean

            // 2. TC (Total Distance)
            row.createCell(5).setCellValue(partsWeights[1][0]); // Min
            row.createCell(6).setCellValue(partsWeights[1][1]); // Std (không áp dụng cho fitness đơn lẻ)
            row.createCell(7).setCellValue(partsWeights[1][2]); // Mean

            // 3. SD (Service Time)
            row.createCell(8).setCellValue(partsWeights[2][0]); // Min
            row.createCell(9).setCellValue(partsWeights[2][1]); // Std
            row.createCell(10).setCellValue(partsWeights[2][2]); // Mean

            // 4. WT (Waiting Time)
            row.createCell(11).setCellValue(partsWeights[3][0]); // Min
            row.createCell(12).setCellValue(partsWeights[3][1]); // Std
            row.createCell(13).setCellValue(partsWeights[3][2]); // Mean
        }
    }
}