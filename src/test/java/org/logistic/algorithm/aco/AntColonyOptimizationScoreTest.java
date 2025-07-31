package org.logistic.algorithm.aco;

import org.logistic.algorithm.aco.AntColonyOptimization;
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
 * Test để chạy Ant Colony Optimization và thu thập điểm số
 */
public class AntColonyOptimizationScoreTest {

    public static void main(String[] args) {
        System.out.println("=== TEST ANT COLONY OPTIMIZATION - THU THẬP ĐIỂM SỐ ===\n");

        // Số lần chạy cho mỗi test case
        int numberOfRuns = 5;
        
        // Chạy test với các bộ dữ liệu khác nhau
        runMultipleTests("data/vrptw/src/c101.txt", "data/vrptw/solution/c101.txt", "VRPTW", "c101", numberOfRuns);
        // runMultipleTests("data/vrptw/src/r203.txt", "data/vrptw/solution/r203.txt", "VRPTW", "r203", numberOfRuns);
    }

    /**
     * Chạy nhiều lần test và thu thập thống kê
     */
    private static void runMultipleTests(String dataFile, String solutionFile, String problemType, String testName, int numberOfRuns) {
        System.out.println("🔄 Chạy " + numberOfRuns + " lần test cho: " + testName + " (" + problemType + ")");
        System.out.println("📁 Data file: " + dataFile);
        System.out.println("📁 Solution file: " + solutionFile);
        System.out.println("=" + "=".repeat(80));

        // Mảng lưu kết quả các lần chạy
        double[] finalFitnesses = new double[numberOfRuns];
        double[] runTimes = new double[numberOfRuns];
        double[] improvements = new double[numberOfRuns];
        Solution[] bestSolutions = new Solution[numberOfRuns];
        
        // Đọc dữ liệu một lần (dùng chung cho tất cả các lần chạy)
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
        int maxCapacity = dataReader.getMaxCapacity();

        if (locations == null) {
            System.out.println("❌ Không đọc được dữ liệu từ: " + dataFile);
            return;
        }

        // Đọc solution ban đầu
        dataReader.readSolution(solutionFile);
        Route[] initialRoutes = dataReader.getRoutes();

        if (initialRoutes == null || initialRoutes.length == 0) {
            System.out.println("❌ Không đọc được solution từ: " + solutionFile);
            return;
        }

        // Khởi tạo utilities
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        
        // Tính fitness ban đầu
        double originalFitness = fitnessUtil.calculatorFitness(initialRoutes, locations);
        System.out.println("🎯 Fitness gốc: " + String.format("%.2f", originalFitness));
        System.out.println();

        // Chạy nhiều lần
        for (int run = 1; run <= numberOfRuns; run++) {
            System.out.println("🏃‍♂️ === LẦN CHẠY " + run + "/" + numberOfRuns + " ===");
            
            long startTime = System.currentTimeMillis();
            
            // Tạo initial solutions cho lần chạy này
            Solution[] initialSolutions = generateInitialSolutions(initialRoutes, locations, fitnessUtil, checkConditionUtil, maxCapacity);
            
            // Tìm best initial fitness
            double bestInitialFitness = Double.MAX_VALUE;
            for (Solution sol : initialSolutions) {
                if (sol.getFitness() < bestInitialFitness) {
                    bestInitialFitness = sol.getFitness();
                }
            }
            
            // Chạy ACO
            AntColonyOptimization aco = new AntColonyOptimization();
            Solution result = aco.run(initialSolutions, fitnessUtil, checkConditionUtil, locations, maxCapacity);
            
            long endTime = System.currentTimeMillis();
            double runTime = (endTime - startTime) / 1000.0;
            
            // Lưu kết quả
            finalFitnesses[run - 1] = result.getFitness();
            runTimes[run - 1] = runTime;
            improvements[run - 1] = bestInitialFitness - result.getFitness();
            bestSolutions[run - 1] = result;
            
            System.out.println("✅ Lần " + run + " - Fitness: " + String.format("%.2f", result.getFitness()) + 
                             " - Thời gian: " + String.format("%.2f", runTime) + "s" +
                             " - Cải thiện: " + String.format("%.2f", improvements[run - 1]));
            System.out.println();
        }

        // Phân tích thống kê tổng hợp
        analyzeMultipleRunsStatistics(finalFitnesses, runTimes, improvements, bestSolutions, originalFitness, testName, problemType);
        
        // Lưu kết quả tổng hợp
        saveMultipleRunsResults(finalFitnesses, runTimes, improvements, bestSolutions, originalFitness, testName, problemType, locations);
        
        System.out.println("=" + "=".repeat(80) + "\n");
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
        int maxCapacity = dataReader.getMaxCapacity();

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

        // Tạo nhiều initial solutions bằng SA để ACO có thể sử dụng
        System.out.println("\n🔥 Tạo initial solutions bằng Simulated Annealing...");
        Solution[] initialSolutions = generateInitialSolutions(initialRoutes, locations, fitnessUtil, checkConditionUtil, maxCapacity);
        
        System.out.println("✅ Đã tạo " + initialSolutions.length + " initial solutions");
        
        // Tìm best initial solution
        double bestInitialFitness = Double.MAX_VALUE;
        for (Solution sol : initialSolutions) {
            if (sol.getFitness() < bestInitialFitness) {
                bestInitialFitness = sol.getFitness();
            }
        }
        System.out.println("🏆 Best initial fitness: " + String.format("%.2f", bestInitialFitness));

        // Khởi tạo và chạy ACO
        System.out.println("\n🐜 Bắt đầu chạy Ant Colony Optimization...");
        long startTime = System.currentTimeMillis();

        AntColonyOptimization aco = new AntColonyOptimization();
        Solution result = aco.run(initialSolutions, fitnessUtil, checkConditionUtil, locations, maxCapacity);

        long endTime = System.currentTimeMillis();
        double runTime = (endTime - startTime) / 1000.0;

        System.out.println("✅ Hoàn thành! Thời gian chạy: " + String.format("%.2f", runTime) + " giây");

        // Phân tích kết quả
        analyzeResult(result, initialFitness, bestInitialFitness, testName, problemType);

        // Ghi kết quả ra file
        saveResults(result, initialSolutions, initialFitness, testName, problemType, runTime, locations);

        System.out.println("=" + "=".repeat(60) + "\n");
    }

    /**
     * Tạo nhiều initial solutions bằng SA
     */
    private static Solution[] generateInitialSolutions(Route[] initialRoutes, Location[] locations, 
                                                     FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, 
                                                     int maxCapacity) {
        int numSolutions = 5; // Tạo 5 initial solutions
        Solution[] solutions = new Solution[numSolutions];
        
        // Solution đầu tiên là solution gốc
        double initialFitness = fitnessUtil.calculatorFitness(initialRoutes, locations);
        solutions[0] = new Solution(initialRoutes, initialFitness);
        
        // Tạo các solutions khác bằng SA
        for (int i = 1; i < numSolutions; i++) {
            // Copy routes để tránh modify original
            Route[] copiedRoutes = new Route[initialRoutes.length];
            for (int j = 0; j < initialRoutes.length; j++) {
                copiedRoutes[j] = new Route();
                copiedRoutes[j].setUse(initialRoutes[j].isUse());
                copiedRoutes[j].setIndLocations(initialRoutes[j].getIndLocations().clone());
                copiedRoutes[j].calculateDistance(locations);
            }
            
            Solution copiedSolution = new Solution(copiedRoutes, fitnessUtil.calculatorFitness(copiedRoutes, locations));
            SimulatedAnnealing sa = new SimulatedAnnealing(copiedSolution);
            
            // Chạy SA với ít iterations để tạo diversity
            solutions[i] = sa.run(new Solution[]{copiedSolution}, fitnessUtil, checkConditionUtil, locations, maxCapacity);
        }
        
        return solutions;
    }

    /**
     * Phân tích thống kê từ nhiều lần chạy
     */
    private static void analyzeMultipleRunsStatistics(double[] finalFitnesses, double[] runTimes, double[] improvements, 
                                                    Solution[] bestSolutions, double originalFitness, String testName, String problemType) {
        System.out.println("📊 === THỐNG KÊ TỔNG HỢP " + finalFitnesses.length + " LẦN CHẠY ===");
        
        // Tính toán thống kê fitness
        double bestFitness = Double.MAX_VALUE;
        double worstFitness = Double.MIN_VALUE;
        double totalFitness = 0;
        int bestRunIndex = 0;
        
        for (int i = 0; i < finalFitnesses.length; i++) {
            totalFitness += finalFitnesses[i];
            if (finalFitnesses[i] < bestFitness) {
                bestFitness = finalFitnesses[i];
                bestRunIndex = i;
            }
            if (finalFitnesses[i] > worstFitness) {
                worstFitness = finalFitnesses[i];
            }
        }
        
        double avgFitness = totalFitness / finalFitnesses.length;
        
        // Tính độ lệch chuẩn fitness
        double sumSquaredDiff = 0;
        for (double fitness : finalFitnesses) {
            sumSquaredDiff += Math.pow(fitness - avgFitness, 2);
        }
        double stdDevFitness = Math.sqrt(sumSquaredDiff / finalFitnesses.length);
        
        // Tính toán thống kê thời gian chạy
        double totalTime = 0;
        double minTime = Double.MAX_VALUE;
        double maxTime = Double.MIN_VALUE;
        
        for (double time : runTimes) {
            totalTime += time;
            if (time < minTime) minTime = time;
            if (time > maxTime) maxTime = time;
        }
        
        double avgTime = totalTime / runTimes.length;
        
        // Tính toán thống kê cải thiện
        double totalImprovement = 0;
        double bestImprovement = Double.MIN_VALUE;
        double worstImprovement = Double.MAX_VALUE;
        int positiveImprovements = 0;
        
        for (double improvement : improvements) {
            totalImprovement += improvement;
            if (improvement > bestImprovement) bestImprovement = improvement;
            if (improvement < worstImprovement) worstImprovement = improvement;
            if (improvement > 0) positiveImprovements++;
        }
        
        double avgImprovement = totalImprovement / improvements.length;
        
        // In kết quả thống kê
        System.out.println("\n🎯 THỐNG KÊ FITNESS:");
        System.out.println("  🏆 Tốt nhất: " + String.format("%.2f", bestFitness) + " (lần " + (bestRunIndex + 1) + ")");
        System.out.println("  📊 Trung bình: " + String.format("%.2f", avgFitness));
        System.out.println("  📉 Tệ nhất: " + String.format("%.2f", worstFitness));
        System.out.println("  📏 Độ lệch chuẩn: " + String.format("%.2f", stdDevFitness));
        System.out.println("  🎯 Fitness gốc: " + String.format("%.2f", originalFitness));
        
        System.out.println("\n⏱️  THỐNG KÊ THỜI GIAN:");
        System.out.println("  ⚡ Nhanh nhất: " + String.format("%.2f", minTime) + "s");
        System.out.println("  📊 Trung bình: " + String.format("%.2f", avgTime) + "s");
        System.out.println("  🐌 Chậm nhất: " + String.format("%.2f", maxTime) + "s");
        System.out.println("  🕐 Tổng thời gian: " + String.format("%.2f", totalTime) + "s");
        
        System.out.println("\n📈 THỐNG KÊ CẢI THIỆN:");
        System.out.println("  🏆 Cải thiện tốt nhất: " + String.format("%.2f", bestImprovement));
        System.out.println("  📊 Cải thiện trung bình: " + String.format("%.2f", avgImprovement));
        System.out.println("  📉 Cải thiện tệ nhất: " + String.format("%.2f", worstImprovement));
        System.out.println("  ✅ Số lần cải thiện: " + positiveImprovements + "/" + improvements.length + 
                         " (" + String.format("%.1f%%", (positiveImprovements * 100.0 / improvements.length)) + ")");
        
        // So sánh với fitness gốc
        double avgImprovementFromOriginal = originalFitness - avgFitness;
        double bestImprovementFromOriginal = originalFitness - bestFitness;
        
        System.out.println("\n🔍 SO SÁNH VỚI FITNESS GỐC:");
        System.out.println("  📈 Cải thiện trung bình: " + String.format("%.2f", avgImprovementFromOriginal) + 
                         " (" + String.format("%.2f%%", (avgImprovementFromOriginal/originalFitness)*100) + ")");
        System.out.println("  🏆 Cải thiện tốt nhất: " + String.format("%.2f", bestImprovementFromOriginal) + 
                         " (" + String.format("%.2f%%", (bestImprovementFromOriginal/originalFitness)*100) + ")");
        
        // Đánh giá tổng thể
        System.out.println("\n🎯 ĐÁNH GIÁ TỔNG THỂ:");
        double successRate = (positiveImprovements * 100.0 / improvements.length);
        if (successRate >= 80 && avgImprovement > 0) {
            System.out.println("✅ ACO hoạt động rất ổn định và hiệu quả!");
        } else if (successRate >= 60 && avgImprovement > 0) {
            System.out.println("✅ ACO hoạt động tốt với độ ổn định khá!");
        } else if (successRate >= 40) {
            System.out.println("⚠️  ACO hoạt động không ổn định, cần điều chỉnh tham số!");
        } else {
            System.out.println("❌ ACO hoạt động kém, cần xem xét lại thuật toán!");
        }
        
        System.out.println("  📊 Tỷ lệ thành công: " + String.format("%.1f%%", successRate));
        System.out.println("  🎯 Độ ổn định: " + (stdDevFitness < (avgFitness * 0.05) ? "Cao" : 
                                                stdDevFitness < (avgFitness * 0.1) ? "Trung bình" : "Thấp"));
        
        // In chi tiết solution tốt nhất
        System.out.println("\n🏆 CHI TIẾT SOLUTION TỐT NHẤT (Lần " + (bestRunIndex + 1) + "):");
        printSolutionDetails(bestSolutions[bestRunIndex]);
    }

    /**
     * Phân tích kết quả ACO
     */
    private static void analyzeResult(Solution result, double originalFitness, double bestInitialFitness, 
                                    String testName, String problemType) {
        System.out.println("\n📊 PHÂN TÍCH KẾT QUẢ ACO:");
        
        double resultFitness = result.getFitness();
        System.out.println("🏆 Fitness cuối cùng: " + String.format("%.2f", resultFitness));
        System.out.println("🎯 Fitness gốc: " + String.format("%.2f", originalFitness));
        System.out.println("🔥 Best initial fitness: " + String.format("%.2f", bestInitialFitness));
        
        // So sánh với fitness gốc
        double improvementFromOriginal = originalFitness - resultFitness;
        double improvementPercentFromOriginal = (improvementFromOriginal / originalFitness) * 100;
        
        System.out.println("📈 Cải thiện so với gốc: " + String.format("%.2f", improvementFromOriginal) + 
                         " (" + String.format("%.2f%%", improvementPercentFromOriginal) + ")");
        
        // So sánh với best initial
        double improvementFromInitial = bestInitialFitness - resultFitness;
        double improvementPercentFromInitial = (improvementFromInitial / bestInitialFitness) * 100;
        
        System.out.println("📈 Cải thiện so với best initial: " + String.format("%.2f", improvementFromInitial) + 
                         " (" + String.format("%.2f%%", improvementPercentFromInitial) + ")");

        // Đánh giá chất lượng
        System.out.println("\n🎯 ĐÁNH GIÁ:");
        if (improvementPercentFromInitial > 5) {
            System.out.println("✅ ACO hoạt động rất tốt - cải thiện đáng kể!");
        } else if (improvementPercentFromInitial > 1) {
            System.out.println("✅ ACO hoạt động tốt - có cải thiện!");
        } else if (improvementPercentFromInitial > 0) {
            System.out.println("⚠️  ACO hoạt động ổn - cải thiện nhẹ!");
        } else {
            System.out.println("❌ ACO chưa cải thiện được so với initial solutions!");
        }

        // In chi tiết solution
        System.out.println("\n🏆 CHI TIẾT SOLUTION TỐT NHẤT:");
        printSolutionDetails(result);
    }

    /**
     * In chi tiết solution
     */
    private static void printSolutionDetails(Solution solution) {
        Route[] routes = solution.getRoutes();
        int usedRoutes = 0;
        
        System.out.println("🛣️  Tổng số routes: " + routes.length);
        
        for (int i = 0; i < routes.length; i++) {
            if (routes[i].isUse()) {
                usedRoutes++;
                int[] locations = routes[i].getIndLocations();
                System.out.println("  Route " + (i + 1) + ": " + Arrays.toString(locations) + 
                                 " (length: " + locations.length + 
                                 ", distance: " + String.format("%.2f", routes[i].getDistance()) + ")");
            }
        }
        
        System.out.println("🚛 Số routes được sử dụng: " + usedRoutes);
    }

    /**
     * Lưu kết quả nhiều lần chạy ra file
     */
    private static void saveMultipleRunsResults(double[] finalFitnesses, double[] runTimes, double[] improvements, 
                                              Solution[] bestSolutions, double originalFitness, String testName, 
                                              String problemType, Location[] locations) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "exports/ACO_MultipleRuns_" + testName + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=== ANT COLONY OPTIMIZATION MULTIPLE RUNS TEST RESULTS ===\n");
            writer.write("Test Name: " + testName + "\n");
            writer.write("Problem Type: " + problemType + "\n");
            writer.write("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("Number of Runs: " + finalFitnesses.length + "\n");
            writer.write("Number of Locations: " + locations.length + "\n");
            writer.write("Original Fitness: " + String.format("%.2f", originalFitness) + "\n\n");

            // Tính toán thống kê
            double bestFitness = Double.MAX_VALUE;
            double worstFitness = Double.MIN_VALUE;
            double totalFitness = 0;
            double totalTime = 0;
            double totalImprovement = 0;
            int bestRunIndex = 0;
            int positiveImprovements = 0;
            
            for (int i = 0; i < finalFitnesses.length; i++) {
                totalFitness += finalFitnesses[i];
                totalTime += runTimes[i];
                totalImprovement += improvements[i];
                
                if (finalFitnesses[i] < bestFitness) {
                    bestFitness = finalFitnesses[i];
                    bestRunIndex = i;
                }
                if (finalFitnesses[i] > worstFitness) {
                    worstFitness = finalFitnesses[i];
                }
                if (improvements[i] > 0) {
                    positiveImprovements++;
                }
            }
            
            double avgFitness = totalFitness / finalFitnesses.length;
            double avgTime = totalTime / runTimes.length;
            double avgImprovement = totalImprovement / improvements.length;
            
            // Tính độ lệch chuẩn
            double sumSquaredDiff = 0;
            for (double fitness : finalFitnesses) {
                sumSquaredDiff += Math.pow(fitness - avgFitness, 2);
            }
            double stdDevFitness = Math.sqrt(sumSquaredDiff / finalFitnesses.length);

            // Ghi thống kê tổng hợp
            writer.write("STATISTICAL SUMMARY:\n");
            writer.write("Best Fitness: " + String.format("%.2f", bestFitness) + " (Run " + (bestRunIndex + 1) + ")\n");
            writer.write("Average Fitness: " + String.format("%.2f", avgFitness) + "\n");
            writer.write("Worst Fitness: " + String.format("%.2f", worstFitness) + "\n");
            writer.write("Standard Deviation: " + String.format("%.2f", stdDevFitness) + "\n");
            writer.write("Average Runtime: " + String.format("%.2f", avgTime) + " seconds\n");
            writer.write("Total Runtime: " + String.format("%.2f", totalTime) + " seconds\n");
            writer.write("Average Improvement: " + String.format("%.2f", avgImprovement) + "\n");
            writer.write("Success Rate: " + positiveImprovements + "/" + finalFitnesses.length + 
                       " (" + String.format("%.1f%%", (positiveImprovements * 100.0 / finalFitnesses.length)) + ")\n\n");

            // Ghi chi tiết từng lần chạy
            writer.write("DETAILED RESULTS BY RUN:\n");
            for (int i = 0; i < finalFitnesses.length; i++) {
                writer.write("Run " + (i + 1) + ":\n");
                writer.write("  Final Fitness: " + String.format("%.2f", finalFitnesses[i]) + "\n");
                writer.write("  Runtime: " + String.format("%.2f", runTimes[i]) + " seconds\n");
                writer.write("  Improvement: " + String.format("%.2f", improvements[i]) + "\n");
                writer.write("  Improvement %: " + String.format("%.2f%%", 
                           improvements[i] > 0 ? (improvements[i] / (finalFitnesses[i] + improvements[i])) * 100 : 0) + "\n");
                writer.write("\n");
            }

            // Ghi chi tiết solution tốt nhất
            writer.write("BEST SOLUTION DETAILS (Run " + (bestRunIndex + 1) + "):\n");
            Route[] bestRoutes = bestSolutions[bestRunIndex].getRoutes();
            int usedRoutes = 0;
            
            for (int i = 0; i < bestRoutes.length; i++) {
                if (bestRoutes[i].isUse()) {
                    usedRoutes++;
                    writer.write("Route " + (i + 1) + ": " + Arrays.toString(bestRoutes[i].getIndLocations()) + 
                               " (distance: " + String.format("%.2f", bestRoutes[i].getDistance()) + ")\n");
                }
            }
            
            writer.write("Total Routes Used: " + usedRoutes + "\n");
            
            // So sánh với fitness gốc
            double bestImprovementFromOriginal = originalFitness - bestFitness;
            double avgImprovementFromOriginal = originalFitness - avgFitness;
            
            writer.write("\nCOMPARISON WITH ORIGINAL:\n");
            writer.write("Best Improvement: " + String.format("%.2f", bestImprovementFromOriginal) + 
                       " (" + String.format("%.2f%%", (bestImprovementFromOriginal/originalFitness)*100) + ")\n");
            writer.write("Average Improvement: " + String.format("%.2f", avgImprovementFromOriginal) + 
                       " (" + String.format("%.2f%%", (avgImprovementFromOriginal/originalFitness)*100) + ")\n");

            System.out.println("📁 Đã lưu kết quả tổng hợp vào: " + filename);

        } catch (IOException e) {
            System.out.println("❌ Lỗi khi lưu file: " + e.getMessage());
        }
    }

    /**
     * Lưu kết quả ra file
     */
    private static void saveResults(Solution result, Solution[] initialSolutions, double originalFitness, 
                                  String testName, String problemType, double runTime, Location[] locations) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "exports/ACO_results_" + testName + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=== ANT COLONY OPTIMIZATION TEST RESULTS ===\n");
            writer.write("Test Name: " + testName + "\n");
            writer.write("Problem Type: " + problemType + "\n");
            writer.write("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
            writer.write("Run Time: " + String.format("%.2f", runTime) + " seconds\n");
            writer.write("Number of Locations: " + locations.length + "\n");
            writer.write("Original Fitness: " + String.format("%.2f", originalFitness) + "\n");
            writer.write("Number of Initial Solutions: " + initialSolutions.length + "\n\n");

            // Ghi thông tin initial solutions
            writer.write("INITIAL SOLUTIONS:\n");
            double bestInitialFitness = Double.MAX_VALUE;
            for (int i = 0; i < initialSolutions.length; i++) {
                double fitness = initialSolutions[i].getFitness();
                writer.write("Initial Solution " + (i + 1) + " Fitness: " + String.format("%.2f", fitness) + "\n");
                if (fitness < bestInitialFitness) {
                    bestInitialFitness = fitness;
                }
            }
            writer.write("Best Initial Fitness: " + String.format("%.2f", bestInitialFitness) + "\n\n");

            // Ghi kết quả ACO
            double resultFitness = result.getFitness();
            double improvementFromOriginal = originalFitness - resultFitness;
            double improvementFromInitial = bestInitialFitness - resultFitness;

            writer.write("ACO RESULTS:\n");
            writer.write("Final Fitness: " + String.format("%.2f", resultFitness) + "\n");
            writer.write("Improvement from Original: " + String.format("%.2f", improvementFromOriginal) + 
                       " (" + String.format("%.2f%%", (improvementFromOriginal/originalFitness)*100) + ")\n");
            writer.write("Improvement from Best Initial: " + String.format("%.2f", improvementFromInitial) + 
                       " (" + String.format("%.2f%%", (improvementFromInitial/bestInitialFitness)*100) + ")\n\n");

            // Ghi chi tiết solution cuối cùng
            writer.write("FINAL SOLUTION DETAILS:\n");
            Route[] routes = result.getRoutes();
            int usedRoutes = 0;
            
            for (int i = 0; i < routes.length; i++) {
                if (routes[i].isUse()) {
                    usedRoutes++;
                    writer.write("Route " + (i + 1) + ": " + Arrays.toString(routes[i].getIndLocations()) + 
                               " (distance: " + String.format("%.2f", routes[i].getDistance()) + ")\n");
                }
            }
            
            writer.write("Total Routes Used: " + usedRoutes + "\n");

            System.out.println("📁 Đã lưu kết quả vào: " + filename);

        } catch (IOException e) {
            System.out.println("❌ Lỗi khi lưu file: " + e.getMessage());
        }
    }
}