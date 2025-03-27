package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Solution;

public class FitnessUtil {
    /**
     * Tính dựa trên các tiêu chí
     * + Số lượng xe
     * + Thời lượng dịch vụ (bao gồm đón và trả)
     * + Tổng quãng đường đi được
     * + Tổng thời gian di chuyển
     * + Tổng nhu cầu vận chuyển
     * + Mức độ vi phạm ràng buộc thời gian
     * @param solution
     * @return
     */
    public static double fitness(Solution solution, Location[] locations) {
        double[][] distances = solution.getDistances();
        
        // Tính các tiêu chí
        int numVehicles = calculateNumberVehicle(distances);
        double serviceTime = calculateServiceTime(locations);
        double totalDistance = calculateTotalDistance(distances, locations);
        double totalTime = calculateTotalTime(distances, locations);
        double totalDemand = calculateTotalDemand(locations);
        double timeWindowViolation = calculateTimeWindowViolation(distances, locations);
        double capacityViolation = calculateCapacityViolation(distances, locations);
        
        // Chuẩn hóa và gán trọng số
        double w1 = 0.15; // Trọng số cho số lượng xe
        double w2 = 0.15; // Trọng số cho thời lượng dịch vụ
        double w3 = 0.15; // Trọng số cho tổng quãng đường
        double w4 = 0.15; // Trọng số cho tổng thời gian
        double w5 = 0.15; // Trọng số cho tổng nhu cầu
        double w6 = 0.15; // Trọng số cho vi phạm thời gian
        double w7 = 0.10; // Trọng số cho vi phạm trọng tải
        
        // Tính điểm fitness tổng hợp
        return w1 * numVehicles + w2 * serviceTime + w3 * totalDistance + 
               w4 * totalTime + w5 * totalDemand + w6 * timeWindowViolation +
               w7 * capacityViolation;
    }

    // Số lượng xe sử dụng
    private static int calculateNumberVehicle(double[][] distances) {
        return distances[0].length;
    }

    // Tính tổng thời lượng dịch vụ (đón và trả)
    private static double calculateServiceTime(Location[] locations) {
        double totalServiceTime = 0;
        for (Location location : locations) {
            if (location.isPick()) {
                totalServiceTime += location.getServiceTimePick();
            }
            if (location.isDeliver()) {
                totalServiceTime += location.getServiceTimeDeliver();
            }
        }
        return totalServiceTime;
    }

    // Tính tổng quãng đường di chuyển
    private static double calculateTotalDistance(double[][] distances, Location[] locations) {
        double totalDistance = 0;
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                if (distances[i][j] > 0) {
                    totalDistance += distances[i][j];
                }
            }
        }
        return totalDistance;
    }

    // Tính tổng thời gian di chuyển
    private static double calculateTotalTime(double[][] distances, Location[] locations) {
        double totalTime = 0;
        double averageSpeed = 1.0; // Tốc độ trung bình
        
        for (int i = 0; i < distances.length; i++) {
            double routeTime = 0;
            for (int j = 0; j < distances[0].length; j++) {
                if (distances[i][j] > 0) {
                    double distance = distances[i][j];
                    routeTime += distance / averageSpeed;
                }
            }
            totalTime += routeTime;
        }
        return totalTime;
    }

    // Tính tổng nhu cầu vận chuyển
    private static double calculateTotalDemand(Location[] locations) {
        double totalDemand = 0;
        for (Location location : locations) {
            if (location.isPick()) {
                totalDemand += location.getDemandPick();
            }
            if (location.isDeliver()) {
                totalDemand += location.getDemandDeliver();
            }
        }
        return totalDemand;
    }

    // Tính mức độ vi phạm ràng buộc thời gian
    private static double calculateTimeWindowViolation(double[][] distances, Location[] locations) {
        double totalViolation = 0;
        double averageSpeed = 1.0;
        
        for (int j = 0; j < distances[0].length; j++) {
            double currentTime = 0;
            for (int i = 0; i < distances.length; i++) {
                if (distances[i][j] > 0) {
                    Location location = locations[i];
                    
                    // Tính thời gian di chuyển dựa trên khoảng cách thực tế
                    double travelTime = distances[i][j] / averageSpeed;
                    
                    // Cộng thêm thời gian dịch vụ
                    if (location.isPick()) {
                        currentTime += location.getServiceTimePick();
                    }
                    if (location.isDeliver()) {
                        currentTime += location.getServiceTimeDeliver();
                    }
                    
                    currentTime += travelTime;
                    
                    // Kiểm tra vi phạm cửa sổ thời gian
                    if (currentTime < location.getLtw()) {
                        totalViolation += location.getLtw() - currentTime;
                    } else if (currentTime > location.getUtw()) {
                        totalViolation += currentTime - location.getUtw();
                    }
                }
            }
        }
        return totalViolation;
    }

    // Tính mức độ vi phạm ràng buộc trọng tải
    private static double calculateCapacityViolation(double[][] distances, Location[] locations) {
        double totalViolation = 0;
        
        // Duyệt qua từng xe (mỗi cột trong ma trận distances)
        for (int j = 0; j < distances[0].length; j++) {
            double currentLoad = 0;
            
            // Duyệt qua các điểm trong lộ trình của xe
            for (int i = 0; i < distances.length; i++) {
                if (distances[i][j] > 0) {
                    Location location = locations[i];
                    
                    // Cập nhật trọng tải hiện tại
                    if (location.isPick()) {
                        currentLoad += location.getDemandPick();
                    }
                    if (location.isDeliver()) {
                        currentLoad -= location.getDemandDeliver();
                    }
                    
                    // Kiểm tra vi phạm trọng tải
                    if (currentLoad > CheckConditionUtil.MAX_CAPACITY) {
                        totalViolation += currentLoad - CheckConditionUtil.MAX_CAPACITY;
                    }
                }
            }
        }
        return totalViolation;
    }
}
