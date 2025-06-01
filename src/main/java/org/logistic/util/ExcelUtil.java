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
     */
    public void initializeExcelWorkbook() {
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
            headerRow1.createCell(14).setCellValue("Time (ms)");

            // Tạo dòng tiêu đề 2 (row 1)
            Row headerRow2 = resultsSheet.createRow(1);

            // Merge "Instance" và "Algorithm"
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            resultsSheet.addMergedRegion(new CellRangeAddress(0, 1, 14, 14));

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
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < 15; i++) { // 2 cột đầu + 4 trọng số * 3 thống kê + 1 cột thời gian
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
     * @param totalWeights Mảng 3 chiều chứa các trọng số
     * @param timeAvgs     Mảng thời gian chạy trung bình
     * @param fileName     Tên file dữ liệu (nếu có)
     */
    public void exportResultsToExcel(double[][][] totalWeights, long[] timeAvgs, String fileName) {
        if (resultsWorkbook == null || resultsSheet == null) {
            System.err.println("Excel workbook chưa được khởi tạo");
            return;
        }

        // Thêm dữ liệu cho từng thuật toán
        for (int algorithmIndex = 0; algorithmIndex < totalWeights.length; algorithmIndex++) {
            Row row = resultsSheet.createRow(currentExcelRow++);
            double[][] partsWeights = totalWeights[algorithmIndex];

            // Tên instance và thuật toán
            row.createCell(0).setCellValue(fileName != null ? fileName : "Single Run");
            row.createCell(1).setCellValue(Main.Algorithm.values()[algorithmIndex].toString());

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

            // Thời gian chạy
            row.createCell(14).setCellValue(timeAvgs[algorithmIndex]);
        }
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
