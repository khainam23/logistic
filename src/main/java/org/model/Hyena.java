package org.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hyena extends Vehicle {

    public Hyena(List<Route> routes, double fitness) {
        super(routes, fitness);
    }

    /**
     * Đại diện cho thuật toán Spoted Hyena Optimizer.
     * Mỗi một hyena đại diện cho một giải pháp trong bài toán.
     */
}
