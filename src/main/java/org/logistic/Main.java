package org.logistic;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        // Đọc dữ liệu
        List<double[][]> data = new ArrayList<>();
        data.add(dataset1("./KDD_cup99.xlsx"));
        data.add(dataset2("./IOT_Intrusion_Dataset.xlsx"));
        data.add(dataset3("./Third_Dataset.xlsx"));

        // Tối ưu hóa
        int an = 0;
        List<double[][][]> solns = new ArrayList<>();
        if (an == 1) {
            for (int i = 0; i < 3; i++) {
                double[][] feat = normalize(subArray(data.get(i), 0, data.get(i)[0].length - 2));
                double[] tar = getLastColumn(data.get(i));

                int per = (int) Math.round(feat.length * 0.70); // 70% learning
                double[][] train_data = subArray(feat, 0, per - 1);
                double[] train_target = subArray(tar, 0, per - 1);
                double[][] test_data = subArray(feat, per, feat.length - 1);
                double[] test_target = subArray(tar, per, tar.length - 1);

                int Npop = 10;
                double[][] xmin = repmat(5, Npop, 1);
                double[][] xmax = repmat(255, Npop, 1);
                double[][] initsol = new double[Npop][xmax[0].length];
                Random rand = new Random();
                for (int p1 = 0; p1 < Npop; p1++) {
                    for (int p2 = 0; p2 < xmax[0].length; p2++) {
                        initsol[p1][p2] = 5 + rand.nextDouble() * (255 - 5);
                    }
                }
                String fname = "objfun";
                int Max_iter = 25;

                System.out.println("PSO...");
                // Gọi hàm PSO
                // double[] bestsol1 = PSO(initsol, fname, xmin, xmax, Max_iter, train_data, train_target, test_data, test_target);

                System.out.println("GWO...");
                // Gọi hàm GWO
                // double[] bestsol2 = GWO(initsol, fname, xmin, xmax, Max_iter, train_data, train_target, test_data, test_target);

                System.out.println("SHO...");
                // Gọi hàm SHO
                // double[] bestsol3 = SHO(initsol, fname, xmin, xmax, Max_iter, train_data, train_target, test_data, test_target);

                // double[][] bestsol = {bestsol1, bestsol2, bestsol3};
                // solns.add(bestsol);
            }
            // Lưu solns vào file
            // saveSolns("solns.npy", solns);
        } else {
            // Đọc solns từ file
            // solns = loadSolns("solns.npy");
        }

        // Phân loại
        an = 0;
        if (an == 1) {
            List<double[][]> Eval_all = new ArrayList<>();
            for (int i = 0; i < 3; i++) { // Với tất cả dataset
                System.out.println(i);
                double[][] feat = normalize(subArray(data.get(i), 0, data.get(i)[0].length - 2));
                double[] tar = getLastColumn(data.get(i));
                double[] pn = {0.35, 0.45, 0.55, 0.65, 0.75, 0.85};
                List<double[][]> Eval_out = new ArrayList<>();
                for (int p = 0; p < 6; p++) { // Với tất cả tỷ lệ học
                    System.out.println(p);
                    int per = (int) Math.round(feat.length * pn[p]);
                    double[][] train_data = subArray(feat, 0, per - 1);
                    double[] train_target = subArray(tar, 0, per - 1);
                    double[][] test_data = subArray(feat, per, feat.length - 1);
                    double[] test_target = subArray(tar, per, tar.length - 1);
                    double[] act = test_target;

                    double[][] EVAL = new double[4][14];
                    for (int n = 0; n < 4; n++) {
                        System.out.println(n);
                        double so;
                        if (n == 0) {
                            so = 10;
                        } else {
                            // double[] sol = solns.get(i)[n-1];
                            // so = sol[0];
                        }
                        // double[] pred = train_nn(train_data, train_target, test_data, Math.round(so));
                        // EVAL[n] = evaln(pred, act);
                    }
                    Eval_out.add(EVAL);
                }
                Eval_all.add(Eval_out);
            }
            // Lưu Eval_all vào file
            // saveEvalAll("Eval_all.npy", Eval_all);
        }

        // Gọi hàm plot_results
        // plot_results();
    }

    // Các hàm hỗ trợ cần chuyển đổi từ Python sang Java
    private static double[][] dataset1(String path) throws IOException {
        // Đọc dữ liệu từ file Excel
        Workbook workbook = WorkbookFactory.create(new File(path));
        // Xử lý dữ liệu và trả về dưới dạng mảng 2 chiều
        return new double[0][];
    }

    private static double[][] dataset2(String path) throws IOException {
        // Đọc dữ liệu từ file Excel
        Workbook workbook = WorkbookFactory.create(new File(path));
        // Xử lý dữ liệu và trả về dưới dạng mảng 2 chiều
        return new double[0][];
    }

    private static double[][] dataset3(String path) throws IOException {
        // Đọc dữ liệu từ file Excel
        Workbook workbook = WorkbookFactory.create(new File(path));
        // Xử lý dữ liệu và trả về dưới dạng mảng 2 chiều
        return new double[0][];
    }

    private static double[][] subArray(double[][] array, int start, int end) {
        double[][] subArray = new double[end - start + 1][array[0].length];
        for (int i = start; i <= end; i++) {
            subArray[i - start] = array[i];
        }
        return subArray;
    }

    private static double[] subArray(double[] array, int start, int end) {
        double[] subArray = new double[end - start + 1];
        System.arraycopy(array, start, subArray, 0, end - start + 1);
        return subArray;
    }

    private static double[] getLastColumn(double[][] array) {
        double[] lastColumn = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            lastColumn[i] = array[i][array[0].length - 1];
        }
        return lastColumn;
    }

    private static double[][] repmat(double value, int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = value;
            }
        }
        return matrix;
    }

    private static double[][] normalize(double[][] data) {
        // Normalize dữ liệu và trả về mảng đã được chuẩn hóa
        return new double[0][];
    }

    // Thêm các hàm PSO, GWO, SHO, train_nn, evaln, plot_results và các hàm đọc ghi file tương ứng
}

