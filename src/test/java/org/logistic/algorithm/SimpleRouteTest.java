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
        dataReader.readProblemData("data/vrptw/src/c101.txt", ReadDataFromFile.ProblemType.VRPTW);

        Location[] locations = dataReader.getLocations();
        int maxCapacity = dataReader.getMaxCapacity();

        if (locations == null) {
            System.out.println("Không đọc được dữ liệu");
            return;
        }

        System.out.println("Đã đọc " + locations.length + " locations");
        System.out.println("Max capacity: " + maxCapacity);

        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();

        // Đọc solution từ file
        dataReader.readSolution("data/vrptw/solution/c101.txt");
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
        initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations, maxCapacity);

        System.out.println("SA đã tạo " + initialSolutions.length + " initial solutions");

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
            Solution result = optimizer.run(initialSolutions, fitnessUtil, checkConditionUtil, locations, 0);

            System.out.println("Fitness: " + String.format("%.2f", result.getFitness()));
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
}