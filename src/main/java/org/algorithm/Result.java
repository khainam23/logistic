package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

/**
 * Kết quả sẽ hiển thị xử lý
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Result {
    double cost; // Chi phí
    ArrayList<Vehicle> vehicles; // Các di chuyển của các xe
}
