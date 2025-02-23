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
    double LTW;
    double UTW;
    int serviceTime; // Bao gồm cả thời lấy và thời gian trả hàng
    boolean isServiced; // Cho biết điểm có thể phục vụ
    public static final Location depot = new Location(0, new Point(0, 0));

    // Khởi tạo ban đầu
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

    public void reset() {
        this.isServiced = false;
    }

    public void print() {
        System.out.printf("Id: %d Point: (x=%d, y=%d) Pick=%d Deliver=%d Window=[%s, %s] Service=%d\n", no, point.x, point.y, demandPick, demandDeliver, LTW, UTW, serviceTime);
    }
    /**
     * Dựa vào các thuộc tính trong database của VRPTW mà triển khai
     * các thuộc tính của một địa điểm
     */
}
