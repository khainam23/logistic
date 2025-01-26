package org.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class Location {
    long no; // id
    Point point;
    int demandPick;
    int demandDeliver;
    int LTW;
    int UTW;
    int serviceTime; // Bao gồm cả thời lấy và thời gian trả hàng
    /**
     * Dựa vào các thuộc tính trong database của VRPTW mà triển khai
     * các thuộc tính của một địa điểm
     */
}
