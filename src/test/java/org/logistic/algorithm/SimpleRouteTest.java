package org.logistic.algorithm;

import org.logistic.algorithm.aco.AntColonyOptimization;
import org.logistic.algorithm.gwo.GreyWolfOptimizer;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.algorithm.woa.WhaleOptimizationAlgorithm;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.util.Arrays;

/**
 * Test đơn giản chỉ in ra routes
 */
public class SimpleRouteTest {

    public static void main(String[] args) {
        System.out.println("=== TEST ROUTES ===");

        // Đọc dữ liệu
        ReadDataFromFile dataReader = new ReadDataFromFile();
        dataReader.readProblemData("data/Wang_Chen/src/rcdp0501.txt", ReadDataFromFile.ProblemType.VRPSPDTW_WANG_CHEN);

        Location[] locations = dataReader.getLocations();
        double maxCapacity = dataReader.getMaxCapacity();

        if (locations == null) {
            System.out.println("Không đọc được dữ liệu");
            return;
        }

        System.out.println("Đã đọc " + locations.length + " locations");
        System.out.println("Max capacity: " + maxCapacity);

        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();

        // Đọc solution từ file
        dataReader.readSolution("data/Wang_Chen/solution/rcdp0501.txt");
        Route[] solutionRoutes = dataReader.getRoutes();

        Solution[] initialSolutions;
        System.out.println("Đã đọc " + solutionRoutes.length + " routes từ solution file");

        // In solution từ file
        System.out.println("\n=== SOLUTION TỪ FILE ===");
        for (int i = 0; i < solutionRoutes.length; i++) {
            int[] routeLocs = solutionRoutes[i].getIndLocations();
            System.out.println("Route " + (i + 1) + ": " + Arrays.toString(routeLocs));
        }
        

        // Tạo initial solutions từ solution đã đọc bằng SA
        Solution mainSolution = new Solution(solutionRoutes, fitnessUtil.calculatorFitness(solutionRoutes, locations));
        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution);
        initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations);

        System.out.println("SA đã tạo " + initialSolutions.length + " initial solutions");
        
        // In chi tiết thông tin fitness của solution gốc
        printDetailedFitnessInfo("SOLUTION GỐC", mainSolution, fitnessUtil, locations);

        // Test SHO
        testAlgorithm("SHO", new SpottedHyenaOptimizer(), initialSolutions, fitnessUtil, checkConditionUtil, locations);

        // Test ACO
        testAlgorithm("ACO", new AntColonyOptimization(), initialSolutions, fitnessUtil, checkConditionUtil, locations);

        // Test GWO
        testAlgorithm("GWO", new GreyWolfOptimizer(), initialSolutions, fitnessUtil, checkConditionUtil, locations);

        // Test WOA
        testAlgorithm("WOA", new WhaleOptimizationAlgorithm(), initialSolutions, fitnessUtil, checkConditionUtil,
                locations);
    }

    private static void testAlgorithm(String name, Optimizer optimizer, Solution[] initialSolutions,
            FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, Location[] locations) {

        System.out.println("\n--- " + name + " ---");

        try {
            Solution result = optimizer.run(initialSolutions, fitnessUtil, checkConditionUtil, locations);

            // In chi tiết thông tin fitness
            printDetailedFitnessInfo(name, result, fitnessUtil, locations);

            System.out.println("Routes:");
            Route[] routes = result.getRoutes();
            for (int i = 0; i < routes.length; i++) {
                int[] routeLocs = routes[i].getIndLocations();
                System.out.println("  Route " + (i + 1) + ": " + Arrays.toString(routeLocs));
            }

        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    /**
     * In chi tiết thông tin tính toán fitness
     */
    private static void printDetailedFitnessInfo(String algorithmName, Solution solution, 
            FitnessUtil fitnessUtil, Location[] locations) {
        
        System.out.println("\n=== CHI TIẾT FITNESS - " + algorithmName + " ===");
        
        // Tính toán các thành phần fitness
        int[] weights = fitnessUtil.calculateWeightsFromSolution(solution, locations);
        
        int numberVehicle = weights[0];
        int totalDistances = weights[1];
        int totalServiceTime = weights[2];
        int totalWaitingTime = weights[3];
        
        System.out.println("Số phương tiện (Number of Vehicles): " + numberVehicle);
        System.out.println("Tổng khoảng cách (Total Distance): " + totalDistances);
        System.out.println("Tổng thời gian phục vụ (Total Service Time): " + totalServiceTime);
        System.out.println("Tổng thời gian chờ (Total Waiting Time): " + totalWaitingTime);
        
        // Tính fitness theo công thức: alpha*distance + beta*serviceTime + gamma*waitingTime + delta*vehicles
        // Với DefaultFitnessStrategy: alpha=beta=gamma=delta=1.0
        double calculatedFitness = 1.0 * totalDistances + 1.0 * totalServiceTime + 
                                  1.0 * totalWaitingTime + 1.0 * numberVehicle;
        
        System.out.println("Fitness được tính: " + String.format("%.2f", calculatedFitness));
        System.out.println("Fitness từ solution: " + String.format("%.2f", solution.getFitness()));
        
        // Phân tích tỷ lệ đóng góp của từng thành phần
        System.out.println("\n--- PHÂN TÍCH ĐÓNG GÓP ---");
        System.out.println("Khoảng cách: " + String.format("%.1f%%", (totalDistances / calculatedFitness) * 100));
        System.out.println("Thời gian phục vụ: " + String.format("%.1f%%", (totalServiceTime / calculatedFitness) * 100));
        System.out.println("Thời gian chờ: " + String.format("%.1f%%", (totalWaitingTime / calculatedFitness) * 100));
        System.out.println("Số phương tiện: " + String.format("%.1f%%", (numberVehicle / calculatedFitness) * 100));
        
        // Thông tin trung bình
        if (numberVehicle > 0) {
            System.out.println("\n--- THÔNG TIN TRUNG BÌNH ---");
            System.out.println("Khoảng cách trung bình/xe: " + String.format("%.2f", (double)totalDistances / numberVehicle));
            System.out.println("Thời gian phục vụ trung bình/xe: " + String.format("%.2f", (double)totalServiceTime / numberVehicle));
            System.out.println("Thời gian chờ trung bình/xe: " + String.format("%.2f", (double)totalWaitingTime / numberVehicle));
        }
    }
}