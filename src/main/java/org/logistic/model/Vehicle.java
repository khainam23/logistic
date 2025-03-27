package org.logistic.model;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Vehicle {
    Point point;
    int capacity;
    int[] route; // Cách chiếc xe này di chuyển
}
