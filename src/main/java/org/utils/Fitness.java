package org.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.model.Location;
import org.model.Pair;
import org.model.Route;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Fitness {
    static Fitness fitness;
    List<Location> customers;

    private Fitness() {}

    public static Fitness getInstance() {
        if (fitness == null) {
            fitness = new Fitness();
        }
        return fitness;
    }

    public double calculate(List<Location> locations, List<Route> routes) {
        customers = locations;
        return fitness(routes);
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
    private double fitness(List<Route> route) {
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
        Location depot = customers.get(0);
        for (Route route : routes) {
            List<Pair<Integer, Location>> indLoc = route.getIndLoc();
            double waitTime = calculatorWaitTime(depot, customers.get(indLoc.get(0).getKey()));
            for (int i = 0; i < indLoc.size() - 1; i++) {
                waitTime += calculatorWaitTime(customers.get(indLoc.get(i).getKey()), customers.get(indLoc.get(i + 1).getKey()));
            }
            waitTime += calculatorWaitTime(customers.get(indLoc.get(indLoc.size() - 1).getKey()), depot);
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
            for (int i = 0; i < route.size(); i++) {
                totalServices += route.get(i).getValue().getServiceTime();
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
        Location depot = customers.get(0);
        for (Route route : routes) {
            List<Pair<Integer, Location>> indLoc = route.getIndLoc();
            totalDistance += depot.distance(customers.get(indLoc.get(0).getKey()));
            for (int i = 0; i < indLoc.size() - 1; i++) {
                totalDistance += customers.get(indLoc.get(i).getKey()).distance(customers.get(indLoc.get(i + 1).getKey()));
            }
            totalDistance += depot.distance(customers.get(indLoc.size() - 1));
        }
        return totalDistance;
    }
}
