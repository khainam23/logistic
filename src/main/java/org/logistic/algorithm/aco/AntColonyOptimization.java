package org.logistic.algorithm.aco;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.logistic.algorithm.AbstractOptimizer;
import org.logistic.algorithm.Agent;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Thuật toán Ant Colony Optimization
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AntColonyOptimization extends AbstractOptimizer {
    // Các tham số của thuật toán
    static final int MAX_ITERATIONS = 100;
    static final int COLONY_SIZE = 20; // Số lượng kiến trong đàn
    static final double ALPHA = 1.0; // Hệ số ảnh hưởng của pheromone
    static final double BETA = 2.0; // Hệ số ảnh hưởng của heuristic (khoảng cách)
    static final double RHO = 0.1; // Tốc độ bay hơi pheromone
    static final double Q = 100.0; // Hằng số chất lượng pheromone

    // Danh sách các kiến
    List<Ant> colony;
    Ant bestAnt; // Kiến có fitness tốt nhất

    // Ma trận pheromone
    double[][] pheromone;
    double[][] heuristic; // Ma trận heuristic (khoảng cách nghịch đảo)
    
    int numLocations; // Số lượng địa điểm

    /**
     * Khởi tạo thuật toán Ant Colony Optimization
     */
    public AntColonyOptimization() {
        super();
    }

    /**
     * Khởi tạo đàn kiến từ các giải pháp ban đầu
     */
    private void initialize(Solution[] initialSolutions) {
        colony = new ArrayList<>();
        numLocations = locations.length;

        // Khởi tạo ma trận pheromone và heuristic
        initializeMatrices();

        // Khởi tạo đàn kiến từ các giải pháp ban đầu
        for (Solution solution : initialSolutions) {
            Ant ant = new Ant(solution.copy(), solution.getFitness());
            colony.add(ant);

            // Cập nhật kiến tốt nhất
            if (bestAnt == null || ant.getFitness() < bestAnt.getFitness()) {
                bestAnt = new Ant(solution.copy(), solution.getFitness());
            }
        }

        // Thêm kiến mới nếu cần để đạt đủ kích thước đàn
        while (colony.size() < COLONY_SIZE) {
            Solution newSolution = createRandomSolution(initialSolutions[0]);
            double newFitness = fitnessUtil.calculatorFitness(newSolution.getRoutes(), locations);
            newSolution.setFitness(newFitness);
            
            Ant ant = new Ant(newSolution, newFitness);
            colony.add(ant);

            // Cập nhật kiến tốt nhất
            if (ant.getFitness() < bestAnt.getFitness()) {
                bestAnt = new Ant(newSolution.copy(), newFitness);
            }
        }
    }

    /**
     * Khởi tạo ma trận pheromone và heuristic
     */
    private void initializeMatrices() {
        pheromone = new double[numLocations][numLocations];
        heuristic = new double[numLocations][numLocations];

        // Khởi tạo pheromone với giá trị nhỏ đồng đều
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                if (i != j) {
                    pheromone[i][j] = 0.1;
                    
                    // Heuristic là nghịch đảo của khoảng cách
                    double distance = locations[i].distance(locations[j]);
                    heuristic[i][j] = distance > 0 ? 1.0 / distance : 0.0;
                }
            }
        }
    }

    /**
     * Tạo giải pháp ngẫu nhiên từ giải pháp mẫu
     */
    private Solution createRandomSolution(Solution template) {
        Solution newSolution = template.copy();
        Route[] routes = newSolution.getRoutes();
        
        // Áp dụng các toán tử đơn tuyến
        for (Route route : routes) {
            int operations = 1 + random.nextInt(2);
            for (int i = 0; i < operations; i++) {
                applyRandomOperation(route);
            }
        }
        
        // Áp dụng các toán tử đa tuyến nếu có đủ tuyến đường
        if (routes.length >= 2) {
            int multiRouteOperations = 1 + random.nextInt(2);
            for (int i = 0; i < multiRouteOperations; i++) {
                applyRandomMultiRouteOperation(routes);
            }
        }
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
        }
        
        return newSolution;
    }

    /**
     * Cập nhật giải pháp của kiến dựa trên pheromone và heuristic
     */
    private void updateAntSolution(Ant ant) {
        Solution currentSolution = ant.getSolution();
        Solution newSolution = currentSolution.copy();
        Route[] routes = newSolution.getRoutes();

        // Cập nhật từng tuyến đường
        for (int routeIndex = 0; routeIndex < routes.length; routeIndex++) {
            Route route = routes[routeIndex];
            
            // Tạo một tuyến đường mới dựa trên pheromone và heuristic
            if (random.nextDouble() < 0.7) { // 70% cơ hội áp dụng ACO
                constructNewRoute(route);
            } else {
                // 30% cơ hội áp dụng toán tử ngẫu nhiên
                applyRandomOperation(route);
            }
            
            // Kiểm tra tính khả thi
            if (!checkConditionUtil.isInsertionFeasible(route, locations, 
                    route.getMaxPayload(), currentTarget)) {
                // Nếu không khả thi, quay lại tuyến đường cũ
                routes[routeIndex] = currentSolution.getRoutes()[routeIndex].copy();
            }
        }
        
        // Áp dụng các toán tử đa tuyến với xác suất 10%
        if (random.nextDouble() < 0.5 && routes.length >= 2) {
            applyRandomMultiRouteOperation(routes);
            
            // Kiểm tra tính khả thi sau khi áp dụng toán tử đa tuyến
            for (int i = 0; i < routes.length; i++) {
                if (!checkConditionUtil.isInsertionFeasible(routes[i], locations,
                        routes[i].getMaxPayload(), currentTarget)) {
                    routes[i] = currentSolution.getRoutes()[i].copy();
                }
            }
        }
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
        }

        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(routes, locations);
        newSolution.setFitness(newFitness);

        // Cập nhật nếu tốt hơn
        if (newFitness < ant.getFitness()) {
            ant.setSolution(newSolution);
            ant.setFitness(newFitness);

            // Cập nhật kiến tốt nhất
            if (newFitness < bestAnt.getFitness()) {
                bestAnt = new Ant(newSolution.copy(), newFitness);
                System.out.println("New best solution found with fitness: " + newFitness);
            }
        }
    }

    /**
     * Xây dựng tuyến đường mới dựa trên pheromone và heuristic
     */
    private void constructNewRoute(Route route) {
        int[] way = route.getIndLocations();
        if (way.length <= 2) return; // Không đủ điểm để xây dựng lại
        
        // Giữ điểm đầu và điểm cuối (thường là depot)
        int start = way[0];
        int end = way[way.length - 1];
        
        // Tạo danh sách các điểm cần thăm (không bao gồm điểm đầu và cuối)
        List<Integer> unvisited = new ArrayList<>();
        for (int i = 1; i < way.length - 1; i++) {
            unvisited.add(way[i]);
        }
        
        // Xây dựng tuyến đường mới
        List<Integer> newWay = new ArrayList<>();
        newWay.add(start);
        
        int current = start;
        while (!unvisited.isEmpty()) {
            int next = selectNextLocation(current, unvisited);
            newWay.add(next);
            current = next;
            unvisited.remove(Integer.valueOf(next));
        }
        
        newWay.add(end);
        
        // Cập nhật tuyến đường
        for (int i = 0; i < way.length; i++) {
            way[i] = newWay.get(i);
        }
    }

    /**
     * Chọn địa điểm tiếp theo dựa trên pheromone và heuristic
     */
    private int selectNextLocation(int current, List<Integer> unvisited) {
        if (unvisited.isEmpty()) return -1;
        if (unvisited.size() == 1) return unvisited.get(0);
        
        // Tính tổng xác suất
        double total = 0.0;
        double[] probabilities = new double[unvisited.size()];
        
        for (int i = 0; i < unvisited.size(); i++) {
            int next = unvisited.get(i);
            double pheromoneValue = pheromone[current][next];
            double heuristicValue = heuristic[current][next];
            
            // Công thức ACO: phe^α * phe^β
            probabilities[i] = Math.pow(pheromoneValue, ALPHA) * Math.pow(heuristicValue, BETA);
            total += probabilities[i];
        }
        
        // Chọn địa điểm tiếp theo theo xác suất
        double rand = random.nextDouble() * total;
        double sum = 0.0;
        
        for (int i = 0; i < unvisited.size(); i++) {
            sum += probabilities[i];
            if (rand <= sum) {
                return unvisited.get(i);
            }
        }
        
        // Mặc định trả về địa điểm cuối cùng
        return unvisited.get(unvisited.size() - 1);
    }

    /**
     * Cập nhật pheromone trên các cạnh
     */
    private void updatePheromone() {
        // Giảm pheromone trên tất cả các cạnh (bay hơi)
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                if (i != j) {
                    pheromone[i][j] *= (1.0 - RHO);
                    if (pheromone[i][j] < 0.1) pheromone[i][j] = 0.1; // Giới hạn dưới
                }
            }
        }
        
        // Thêm pheromone cho các cạnh trong giải pháp tốt nhất
        Solution bestSolution = bestAnt.getSolution();
        double deltaPheromone = Q / bestAnt.getFitness();
        
        for (Route route : bestSolution.getRoutes()) {
            int[] way = route.getIndLocations();
            for (int i = 0; i < way.length - 1; i++) {
                int from = way[i];
                int to = way[i + 1];
                pheromone[from][to] += deltaPheromone;
                pheromone[to][from] += deltaPheromone; // Đồ thị vô hướng
                
                // Giới hạn trên
                if (pheromone[from][to] > 10.0) pheromone[from][to] = 10.0;
                if (pheromone[to][from] > 10.0) pheromone[to][from] = 10.0;
            }
        }
    }

    /**
     * Chạy thuật toán ACO
     */
    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, Location[] locations,
                        int currentTarget) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations, currentTarget);

        // Khởi tạo đàn kiến
        initialize(initialSolutions);

        // Vòng lặp chính
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Cập nhật giải pháp cho từng kiến
            for (Ant ant : colony) {
                updateAntSolution(ant);
            }
            
            // Cập nhật pheromone
            updatePheromone();
            
            // Đa dạng hóa đàn kiến định kỳ
            if (iteration % 10 == 0) {
                diversifyColony();
            }
        }

        return bestAnt.getSolution();
    }

    /**
     * Đa dạng hóa đàn kiến
     */
    private void diversifyColony() {
        // Sắp xếp đàn kiến theo fitness
        colony.sort(Comparator.comparingDouble(Agent::getFitness));
        
        // Giữ lại 30% kiến tốt nhất
        int eliteCount = (int) (COLONY_SIZE * 0.3);
        
        // Tạo kiến mới cho 70% còn lại
        for (int i = eliteCount; i < COLONY_SIZE; i++) {
            if (i < colony.size()) {
                // Tạo giải pháp mới từ giải pháp tốt nhất
                Solution newSolution = createDiversifiedSolution(bestAnt.getSolution());
                double newFitness = fitnessUtil.calculatorFitness(newSolution.getRoutes(), locations);
                
                colony.get(i).setSolution(newSolution);
                colony.get(i).setFitness(newFitness);
            }
        }
        
        System.out.println("Colony diversified. Elite count: " + eliteCount);
    }

    /**
     * Tạo giải pháp đa dạng hóa từ giải pháp gốc
     */
    private Solution createDiversifiedSolution(Solution original) {
        Solution newSolution = original.copy();
        Route[] routes = newSolution.getRoutes();
        
        // Áp dụng các toán tử đơn tuyến
        for (Route route : routes) {
            int operations = 1 + random.nextInt(2);
            for (int i = 0; i < operations; i++) {
                applyRandomOperation(route);
            }
        }
        
        // Áp dụng các toán tử đa tuyến (PD-Shift và PD-Exchange)
        if (routes.length >= 2) {
            int multiRouteOperations = 1 + random.nextInt(2);
            for (int i = 0; i < multiRouteOperations; i++) {
                applyRandomMultiRouteOperation(routes);
            }
        }
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
        }
        
        return newSolution;
    }
}