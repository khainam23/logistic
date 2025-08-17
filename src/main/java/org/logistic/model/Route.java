package org.logistic.model;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    double maxPayload; // Trọng tải tối đa trên tuyến này
    double distance; // Khoảng cách của tuyến đường
    boolean isUse;

    /**
     * Constructor với chỉ indLocations và maxPayload
     * 
     * @param indLocations Mảng chỉ số các địa điểm
     * @param maxPayload   Trọng tải tối đa
     */
    public Route(int[] indLocations, double maxPayload) {
        this.indLocations = indLocations;
        this.maxPayload = maxPayload;
        this.distance = 0;
        this.isUse = true;
    }

    public Route(int[] indLocations, double maxPayload, double distance) {
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
     * Tính khoảng cách của tuyến đường sử dụng tọa độ Euclidean
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

    /**
     * Tính khoảng cách của tuyến đường sử dụng thông tin DistanceTime
     *
     * @param locations     Mảng các địa điểm
     * @param distanceTimes Mảng thông tin khoảng cách-thời gian
     */
    public void calculateDistance(Location[] locations, DistanceTime[] distanceTimes) {
        if (indLocations == null || indLocations.length < 1) {
            this.distance = 0;
            return;
        }

        if (distanceTimes == null || distanceTimes.length == 0) {
            // Fallback về phương thức cũ nếu không có DistanceTime
            calculateDistance(locations);
            return;
        }

        // Tạo map để tra cứu nhanh DistanceTime
        Map<String, DistanceTime> distanceMap = Arrays.stream(distanceTimes)
                .collect(Collectors.toMap(
                    dt -> dt.getFromNode() + "-" + dt.getToNode(),
                    dt -> dt,
                    (existing, replacement) -> existing
                ));

        double totalDistance = 0;

        for (int i = 0; i < indLocations.length - 1; i++) {
            int fromNode = indLocations[i];
            int toNode = indLocations[i + 1];
            
            DistanceTime dt = distanceMap.get(fromNode + "-" + toNode);
            if (dt != null) {
                totalDistance += dt.getDistance();
            } else {
                // Fallback về tính toán Euclidean nếu không tìm thấy
                if (fromNode < locations.length && toNode < locations.length) {
                    totalDistance += locations[fromNode].distance(locations[toNode]);
                }
            }
        }

        // Thêm khoảng cách về depot (node 0)
        int lastNode = indLocations[indLocations.length - 1];
        DistanceTime dt = distanceMap.get(lastNode + "-0");
        if (dt != null) {
            totalDistance += dt.getDistance();
        } else {
            if (lastNode < locations.length) {
                totalDistance += locations[lastNode].distance(locations[0]);
            }
        }

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
