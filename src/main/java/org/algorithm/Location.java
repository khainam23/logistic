package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.awt.*;

/**
 * Thể hiện tính chất của một điểm trong không gian
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class Location {
    int x; // Tọa độ x trong không gian
    int y; // Tọa độ y trong không gian
    boolean isPickup; // Có là điểm lấy hàng
    boolean isDrop; // Có là điểm thả hàng
    int loadPickup; // Số lượng phải lấy
    int loadDrop; // Số lượng phải thả
    boolean isGet; // Cho biết đã lấy chưa
    boolean isGive; // Cho biết đã thả chưa
    int LTW; // Low time window: Thời gian sớm nhất có thể tới
    int UTW; // Upper time window: Thời gian trễ nhất có thể tới

    /**
     * Tính khoảng cách bằng phương pháp euclid.
     * Sử dụng công thức tính trong Point đã được java triển khai.
     */
    public double calculatorDistance(Location oLoc) {
        Point point = new Point(x, y);
        Point oPoint = new Point(oLoc.x, oLoc.y);
        return point.distance(oPoint);
    }
}
