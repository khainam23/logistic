package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;
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
    
    public Solution[] run(FitnessUtil fitnessUtil) {
        List<Solution> population = new ArrayList<>();
        double temperature = INITIAL_TEMPERATURE;
        
        Solution currentSolution = solution.copy();
        Solution bestSolution = currentSolution.copy();
        
        while (temperature > FINAL_TEMPERATURE) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                Solution newSolution = perturbSolution(currentSolution);
                
                double currentEnergy = calculateEnergy(currentSolution);
                double newEnergy = calculateEnergy(newSolution);
                double deltaEnergy = newEnergy - currentEnergy;
                
                if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                    currentSolution = newSolution.copy();
                    if (newEnergy < calculateEnergy(bestSolution)) {
                        bestSolution = newSolution.copy();
                    }
                }
            }
            
            population.add(currentSolution.copy());
            temperature *= COOLING_RATE;
        }
        
        return population.toArray(new Solution[0]);
    }
    
    private Solution perturbSolution(Solution solution) {
        Solution newSolution = solution.copy();
        double[][] distances = newSolution.getDistances();
        
        // Chọn ngẫu nhiên một toán tử biến đổi: swap, insert, hoặc reverse
        int operator = random.nextInt(3);
        
        // Lấy các tuyến đường từ giải pháp
        boolean[][] routes = newSolution.getRoutes();
        
        // Chọn ngẫu nhiên một tuyến đường để biến đổi
        int routeIndex = random.nextInt(routes.length);
        boolean[] route = routes.get(routeIndex);
        
        // Áp dụng toán tử biến đổi được chọn
        switch (operator) {
            case 0: // Swap operator
                applySwapOperator(route);
                break;
            case 1: // Insert operator
                applyInsertOperator(route);
                break;
            case 2: // Reverse operator
                applyReverseOperator(route);
                break;
        }
        
        // Cập nhật ma trận khoảng cách dựa trên tuyến đường mới
        updateDistanceMatrix(distances, routes);
        
        return newSolution;
    }
    
    // Toán tử Swap: hoán đổi vị trí của hai điểm trong tuyến đường
    private void applySwapOperator(boolean[] route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int pos1 = random.nextInt(route.length - 2) + 1;
        int pos2 = random.nextInt(route.length - 2) + 1;
        
        // Đảm bảo pos1 khác pos2
        while (pos1 == pos2) {
            pos2 = random.nextInt(route.length - 2) + 1;
        }
        
        // Hoán đổi hai điểm
        int temp = route.get(pos1);
        route.set(pos1, route.get(pos2));
        route.set(pos2, temp);
    }

    // Toán tử Insert: di chuyển một điểm đến vị trí mới trong tuyến đường
    private void applyInsertOperator(List<Integer> route) {
        // Chọn một điểm để di chuyển (không bao gồm depot ở đầu và cuối)
        int removePos = random.nextInt(route.size() - 2) + 1;
        int point = route.get(removePos);

        // Xóa điểm khỏi vị trí hiện tại
        route.remove(removePos);

        // Chọn vị trí mới để chèn điểm (không bao gồm depot ở đầu và cuối)
        int insertPos = random.nextInt(route.size() - 1) + 1;

        // Chèn điểm vào vị trí mới
        route.add(insertPos, point);
    }
    
    // Toán tử Reverse: đảo ngược thứ tự của một đoạn trong tuyến đường
    private void applyReverseOperator(List<Integer> route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int pos1 = random.nextInt(route.size() - 2) + 1;
        int pos2 = random.nextInt(route.size() - 2) + 1;
        
        // Đảm bảo pos1 < pos2
        if (pos1 > pos2) {
            int temp = pos1;
            pos1 = pos2;
            pos2 = temp;
        }
        
        // Đảo ngược đoạn từ pos1 đến pos2
        while (pos1 < pos2) {
            int temp = route.get(pos1);
            route.set(pos1, route.get(pos2));
            route.set(pos2, temp);
            pos1++;
            pos2--;
        }
    }
    
    // Cập nhật ma trận khoảng cách dựa trên các tuyến đường mới
    private void updateDistanceMatrix(double[][] distances, List<List<Integer>> routes) {
        // Đặt lại tất cả các giá trị trong ma trận khoảng cách về 0
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[0].length; j++) {
                distances[i][j] = 0;
            }
        }
        
        // Cập nhật ma trận khoảng cách dựa trên các tuyến đường mới
        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            List<Integer> route = routes.get(routeIndex);
            for (int i = 0; i < route.size() - 1; i++) {
                int from = route.get(i);
                int to = route.get(i + 1);
                
                // Cập nhật khoảng cách giữa các điểm liên tiếp trong tuyến đường
                // Giả định rằng chỉ số cột trong ma trận khoảng cách tương ứng với chỉ số tuyến đường
                if (routeIndex < distances[0].length) {
                    distances[from][routeIndex] = 1.0;
                    // Có thể thay đổi giá trị 1.0 bằng khoảng cách thực tế giữa from và to
                    // nếu có thông tin về tọa độ của các điểm
                }
            }
        }
    }
    
    private double calculateEnergy(Solution solution) {
        double[][] distances = solution.getDistances();
        double totalDistance = 0.0;
        
        // Tính tổng khoảng cách
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[0].length; j++) {
                if (distances[i][j] > 0) {
                    totalDistance += distances[i][j];
                }
            }
        }
        
        // Trong trường hợp thực tế, chúng ta cần tính thêm các vi phạm về cửa sổ thời gian
        // và vi phạm về trọng tải. Tuy nhiên, để làm được điều này, chúng ta cần thông tin
        // về các Location và Vehicle, mà hiện tại không có sẵn trong phương thức này.
        // Có thể cải thiện bằng cách truyền thêm các tham số Location[] và Vehicle[] vào phương thức.
        
        // Giả sử năng lượng tỷ lệ thuận với tổng khoảng cách (càng ngắn càng tốt)
        return totalDistance;
    }
    
    private double acceptanceProbability(double deltaEnergy, double temperature) {
        return Math.exp(-deltaEnergy / temperature);
    }
}
