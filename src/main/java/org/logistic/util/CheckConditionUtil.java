package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class CheckConditionUtil {
    public static final int MAX_CAPACITY = 100;

    /**
     * Kiểm tra xem việc chèn điểm đón và điểm trả vào tuyến đường có khả thi không
     * @param pickup Điểm đón khách hàng
     * @param delivery Điểm trả khách hàng
     * @param pInsert Vị trí chèn điểm đón
     * @param dInsert Vị trí chèn điểm trả
     * @param vehicle Phương tiện vận chuyển
     * @return true nếu việc chèn khả thi, false nếu không
     */
    public static boolean isInsertionFeasible(Location pickup, Location delivery, int pInsert, int dInsert, Vehicle vehicle) {
        // Kiểm tra ràng buộc thứ tự: điểm đón phải trước điểm trả
        if (pInsert > dInsert) {
            return false;
        }

        // Tạo bản sao của tuyến đường hiện tại
        List<Location> route = cloneRoute(vehicle);
        
        // Chèn điểm trả và điểm đón vào tuyến đường
        insertAt(route, dInsert, delivery);
        insertAt(route, pInsert, pickup);
        
        // Kiểm tra ràng buộc về trọng tải
        int currentCapacity = 0;
        for (int i = 0; i < route.size(); i++) {
            Location location = route.get(i);
            if (location.isPick()) {
                currentCapacity += location.getDemandPick();
            } else if (location.isDeliver()) {
                currentCapacity -= location.getDemandDeliver();
            }
            
//            if (currentCapacity > vehicle.getCapacity()) {
//                return false;
//            }
        }

        // Kiểm tra ràng buộc về thời gian
        double currentTime = 0;
        double arrivalTime;
        Location depot = null; // Giả định depot là điểm đầu tiên
        
        for (int i = 0; i < route.size(); i++) {
            Location current = route.get(i);
            
            if (i == 0) {
                // Điểm đầu tiên trong tuyến đường
                arrivalTime = 0; // Thời gian bắt đầu từ depot
                depot = current; // Lưu lại depot để kiểm tra cuối cùng
            } else {
                // Tính thời gian di chuyển từ điểm trước đó đến điểm hiện tại
                arrivalTime = currentTime + route.get(i-1).getPoint().distanceTo(current.getPoint());
            }
            
            // Kiểm tra cửa sổ thời gian
            if (arrivalTime > current.getUtw()) { // Nếu đến trễ hơn upper time window
                return false;
            }
            
            // Cập nhật thời gian hiện tại
            if (current.isPick()) {
                currentTime = Math.max(arrivalTime, current.getLtw()) + current.getServiceTimePick();
            } else if (current.isDeliver()) {
                currentTime = Math.max(arrivalTime, current.getLtw()) + current.getServiceTimeDeliver();
            } else {
                currentTime = Math.max(arrivalTime, current.getLtw());
            }
        }
        
        // Kiểm tra thời gian quay về depot
        if (route.size() > 0) {
            Location lastLocation = route.get(route.size() - 1);
            arrivalTime = currentTime + lastLocation.getPoint().distanceTo(depot.getPoint());
            if (arrivalTime > depot.getUtw()) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Tạo bản sao của tuyến đường hiện tại
     * @param vehicle Phương tiện chứa tuyến đường
     * @return Bản sao của tuyến đường
     */
    private static List<Location> cloneRoute(Vehicle vehicle) {
        // Trong thực tế, cần lấy tuyến đường từ vehicle
        // Đây là phương thức giả định, cần điều chỉnh theo cấu trúc dữ liệu thực tế
        return new ArrayList<>();
    }
    
    /**
     * Chèn một điểm vào tuyến đường tại vị trí chỉ định
     * @param route Tuyến đường
     * @param index Vị trí chèn
     * @param location Điểm cần chèn
     */
    private static void insertAt(List<Location> route, int index, Location location) {
        if (index >= 0 && index <= route.size()) {
            route.add(index, location);
        }
    }


}
