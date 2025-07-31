package org.logistic.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    int maxPayload; // Trọng tải tối đa trên tuyến này
    double distance; // Khoảng cách của tuyến đường
    boolean isUse;

    /**
     * Constructor với chỉ indLocations và maxPayload
     * 
     * @param indLocations Mảng chỉ số các địa điểm
     * @param maxPayload   Trọng tải tối đa
     */
    public Route(int[] indLocations, int maxPayload) {
        this.indLocations = indLocations;
        this.maxPayload = maxPayload;
        this.distance = 0;
        this.isUse = true;
    }

    public Route(int[] indLocations, int maxPayload, double distance) {
        this.indLocations = indLocations;
        this.maxPayload = maxPayload;
        this.distance = distance;
        this.isUse = true;
    }

    /**
     * Tạo bản sao của tuyến đường
     * 
     * @return Bản sao của tuyến đường
     */
    public Route copy() {
        int[] copiedIndLocations = (indLocations != null) ? indLocations.clone() : new int[0];
        return new Route(copiedIndLocations, maxPayload, distance);
    }

    /**
     * Tính khoảng cách của tuyến đường
     *
     * @param locations Mảng các địa điểm
     */
    public void calculateDistance(Location[] locations) {
        if (indLocations == null || indLocations.length < 1) {
            this.distance = 0;
            return;
        }

        double totalDistance = 0;

        for (int i = 0; i < indLocations.length - 1; i++) {
            int currentIndex = indLocations[i];
            int nextIndex = indLocations[i + 1];

            if (currentIndex < locations.length && nextIndex < locations.length) {
                totalDistance += locations[currentIndex].distance(locations[nextIndex]);
            }
        }
        totalDistance += locations[indLocations[indLocations.length - 1]].distance(locations[0]); // Về kho

        this.distance = totalDistance;
    }

    @Override
    public String toString() {
        return "Route: " + Arrays.toString(indLocations) + " - payload: " + maxPayload + " - distance: " + distance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Route route = (Route) o;
        return maxPayload == route.maxPayload && Objects.deepEquals(indLocations, route.indLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(indLocations), maxPayload);
    }
}
