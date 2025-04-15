package org.logistic.algorithm.sa;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.algorithm.Optimizer;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.WriteLogUtil;

import java.util.*;

/**
 * Thuật toán Simulated Annealing
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimulatedAnnealing implements Optimizer {
    static final double INITIAL_TEMPERATURE = 100.0;
    static double COOLING_RATE = 0.95;
    static final double FINAL_TEMPERATURE = 0.1;
    static final int MAX_ITERATIONS = 1000;

    final Solution solution;
    final Random random;
    final WriteLogUtil writeLogUtil;

    public SimulatedAnnealing(Solution solution, WriteLogUtil writeLogUtil) {
        this.solution = solution;
        this.random = new Random();
        this.writeLogUtil = writeLogUtil;
        this.writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.SA.getPath());
    }

    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil, 
                      CheckConditionUtil checkConditionUtil, Location[] locations, int currentTarget) {
        // Ghi lại các tham số ban đầu
        writeLogUtil.info("Initial temperature: " + INITIAL_TEMPERATURE);
        writeLogUtil.info("Cooling rate: " + COOLING_RATE);
        writeLogUtil.info("Final temperature: " + FINAL_TEMPERATURE);
        writeLogUtil.info("Max iterations: " + MAX_ITERATIONS);

        // Ghi lại tập giải pháp ban đầu
        writeLogUtil.info("Initial solution: ");
        writeLogUtil.info(solution.toString());

        Set<Solution> population = new HashSet<>();
        population.add(solution);
        double temperature = INITIAL_TEMPERATURE;

        Solution currentSolution = solution;
        Solution bestSolution = currentSolution;
        double bestEnergy = calculateEnergy(fitnessUtil, bestSolution.getRoutes(), locations);

        writeLogUtil.info("*".repeat(20));

        while (temperature > FINAL_TEMPERATURE) {
            // Ghi lại từng quá trình
            writeLogUtil.info("Temperature: " + temperature);

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution.copy(), checkConditionUtil, locations, currentTarget);

               if(!newSolution.equals(currentSolution)) { // Không nên kiểm tra trùng vì có thể giảm khả năng khám phá
                   // Ghi lại nếu có sự thay đổi
                   writeLogUtil.warn("New solution is different from current solution");
                   writeLogUtil.info("New solution: ");
                   writeLogUtil.info(newSolution.toString());

                   double currentEnergy = calculateEnergy(fitnessUtil, currentSolution.getRoutes(), locations);
                   double newEnergy = calculateEnergy(fitnessUtil, newSolution.getRoutes(), locations);
                   double deltaEnergy = newEnergy - currentEnergy;

                   if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                       currentSolution = newSolution.copy();
                       if (newEnergy < bestEnergy) {
                           bestSolution = newSolution.copy();
                           bestEnergy = newEnergy;

                           // Ghi lại giải pháp tốt nhất của vòng này
                           writeLogUtil.info("New best solution: ");
                           writeLogUtil.info(bestSolution.toString());
                           writeLogUtil.info("New best energy: " + bestEnergy);
                       }
                   }
               }
            }

            population.add(currentSolution);
            temperature *= COOLING_RATE;
        }

        writeLogUtil.info("*".repeat(20));

        // Ghi lại kết quả tìm được
        writeLogUtil.info("Best solution: ");
        writeLogUtil.info(bestSolution.toString());
        writeLogUtil.info("Best energy: " + bestEnergy);

        // Trả về giải pháp tốt nhất thay vì toàn bộ quần thể
        return bestSolution;
    }
    
    /**
     * Phương thức này được giữ lại để tương thích ngược với code cũ
     */
    public Solution[] runAndGetPopulation(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, 
                                        Location[] locations, int currentTarget) {
        run(new Solution[]{solution}, fitnessUtil, checkConditionUtil, locations, currentTarget);
        
        Set<Solution> population = new HashSet<>();
        population.add(solution);
        double temperature = INITIAL_TEMPERATURE;
        Solution currentSolution = solution;
        
        while (temperature > FINAL_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution.copy(), checkConditionUtil, locations, currentTarget);
                double currentEnergy = calculateEnergy(fitnessUtil, currentSolution.getRoutes(), locations);
                double newEnergy = calculateEnergy(fitnessUtil, newSolution.getRoutes(), locations);
                double deltaEnergy = newEnergy - currentEnergy;

                if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                    currentSolution = newSolution.copy();
                }
            }
            population.add(currentSolution);
            temperature *= COOLING_RATE;
        }
        
        return population.toArray(new Solution[0]);
    }

    private Solution perturbSolution(Solution solution, CheckConditionUtil checkConditionUtil, Location[] locations, int currentTarget) {
        // Chọn ngẫu nhiên một toán tử biến đổi: swap, insert, hoặc reverse
        int operator = random.nextInt(3);

        // Lấy các tuyến đường từ giải pháp
        Route[] routes = solution.getRoutes(); // Tạo một bản sao của routes

        // Chọn ngẫu nhiên một tuyến đường để biến đổi
        int routeIndex = random.nextInt(routes.length);
        Route cloneRoute = routes[routeIndex].copy();

        switch (operator) {
            case 0 -> applySwapOperator(cloneRoute);
            case 1 -> applyInsertOperator(cloneRoute);
            case 2 -> applyReverseOperator(cloneRoute);
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        }

        if (checkConditionUtil.isInsertionFeasible(cloneRoute, locations, cloneRoute.getMaxPayload(), currentTarget)) {
            routes[routeIndex] = cloneRoute; // Cập nhật
        }

        return solution;
    }

    // Toán tử Swap: hoán đổi vị trí của hai điểm trong tuyến đường
    private void applySwapOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);

        // Đảm bảo pos1 khác pos2
        while (pos1 == pos2) {
            pos2 = random.nextInt(way.length);
        }

        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;

    }

    // Toán tử Insert: di chuyển một điểm đến vị trí mới trong tuyến đường
    private void applyInsertOperator(Route route) {
        // Chọn một điểm để di chuyển (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos = random.nextInt(way.length);

        // Chọn vị trí mới để chèn điểm (không bao gồm depot ở đầu và cuối)
        int insertPos = random.nextInt(way.length);
        int posVal = way[Math.max(insertPos, pos)];

        for (int i = Math.min(insertPos, pos); i <= Math.max(insertPos, pos); i++) {
            int tempVal = way[i];
            way[i] = posVal;
            posVal = tempVal;
        }
    }

    // Toán tử Reverse: đảo ngược thứ tự của một đoạn trong tuyến đường
    private void applyReverseOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);

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

    }

    private double calculateEnergy(FitnessUtil fitnessUtil, Route[] route, Location[] locations) {
        return fitnessUtil.calculatorFitness(route, locations);
    }

    private double acceptanceProbability(double deltaEnergy, double temperature) {
        return Math.exp(-deltaEnergy / temperature);
    }
}
