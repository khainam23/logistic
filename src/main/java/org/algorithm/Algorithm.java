package org.algorithm;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Location;
import org.model.Pair;
import org.model.Route;
import org.model.Vehicle;
import org.utils.CheckConstraint;
import org.utils.Fitness;

import java.util.Arrays;
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
    Vehicle vehicle; // Phương tiện sở hữu
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

    // Lấy ngẫu nhiên cặp pick - delivery
    public List<Pair<Integer, Location>> getRdPD(Route route) {
        int i1 = rd.nextInt((int) (route.size() * 0.5)); // Đảm bảo luôn có điểm lấy
        int i2 = rd.nextInt(route.size());
        while (CheckConstraint.getInstance()
                .isInsertionFeasible(vehicle,
                        route.get(i1).getValue(), route.get(i2).getValue(),
                        i1, i2)
        ) {
            i2 = rd.nextInt(route.size());
        }
        return Arrays.asList(route.get(i1), route.get(i2));
    }

    // Các thuật toán kế thừa triển khai tại đây
    public abstract Result optimizer();

}
