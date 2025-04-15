package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    int maxPayload; // Trọng tải tối đa trên tuyến này
    private double distance; // Khoảng cách của tuyến đường

    /**
     * Constructor với chỉ indLocations và maxPayload
     * @param indLocations Mảng chỉ số các địa điểm
     * @param maxPayload Trọng tải tối đa
     */
    public Route(int[] indLocations, int maxPayload) {
        this.indLocations = indLocations;
        this.maxPayload = maxPayload;
        this.distance = 0;
    }

    /**
     * Tạo bản sao của tuyến đường
     * @return Bản sao của tuyến đường
     */
    public Route copy() {
        return new Route(indLocations.clone(), maxPayload, distance);
    }

    /**
     * Tính khoảng cách của tuyến đường
     * @param locations Mảng các địa điểm
     * @return Khoảng cách của tuyến đường
     */
    public double calculateDistance(Location[] locations) {
        if (indLocations.length <= 1) {
            return 0;
        }
        
        double totalDistance = 0;
        for (int i = 0; i < indLocations.length - 1; i++) {
            int currentIndex = indLocations[i];
            int nextIndex = indLocations[i + 1];
            
            if (currentIndex < locations.length && nextIndex < locations.length) {
                totalDistance += locations[currentIndex].distance(locations[nextIndex]);
            }
        }
        
        this.distance = totalDistance;
        return totalDistance;
    }

    /**
     * Lấy khoảng cách của tuyến đường
     * @return Khoảng cách của tuyến đường
     */
    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "Route: " + Arrays.toString(indLocations) + " - payload: " + maxPayload + " - distance: " + distance;
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
