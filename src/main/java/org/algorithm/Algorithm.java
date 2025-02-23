package org.algorithm;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Location;
import org.model.Route;
import org.utils.Fitness;

import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Data
public abstract class Algorithm {
    final int MAX_ITERATOR = 100; // Giới hạn số lần tìm giải pháp
    final int MAX_TIME_SOLUTION = 60000; // Giới hạn thời gian tìm giải pháp
    double[][] distances; // Khoảng các giữa các điểm
    List<Location> locations; // Các khách hàng
    List<Route> solution; // Giải pháp hiện có
    long firstTimeSolution;
    final Random rd = new Random();

    public Algorithm(List<Location> locations, List<Route> solution) {
        this.locations = locations;
        this.solution = solution;
        this.distances = new double[locations.size()][locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.size(); j++) {
                distances[i][j] = locations.get(i).distance(locations.get(j));
            }
        }
    }

    public double fitness(List<Route> routes) {
        return Fitness.getInstance().calculate(locations, routes);
    }

    public int calculateLoad(Route route) {
        int totalLoad = 0;
        for (int i = 0; i < route.size(); i++) {
            totalLoad += route.get(i).getValue().getLoad();
        }
        return totalLoad;
    }

    // Các thuật toán kế thừa triển khai tại đây
    public abstract Result optimizer();

}
