package org.logistic.data;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataAugmentation {
    public static int[] findString(String[] array, String s) {
        List<Integer> matchedIndexes = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (s.equals(array[i])) {
                matchedIndexes.add(i);
            }
        }
        return matchedIndexes.stream().mapToInt(i -> i).toArray();
    }

    public static double[][] dataset1(String filename) throws IOException {
        boolean an = false;
        double[][] data;

        if (an) {
            FileInputStream file = new FileInputStream(filename);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);

            int numRows = sheet.getPhysicalNumberOfRows();
            int numCols = sheet.getRow(0).getPhysicalNumberOfCells();
            data = new double[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < numCols; j++) {
                    Cell cell = row.getCell(j);
                    if (cell.getCellType() == CellType.STRING) {
                        data[i][j] = cell.getStringCellValue().hashCode();
                    } else if (cell.getCellType() == CellType.NUMERIC) {
                        data[i][j] = cell.getNumericCellValue();
                    }
                }
            }

            for (int n1 = 0; n1 < numCols; n1++) {
                if (sheet.getRow(0).getCell(n1).getCellType() == CellType.STRING) {
                    if (n1 == numCols - 1) {
                        double[] val = new double[numRows];
                        Arrays.fill(val, 1);
                        int[] ind = findString(getColumn(data, n1), "normal");
                        for (int i : ind) {
                            val[i] = 0;
                        }
                        setColumn(data, n1, val);
                    } else {
                        String[] uniqueValues = Arrays.stream(getColumn(data, n1)).distinct().toArray(String[]::new);
                        for (int n2 = 0; n2 < uniqueValues.length; n2++) {
                            int[] ind = findString(getColumn(data, n1), uniqueValues[n2]);
                            for (int i : ind) {
                                data[i][n1] = n2;
                            }
                        }
                    }
                }
            }

            // Save the data to a file (serialization can be done here)
            saveData("data.npy", data);
            workbook.close();
        } else {
            data = loadData("data.npy");
        }

        return data;
    }

    // Similar methods for dataset2 and dataset3

    public static String[] getColumn(double[][] array, int index) {
        String[] column = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            column[i] = String.valueOf(array[i][index]);
        }
        return column;
    }

    public static void setColumn(double[][] array, int index, double[] column) {
        for (int i = 0; i < array.length; i++) {
            array[i][index] = column[i];
        }
    }

    public static void saveData(String filename, double[][] data) {
        // Implement serialization here
    }

    public static double[][] loadData(String filename) {
        // Implement deserialization here
        return new double[0][0];
    }

    public static void main(String[] args) throws IOException {
        double[][] data = dataset1("your_excel_file.xlsx");
        // Similarly for dataset2 and dataset3
    }
}
