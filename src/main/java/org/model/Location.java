package org.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Location {
    long no; // id
    Point point;
    int demandPick;
    int demandDeliver;
    int LTW;
    int UTW;
    int serviceTime; // Bao gồm cả thời lấy và thời gian trả hàng
    boolean isServiced; // Cho biết điểm có thể phục vụ

    // Dành cho depot
    public Location(long no, Point point) {
        this.no = no;
        this.point = point;
    }

    // Dành cho khởi tạo giá trị giả
    public Location(long no, Point point, int demandPick, int demandDeliver, int serviceTime) {
        this.no = no;
        this.point = point;
        this.demandPick = demandPick;
        this.demandDeliver = demandDeliver;
        this.serviceTime = serviceTime;
        this.isServiced = false;
    }

    // Tính khoảng cách euclidian giữa hai điểm
    public double distance(Location location) {
        return this.point.distance(location.point);
    }

    // Lấy trọng lượng khi xe tới
    public int getLoad() {
        return demandPick - demandDeliver;
    }
    /**
     * Dựa vào các thuộc tính trong database của VRPTW mà triển khai
     * các thuộc tính của một địa điểm
     */
}
