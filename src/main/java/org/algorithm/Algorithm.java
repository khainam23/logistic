package org.algorithm;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Location;
import org.model.Route;

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

    /**
     * Đánh giá điểm số trên giải pháp nhận được.
     * !!! CÀNG THẤP THÌ CÀNG TỐT !!!
     * Tính toán giá trị tối ưu trên các tiêu chí:
     * + NV - Số lượng đội xe sử dụng
     * + TC - Tổng quảng đường đi được
     * + SD - Thời lượng của dịch vụ
     * + WT - Thời gian phải chờ của khách hàng
     *
     * @return
     */
    public double fitness(List<Route> route) {
        int NV = route.size(); // Mỗi route đại diện cho cách di chuyển của 1 phương tiện
        double TC = calculationDistanceRoutes(route);
        int SD = calculationServices(route);
        int WT = calculatorWidthTime(route);
        return NV + TC + SD + WT;
    }

    /**
     * Tính thời gian chờ của khách.
     * Di chuyển tới + dịch vụ
     *
     * @param routes
     * @return
     */
    private int calculatorWidthTime(List<Route> routes) {
        int totalWT = 0;
        Location depot = locations.get(0);
        for (Route route : routes) {
            List<Integer> indLoc = route.getIndLoc();
            double waitTime = calculatorWaitTime(depot, locations.get(indLoc.get(0)));
            for (int i = 0; i < indLoc.size() - 1; i++) {
                waitTime += calculatorWaitTime(locations.get(indLoc.get(i)), locations.get(indLoc.get(i + 1)));
            }
            waitTime += calculatorWaitTime(locations.get(indLoc.get(indLoc.size() - 1)), depot);
            totalWT += waitTime;
        }
        return totalWT;
    }

    /**
     * Tính thời gian chờ khi di chuyển giữa hai điểm
     *
     * @param location
     * @param oLocation
     * @return
     */
    private double calculatorWaitTime(Location location, Location oLocation) {
        double wait = location.distance(oLocation) + location.getServiceTime() - oLocation.getLTW();
        return wait < 0 ? 0 : wait;
    }

    /**
     *
     */
    private int calculationServices(List<Route> routes) {
        int totalServices = 0;
        for (Route route : routes) {
            for (Integer index : route.getIndLoc()) {
                totalServices += locations.get(index).getServiceTime();
            }
        }
        return totalServices;
    }

    /**
     * Tính khoảng cách di chuyển trong route,
     * bao gồm cả thời gian đi từ depot và về nó
     *
     * @param routes
     * @return
     */
    private double calculationDistanceRoutes(List<Route> routes) {
        double totalDistance = 0;
        Location depot = locations.get(0);
        for (Route route : routes) {
            List<Integer> indLoc = route.getIndLoc();
            totalDistance += depot.distance(locations.get(indLoc.get(0)));
            for (int i = 0; i < indLoc.size() - 1; i++) {
                totalDistance += locations.get(indLoc.get(i)).distance(locations.get(indLoc.get(i + 1)));
            }
            totalDistance += depot.distance(locations.get(indLoc.size() - 1));
        }
        return totalDistance;
    }

    // Các thuật toán kế thừa triển khai tại đây
    public abstract Result optimizer();

}
