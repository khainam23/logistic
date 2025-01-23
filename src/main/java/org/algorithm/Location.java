package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    long id;
    long no;
    int x;
    int y;
    int demandPick;
    int demandDeliver;
    int lowerTime;
    int upperTime;
    int serviceTime; // Bao gồm cả thời lấy và thời gian trả hàng

    /**
     * Dựa vào các thuộc tính trong database của VRPTW mà triển khai
     * các thuộc tính của một địa điểm
     */
}
