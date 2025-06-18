package org.logistic.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.logistic.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp tiện ích để xử lý các thao tác với Excel
 */
public class ExcelUtil {
    private static final String EXPORT_DIRECTORY = "exports";
    private static final String EXCEL_RESULTS_FILE = "optimization_results.xlsx";

    // Biến instance cho singleton pattern
    private static ExcelUtil instance;

    // Biến để lưu trữ workbook Excel
    private Workbook resultsWorkbook;
    private Sheet resultsSheet;
    private int currentExcelRow = 2;

    /**
     * Constructor riêng tư cho singleton pattern
     */
    private ExcelUtil() {
        // Khởi tạo private để ngăn việc tạo instance từ bên ngoài
    }

    /**
     * Lấy instance của ExcelUtil (singleton pattern)
     *
     * @return Instance của ExcelUtil
     */
    public static synchronized ExcelUtil getInstance() {
        if (instance == null) {
            instance = new ExcelUtil();
        }
        return instance;
    }

    /**
     * Khởi tạo workbook Excel với các tiêu đề cần thiết
     * 
     * @param fitnessStrategy Strategy fitness để xác định các cột cần thiết
     */
    public void initializeExcelWorkbook(FitnessStrategy fitnessStrategy) {
        try {
            resultsWorkbook = new XSSFWorkbook();
            resultsSheet = resultsWorkbook.createSheet("Optimization Results");

            // Định nghĩa các trọng số và thống kê dựa trên strategy
            String[] allWeights = {"NV", "TC", "SD", "WT"};
            boolean[] useFlags = {
                fitnessStrategy.needsVehicleCount(),
                fitnessStrategy.needsDistance(), 
                fitnessStrategy.needsServiceTime(),
                fitnessStrategy.needsWaitingTime()
            };
            
            // Chỉ lấy những weights được sử dụng
            java.util.List<String> activeWeights = new java.util.ArrayList<>();
            for (int i = 0; i < allWeights.length; i++) {
                if (useFlags[i]) {
                    activeWeights.add(allWeights[i]);
                }
            }
            
            String[] weights = activeWeights.toArray(new String[0]);
            String[] partWeights = {"Min", "Std", "Mean"};

            // Tạo dòng tiêu đề 1 (row 0)
            Row headerRow1 = resultsSheet.createRow(0);
            headerRow1.createCell(0).setCellValue("Instance");
            headerRow1.createCell(1).setCellValue("Algorithm");
            
            // Tính vị trí cột Time động
            int timeColIndex = 2 + weights.length * partWeights.length;
            headerRow1.createCell(timeColIndex).setCellValue("Time (ms)");

            // Tạo dòng tiêu đề 2 (row 1)
            Row headerRow2 = resultsSheet.createRow(1);

            // Merge "Instance" và "Algorithm" và "Time"
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, timeColIndex, timeColIndex));

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
    public void saveExcelWorkbook() {
        if (resultsWorkbook == null) {
            return;
        }

        try {
            // Tự động điều chỉnh độ rộng cột - tính động dựa trên số cột thực tế
            int totalColumns = resultsSheet.getRow(0).getLastCellNum();
            for (int i = 0; i < totalColumns; i++) {
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

    /**
     * Xuất kết quả ra file Excel
     *
     * @param totalWeights    Mảng 3 chiều chứa các trọng số
     * @param timeAvgs        Mảng thời gian chạy trung bình
     * @param fileName        Tên file dữ liệu (nếu có)
     * @param fitnessStrategy Strategy fitness để xác định dữ liệu cần xuất
     */
    public void exportResultsToExcel(double[][][] totalWeights, long[] timeAvgs, String fileName, FitnessStrategy fitnessStrategy) {
        if (resultsWorkbook == null || resultsSheet == null) {
            System.err.println("Excel workbook chưa được khởi tạo");
            return;
        }

        // Xác định mapping giữa chỉ số weights và các flag
        boolean[] useFlags = {
            fitnessStrategy.needsVehicleCount(),  // NV
            fitnessStrategy.needsDistance(),      // TC
            fitnessStrategy.needsServiceTime(),   // SD
            fitnessStrategy.needsWaitingTime()    // WT
        };
        
        // Thêm dữ liệu cho từng thuật toán
        for (int algorithmIndex = 0; algorithmIndex < totalWeights.length; algorithmIndex++) {
            Row row = resultsSheet.createRow(currentExcelRow++);
            double[][] partsWeights = totalWeights[algorithmIndex];

            // Tên instance và thuật toán
            row.createCell(0).setCellValue(fileName != null ? fileName : "Single Run");
            row.createCell(1).setCellValue(Main.Algorithm.values()[algorithmIndex].toString());

            // Xuất chỉ những thông số được kích hoạt
            int colIndex = 2;
            for (int weightIndex = 0; weightIndex < useFlags.length; weightIndex++) {
                if (useFlags[weightIndex]) {
                    // Xuất Min, Std, Mean cho weight này
                    row.createCell(colIndex++).setCellValue(partsWeights[weightIndex][0]); // Min
                    row.createCell(colIndex++).setCellValue(partsWeights[weightIndex][1]); // Std
                    row.createCell(colIndex++).setCellValue(partsWeights[weightIndex][2]); // Mean
                }
            }

            // Thời gian chạy (luôn ở cột cuối)
            row.createCell(colIndex).setCellValue(timeAvgs[algorithmIndex]);
        }
    }

    /**
     * Xuất kết quả ra file Excel (phương thức cũ để tương thích ngược)
     *
     * @param totalWeights Mảng 3 chiều chứa các trọng số
     * @param timeAvgs     Mảng thời gian chạy trung bình
     * @param fileName     Tên file dữ liệu (nếu có)
     * @deprecated Sử dụng exportResultsToExcel(totalWeights, timeAvgs, fileName, fitnessStrategy) để có điều khiển tốt hơn
     */
    @Deprecated
    public void exportResultsToExcel(double[][][] totalWeights, long[] timeAvgs, String fileName) {
        // Tạo default strategy với tất cả các thông số enabled để tương thích ngược
        FitnessStrategy defaultStrategy = new FitnessStrategy() {
            @Override
            public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
                return totalDistances + 100.0 * numberVehicle + totalServiceTime + totalWaitingTime;
            }
        };
        exportResultsToExcel(totalWeights, timeAvgs, fileName, defaultStrategy);
    }
    
    /**
     * Khởi tạo workbook Excel với cấu hình mặc định (phương thức cũ để tương thích ngược)
     * @deprecated Sử dụng initializeExcelWorkbook(FitnessStrategy) để có điều khiển tốt hơn
     */
    @Deprecated
    public void initializeExcelWorkbook() {
        // Tạo default strategy với tất cả các thông số enabled để tương thích ngược
        FitnessStrategy defaultStrategy = new FitnessStrategy() {
            @Override
            public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
                return totalDistances + 100.0 * numberVehicle + totalServiceTime + totalWaitingTime;
            }
        };
        initializeExcelWorkbook(defaultStrategy);
    }

    /**
     * Kiểm tra xem workbook Excel đã được khởi tạo chưa
     *
     * @return true nếu workbook đã được khởi tạo, false nếu chưa
     */
    public boolean isWorkbookInitialized() {
        return resultsWorkbook != null;
    }
}
