package org.logistic;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
//    //These are the variables which can be changed
//    private static final int REQUESTS = 50;//Any value from: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
//    private static final boolean READ_FROM_DATASET = true;//Test on the same dataset used in the report, or generate new dataset
//    private static final int ITERATIONS = 1;//Number of times to retest the same algorithm on the same dataset. Note that each test takes up to 1 minute.
//    private static final int ALGORITHM = 0;//Choose which algorithm to test. 0 = Ant Colony, 1 = Cuckoo Search, 2 = Tabu Search
//
//
//    //Do not change anything below this line
//    private static ArrayList<Location> locations = new ArrayList<Location>();
//    private static int[][] distances = new int[REQUESTS * 2 + 1][REQUESTS * 2 + 1];
//    private static int capacity = 0;
//
//    public static void main(String[] args) {
//        //Check a valid algorithm was chosen:
//        if(ALGORITHM>=0 && ALGORITHM<=2) {
//            if (READ_FROM_DATASET) {
//                //Only values from 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 are allowed
//                if (REQUESTS > 0 && REQUESTS < 51 && REQUESTS % 5 == 0) {
//                    //Test the same dataset used in the report
//                    readFromFile();
//                } else {
//                    System.out.println("Error, incorrect REQUEST number." +
//                            "\nPlease use one of the following: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50.\n");
//                    System.exit(0);
//                }
//            } else {
//                //Generate new dataset
//                Generator g = new Generator();
//                g.generateData(REQUESTS, 10);
//                locations = g.getLocations();
//                distances = g.getDistances();
//                capacity = g.getCapacity();
//            }
//
//
//            Result[] results = new Result[ITERATIONS];
//
//            //Running the tests
//            if (ALGORITHM == 0) {
//                // Ant Colony Optimization
//                for (int i = 0; i < ITERATIONS; i++) {
//                    System.out.println("Ant Colony Optimization:");
//                    AntColonyOptimization ACO = new AntColonyOptimization(locations, distances, capacity);
//                    results[i] = ACO.go();
//                }
//            } else if (ALGORITHM == 1) {
//                //Cuckoo Search
//                for (int i = 0; i < ITERATIONS; i++) {
//                    CuckooSearch mycuckoo = new CuckooSearch(locations, distances, capacity);
//                    /* ("numberofnests/population" recommended 15-25, "numberofIterations" recommended 500-1000 *but sometimes up to 2000 is fine
//                     *  , The running time in milliseconds can be set) default is 1 minute = 60 000 ms
//                     */
//                    results[i] = mycuckoo.cuckooSearch(25, 2000, 60000);
//                    System.out.printf("Valid: %b  cost: %d  Time Of First Valid Solution: %s ms   Number Of Broken Constraints: %d", results[i].isValid(),
//                            results[i].getCost(), "" + results[i].getTimeOfFirstValidSolution(), results[i].getNumberOfConstraintsBroken());
//                    System.out.println();
//                }
//            } else if (ALGORITHM == 2) {
//                // Tabu Search
//                for (int i = 0; i < ITERATIONS; i++) {
//                    TabuSearch tabu = new TabuSearch(locations, distances, capacity);
//                    System.out.println("Obtaining Initial Solution");
//                    Route route = tabu.getFeasableRoute();
//                    System.out.println("Begin Fitness " + route.getFitnessValue());
//                    for (int k = 0; k < route.getRoute().length; k++) {
//                        System.out.print(route.getRoute()[k] + " ");
//                    }
//                    System.out.println();
//                    System.out.println(route.isValid());
//                    System.out.println("--------------------------------------------");
//
//                    tabu.main(route);
//                    results[i] = tabu.getResult();
//                    System.out.println("Valid : " + tabu.getResult().isValid() + " | Cost : " + tabu.getResult().getCost() + " | Time of First Valid Solution : " + tabu.getResult().getTimeOfFirstValidSolution() + " | Number of constraints broken : " + tabu.getResult().getNumberOfConstraintsBroken());
//                }
//            }
//
//            //Print the best and average results
//            printResults(results);
//        }else{
//            System.out.println("Error, incorrect ALGORITHM number." +
//                    "\nPlease use one of the following: 0: Ant Colony, 1: Cuckoo Search, 2: Tabu Search.\n");
//            System.exit(0);
//        }
//    }
//
//    private static void printResults(Result[] r) {
//        int countValid = 0;
//        int costTotal = 0;
//        int bestCost = Integer.MAX_VALUE;
//        long firstTimeTotal = 0;
//        long firstTimeBest = Integer.MAX_VALUE;
//        int constraintTotal = 0;
//        int constraintBest = Integer.MAX_VALUE;
//        for (int i = 0; i < r.length; i++) {
//            if (r[i].isValid()) {
//                countValid++;
//                int cost = r[i].getCost();
//                costTotal += cost;
//                if (cost < bestCost)
//                    bestCost = cost;
//                long firstTime = r[i].getTimeOfFirstValidSolution();
//                firstTimeTotal += firstTime;
//                if (firstTime < firstTimeBest)
//                    firstTimeBest = firstTime;
//
//            }
//            int constraint = r[i].getNumberOfConstraintsBroken();
//            constraintTotal += constraint;
//            if (constraint < constraintBest)
//                constraintBest = constraint;
//        }
//        System.out.println(constraintTotal);
//        double averageCost = -1;
//        long averageFirstTime = -1;
//        if (countValid > 0) {
//            averageCost = ((double) costTotal) / countValid;
//            averageFirstTime = firstTimeTotal / countValid;
//        } else {
//            bestCost = -1;
//            firstTimeBest = -1;
//        }
//
//        double averageConstraint = 0;
//        if (countValid < r.length) {
//            averageConstraint = ((double) constraintTotal) / ((double) (r.length - countValid));
//        }
//
//        System.out.println();
//        System.out.println(String.format("Valid: %s/%s", countValid, r.length));
//        System.out.println(String.format("Best cost: %s", bestCost));
//        System.out.println(String.format("Average cost: %s", averageCost));
//        System.out.println(String.format("Best time to first valid solution: %sms", firstTimeBest));
//        System.out.println(String.format("Average time to first valid solution: %sms", averageFirstTime));
//        System.out.println(String.format("Fewest constraints broken: %s", constraintBest));
//        System.out.println(String.format("Average constrains broken: %s", averageConstraint));
//
//    }
//
//    public static void readFromFile() {
//        try {
//            BufferedReader csvReader = new BufferedReader(new FileReader("datasets/" + REQUESTS + "locations.csv"));
//            String row;
//            while ((row = csvReader.readLine()) != null) {
//                String[] data = row.split(",");
//                Location location = new Location(data[0].equals("true"), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
//                location.setLTW(Integer.parseInt(data[4]));
//                location.setUTW(Integer.parseInt(data[5]));
//                locations.add(location);
//            }
//            csvReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            BufferedReader csvReader = new BufferedReader(new FileReader("datasets/" + REQUESTS + "distances.csv"));
//            String row;
//            int j = 0;
//            while ((row = csvReader.readLine()) != null) {
//                String[] data = row.split(",");
//                for (int i = 0; i < data.length; i++) {
//                    distances[i][j] = Integer.parseInt(data[i]);
//                }
//                j++;
//            }
//            csvReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            BufferedReader csvReader = new BufferedReader(new FileReader("datasets/" + REQUESTS + "capacity.csv"));
//            String row;
//            while ((row = csvReader.readLine()) != null) {
//                String[] data = row.split(",");
//                capacity = Integer.parseInt(data[0]);
//            }
//            csvReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


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

