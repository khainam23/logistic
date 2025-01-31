package org.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route {
    int[] routes; // Các location trong nhánh này
    int currentCapacity;
    /**
     * Ràng buộc về việc di chuyển nhận giao giữa các location
     */
}
