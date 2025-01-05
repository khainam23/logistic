package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.awt.*;
import java.util.List;

/**
 * Định danh về phương tiện trong không gian
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Vehicle {
    long id; // Định danh cho chiếc xe
    Point point; // Tọa độ trong không gian
    int Q; // Khả năng trọng tải tối đa (bao gồm cả hàng nhận và trả)
    int q; // Trọng tải hiện đang có
    List<Location> way; // Đoạn đường sẽ đi
}
