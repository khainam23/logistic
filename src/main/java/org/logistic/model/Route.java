package org.logistic.model;

import lombok.Getter;

@Getter
public class Route {
    int[] indLocations; // Những điểm mà đoạn đường đi qua
    int[] distances; // Khoảng cách giữa chúng
    int[] serviceTimes; // Thời gian phục vụ

    // Tổng quảng cách cần di chuyển
    public int totalDistances() {
        int total = 0;
        for (int i = 0; i < distances.length - 1; i++) {
            total += distances[i];
        }
        return total;
    }

    // Tổng thời gian phục vụ
    public int totalServiceTimes() {
        int total = 0;
        for (int i = 0; i < serviceTimes.length - 1; i++) {
            total += serviceTimes[i];
        }
        return total;
    }
}
