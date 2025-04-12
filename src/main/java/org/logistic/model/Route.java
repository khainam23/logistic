package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    int maxPayload; // Trọng tải tối đa trên tuyến này

    public Route copy() {
        return new Route(indLocations.clone(), maxPayload);
    }

    @Override
    public String toString() {
        return "Route: " + Arrays.toString(indLocations) + " - payload: " + maxPayload;
    }
}
