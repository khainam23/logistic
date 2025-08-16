package org.logistic.algorithm.sa;

import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Test để chạy Simulated Annealing và thu thập điểm số từ population
 */
public class SimulatedAnnealingScoreTest {

    public static void main(String[] args) {
        System.out.println("=== TEST SIMULATED ANNEALING - THU THẬP ĐIỂM SỐ ===\n");

        // Chạy test với các bộ dữ liệu khác nhau
        runTestCase("data/vrptw/src/r203.txt", "data/vrptw/solution/r203.txt", "VRPTW", "r203");
        // runTestCase("data/vrpspdtw_Liu_Tang_Yao/src/200_1.txt", "data/vrpspdtw_Liu_Tang_Yao/solution/200_1.txt", "VRPSPDTW_LIU", "200_1");
    }

    private static void runTestCase(String dataFile, String solutionFile, String problemType, String testName) {
        System.out.println("🚀 Bắt đầu test: " + testName + " (" + problemType + ")");
        System.out.println("📁 Data file: " + dataFile);
        System.out.println("📁 Solution file: " + solutionFile);

        // Đọc dữ liệu
        ReadDataFromFile dataReader = new ReadDataFromFile();
        
        ReadDataFromFile.ProblemType type;
        switch (problemType) {
            case "VRPTW":
                type = ReadDataFromFile.ProblemType.VRPTW;
                break;
            case "VRPSPDTW_LIU":
                type = ReadDataFromFile.ProblemType.VRPSPDTW_LIU_TANG_YAO;
                break;
            default:
                System.out.println("❌ Loại bài toán không được hỗ trợ: " + problemType);
                return;
        }
        
        dataReader.readProblemData(dataFile, type);
        Location[] locations = dataReader.getLocations();
        double maxCapacity = dataReader.getMaxCapacity();

        if (locations == null) {
            System.out.println("❌ Không đọc được dữ liệu từ: " + dataFile);
            return;
        }

        System.out.println("✅ Đã đọc " + locations.length + " locations");
        System.out.println("📦 Sức chứa tối đa: " + maxCapacity);

        // Khởi tạo các utility
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();

        // Đọc solution ban đầu
        dataReader.readSolution(solutionFile);
        Route[] initialRoutes = dataReader.getRoutes();

        if (initialRoutes == null || initialRoutes.length == 0) {
            System.out.println("❌ Không đọc được solution từ: " + solutionFile);
            return;
        }

        System.out.println("🛣️  Đã đọc " + initialRoutes.length + " routes từ solution");

        // Tính fitness ban đầu
        double initialFitness = fitnessUtil.calculatorFitness(initialRoutes, locations);
        System.out.println("🎯 Fitness ban đầu: " + String.format("%.2f", initialFitness));

        // Tạo solution ban đầu và khởi tạo SA
        Solution initialSolution = new Solution(initialRoutes, initialFitness);
        SimulatedAnnealing sa = new SimulatedAnnealing(initialSolution);

        System.out.println("\n🔥 Bắt đầu chạy Simulated Annealing...");
        long startTime = System.currentTimeMillis();

        // Chạy SA và lấy population
        Solution[] population = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations);

        long endTime = System.currentTimeMillis();
        double runTime = (endTime - startTime) / 1000.0;

        System.out.println("✅ Hoàn thành! Thời gian chạy: " + String.format("%.2f", runTime) + " giây");

        // Phân tích kết quả population
        analyzePopulation(population, initialFitness, testName, problemType);

        // Ghi kết quả ra file
        saveResults(population, initialFitness, testName, problemType, runTime, locations);

        System.out.println("=" + "=".repeat(60) + "\n");
    }

    /**
     * Phân tích population để thu thập thông tin
     */
    private static void analyzePopulation(Solution[] population, double initialFitness, String testName, String problemType) {
        System.out.println("\n📊 PHÂN TÍCH POPULATION:");
        System.out.println("🔢 Số lượng solutions: " + population.length);

        if (population.length == 0) {
            System.out.println("❌ Population rỗng!");
            return;
        }

        // Tìm fitness tốt nhất, xấu nhất, trung bình
        double bestFitness = Double.MAX_VALUE;
        double worstFitness = Double.MIN_VALUE;
        double totalFitness = 0;
        Solution bestSolution = null;

        for (Solution solution : population) {
            double fitness = solution.getFitness();
            totalFitness += fitness;

            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = solution;
            }
            if (fitness > worstFitness) {
                worstFitness = fitness;
            }
        }

        double averageFitness = totalFitness / population.length;

        // In thống kê
        System.out.println("🏆 Fitness tốt nhất: " + String.format("%.2f", bestFitness));
        System.out.println("😞 Fitness xấu nhất: " + String.format("%.2f", worstFitness));
        System.out.println("📊 Fitness trung bình: " + String.format("%.2f", averageFitness));
        System.out.println("🎯 Fitness ban đầu: " + String.format("%.2f", initialFitness));
        
        double improvement = initialFitness - bestFitness;
        double improvementPercent = (improvement / initialFitness) * 100;
        
        System.out.println("📈 Cải thiện: " + String.format("%.2f", improvement) + 
                         " (" + String.format("%.2f%%", improvementPercent) + ")");

        // Tính độ đa dạng của population
        calculateDiversity(population);

        // In chi tiết solution tốt nhất
        if (bestSolution != null) {
            System.out.println("\n🏆 CHI TIẾT SOLUTION TỐT NHẤT:");
            printSolutionDetails(bestSolution);
        }
    }

    /**
     * Tính độ đa dạng của population
     */
    private static void calculateDiversity(Solution[] population) {
        int uniqueSolutions = 0;
        int totalComparisons = 0;
        int duplicateCount = 0;

        for (int i = 0; i < population.length; i++) {
            boolean isUnique = true;
            for (int j = i + 1; j < population.length; j++) {
                totalComparisons++;
                if (solutionsEqual(population[i], population[j])) {
                    isUnique = false;
                    duplicateCount++;
                }
            }
            if (isUnique) {
                uniqueSolutions++;
            }
        }

        double diversityPercent = ((double) uniqueSolutions / population.length) * 100;
        System.out.println("🌈 Độ đa dạng: " + uniqueSolutions + "/" + population.length + 
                         " (" + String.format("%.1f%%", diversityPercent) + ")");
        System.out.println("👥 Số solutions trùng lặp: " + duplicateCount);
    }

    /**
     * Kiểm tra xem hai solutions có giống nhau không
     */
    private static boolean solutionsEqual(Solution sol1, Solution sol2) {
        Route[] routes1 = sol1.getRoutes();
        Route[] routes2 = sol2.getRoutes();

        if (routes1.length != routes2.length) {
            return false;
        }

        for (int i = 0; i < routes1.length; i++) {
            if (!Arrays.equals(routes1[i].getIndLocations(), routes2[i].getIndLocations())) {
                return false;
            }
        }

        return true;
    }

    /**
     * In chi tiết solution
     */
    private static void printSolutionDetails(Solution solution) {
        Route[] routes = solution.getRoutes();
        System.out.println("🛣️  Số routes: " + routes.length);
        
        for (int i = 0; i < routes.length; i++) {
            int[] locations = routes[i].getIndLocations();
            System.out.println("  Route " + (i + 1) + ": " + Arrays.toString(locations) + 
                             " (length: " + locations.length + ")");
        }
    }

    /**
     * Lưu kết quả ra file
     */
    private static void saveResults(Solution[] population, double initialFitness, String testName, 
                                  String problemType, double runTime, Location[] locations) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "exports/SA_results_" + testName + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=== SIMULATED ANNEALING TEST RESULTS ===\n");
            writer.write("Test Name: " + testName + "\n");
            writer.write("Problem Type: " + problemType + "\n");
            writer.write("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("Run Time: " + String.format("%.2f", runTime) + " seconds\n");
            writer.write("Number of Locations: " + locations.length + "\n");
            writer.write("Initial Fitness: " + String.format("%.2f", initialFitness) + "\n");
            writer.write("Population Size: " + population.length + "\n\n");

            // Ghi thống kê population
            if (population.length > 0) {
                double bestFitness = Double.MAX_VALUE;
                double worstFitness = Double.MIN_VALUE;
                double totalFitness = 0;

                for (Solution solution : population) {
                    double fitness = solution.getFitness();
                    totalFitness += fitness;
                    if (fitness < bestFitness) bestFitness = fitness;
                    if (fitness > worstFitness) worstFitness = fitness;
                }

                double averageFitness = totalFitness / population.length;
                double improvement = initialFitness - bestFitness;

                writer.write("POPULATION STATISTICS:\n");
                writer.write("Best Fitness: " + String.format("%.2f", bestFitness) + "\n");
                writer.write("Worst Fitness: " + String.format("%.2f", worstFitness) + "\n");
                writer.write("Average Fitness: " + String.format("%.2f", averageFitness) + "\n");
                writer.write("Improvement: " + String.format("%.2f", improvement) + "\n");
                writer.write("Improvement %: " + String.format("%.2f%%", (improvement/initialFitness)*100) + "\n\n");

                // Ghi chi tiết từng solution
                writer.write("DETAILED SOLUTIONS:\n");
                for (int i = 0; i < population.length; i++) {
                    Solution solution = population[i];
                    writer.write("Solution " + (i + 1) + " (Fitness: " + 
                               String.format("%.2f", solution.getFitness()) + "):\n");
                    
                    Route[] routes = solution.getRoutes();
                    for (int j = 0; j < routes.length; j++) {
                        writer.write("  Route " + (j + 1) + ": " + 
                                   Arrays.toString(routes[j].getIndLocations()) + "\n");
                    }
                    writer.write("\n");
                }
            }

            System.out.println("📁 Đã lưu kết quả vào: " + filename);

        } catch (IOException e) {
            System.out.println("❌ Lỗi khi lưu file: " + e.getMessage());
        }
    }
}