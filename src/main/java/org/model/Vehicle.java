package org.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class Vehicle {
    int[] route;
    List<Location> locations;
    double[][] distances;
    int capacity;
    double[] lowerBounds;
    double[] upperBounds;

    public Vehicle(List<Location> locations, double[][] distances) {
        this.locations = locations;
        lowerBounds = new double[locations.size()];
        upperBounds = new double[locations.size()];
        this.distances = new double[distances.length][distances.length];
        for (int rows = 0; rows < distances.length; rows++) {
            for (int cols = 0; cols < distances.length; cols++) {
                this.distances[rows][cols] = distances[rows][cols];
            }
            lowerBounds[rows] = locations.get(rows).getLTW();
            upperBounds[rows] = locations.get(rows).getUTW();
        }
        this.route = new int[locations.size()];
    }

}
