package org.logistic.algorithm.sa;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.algorithm.AbstractOptimizer;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

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
     */
    public SimulatedAnnealing(Solution solution) {
        super();
        this.initialSolution = solution;
    }

    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);

        // Sử dụng giải pháp ban đầu từ constructor hoặc từ tham số nếu có
        Solution startSolution = initialSolution;
        if (initialSolutions != null && initialSolutions.length > 0) {
            startSolution = initialSolutions[0];
        }

        Set<Solution> population = new HashSet<>();
        population.add(startSolution);
        double temperature = INITIAL_TEMPERATURE;

        Solution currentSolution = startSolution;
        Solution bestSolution = currentSolution.copy();
        double bestEnergy = calculateEnergy(bestSolution.getRoutes());

        while (temperature > FINAL_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution.copy());

                if (!newSolution.equals(currentSolution)) {
                    double currentEnergy = calculateEnergy(currentSolution.getRoutes());
                    double newEnergy = calculateEnergy(newSolution.getRoutes());
                    double deltaEnergy = newEnergy - currentEnergy;

                    if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                        currentSolution = newSolution.copy();
                        if (newEnergy < bestEnergy) {
                            bestSolution = newSolution.copy();
                            bestEnergy = newEnergy;
                        }
                    }
                }
            }

            population.add(currentSolution);
            temperature *= COOLING_RATE;
        }

        // Trả về giải pháp tốt nhất thay vì toàn bộ quần thể
        return bestSolution;
    }

    /**
     * Phương thức này được giữ lại để tương thích ngược với code cũ
     */
    public Solution[] runAndGetPopulation(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
                                          Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);

        // Chạy thuật toán để tìm giải pháp tốt nhất
        run(new Solution[]{initialSolution}, fitnessUtil, checkConditionUtil, locations);

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
        // Lấy các tuyến đường từ giải pháp
        Route[] routes = solution.getRoutes();

        // Quyết định áp dụng toán tử đơn tuyến hoặc đa tuyến
        boolean useMultiRouteOperator = random.nextDouble() < 0.5 && routes.length >= 2;

        if (useMultiRouteOperator) {
            // Áp dụng toán tử đa tuyến (PD-Shift hoặc PD-Exchange)
            applyRandomMultiRouteOperation(routes);

            // Kiểm tra tính khả thi của tất cả các tuyến đường
            for (int i = 0; i < routes.length; i++) {
                if (!checkConditionUtil.isInsertionFeasible(routes[i], locations,
                        routes[i].getMaxPayload())) {
                    // Khôi phục tuyến đường không khả thi
                    routes[i] = solution.getRoutes()[i].copy();
                }
            }
        } else {
            // Chọn ngẫu nhiên một tuyến đường để biến đổi
            int routeIndex = random.nextInt(routes.length);
            Route cloneRoute = routes[routeIndex].copy();

            applyRandomOperation(cloneRoute);

            // Kiểm tra tính khả thi của tuyến đường mới
            if (checkConditionUtil.isInsertionFeasible(cloneRoute, locations, cloneRoute.getMaxPayload())) {
                routes[routeIndex] = cloneRoute; // Cập nhật tuyến đường
            }
        }
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
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
