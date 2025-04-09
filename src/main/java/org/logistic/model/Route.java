package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    int maxPayload; // Trọng tải tối đa trên tuyến này

    public Route(int[] indLocations) {
        this.indLocations = indLocations;
    }

    public Route copy() {
        return new Route(indLocations.clone());
    }

    @Override
    public String toString() {
        return "Route: " + Arrays.toString(indLocations);
    }
}
