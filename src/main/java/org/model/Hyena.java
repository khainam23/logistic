package org.model;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hyena extends Vehicle {
    @Setter
    double fitness;

    public Hyena(List<Location> ways, double[][] distances, int maxCapacity) {
        super(ways, distances, maxCapacity);
    }

}
