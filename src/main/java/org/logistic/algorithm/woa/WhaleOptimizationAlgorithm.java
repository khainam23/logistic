package org.logistic.algorithm.woa;

import java.util.ArrayList;
import java.util.List;

import org.logistic.algorithm.AbstractOptimizer;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

public class WhaleOptimizationAlgorithm extends AbstractOptimizer {
    // Các tham số của thuật toán
    static final int MAX_ITERATIONS = 100;
    static final int b = 1; // Xác định hình dạng xoắn óc (constant defining the logarithmic spiral)
    Whale bestWhale;
    List<Whale> population;

    /**
     * Khởi tạo optimizer
     */
    public WhaleOptimizationAlgorithm() {
        super();
    }

    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);

        // Khởi tạo quần thể
        initialize(initialSolutions);

        // Vòng lặp chính
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Hệ số a giảm tuyến tính từ 2 về 0
            double a = 2 * (1 - (double) iteration / MAX_ITERATIONS);

            // Cập nhật vị trí từng cá voi
            for (Whale whale : population) {
                updatePositionWhale(whale, a);
            }
        }

        return bestWhale.getSolution();
    }

    public void updatePositionWhale(Whale whale, double a) {
        Solution currentSolution = whale.getSolution();
        Solution bestSolution = bestWhale.getSolution();

        // Tạo giải pháp mới
        Solution newSolution = currentSolution.copy();
        Route[] routes = newSolution.getRoutes();

        // Số chiều (số tuyến đường)
        int dimensions = routes.length;

        double[] A = calculatorAVector(dimensions, a);
        double[] C = calculatorCVector(dimensions);

        Route[] bestRoute = bestSolution.getRoutes();

        for (int i = 0; i < dimensions; i++) {
            double p = random.nextDouble();

            if (p < 0.5) {
                if (Math.abs(A[i]) < 1) {
                    // Bao vây con mồi
                    encirclingPrey(routes[i], bestRoute[i], A[i], C[i]);
                } else {
                    // Khám phá
                    double q = random.nextDouble();

                    if (q < 0.5) {
                        applyRandomOperation(routes[i]); // Khám phá giải pháp liền kề
                    } else {
                        applyRandomMultiRouteOperation(routes); // Khám phá giải pháp hoàn toàn mới
                    }
                }
            } else {
                // Cập nhật theo xoắn ốc
                spiralMovement(routes[i], bestRoute[i], C[i]);
            }

            // Kiểm tra tính khả thi
            if (!checkConditionUtil.isInsertionFeasible(routes[i], locations,
                    routes[i].getMaxPayload())) {
                routes[i] = currentSolution.getRoutes()[i].copy();
            }
        }

        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(routes, locations);
        newSolution.setFitness(newFitness);

        // Cập nhật nếu tốt hơn
        if (newFitness < whale.getFitness()) {
            whale.setSolution(newSolution);
            whale.setFitness(newFitness);

            // Cập nhật cá voi tốt nhất
            if (newFitness < bestWhale.getFitness()) {
                bestWhale = new Whale(newSolution.copy(), newFitness);
                System.out.println("New best solution found with fitness: " + newFitness);
            }
        }
    }

    public double[] calculatorAVector(int dimensions, double a) {
        double[] A = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            A[i] = 2 * a * random.nextDouble() - a;
        }
        return A;
    }

    public double[] calculatorCVector(int dimensions) {
        double[] C = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            C[i] = 2 * random.nextDouble();
        }
        return C;
    }

    public void encirclingPrey(Route route, Route bestRoute, double A, double C) {
        int[] way = route.getIndLocations();
        int[] bestWay = bestRoute.getIndLocations();
        int minLength = Math.min(way.length, bestWay.length);

        for (int i = 0; i < minLength; i++) {
            double D = Math.abs(C * bestWay[i] - way[i]); // D = |C * X_best - X_i|

            double newPos = bestWay[i] - A * D;
            int adjustedPos = (int) Math.round(newPos); // X_best - A * D

            // Kiểm tra tính hợp lệ của chỉ số location
            if (locations != null && adjustedPos >= 1 && adjustedPos < locations.length) {
                way[i] = adjustedPos;
            }
            // Nếu không hợp lệ, giữ nguyên giá trị cũ
        }
    }

    public void spiralMovement(Route route, Route bestRoute, double C) {
        int[] way = route.getIndLocations();
        int[] bestWay = bestRoute.getIndLocations();
        int minLength = Math.min(way.length, bestWay.length);

        for (int i = 0; i < minLength; i++) {
            double D_ = Math.abs(bestWay[i] - way[i]); // D' = C * X_best - X_i

            double l = random.nextDouble(-1, 1);
            double newPos = D_ * Math.exp(b * l) * Math.cos(2 * Math.PI * l) + bestWay[i]; // D' * e^(bl) * cos(2pi * l) + X_best
            int adjustedPos = (int) Math.round(newPos);

            // Kiểm tra tính hợp lệ của chỉ số location
            if (locations != null && adjustedPos >= 1 && adjustedPos < locations.length) {
                way[i] = adjustedPos;
            }
            // Nếu không hợp lệ, giữ nguyên giá trị cũ
        }
    }

    public void initialize(Solution[] initialSolutions) {
        population = new ArrayList<>();

        // Khởi tạo quần thể từ các giải pháp ban đầu
        for (Solution solution : initialSolutions) {
            Whale whale = new Whale(solution.copy(), solution.getFitness());
            population.add(whale);

            // Cập nhật cá voi tốt nhất
            if (bestWhale == null || whale.getFitness() < bestWhale.getFitness()) {
                bestWhale = whale;
            }
        }
    }
}
