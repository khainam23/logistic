package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;

/**
 * Tiện ích tính toán giá trị fitness cho các giải pháp
 */
public class FitnessUtil {
    private static FitnessUtil instance;

    private FitnessUtil() {
        // Private constructor để ngăn khởi tạo trực tiếp
    }

    /**
     * Lấy instance của FitnessUtil (Singleton pattern)
     * 
     * @return Instance của FitnessUtil
     */
    public static synchronized FitnessUtil getInstance() {
        if (instance == null) {
            instance = new FitnessUtil();
        }
        return instance;
    }

    /**
     * Tính giá trị fitness của giải pháp dựa trên các tuyến đường và vị trí
     *
     * @param routes Mảng các tuyến đường
     * @param locations Mảng các vị trí
     * @return Giá trị fitness (càng thấp càng tốt)
     */
    public double calculatorFitness(Route[] routes, Location[] locations) {
        int totalDistances = 0;
        int totalServiceTime = 0;
        int totalWaitingTime = 0;
        int numberVehicle = routes.length;  // Đếm số lượng xe

        for (Route route : routes) {
            int[] indLocs = route.getIndLocations();
            for (int j = 0; j < indLocs.length - 1; j++) {
                Location currLoc = locations[indLocs[j]];
                Location nextLoc = locations[indLocs[j + 1]];

                // Tính khoảng cách
                int distance = currLoc.distance(nextLoc);
                totalDistances += distance;

                // Tính thời gian phục vụ
                totalServiceTime += nextLoc.totalServiceTime();

                // Tính thời gian chờ của khách hàng
                int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - distance;
                if (waitingTime > 0) {
                    totalWaitingTime += waitingTime;
                }
            }
        }

        // Trả về giá trị fitness
        return totalDistances + totalServiceTime + totalWaitingTime + numberVehicle;
    }
}
