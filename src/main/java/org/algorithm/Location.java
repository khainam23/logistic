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
    Point point; // Tọa độ trong không gian
    boolean isPickup; // Đã lấy hàng chưa
    boolean isDrop; // Đã thả hàng chưa
    int loadPickup; // Số lượng phải lấy
    int loadDrop; // Số lượng phải thả
    int LTW; // Low time window: Thời gian sớm nhất có thể tới
    int UTW; // Upper time window: Thời gian trễ nhất có thể tới

    // Tính khoảng cách tọa độ (Euclid)
    public double calculatorDistance(Location otherLocation) {
        return this.point.distance(otherLocation.point);
    }

    // Số lượng dịch vụ phải làm tại điểm
    public int calculatorService() {
        return loadDrop + loadPickup;
    }
}
