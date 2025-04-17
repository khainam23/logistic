package org.logistic.util;

import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích để xuất dữ liệu ra các định dạng khác nhau như Excel, TXT, CSV
 */
public class ExportDataUtil {
    private static ExportDataUtil instance;

    public static ExportDataUtil getInstance() {
        if (instance == null) {
            instance = new ExportDataUtil();
        }
        return instance;
    }

    private ExportDataUtil() {}

    public void exportSolutionToExcel(String path) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Solution");

        String[] weights = {"NV", "TC", "SD", "WT"};
        String[] partWeights = {"Min", "Std", "Mean"};

        // Tạo dòng tiêu đề 1 (row 0)
        Row headerRow1 = sheet.createRow(0);
        headerRow1.createCell(0).setCellValue("Instance");
        headerRow1.createCell(1).setCellValue("Aglo.");

        // Tạo dòng tiêu đề 2 (row 1)
        Row headerRow2 = sheet.createRow(1);

        // Merge "Instance" và "Aglo."
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

        // Thêm các cột dữ liệu cho từng trọng số và thống kê
        for (int w = 0; w < weights.length; w++) {
            int baseCol = 2 + w * partWeights.length;

            // Gộp 3 ô cho mỗi trọng số (VD: NV -> Min, Std, Mean)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, baseCol, baseCol + partWeights.length - 1));
            headerRow1.createCell(baseCol).setCellValue(weights[w]);

            for (int j = 0; j < partWeights.length; j++) {
                headerRow2.createCell(baseCol + j).setCellValue(partWeights[j]);
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(path)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        workbook.close();
    }

}
