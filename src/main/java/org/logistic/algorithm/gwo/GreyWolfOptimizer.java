package org.logistic.algorithm.gwo;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.algorithm.AbstractOptimizer;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;


import java.util.ArrayList;
import java.util.List;

/**
 * Thuật toán Grey Wolf Optimizer
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GreyWolfOptimizer extends AbstractOptimizer {
    // Các tham số của thuật toán
    static final int MAX_ITERATIONS = 100;

    // Danh sách các sói
    List<Wolf> population;
    Wolf alpha; // Sói alpha (tốt nhất)
    Wolf beta;  // Sói beta (tốt thứ hai)
    Wolf delta; // Sói delta (tốt thứ ba)

    /**
     * Khởi tạo thuật toán Grey Wolf Optimizer
     */
    public GreyWolfOptimizer() {
        super();
    }

    /**
     * Khởi tạo quần thể sói từ các giải pháp ban đầu
     */

    private void initialize(Solution[] initialSolutions) {
        population = new ArrayList<>();

        // Khởi tạo quần thể từ các giải pháp ban đầu
        for (Solution solution : initialSolutions) {
            Wolf wolf = new Wolf(solution.copy(), solution.getFitness());
            population.add(wolf);
        }

        // Sắp xếp quần thể theo fitness
        population.sort((w1, w2) -> Double.compare(w1.getFitness(), w2.getFitness()));

        // Xác định sói alpha, beta và delta
        if (!population.isEmpty()) {
            alpha = new Wolf(population.get(0).getSolution().copy(), population.get(0).getFitness());
            
            if (population.size() > 1) {
                beta = new Wolf(population.get(1).getSolution().copy(), population.get(1).getFitness());
            } else {
                beta = alpha;
            }
            
            if (population.size() > 2) {
                delta = new Wolf(population.get(2).getSolution().copy(), population.get(2).getFitness());
            } else {
                delta = beta;
            }
        }
    }

    /**
     * Tính toán vector A (hệ số điều chỉnh bao vây)
     */
    private double[] calculateAVector(int dimensions, double a) {
        double[] A = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            // Công thức GWO: A = 2a * r1 - a, với r1 là số ngẫu nhiên trong [0,1]
            A[i] = 2 * a * random.nextDouble() - a;
        }
        return A;
    }

    /**
     * Tính toán vector C (hệ số điều chỉnh khoảng cách)
     */
    private double[] calculateCVector(int dimensions) {
        double[] C = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            // Công thức GWO: C = 2 * r2, với r2 là số ngẫu nhiên trong [0,1]
            C[i] = 2 * random.nextDouble();
        }
        return C;
    }

    /**
     * Cập nhật vị trí của sói theo thuật toán GWO
     */

    private void updateWolfPosition(Wolf wolf, double a) {
        Solution currentSolution = wolf.getSolution();
        
        // Tạo giải pháp mới
        Solution newSolution = currentSolution.copy();
        Route[] routes = newSolution.getRoutes();
        
        // Số chiều (số tuyến đường)
        int dimensions = routes.length;
        
        // Tính toán vector A và C
        double[] A1 = calculateAVector(dimensions, a);
        double[] A2 = calculateAVector(dimensions, a);
        double[] A3 = calculateAVector(dimensions, a);
        
        double[] C1 = calculateCVector(dimensions);
        double[] C2 = calculateCVector(dimensions);
        double[] C3 = calculateCVector(dimensions);
        
        // Cập nhật từng tuyến đường (từng chiều)
        for (int i = 0; i < dimensions; i++) {
            Route currentRoute = routes[i];
            
            // Tính toán khoảng cách đến sói alpha, beta và delta
            double D_alpha = calculateRouteDistance(currentRoute, alpha.getSolution().getRoutes()[i], C1[i]);
            double D_beta = calculateRouteDistance(currentRoute, beta.getSolution().getRoutes()[i], C2[i]);
            double D_delta = calculateRouteDistance(currentRoute, delta.getSolution().getRoutes()[i], C3[i]);
            
            // Tính toán vị trí mới theo hướng dẫn của sói alpha, beta và delta
            Route X1 = moveTowardsLeader(alpha.getSolution().getRoutes()[i], D_alpha, A1[i]);
            Route X2 = moveTowardsLeader(beta.getSolution().getRoutes()[i], D_beta, A2[i]);
            Route X3 = moveTowardsLeader(delta.getSolution().getRoutes()[i], D_delta, A3[i]);
            
            // Cập nhật vị trí mới là trung bình của ba vị trí
            updateRouteFromLeaders(currentRoute, X1, X2, X3);
            
            // Kiểm tra tính khả thi
            if (!checkConditionUtil.isInsertionFeasible(currentRoute, locations,
                    currentRoute.getMaxPayload())) {
                routes[i] = currentSolution.getRoutes()[i].copy();
            }
        }
        
        // Áp dụng các toán tử đa tuyến với xác suất 30%
        if (random.nextDouble() < 0.3) {
            applyRandomMultiRouteOperation(routes);
            
            // Kiểm tra tính khả thi sau khi áp dụng toán tử đa tuyến
            for (int i = 0; i < dimensions; i++) {
                if (!checkConditionUtil.isInsertionFeasible(routes[i], locations,
                        routes[i].getMaxPayload())) {
                    routes[i] = currentSolution.getRoutes()[i].copy();
                }
            }
        }
        
        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(routes, locations);
        newSolution.setFitness(newFitness);
        
        // Cập nhật nếu tốt hơn
        if (newFitness < wolf.getFitness()) {
            wolf.setSolution(newSolution);
            wolf.setFitness(newFitness);
            
            // Cập nhật thứ bậc sói
            updateHierarchy(wolf);
        }
    }

    /**
     * Cập nhật thứ bậc sói (alpha, beta, delta)
     */
    private void updateHierarchy(Wolf wolf) {
        if (wolf.getFitness() < alpha.getFitness()) {
            delta = beta;
            beta = alpha;
            alpha = new Wolf(wolf.getSolution().copy(), wolf.getFitness());
            System.out.println("New alpha wolf with fitness: " + alpha.getFitness());
        } else if (wolf.getFitness() < beta.getFitness()) {
            delta = beta;
            beta = new Wolf(wolf.getSolution().copy(), wolf.getFitness());
        } else if (wolf.getFitness() < delta.getFitness()) {
            delta = new Wolf(wolf.getSolution().copy(), wolf.getFitness());
        }
    }

    /**
     * Tính toán khoảng cách giữa hai tuyến đường theo công thức GWO
     */
    private double calculateRouteDistance(Route route, Route leaderRoute, double C) {
        int[] way = route.getIndLocations();
        int[] leaderWay = leaderRoute.getIndLocations();
        int minLength = Math.min(way.length, leaderWay.length);
        
        double distance = 0;
        for (int i = 0; i < minLength; i++) {
            // Công thức GWO: D = |C * X_leader - X|
            distance += Math.abs(C * leaderWay[i] - way[i]);
        }
        return distance / minLength;
    }

    /**
     * Di chuyển về phía sói lãnh đạo theo công thức GWO
     */
    private Route moveTowardsLeader(Route leaderRoute, double D, double A) {
        Route newRoute = leaderRoute.copy();
        int[] leaderWay = leaderRoute.getIndLocations();
        int[] newWay = newRoute.getIndLocations();
        
        // Xác định giới hạn trên cho chỉ số location
        int maxLocationIndex = locations.length - 1;
        
        for (int i = 0; i < newWay.length; i++) {
            // Công thức GWO: X_new = X_leader - A * D
            double newPos = leaderWay[i] - A * D;
            
            // Làm tròn và giới hạn trong phạm vi hợp lệ [0, maxLocationIndex]
            int adjustedPos = (int) Math.round(newPos);
            if (adjustedPos >= 0 && adjustedPos <= maxLocationIndex) {
                newWay[i] = adjustedPos;
            } else if (adjustedPos > maxLocationIndex) {
                // Nếu vượt quá giới hạn, gán bằng giới hạn
                newWay[i] = maxLocationIndex;
            }
        }
        
        return newRoute;
    }

    /**
     * Cập nhật tuyến đường từ ba sói lãnh đạo
     */
    private void updateRouteFromLeaders(Route route, Route X1, Route X2, Route X3) {
        int[] way = route.getIndLocations();
        int[] way1 = X1.getIndLocations();
        int[] way2 = X2.getIndLocations();
        int[] way3 = X3.getIndLocations();
        
        int minLength = Math.min(way.length, Math.min(way1.length, Math.min(way2.length, way3.length)));
        
        // Xác định giới hạn trên cho chỉ số location
        int maxLocationIndex = locations.length - 1;
        
        for (int i = 0; i < minLength; i++) {
            // Công thức GWO: X_new = (X1 + X2 + X3) / 3
            double newPos = (way1[i] + way2[i] + way3[i]) / 3.0;
            
            // Làm tròn và giới hạn trong phạm vi hợp lệ [0, maxLocationIndex]
            int adjustedPos = (int) Math.round(newPos);
            if (adjustedPos >= 0 && adjustedPos <= maxLocationIndex) {
                way[i] = adjustedPos;
            } else if (adjustedPos > maxLocationIndex) {
                // Nếu vượt quá giới hạn, gán bằng giới hạn
                way[i] = maxLocationIndex;
            }
        }
        
        // Áp dụng toán tử ngẫu nhiên để đa dạng hóa
        if (random.nextDouble() < 0.5) {
            applyRandomOperation(route);
        }
    }

    /**
     * Chạy thuật toán Grey Wolf Optimizer
     */
    @Override

    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);
        
        // Khởi tạo quần thể
        initialize(initialSolutions);
        
        // Vòng lặp chính
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Hệ số a giảm tuyến tính từ 2 về 0
            double a = 2 - iteration * (2.0 / MAX_ITERATIONS);
            
            // Cập nhật vị trí từng sói
            for (Wolf wolf : population) {
                updateWolfPosition(wolf, a);
            }
            
            // Đa dạng hóa quần thể định kỳ
            if (iteration % 10 == 0) {
                diversifyPopulation();
            }
        }
        
        return alpha.getSolution();
    }

    /**
     * Đa dạng hóa quần thể
     */

    private void diversifyPopulation() {
        // Sắp xếp quần thể theo fitness
        population.sort((w1, w2) -> Double.compare(w1.getFitness(), w2.getFitness()));
        
        // Giữ lại 30% sói tốt nhất, thay thế phần còn lại
        int keepCount = (int) (population.size() * 0.3);
        
        for (int i = keepCount; i < population.size(); i++) {
            Solution newSolution = createDiversifiedSolution();
            double newFitness = fitnessUtil.calculatorFitness(newSolution.getRoutes(), locations);
            population.get(i).setSolution(newSolution);
            population.get(i).setFitness(newFitness);
            
            // Cập nhật thứ bậc sói nếu cần
            if (newFitness < alpha.getFitness()) {
                updateHierarchy(population.get(i));
            }
        }
    }

    /**
     * Tạo giải pháp đa dạng hóa
     */
    private Solution createDiversifiedSolution() {
        // Chọn ngẫu nhiên từ sói alpha, beta hoặc delta
        Wolf leader;
        double rand = random.nextDouble();
        if (rand < 0.6) {
            leader = alpha; // Ưu tiên học từ alpha
        } else if (rand < 0.8) {
            leader = beta;
        } else {
            leader = delta;
        }
        
        Solution newSolution = leader.getSolution().copy();
        Route[] routes = newSolution.getRoutes();
        
        // Áp dụng các toán tử đơn tuyến
        for (Route route : routes) {
            int operations = 1 + random.nextInt(2);
            for (int i = 0; i < operations; i++) {
                applyRandomOperation(route);
            }
        }
        
        // Áp dụng các toán tử đa tuyến (PD-Shift và PD-Exchange)
        int multiRouteOperations = 1 + random.nextInt(2);
        for (int i = 0; i < multiRouteOperations; i++) {
            applyRandomMultiRouteOperation(routes);
        }
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
        }
        
        return newSolution;
    }
}
