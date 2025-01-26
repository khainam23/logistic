package org.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class Vehicle {
    List<Location> ways;
    double[][] distances;
    int maxCapacity;
    double[] lowerBounds;
    double[] upperBounds;

    public Vehicle(List<Location> ways, double[][] distances, int maxCapacity) {
        this.ways = ways;
        lowerBounds = new double[ways.size()];
        upperBounds = new double[ways.size()];
        this.distances = new double[distances.length][distances.length];
        for (int rows = 0; rows < distances.length; rows++) {
            for (int cols = 0; cols < distances.length; cols++) {
                this.distances[rows][cols] = distances[rows][cols];
            }
            lowerBounds[rows] = ways.get(rows).getLTW();
            upperBounds[rows] = ways.get(rows).getUTW();
        }
        this.maxCapacity = maxCapacity;
    }

}
