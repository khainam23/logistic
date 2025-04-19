package org.logistic.util;

import lombok.Getter;
import org.logistic.model.Location;
import org.logistic.model.Route;

/**
 * Tiện ích tính toán giá trị fitness cho các giải pháp
 */
@Getter
public class FitnessUtil {
    private static FitnessUtil instance;
    private int[] tempWeights;

    private FitnessUtil() {
        this.tempWeights = new int[4];
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
        int numberVehicle = 0;

        for (Route route : routes) {
            int[] indLocs = route.getIndLocations();
            for (int j = 0; j < indLocs.length - 1; j++) {
                // Nếu route có giải pháp dựa trên cạnh trên thì tăng số xe tài
                if(j == 0) ++numberVehicle;

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
            totalDistances += indLocs.length == 0 ? 0 : locations[indLocs[indLocs.length - 1]].distance(locations[0]); // Về kho
        }

        tempWeights[0] = numberVehicle;
        tempWeights[1] = totalDistances;
        tempWeights[2] = totalServiceTime;
        tempWeights[3] = totalWaitingTime;

        // Trả về giá trị fitness
        return totalDistances + totalServiceTime + totalWaitingTime + numberVehicle;
    }

}
