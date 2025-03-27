package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Vehicle;

public class CheckConditionUtil {
    public static final int MAX_CAPACITY = 100;

    public static boolean isInsertionFeasible(Location pickup, Location delivery, int pInsert, int dInsert, Vehicle vehicle) {
        // Kiểm tra ràng buộc thứ tự: điểm đón phải trước điểm trả
        if (pInsert > dInsert) {
            return false;
        }

        // Kiểm tra ràng buộc về trọng tải
        int currentCapacity = 0;
        if (pickup.getDemandPick() + currentCapacity > vehicle.getCapacity() ||
            delivery.getDemandDeliver() + currentCapacity > vehicle.getCapacity()) {
            return false;
        }

        // Kiểm tra ràng buộc về thời gian
        double currentTime = 0;
        double arrivalTime;

        // Tính thời gian đến điểm đón
        arrivalTime = currentTime + pickup.getPoint().distanceTo(vehicle.getPoint());
        if (arrivalTime > pickup.getUtw()) { // Nếu đến trễ hơn upper time window
            return false;
        }
        currentTime = Math.max(arrivalTime, pickup.getLtw()) + pickup.getServiceTimePick();

        // Tính thời gian đến điểm trả
        arrivalTime = currentTime + pickup.getPoint().distanceTo(delivery.getPoint());
        if (arrivalTime > delivery.getUtw()) { // Nếu đến trễ hơn upper time window
            return false;
        }
        currentTime = Math.max(arrivalTime, delivery.getLtw()) + delivery.getServiceTimeDeliver();

        return true;
    }
}
