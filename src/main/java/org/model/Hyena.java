package org.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hyena extends Vehicle {
    public Hyena(List<Location> ways, double[][] distances) {
        super(ways, distances);
    }

}
