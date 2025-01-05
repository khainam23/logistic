package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.*;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AAlgorithm {
    final int MAX_ITERATIONS = 1000;
    final static Random rand = new Random();

    // Các giải pháp có quyền khởi tạo thêm tham số riêng của mình
    public abstract List<Vehicle> optimize(List<List<Vehicle>> solutions, Object... args);

    // Truy tìm giải pháp tối ưu khác theo phương pháp Simulated Annealing
    public List<List<Vehicle>> generateInitialPopulationBySa(List<Vehicle> solution) {
        Set<List<Vehicle>> result  = new HashSet<>();
        List<Vehicle> currentSolution = new ArrayList<>(solution);
        double currentFitness = calculateFitness(currentSolution);
        double T = 1.0;
        double tMin = 0.00001;
        double alpha = 0.9;
        while (T > tMin) {
            for (int j = 0; j < MAX_ITERATIONS; j++) {
                List<Vehicle> neighborSolution = generateNewSolution(currentSolution);
                double newFitness = calculateFitness(neighborSolution);
                double delta = newFitness - currentFitness;
                double ap = delta < 0 ? 1 : Math.exp(-delta / T);
                if (ap > rand.nextDouble()) {
                    currentSolution = neighborSolution;
                    currentFitness = newFitness;
                    result.add(currentSolution);
                }
            }
            T *= alpha;
        }
        return result.stream().toList();
    }

    // Tìm ra một lời giải gần với lời giải hiện có
    // Bằng cách hoán đổi điểm trong đoạn đường ngẫu nhiên
    private List<Vehicle> generateNewSolution(List<Vehicle> oldSolution) {
        List<Vehicle> newSolution = new ArrayList<>(oldSolution);
        // Chọn ra một chiếc xe bất kỳ
        int indRand = rand.nextInt(newSolution.size());
        Vehicle vehicle = newSolution.get(indRand);
        List<Location> way = vehicle.getWay();
        // Hoán đổi 2 phần tử trong đoạn đường phải di chuyển của nó
        int ind1 = rand.nextInt(way.size());
        int ind2 = rand.nextInt(way.size());
        while (ind1 == ind2)
            ind2 = rand.nextInt(way.size());
        Location temp = way.get(ind1);
        way.set(ind1, way.get(ind2));
        way.set(ind2, temp);
        return newSolution;
    }

    /**
     * Tính giá trị tối ưu của một lời giải.
     * Số lượng xe + Khoảng cách di chuyển + Thời gian chung cho việc vận tải + Thời gian chờ của khách hàng
     */
    public double calculateFitness(List<Vehicle> vehicles) {
        return vehicles.size() +
                calculateTotalTravelDistance(vehicles) +
                calculateTotalTimeGeneral(vehicles) +
                calculateTotalTimeWaiting(vehicles);
    }

    // Thời gian thực hiện dịch vụ trên toàn chuyến
    private double calculateTotalTimeGeneral(List<Vehicle> vehicles) {
        return calculateTotalTravelDistance(vehicles) + calculateTotalService(vehicles);
    }

    // Tổng thời gian thực hiện dịch vụ trên không gian
    private int calculateTotalService(List<Vehicle> vehicles) {
        // Xử lý tính toán song song
        return vehicles.parallelStream()
                .mapToInt(vehicle -> vehicle.getWay()
                        .parallelStream()
                        .mapToInt(Location::calculatorService)
                        .sum())
                .sum();
    }

    // Tổng thời gian đi của toàn đội xe
    private double calculateTotalTravelDistance(List<Vehicle> vehicles) {
        // Xử lý tính toán song song
        return vehicles.parallelStream()
                .mapToDouble(this::calculateTravelDistance)
                .sum();
    }

    // Thời gian đi của 1 tuyến xe
    private double calculateTravelDistance(Vehicle vehicle) {
        double distance = 0.0;
        List<Location> way = vehicle.getWay();
        for (int i = 0; i < way.size() - 1; i++)
            distance += way.get(i).calculatorDistance(way.get(i + 1));
        distance += vehicle.getPoint().distance(way.get(0).getPoint()); // Bắt đầu đi
        distance += way.get(way.size() - 1).getPoint().distance(vehicle.getPoint()); // Thời gian về nơi để xe ban đầu
        return distance;
    }

    // Tổng thời gian chờ trên toàn không gian
    private double calculateTotalTimeWaiting(List<Vehicle> vehicles) {
        // Xử lý tính toán song song
        return vehicles.parallelStream()
                .mapToDouble(this::calculateTimeWaiting)
                .sum();
    }

    /**
     * Tính thời gian chờ của khách trên lộ trình của xe
     */
    private double calculateTimeWaiting(Vehicle vehicle) {
        double timeWaiting = 0.0;
        double currentTime = 0.0;
        List<Location> way = vehicle.getWay();

        // Tính thời gian từ điểm hiện tại đến điểm đầu tiên
        currentTime += vehicle.getPoint().distance(way.get(0).getPoint());
        timeWaiting += calculateWaitingTime(currentTime, way.get(0).getLTW());
        currentTime += way.get(0).calculatorService(); // Thực hiện dịch vụ tại điểm

        // Duyệt qua các cặp điểm liên tiếp trong danh sách
        for (int i = 0; i < way.size() - 1; i++) {
            Location currentLocation = way.get(i);
            Location nextLocation = way.get(i + 1);

            double distance = currentLocation.calculatorDistance(nextLocation);
            currentTime += distance;
            timeWaiting += calculateWaitingTime(currentTime, currentLocation.getLTW());
            currentTime += currentLocation.calculatorService();
        }

        return timeWaiting;
    }

    // Phương thức tiện ích để tính thời gian chờ tại một điểm
    // Nếu xe đến sớm khách không phải chờ
    private double calculateWaitingTime(double currentTime, double ltw) {
        return Math.max(0, currentTime - ltw);
    }
}
