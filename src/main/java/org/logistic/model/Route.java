package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return maxPayload == route.maxPayload && Objects.deepEquals(indLocations, route.indLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(indLocations), maxPayload);
    }
}
