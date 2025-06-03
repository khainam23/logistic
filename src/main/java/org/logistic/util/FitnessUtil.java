package org.logistic.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.logistic.model.Location;
import org.logistic.model.Route;

/**
 * Tiện ích tính toán giá trị fitness cho các giải pháp
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FitnessUtil {
    static FitnessUtil instance;
    int[] tempWeights;

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
     * @param routes    Mảng các tuyến đường
     * @param locations Mảng các vị trí
     * @return Giá trị fitness (càng thấp càng tốt)
     */
    public double calculatorFitness(Route[] routes, Location[] locations) {
        AtomicInteger totalDistances = new AtomicInteger(0);
        AtomicInteger totalServiceTime = new AtomicInteger(0);
        AtomicInteger totalWaitingTime = new AtomicInteger(0);
        AtomicInteger numberVehicle = new AtomicInteger(0);

        Arrays.stream(routes).parallel().forEach(route -> {
            int[] indLocs = route.getIndLocations();

            if (indLocs.length > 0) {
                numberVehicle.incrementAndGet();

                for (int j = 0; j < indLocs.length - 1; j++) {
                    Location currLoc = locations[indLocs[j]];
                    Location nextLoc = locations[indLocs[j + 1]];

                    // Tính khoảng cách
                    totalDistances.addAndGet(currLoc.distance(nextLoc));

                    // Tính thời gian phục vụ
                    totalServiceTime.addAndGet(nextLoc.totalServiceTime());

                    // Tính thời gian chờ của khách hàng
                    int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - currLoc.distance(nextLoc);
                    if (waitingTime > 0) {
                        totalWaitingTime.addAndGet(waitingTime);
                    }
                }

                // Thêm khoảng cách về kho
                totalDistances.addAndGet(
                        locations[indLocs[indLocs.length - 1]].distance(locations[0]));
            }
        });

        tempWeights[0] = numberVehicle.get();
        tempWeights[1] = totalDistances.get();
        tempWeights[2] = totalServiceTime.get();
        tempWeights[3] = totalWaitingTime.get();

        // Trả về giá trị fitness
        double alpha = 1.0, beta = 1.0, gamma = 1.0, delta = 1.0;
        return alpha * totalDistances.get() + beta * totalServiceTime.get() +
                gamma * totalWaitingTime.get() + delta * numberVehicle.get();

    }
    
    /**
     * Lấy các giá trị weights tạm thời
     * @return mảng [numberVehicle, totalDistances, totalServiceTime, totalWaitingTime]
     */
    public int[] getTempWeights() {
        return tempWeights.clone();
    }

}
