package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

import java.lang.module.FindException;

public class FitnessUtil {
    static FitnessUtil fitnessUtil;

    private FitnessUtil() {
    }

    public static synchronized FitnessUtil getInstance() {
        if (fitnessUtil == null)
            fitnessUtil = new FitnessUtil();
        return fitnessUtil;
    }

    /**
     * Tính giá trị của giải pháp.
     *
     * @return
     */
    public double calculatorFitness(Route[] routes, Location[] locations) {
        int totalDistances = 0, totalServiceTime = 0, totalWaitingTime = 0;
        int numberVehicle = routes.length;  // Đếm số lượng xe

        for (Route route : routes) {
            int[] indLocs = route.getIndLocations();
            for (int j = 0; j < indLocs.length - 1; j++) {
                Location currLoc = locations[indLocs[j]];
                Location nextLoc = locations[indLocs[j + 1]];

                // Tính khoảng cách
                totalDistances += currLoc.distance(nextLoc);

                // Tính thời gian phục vụ
                totalServiceTime += nextLoc.totalServiceTime();

                // Tính thời gian chờ
                int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - currLoc.distance(nextLoc);
                if (waitingTime > 0) totalWaitingTime += waitingTime;
            }
        }

        // Trả về giá trị fitness
        return totalDistances + totalServiceTime + totalWaitingTime + numberVehicle;
    }

}
