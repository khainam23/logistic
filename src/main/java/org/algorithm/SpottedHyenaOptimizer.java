package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.*;

/**
 * Thuật toán tối ưu hóa theo đàn linh cẩu.
 * Out: Lộ trình được cho là tối ưu nhất.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends AAlgorithm {
    int MAX_ITERATION; // Giới hạn số lần thực hiện

    @Override
    public void optimize() {
        // Step 1: Khởi tạo các giải pháp
        Map<Vehicle, ArrayList<ArrayList<Location>>> clusterSolutions = initialPopulation();
    }

    /**
     * Tìm ra giải pháp cho là tối ưu cho mỗi phương tiện
     */
    public Map<Vehicle, ArrayList<ArrayList<Location>>> initialPopulation() {
        Map<Vehicle, ArrayList<ArrayList<Location>>> result = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            result.put(vehicle, createRandomSolution(vehicle));
        }
    }

    /**
     * Sử dụng thuật toán SA để tạo ra các giải pháp khả thi.
     * 1. Sinh ra một giải pháp ngẫu nhiên (randomSolution) dựa trên firstSolution (đường đi được cho đầu tiên)
     * 2. Tính chi phí randomSolution
     * 3. Tạo ra một giải pháp khác lân cận (neighborSolution) giải pháp trước (randomSolution)
     * 4. So sánh chi phí randomSolution và neighborSolutiion
     * 5. Lựa chọn giải pháp tốt hơn
     * 6. Lặp lại trên điều kiện
     */
    public ArrayList<ArrayList<Location>> createRandomSolution(Vehicle vehicle) {
        ArrayList<ArrayList<Location>> result = new ArrayList<>();
        final Random rd = new Random();
        ArrayList<Location> currentLocation = new ArrayList<>(vehicle.getWay());
        double oldCost = calculatorCost(currentLocation, vehicle); // Chi phí của cách đó đối với loại xe
        double T = 1; // Giá trị bắt đầu
        double TMin = 0.0001; // Ngưỡng dừng
        double alpha = 0.9; // Giá trị giảm mỗi vòng
        while (T > TMin) {
            // Thử tìm một giá trị mới trong giới hạn lần
            for (int i = 0; i < MAX_ITERATION; i++) {
                ArrayList<Location> newLocation = neighborBySA(currentLocation); // Tìm ra lộ trình có giá trị gần
                double newCost = calculatorCost(newLocation, vehicle); // Tính chi phí đường mới đối với xe
                double ap = acceptanceProbability(oldCost, newCost, T); // Tính
                if(ap > rd.nextDouble()) {
                    // Chấp nhận ngẫu nhiên
                    currentLocation = newLocation;
                    oldCost = newCost;
                }
            }
            T *= alpha;
        }
        vehicle.setWay(currentLocation);
        return currentLocation;
    }

    public ArrayList<Location> neighborBySA(ArrayList<Location> locations) {
        return null;
    }

    /**
     * Tính phần trăm chấp nhận giá trị mới.
     * Nếu nó tốt hơn (old > new) thì chấp nhận.
     * Ngược lại tính theo công thức e^((old - new) / T) lý thuyết xác suất.
     */
    public double acceptanceProbability(double oldCost, double newCost, double T) {
        return newCost <= oldCost ? 1 : Math.pow(Math.E, (oldCost - newCost) / T);
    }

    /**
     * Tính giá trị của điểm.
     * Giả sử thời gian di chuyển của xe thỏa không gian của lộ trình.
     * (bắt đầu và kết thúc đủ để đi hết)
     * Kết quả = Tổng thời gian + Điểm phạt nếu có + Chi phí vận hành xe
     * Giá trị lớn = Tốt
     * Giá trị dương thấp = Tạm
     * Giá trị âm = Chưa ổn
     * Lý do cộng cả trọng tải và thời gian là vì có thể có nhiều điểm ra 0,
     * Và từ đó sẽ làm khó trong quá trình so sánh.
     * (Trong bài báo không đề cập nên cách tính sẽ là điểm thưởng phạt)
     */
    private double calculationFitness(ArrayList<Location> location, Vehicle vehicle) {
        double fitness = 0; // Kết quả điểm
        double currentTime = 0; // Đánh giá cho thời gian
        double currentLoad = 0; // Đánh giá cho trọng tải
        final int bonus = 100; // Giá trị giả định nếu có thưởng / phạt

        // Triển khai tính điểm
        for (int i = 0; i < location.size() - 1; i++) {
            Location currentLoc = location.get(i);
            Location nextLoc = location.get(i + 1);

            // Giả định khoảng cách 2 điểm chính là thời gian di chuyển
            double tempTime = currentLoc.calculatorDistance(nextLoc);

            // Cập nhật thời gian
            currentTime += tempTime;
            if(currentTime < currentLoc.getLTW()) {
                // Đến trước thời gian cần giao -> Thưởng
                fitness += bonus;
            }
            if (currentTime > currentLoc.getUTW()) {
                // Đến trễ hơn thời gian cần giao -> Phạt
                fitness -= bonus;
            }

            // Cập nhật tải trọng
            currentLoad += calculatorLoad(currentLoc);
            if(currentLoad < 0 || currentLoad > vehicle.getMaxLoad()) {
                // Số hàng phải thả nhiều hơn số hàng hiện có -> Phạt
                // Số hàng phải lấy lấy hơn giá trị chứa -> Phạt
                fitness -= bonus;
            }

            fitness += currentTime;
        }
        fitness += vehicle.getCost();
        return fitness;
    }

    /**
     * Tính toán tải trọng nhận trên điểm.
     * Tổng = Số hàng phải lấy - Số hàng phải thả
     */
    public double calculatorLoad(Location location) {
        int total = 0;
        if(location.isPickup()) total += location.getLoadPickup();
        if(location.isDrop()) total -= location.getLoadDrop();
        return total;
    }
}
