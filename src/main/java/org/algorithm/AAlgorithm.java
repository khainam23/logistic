package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quy định chung về thuật toán.
 * Yêu cầu: Bất kỳ triển khai thuật toán nào cũng phải extend class này
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AAlgorithm {
    ArrayList<Location> firstLocation; // Vị trí của các điểm ban đầu
    ArrayList<Vehicle> vehicles; // Các xe ban đầu

    /**
     * Khởi chạy thuật toán. Mỗi thuật toán có cách chạy và biến khác nhau.
     * Yêu cầu:
     * + Thời gian mỗi điểm trong firstLocation phải hợp lý (LTW, UTW),
     * phải sắp theo thứ tự và điểm sau phải lớn hơn điểm trước.
     */
    public abstract void optimize();

    /**
     * Chi phí trên đường đi.
     * Chi phí = Thời gian di chuyển tối đa + Chi phí vận hành của xe
     * (Để đơn giản hóa. Ngoài ra còn phải quan tâm giá xăng, mức độ kẹt xe,...)
     */
    protected double calculatorCost(ArrayList<Location> location, Vehicle vehicle) {
        return location.get(location.size() - 1).getUTW() + vehicle.getCost();
    }

    protected List<ArrayList<Location>> getLocations() {
        return vehicles.stream().map(Vehicle::getWay).collect(Collectors.toList());
    }
}
