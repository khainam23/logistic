package org.logistic;

import java.util.Arrays;

import org.logistic.data.ReadDataFromFile;
import org.logistic.parallel.ParallelExecutionManager;
import org.logistic.parallel.PerformanceMonitor;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.DefaultFitnessStrategy;
import org.logistic.util.ExcelUtil;
import org.logistic.util.ExecutionUtil;
import org.logistic.util.FitnessStrategy;
import org.logistic.util.FitnessUtil;
import org.logistic.util.PrintUtil;
import org.logistic.util.RLUtil;

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
        SINGLE_FILE, DIRECTORY, RL
    }

    /**
     * Các định dạng xuất dữ liệu
     */
    public enum ExportType {
        NONE, EXCEL, CSV, TXT, ALL
    }

    /**
     * Lớp nội bộ để lưu trữ các tham số cấu hình
     */
    private static class ConfigParams {
        // Chế độ chạy mặc định là xử lý tất cả các file trong thư mục
        // Thay đổi thành RunMode.RL để chạy chế độ tăng cường (Reinforcement Learning)
        // Thay đổi thành RunMode.SINGLE_FILE để chạy với một file duy nhất
        RunMode runMode = RunMode.RL;
        String dataLocation = "data/vrptw/src/c101.txt";
        String dataSolution = "data/vrptw/solution/c101.txt";
        String srcDirectory = "data/vrptw/src";
        String solutionDirectory = "data/vrptw/solution";
        // Mặc định xuất dữ liệu ra Excel
        ExportType exportType = ExportType.EXCEL;
        // Số lần chạy lặp lại cho mỗi thuật toán (tăng để thấy hiệu quả parallel)
        int iterations = 1;
        // Bật/tắt chế độ song song (mặc định là bật)
        // Đặt thành false để chạy tuần tự (không song song)
        boolean parallelEnabled = false;
        // Số vòng chạy cho RL
        int epoch = 2;
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
        FitnessStrategy strategy = new DefaultFitnessStrategy();
        // FitnessStrategy strategy = FitnessUtil.createStrategyBuilder()
        //         .useDistance(true)
        //         .useVehicleCount(true)
        //         .useServiceTime(false)
        //         .useWaitingTime(false)
        //         .withAlpha(1.0)
        //         .withDelta(100.0)
        //         .build();
        fitnessUtil.setFitnessStrategy(strategy);
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        System.out.println("Chế độ chạy: Tất cả các thuật toán (SHO, ACO, GWO, WOA) sẽ được chạy " + 
                          (config.parallelEnabled ? "song song" : "tuần tự"));
        System.out.println("Số lần chạy lặp lại cho mỗi thuật toán: " + config.iterations);
        System.out.println("Chế độ song song: " + (config.parallelEnabled ? "BẬT" : "TẮT"));

        // Khởi tạo ExcelUtil và file Excel nếu cần
        ExcelUtil excelUtil = ExcelUtil.getInstance();
        if (config.exportType == ExportType.EXCEL || config.exportType == ExportType.ALL) {
            excelUtil.initializeExcelWorkbook(strategy);
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
            ExecutionUtil.processAllFilesInDirectory(config.srcDirectory, config.solutionDirectory,
                    rdff, fitnessUtil, printUtil, checkConditionUtil, problemType, strategy,
                    config.exportType, config.iterations, config.parallelEnabled);
        } else if(config.runMode == RunMode.SINGLE_FILE){
            // Chạy với một file duy nhất
            ExecutionUtil.processSingleFile(config.dataLocation, config.dataSolution,
                    rdff, fitnessUtil, printUtil, checkConditionUtil, problemType, strategy,
                    config.exportType, config.iterations, config.parallelEnabled);
        } else if(config.runMode == RunMode.RL) {
            // Xử lý học tăng cường
            RLUtil.processRL(config.srcDirectory, config.solutionDirectory,
                    rdff, fitnessUtil, printUtil, checkConditionUtil, problemType, strategy,
                    config.exportType, config.iterations, config.parallelEnabled, config.iterations, config.epoch);
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
}