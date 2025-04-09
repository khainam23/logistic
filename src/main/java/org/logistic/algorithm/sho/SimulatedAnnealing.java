package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimulatedAnnealing {
    static final double INITIAL_TEMPERATURE = 100.0;
    static final double COOLING_RATE = 0.95;
    static final double FINAL_TEMPERATURE = 0.1;
    static final int MAX_ITERATIONS = 100;

    Solution solution;
    Random random;

    public SimulatedAnnealing(Solution solution) {
        this.solution = solution;
        this.random = new Random();
    }

    public Solution[] run(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, Location[] locations) {
        List<Solution> population = new ArrayList<>();
        double temperature = INITIAL_TEMPERATURE;

        Solution currentSolution = solution.copy();
        Solution bestSolution = currentSolution.copy();
        double bestEnergy = calculateEnergy(fitnessUtil, bestSolution.getRoutes(), locations);

        while (temperature > FINAL_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution, checkConditionUtil, locations);

                double currentEnergy = calculateEnergy(fitnessUtil, currentSolution.getRoutes(), locations);
                double newEnergy = calculateEnergy(fitnessUtil, newSolution.getRoutes(), locations);
                double deltaEnergy = newEnergy - currentEnergy;

                if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                    currentSolution = newSolution.copy();
                    if (newEnergy < bestEnergy) {
                        bestSolution = newSolution.copy();
                        bestEnergy = calculateEnergy(fitnessUtil, bestSolution.getRoutes(), locations);
                    }
                }
            }

            population.add(currentSolution.copy());
            temperature *= COOLING_RATE;
        }

        return population.toArray(new Solution[0]);
    }

    private Solution perturbSolution(Solution solution, CheckConditionUtil checkConditionUtil, Location[] locations) {
        Solution newSolution = solution.copy();

        // Chọn ngẫu nhiên một toán tử biến đổi: swap, insert, hoặc reverse
        int operator = random.nextInt(3);

        // Lấy các tuyến đường từ giải pháp
        Route[] routes = solution.getRoutes();

        // Chọn ngẫu nhiên một tuyến đường để biến đổi
        int routeIndex = random.nextInt(routes.length);
        Route route = routes[routeIndex];
        Route cloneRoute = route.copy();

        // Áp dụng toán tử biến đổi được chọn
        switch (operator) {
            case 0: // Swap operator
                cloneRoute = applySwapOperator(cloneRoute);
                break;
            case 1: // Insert operator
                cloneRoute = applyInsertOperator(cloneRoute);
                break;
            case 2: // Reverse operator
                cloneRoute = applyReverseOperator(cloneRoute);
                break;
        }

        if (checkConditionUtil.isInsertionFeasible(cloneRoute, locations, route.getMaxPayload())) {

        }

        return newSolution;
    }

    // Toán tử Swap: hoán đổi vị trí của hai điểm trong tuyến đường
    private Route applySwapOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length - 2) + 1;
        int pos2 = random.nextInt(way.length - 2) + 1;

        // Đảm bảo pos1 khác pos2
        while (pos1 == pos2) {
            pos2 = random.nextInt(way.length - 2) + 1;
        }

        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;

        return route;
    }

    // Toán tử Insert: di chuyển một điểm đến vị trí mới trong tuyến đường
    private Route applyInsertOperator(Route route) {
        // Chọn một điểm để di chuyển (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos = random.nextInt(way.length - 2) + 1;
        int posVal = way[pos];

        // Chọn vị trí mới để chèn điểm (không bao gồm depot ở đầu và cuối)
        int insertPos = random.nextInt(way.length - 2) + 1;

        for (int i = Math.min(insertPos, pos); i <  Math.max(insertPos, pos); i++) {
            int tempVal = way[i];
            way[i] = posVal;
            posVal = tempVal;
        }
        way[Math.max(insertPos, pos)] = posVal;

        return route;
    }

    // Toán tử Reverse: đảo ngược thứ tự của một đoạn trong tuyến đường
    private Route applyReverseOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length - 2) + 1;
        int pos2 = random.nextInt(way.length - 2) + 1;

        // Đảm bảo pos1 < pos2
        if (pos1 > pos2) {
            int temp = pos1;
            pos1 = pos2;
            pos2 = temp;
        }

        // Đảo ngược đoạn từ pos1 đến pos2
        while (pos1 < pos2) {
            int temp = way[pos1];
            way[pos1] = way[pos2];
            way[pos2] = temp;
            pos1++;
            pos2--;
        }

        return route;
    }

    private double calculateEnergy(FitnessUtil fitnessUtil,Route[] route, Location[] locations) {
        return fitnessUtil.calculatorFitness(route, locations);
    }

    private double acceptanceProbability(double deltaEnergy, double temperature) {
        return Math.exp(-deltaEnergy / temperature);
    }
}
