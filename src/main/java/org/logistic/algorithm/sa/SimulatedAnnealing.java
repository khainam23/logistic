package org.logistic.algorithm.sa;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.algorithm.AbstractOptimizer;
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
public class SimulatedAnnealing extends AbstractOptimizer {
    // Các tham số của thuật toán
    static final double INITIAL_TEMPERATURE = 100.0;
    static final double COOLING_RATE = 0.95;
    static final double FINAL_TEMPERATURE = 0.1;
    static final int MAX_ITERATIONS = 1000;

    // Giải pháp ban đầu
    final Solution initialSolution;

    /**
     * Khởi tạo thuật toán Simulated Annealing với giải pháp ban đầu
     * 
     * @param solution Giải pháp ban đầu
     * @param writeLogUtil Tiện ích ghi log
     */
    public SimulatedAnnealing(Solution solution, WriteLogUtil writeLogUtil) {
        super(writeLogUtil);
        this.initialSolution = solution;
        this.writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.SA.getPath());
    }

    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil, 
                      CheckConditionUtil checkConditionUtil, Location[] locations, int currentTarget) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget);
        
        // Ghi lại các tham số ban đầu
        writeLogUtil.info("Starting Simulated Annealing");
        writeLogUtil.info("Initial temperature: " + INITIAL_TEMPERATURE);
        writeLogUtil.info("Cooling rate: " + COOLING_RATE);
        writeLogUtil.info("Final temperature: " + FINAL_TEMPERATURE);
        writeLogUtil.info("Max iterations: " + MAX_ITERATIONS);

        // Sử dụng giải pháp ban đầu từ constructor hoặc từ tham số nếu có
        Solution startSolution = initialSolution;
        if (initialSolutions != null && initialSolutions.length > 0) {
            startSolution = initialSolutions[0];
        }
        
        // Ghi lại tập giải pháp ban đầu
        writeLogUtil.info("Initial solution: ");
        writeLogUtil.info(startSolution.toString());

        Set<Solution> population = new HashSet<>();
        population.add(startSolution);
        double temperature = INITIAL_TEMPERATURE;

        Solution currentSolution = startSolution;
        Solution bestSolution = currentSolution.copy();
        double bestEnergy = calculateEnergy(bestSolution.getRoutes());

        writeLogUtil.info("*".repeat(20));

        while (temperature > FINAL_TEMPERATURE) {
            // Ghi lại từng quá trình
            writeLogUtil.info("Temperature: " + temperature);

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution.copy());

               if(!newSolution.equals(currentSolution)) { // Không nên kiểm tra trùng vì có thể giảm khả năng khám phá
                   // Ghi lại nếu có sự thay đổi
                   writeLogUtil.warn("New solution is different from current solution");
                   writeLogUtil.info("New solution: ");
                   writeLogUtil.info(newSolution.toString());

                   double currentEnergy = calculateEnergy(currentSolution.getRoutes());
                   double newEnergy = calculateEnergy(newSolution.getRoutes());
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
        writeLogUtil.info("Simulated Annealing completed");
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
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget);
        
        // Chạy thuật toán để tìm giải pháp tốt nhất
        run(new Solution[]{initialSolution}, fitnessUtil, checkConditionUtil, locations, currentTarget);
        
        // Tạo tập quần thể
        Set<Solution> population = new HashSet<>();
        population.add(initialSolution);
        double temperature = INITIAL_TEMPERATURE;
        Solution currentSolution = initialSolution;
        
        while (temperature > FINAL_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution.copy());
                double currentEnergy = calculateEnergy(currentSolution.getRoutes());
                double newEnergy = calculateEnergy(newSolution.getRoutes());
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

    /**
     * Biến đổi giải pháp bằng cách áp dụng một toán tử ngẫu nhiên
     * 
     * @param solution Giải pháp cần biến đổi
     * @return Giải pháp mới sau khi biến đổi
     */
    private Solution perturbSolution(Solution solution) {
        // Chọn ngẫu nhiên một toán tử biến đổi: swap, insert, hoặc reverse
        int operator = random.nextInt(3);

        // Lấy các tuyến đường từ giải pháp
        Route[] routes = solution.getRoutes();

        // Chọn ngẫu nhiên một tuyến đường để biến đổi
        int routeIndex = random.nextInt(routes.length);
        Route cloneRoute = routes[routeIndex].copy();

        // Áp dụng toán tử biến đổi
        switch (operator) {
            case 0 -> applySwapOperator(cloneRoute);
            case 1 -> applyInsertOperator(cloneRoute);
            case 2 -> applyReverseOperator(cloneRoute);
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        }

        // Kiểm tra tính khả thi của tuyến đường mới
        if (checkConditionUtil.isInsertionFeasible(cloneRoute, locations, cloneRoute.getMaxPayload(), currentTarget)) {
            routes[routeIndex] = cloneRoute; // Cập nhật tuyến đường
        }

        return solution;
    }

    // Các phương thức applySwapOperator, applyInsertOperator, applyReverseOperator
    // đã được chuyển lên lớp cha AbstractOptimizer

    /**
     * Tính toán năng lượng (fitness) của một tập tuyến đường
     * 
     * @param routes Tập tuyến đường cần tính năng lượng
     * @return Giá trị năng lượng
     */
    private double calculateEnergy(Route[] routes) {
        return fitnessUtil.calculatorFitness(routes, locations);
    }

    /**
     * Tính xác suất chấp nhận một giải pháp tệ hơn
     * 
     * @param deltaEnergy Chênh lệch năng lượng
     * @param temperature Nhiệt độ hiện tại
     * @return Xác suất chấp nhận
     */
    private double acceptanceProbability(double deltaEnergy, double temperature) {
        return Math.exp(-deltaEnergy / temperature);
    }
}
