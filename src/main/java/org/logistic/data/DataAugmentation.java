package org.logistic.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.nio.file.Files;
import java.nio.file.Paths;

// Xử lý dữ liệu trong file excel (hay có thể xem là file dataset)
public class DataAugmentation {
    public static int[] findString(String[] l1, String s) {
        return Arrays.stream(l1)
                .filter(s::equals)
                .mapToInt(i -> Arrays.asList(l1).indexOf(i))
                .toArray();
    }

    public static Object[][] dataset1(String filename) throws IOException {
        int an = 0;
        Object[][] data;
        if (an == 1) {
            Workbook wb = new XSSFWorkbook(new File(filename));
            Sheet sheet = wb.getSheet("Sheet1");
            data = new Object[sheet.getPhysicalNumberOfRows()][sheet.getRow(0).getPhysicalNumberOfCells()];
            for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    data[i][j] = row.getCell(j).toString();
                }
            }
            wb.close();

            for (int n1 = 0; n1 < data[0].length; n1++) {
                if (data[0][n1] instanceof String) {
                    if (n1 == data[0].length - 1) {
                        double[] val = new double[data.length];
                        int[] ind = findString(Arrays.stream(data).map(row -> (String) row[n1]).toArray(String[]::new), "normal");
                        for (int index : ind) {
                            val[index] = 0;
                        }
                        data = updateColumn(data, n1, val);
                    } else {
                        String[] u = Arrays.stream(data).map(row -> (String) row[n1]).distinct().toArray(String[]::new);
                        for (int n2 = 0; n2 < u.length; n2++) {
                            int[] ind = findString(Arrays.stream(data).map(row -> (String) row[n1]).toArray(String[]::new), u[n2]);
                            for (int index : ind) {
                                data[index][n1] = n2;
                            }
                        }
                    }
                }
                saveData("data.npy", data);
            } else{
                data = loadData("data.npy");
            }
            return data;
        }

        public static Object[][] dataset2 (String filename) throws IOException {
            int an = 0;
            Object[][] data;
            if (an == 1) {
                Workbook excel = new XSSFWorkbook(new File(filename));
                Sheet sheet = excel.getSheet("Sheet1");
                data = new Object[sheet.getPhysicalNumberOfRows()][sheet.getRow(0).getPhysicalNumberOfCells()];
                for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
                    Row row = sheet.getRow(i);
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        data[i][j] = row.getCell(j).toString();
                    }
                }
                excel.close();

                for (int col = 0; col < data[0].length; col++) {
                    if (data[0][col] instanceof String) {
                        if (col == data[0].length - 1) {
                            double[] value = new double[data.length];
                            int[] index = findString(Arrays.stream(data).map(row -> (String) row[col]).toArray(String[]::new), "Normal");
                            for (int ind : index) {
                                value[ind] = 0;
                            }
                            data = updateColumn(data, col, value);
                        } else {
                            String[] uniq = Arrays.stream(data).map(row -> (String) row[col]).distinct().toArray(String[]::new);
                            for (int ind_uniq = 0; ind_uniq < uniq.length; ind_uniq++) {
                                int[] index = findString(Arrays.stream(data).map(row -> (String) row[col]).toArray(String[]::new), uniq[ind_uniq]);
                                for (int ind : index) {
                                    data[ind][col] = ind_uniq;
                                }
                            }
                        }
                    }
                }
                saveData("data2.npy", data);
            } else {
                data = loadData("data2.npy");
            }
            return data;
        }

        public static Object[][] dataset3 (String filename) throws IOException {
            int an = 0;
            Object[][] data;
            if (an == 1) {
                Workbook excel = new XSSFWorkbook(new File(filename));
                Sheet sheet = excel.getSheet("Sheet1");
                data = new Object[sheet.getPhysicalNumberOfRows()][sheet.getRow(0).getPhysicalNumberOfCells()];
                for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
                    Row row = sheet.getRow(i);
                    for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                        data[i][j] = row.getCell(j).toString();
                    }
                }
                excel.close();

                for (int col = 0; col < data[0].length; col++) {
                    if (data[0][col] instanceof String) {
                        if (col == data[0].length - 1) {
                            double[] value = new double[data.length];
                            int[] index = findString(Arrays.stream(data).map(row -> (String) row[col]).toArray(String[]::new), "Normal");
                            for (int ind : index) {
                                value[ind] = 0;
                            }
                            data = updateColumn(data, col, value);
                        } else {
                            String[] uniq = Arrays.stream(data).map(row -> (String) row[col]).distinct().toArray(String[]::new);
                            for (int ind_uniq = 0; ind_uniq < uniq.length; ind_uniq++) {
                                int[] index = findString(Arrays.stream(data).map(row -> (String) row[col]).toArray(String[]::new), uniq[ind_uniq]);
                                for (int ind : index) {
                                    data[ind][col] = ind_uniq;
                                }
                            }
                        }
                    }
                }
                saveData("data3.npy", data);
            } else {
                data = loadData("data3.npy");
            }
            return data;
        }

        private static Object[][] updateColumn (Object[][]data,int col, double[] values){
            for (int i = 0; i < data.length; i++) {
                data[i][col] = values[i];
            }
            return data;
        }

        private static void saveData (String filename, Object[][]data) throws IOException {
            // Implement saving logic here
        }

        private static Object[][] loadData (String filename) throws IOException {
            // Implement loading logic here
            return new Object[0][0];
        }
    }
