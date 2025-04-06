package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua

    public Route(int[] indLocations) {
        this.indLocations = indLocations;
    }

    @Override
    public String toString() {
        return "Route: " + Arrays.toString(indLocations);
    }
}
