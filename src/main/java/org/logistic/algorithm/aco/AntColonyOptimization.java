package org.logistic.algorithm.aco;

import java.util.ArrayList;
import java.util.List;

import org.logistic.algorithm.AbstractOptimizer;

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
     * Khởi tạo đàn kiến từ các giải pháp ban đầu (sử dụng trực tiếp)
     */
    private void initialize(Solution[] initialSolutions) {
        colony = new ArrayList<>();
        numLocations = locations.length;

        // Khởi tạo ma trận pheromone và heuristic
        initializeMatrices();

        // Sử dụng trực tiếp các giải pháp ban đầu làm kiến
        for (Solution solution : initialSolutions) {
            Ant ant = new Ant(solution.copy(), solution.getFitness());
            colony.add(ant);

            // Cập nhật kiến tốt nhất
            if (bestAnt == null || ant.getFitness() < bestAnt.getFitness()) {
                bestAnt = new Ant(solution.copy(), solution.getFitness());
            }
        }

        System.out.println("Initialized ACO with " + colony.size() + " ants from initial solutions");
        System.out.println("Initial best fitness: " + bestAnt.getFitness());
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
     * Cải thiện giải pháp của kiến dựa trên pheromone và heuristic (ACO gốc)
     */
    private void improveAntSolution(Ant ant) {
        Solution currentSolution = ant.getSolution();
        Solution newSolution = currentSolution.copy();
        Route[] routes = newSolution.getRoutes();

        // Cải thiện từng tuyến đường dựa trên pheromone
        for (Route route : routes) {
            if (route.isUse()) {
                // 80% cơ hội áp dụng ACO để xây dựng lại tuyến đường
                if (random.nextDouble() < 0.8) {
                    constructRouteWithPheromone(route);
                } else {
                    // 20% cơ hội áp dụng toán tử ngẫu nhiên để đa dạng hóa
                    applyRandomOperation(route);
                }
            }
        }
        
        // Áp dụng các toán tử đa tuyến với xác suất thấp để cải thiện thêm
        if (random.nextDouble() < 0.3 && routes.length >= 2) {
            applyRandomMultiRouteOperation(routes);
        }

        // Kiểm tra ràng buộc
        for (Route route : routes) {
            if (!checkConditionUtil.isInsertionFeasible(route, locations, routes[0].getMaxPayload())) {
                
            }
        }
        
        
        // Cập nhật khoảng cách cho tất cả các tuyến đường
        for (Route route : routes) {
            route.calculateDistance(locations);
        }

        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(routes, locations);
        newSolution.setFitness(newFitness);

        // Cập nhật nếu tốt hơn (greedy acceptance)
        if (newFitness < ant.getFitness()) {
            ant.setSolution(newSolution);
            ant.setFitness(newFitness);

            // Cập nhật kiến tốt nhất
            if (newFitness < bestAnt.getFitness()) {
                bestAnt = new Ant(newSolution.copy(), newFitness);
                System.out.println("ACO improved solution with fitness: " + newFitness);
            }
        }
    }

    /**
     * Xây dựng lại tuyến đường dựa trên pheromone và heuristic (ACO gốc)
     */
    private void constructRouteWithPheromone(Route route) {
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
        
        // Xây dựng tuyến đường mới dựa trên pheromone
        List<Integer> newWay = new ArrayList<>();
        newWay.add(start);
        
        int current = start;
        while (!unvisited.isEmpty()) {
            int next = selectNextLocationByPheromone(current, unvisited);
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
     * Chọn địa điểm tiếp theo dựa trên pheromone và heuristic (ACO gốc)
     */
    private int selectNextLocationByPheromone(int current, List<Integer> unvisited) {
        if (unvisited.isEmpty()) return -1;
        if (unvisited.size() == 1) return unvisited.get(0);
        
        // Tính tổng xác suất
        double total = 0.0;
        double[] probabilities = new double[unvisited.size()];
        
        for (int i = 0; i < unvisited.size(); i++) {
            int next = unvisited.get(i);
            double pheromoneValue = pheromone[current][next];
            double heuristicValue = heuristic[current][next];
            
            // Công thức ACO gốc: τ^α * η^β
            probabilities[i] = Math.pow(pheromoneValue, ALPHA) * Math.pow(heuristicValue, BETA);
            total += probabilities[i];
        }
        
        // Roulette wheel selection
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
     * Cập nhật pheromone theo ACO gốc (Ant System)
     */
    private void updatePheromone() {
        // Bước 1: Bay hơi pheromone trên tất cả các cạnh
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                if (i != j) {
                    pheromone[i][j] *= (1.0 - RHO);
                    // Đảm bảo pheromone không quá nhỏ
                    if (pheromone[i][j] < 0.01) {
                        pheromone[i][j] = 0.01;
                    }
                }
            }
        }
        
        // Bước 2: Thêm pheromone từ tất cả các kiến (Ant System gốc)
        for (Ant ant : colony) {
            Solution solution = ant.getSolution();
            double deltaPheromone = Q / ant.getFitness();
            
            // Thêm pheromone cho từng tuyến đường của kiến này
            for (Route route : solution.getRoutes()) {
                if (route.isUse()) {
                    int[] way = route.getIndLocations();
                    for (int i = 0; i < way.length - 1; i++) {
                        int from = way[i];
                        int to = way[i + 1];
                        pheromone[from][to] += deltaPheromone;
                        pheromone[to][from] += deltaPheromone; // Đồ thị vô hướng
                    }
                }
            }
        }
        
        // Bước 3: Giới hạn pheromone tối đa để tránh stagnation
        for (int i = 0; i < numLocations; i++) {
            for (int j = 0; j < numLocations; j++) {
                if (i != j && pheromone[i][j] > 10.0) {
                    pheromone[i][j] = 10.0;
                }
            }
        }
    }

    /**
     * Chạy thuật toán ACO
     */
    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);

        // Khởi tạo đàn kiến
        initialize(initialSolutions);

        // Vòng lặp chính của ACO
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Cải thiện giải pháp cho từng kiến dựa trên pheromone
            for (Ant ant : colony) {
                improveAntSolution(ant);
            }
            
            // Cập nhật pheromone dựa trên tất cả các kiến
            updatePheromone();
            
            // In thông tin tiến trình
            if (iteration % 20 == 0) {
                System.out.println("ACO Iteration " + iteration + ", Best fitness: " + bestAnt.getFitness());
            }
        }

        return bestAnt.getSolution();
    }


}