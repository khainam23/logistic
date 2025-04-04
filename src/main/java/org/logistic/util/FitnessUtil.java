package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

public class FitnessUtil {
    /**
     * Tính giá trị của giải pháp.
     *
     * @param solution
     * @return
     */
    public double calculatorFitness(Solution solution, Location[] locations) {
        int totalDistances = 0, totalServiceTime = 0, totalWaitingTime = 0;
        int numberVehicle = solution.countVehicles();  // Đếm số lượng xe

        for (Route route : solution.getRoutes()) {
            totalDistances += route.totalDistances(); // Tính tổng khoảng cách
            totalServiceTime += route.totalServiceTimes(); // Tính tổng thời gian phục vụ

            int[] indLocs = route.getIndLocations();
            for (int j = 0; j < indLocs.length - 1; j++) {
                Location currLoc = locations[indLocs[j]];
                Location nextLoc = locations[indLocs[j + 1]];

                // Tính thời gian chờ
                int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - currLoc.distance(nextLoc);
                if (waitingTime > 0) totalWaitingTime += waitingTime;
            }
        }

        // Trả về giá trị fitness
        return totalDistances + totalServiceTime + totalWaitingTime + numberVehicle;
    }

}
