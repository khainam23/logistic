package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;
import org.logistic.util.FitnessUtil;
import org.logistic.model.Vehicle;
import org.logistic.util.CheckConditionUtil;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.util.WriteLog;

import java.util.Arrays;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer {
    // Tham số cố định của thuật toán
    static final int POPULATION_SIZE = 50;
    static final int MAX_ITERATIONS = 500;
    static final double ENCIRCLING_COEFFICIENT = 1.5;

    // Tham số linh động sử dụng trong thuật toán
    Hyena[] hyenas;
    Hyena bestHyena;
    Random random;
    int currentIteration;

    // Tham số của việc khởi tạo 
    Location[] locations;
    Vehicle[] vehicles;

    public SpottedHyenaOptimizer(Location[] locations, Vehicle[] vehicles) {
        this.random = new Random();
        this.currentIteration = 1; // Vòng lặp được đánh số bắt đầu từ 1
        this.locations = locations;
        this.vehicles = vehicles;
    }

    public Vehicle[] getOptimizedVehicles() {
        return this.vehicles;
    }

    public Solution optimize(Solution initialSolution) {
        // Khởi tạo quần thể ban đầu sử dụng SA
        Solution[] initialPopulation = initPopulation(initialSolution);
        hyenas = new Hyena[POPULATION_SIZE];

        // Tính các hệ số ban đầu
        double h = ENCIRCLING_COEFFICIENT - (currentIteration * (ENCIRCLING_COEFFICIENT / MAX_ITERATIONS));
        double E = 2 * h * random.nextDouble() - h;
        double B = 2 * random.nextDouble();

        // Chuyển đổi các Solution thành Hyena
        for (int i = 0; i < POPULATION_SIZE; i++) {
            hyenas[i] = new Hyena(initialPopulation[i], FitnessUtil.fitness(initialPopulation[i], this.locations));
        }

        // Tìm Hyena tốt nhất ban đầu
        updateBestHyena();

        // Ghi thông tin về giải pháp ban đầu
        Solution currentBestSolution = bestHyena.getSolution();
        WriteLog.getInstance().logInitialInfo(bestHyena.getFitness());
        WriteLog.getInstance().logInitialSolution(currentBestSolution.getRoutes(), currentBestSolution.getDistances());

        // Vòng lặp chính của thuật toán SHO

        while (!isTerminationCriteriaMet()) {
            WriteLog.getInstance().logIterationInfo(currentIteration, MAX_ITERATIONS, bestHyena.getFitness());
            for (Hyena hyena : hyenas) {
                // Giả định khoảng cách giữa hyena và con mồi 
                double D = Math.abs(B * bestHyena.getFitness() - hyena.getFitness());

                // Cập nhật vị trí của Hyena
                Solution newSolution = updatePosition(hyena.getSolution(), bestHyena.getSolution(), E, D);
                Hyena newHyena = new Hyena(newSolution, FitnessUtil.fitness(newSolution, this.locations));

                // Cập nhật nếu vị trí mới tốt hơn cho linh cẩu này
                if (newHyena.getFitness() < hyena.getFitness()) {
                    hyena.setSolution(newSolution);
                    hyena.setFitness(newHyena.getFitness());
                }
            }

            // Cập nhật các hệ số
            h = ENCIRCLING_COEFFICIENT - (currentIteration * (ENCIRCLING_COEFFICIENT / MAX_ITERATIONS));
            E = 2 * h * random.nextDouble() - h;
            B = 2 * random.nextDouble();

            // Kiểm tra giới hạn

            updateBestHyena();

            currentIteration++;
        }

        // Cập nhật tuyến đường cho các xe từ giải pháp tối ưu
        Solution finalBestSolution = bestHyena.getSolution();
        double[][] bestDistances = finalBestSolution.getDistances();
        boolean[] assignedPoints = new boolean[bestDistances.length];
        
        // Tính toán khoảng cách từ mỗi điểm đến kho
        double[] depotDistances = new double[bestDistances.length];
        Point depot = vehicles[0].getPoint();
        for (int i = 0; i < bestDistances.length; i++) {
            depotDistances[i] = calculateDistance(locations[i], new Location(depot, 0));
        }
        
        // Phân bổ các điểm cho các xe dựa trên khoảng cách đến kho
        int pointsPerVehicle = bestDistances.length / vehicles.length;
        int remainingPoints = bestDistances.length % vehicles.length;
        
        // Sắp xếp các điểm theo khoảng cách đến kho
        Integer[] sortedPoints = new Integer[bestDistances.length];
        for (int i = 0; i < bestDistances.length; i++) {
            sortedPoints[i] = i;
        }
        Arrays.sort(sortedPoints, (a, b) -> Double.compare(depotDistances[a], depotDistances[b]));
        
        // Phân bổ các điểm cho từng xe
        int currentIndex = 0;
        for (int i = 0; i < vehicles.length; i++) {
            int numPoints = pointsPerVehicle + (i < remainingPoints ? 1 : 0);
            if (numPoints == 0) {
                vehicles[i].setRoute(new int[0]);
                continue;
            }
            
            int[] route = new int[numPoints];
            int routeIndex = 0;
            
            // Lấy các điểm cho xe này
            for (int j = 0; j < numPoints && currentIndex < sortedPoints.length; j++) {
                route[routeIndex++] = sortedPoints[currentIndex++];
                assignedPoints[route[routeIndex - 1]] = true;
            }
            
            // Tối ưu hóa tuyến đường cho xe này
            if (routeIndex > 0) {
                int[] optimizedRoute = new int[routeIndex];
                boolean[] visited = new boolean[routeIndex];
                
                // Bắt đầu từ điểm gần kho nhất
                double minDepotDist = Double.MAX_VALUE;
                int firstPoint = 0;
                for (int j = 0; j < routeIndex; j++) {
                    double dist = calculateDistance(locations[route[j]], new Location(depot, 0));
                    if (dist < minDepotDist) {
                        minDepotDist = dist;
                        firstPoint = j;
                    }
                }
                
                optimizedRoute[0] = route[firstPoint];
                visited[firstPoint] = true;
                int currentPoint = firstPoint;
                
                // Xây dựng tuyến đường theo nguyên tắc điểm gần nhất
                for (int j = 1; j < routeIndex; j++) {
                    double minDistance = Double.MAX_VALUE;
                    int nextPoint = -1;
                    
                    for (int k = 0; k < routeIndex; k++) {
                        if (!visited[k]) {
                            double distance = calculateDistance(locations[route[currentPoint]], locations[route[k]]);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nextPoint = k;
                            }
                        }
                    }
                    
                    if (nextPoint != -1) {
                        optimizedRoute[j] = route[nextPoint];
                        visited[nextPoint] = true;
                        currentPoint = nextPoint;
                    }
                }
                
                vehicles[i].setRoute(optimizedRoute);
                
                // Cập nhật ma trận khoảng cách cho tuyến đường mới
                for (int j = 0; j < optimizedRoute.length - 1; j++) {
                    bestDistances[optimizedRoute[j]][i] = calculateDistance(locations[optimizedRoute[j]], locations[optimizedRoute[j + 1]]);
                }
            }
        }
        
        // Ghi thông tin về giải pháp tối ưu cuối cùng
        WriteLog.getInstance().logFinalResult(bestHyena.getFitness(), finalBestSolution.getRoutes(), finalBestSolution.getDistances());
        
        return finalBestSolution;
    }

    // Khởi tạo quần thể ban đầu 
    private Solution[] initPopulation(Solution solution) {
        SimulatedAnnealing sa = new SimulatedAnnealing(solution);
        return sa.run();
    }

    private void updateBestHyena() {
        bestHyena = Arrays.stream(hyenas)
                .min((h1, h2) -> Double.compare(h1.getFitness(), h2.getFitness()))
                .orElse(hyenas[0]);
    }

    private boolean isTerminationCriteriaMet() {
        return currentIteration >= MAX_ITERATIONS;
    }

    private Solution updatePosition(Solution currentSolution, Solution bestSolution, double E, double D) {
        Solution newSolution = currentSolution.copy();
        double[][] distances = newSolution.getDistances();
        
        // Cập nhật tuyến đường cho các xe
        for (int i = 0; i < vehicles.length; i++) {
            int[] currentRoute = vehicles[i].getRoute();
            if (currentRoute.length > 0) {
                // Cập nhật ma trận khoảng cách cho tuyến đường hiện tại
                for (int j = 0; j < currentRoute.length - 1; j++) {
                    distances[currentRoute[j]][i] = calculateDistance(locations[currentRoute[j]], locations[currentRoute[j + 1]]);
                }
            }
        }
        
        int rows = distances.length;
        int cols = distances[0].length;

        // Tính toán phạm vi thay đổi dựa trên D
        int changeRange = (int) Math.max(1, Math.min(5, D));

        // Xác suất áp dụng các toán tử biến đổi dựa trên hệ số bao vây E
        double operatorProbability = Math.abs(E);

        if (operatorProbability > 0.5) { // Exploration phase - Thay đổi mạnh
            // Thực hiện hoán đổi ngẫu nhiên các điểm trong tuyến đường
            for (int k = 0; k < changeRange; k++) {
                int vehicleIndex = random.nextInt(vehicles.length);
                Vehicle vehicle = vehicles[vehicleIndex];
                int[] route = vehicle.getRoute();
                
                if (route.length > 1) {
                    // Chọn hai vị trí ngẫu nhiên trong tuyến đường để hoán đổi
                    int pos1 = random.nextInt(route.length);
                    int pos2 = random.nextInt(route.length);
                    
                    // Kiểm tra tính khả thi của việc hoán đổi
                    Location loc1 = locations[route[pos1]];
                    Location loc2 = locations[route[pos2]];
                    
                    if (CheckConditionUtil.isInsertionFeasible(loc1, loc2, route[pos1], route[pos2], vehicle)) {
                        // Hoán đổi các điểm trong tuyến đường
                        int temp = route[pos1];
                        route[pos1] = route[pos2];
                        route[pos2] = temp;
                        
                        // Cập nhật ma trận khoảng cách
                        updateDistanceMatrix(distances, route, vehicleIndex);
                    }
                }
            }
        } else { // Exploitation phase - Thay đổi nhẹ
            // Tối ưu hóa cục bộ bằng cách điều chỉnh thứ tự các điểm trong tuyến đường
            for (int i = 0; i < vehicles.length; i++) {
                Vehicle vehicle = vehicles[i];
                int[] route = vehicle.getRoute();
                
                if (route.length > 2) {
                    // Chọn một đoạn con trong tuyến đường để tối ưu
                    int start = random.nextInt(route.length - 2);
                    int end = Math.min(start + 2, route.length - 1);
                    
                    // Thử đảo ngược đoạn con để cải thiện khoảng cách
                    double currentDistance = calculateRouteDistance(route, start, end, distances);
                    int[] tempRoute = route.clone();
                    reverseSubRoute(tempRoute, start, end);
                    double newDistance = calculateRouteDistance(tempRoute, start, end, distances);
                    
                    if (newDistance < currentDistance) {
                        // Nếu việc đảo ngược cải thiện khoảng cách, áp dụng thay đổi
                        System.arraycopy(tempRoute, 0, route, 0, route.length);
                        updateDistanceMatrix(distances, route, i);
                    }
                }
            }
        }

        return newSolution;
    }

    private void updateDistanceMatrix(double[][] distances, int[] route, int vehicleIndex) {
        // Cập nhật ma trận khoảng cách dựa trên tuyến đường mới
        for (int i = 0; i < route.length - 1; i++) {
            distances[route[i]][vehicleIndex] = calculateDistance(locations[route[i]], locations[route[i + 1]]);
        }
    }

    private double calculateDistance(Location loc1, Location loc2) {
        Point p1 = loc1.getPoint();
        Point p2 = loc2.getPoint();
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }

    private double calculateRouteDistance(int[] route, int start, int end, double[][] distances) {
        double totalDistance = 0;
        for (int i = start; i < end; i++) {
            totalDistance += distances[route[i]][route[i + 1]];
        }
        return totalDistance;
    }

    private void reverseSubRoute(int[] route, int start, int end) {
        while (start < end) {
            int temp = route[start];
            route[start] = route[end];
            route[end] = temp;
            start++;
            end--;
        }
    }
}
