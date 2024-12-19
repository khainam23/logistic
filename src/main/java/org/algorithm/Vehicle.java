package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

/**
 * Định danh về phương tiện trong không gian
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Vehicle {
    long id; // Định danh cho chiếc xe
    int x; // Tọa độ x trong không gian
    int y; // Tọa độ y trong không gian
    int maxLoad; // Khả năng trọng tải tối đa
    double cost; // Chi phí khi vận hành xe
    double timeStart; // Thời gian bắt đầu di chuyển
    double timeEnd; // Thời gian sẽ ngừng
    ArrayList<Location> way; // Đoạn đường sẽ đi
}
