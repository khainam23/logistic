package org.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.model.Location;
import org.model.Pair;
import org.model.Route;
import org.model.Vehicle;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckConstraint {
    static CheckConstraint checkConstraint;

    private CheckConstraint() {}

    public static CheckConstraint getInstance() {
        if (checkConstraint == null) {
            checkConstraint = new CheckConstraint();
        }
        return checkConstraint;
    }

    private boolean isInsertionFeasible(Vehicle vehicle, Location cusPick, Location cusDelivery, int indPick, int indDelivery) {
        if (indPick > indDelivery) return false;

        Route cloneRoute = vehicle.cloneRoute();
        cloneRoute.add(Pair.<Integer, Location>builder().key(indPick).value(cusPick).build());
        cloneRoute.add(Pair.<Integer, Location>builder().key(indDelivery).value(cusDelivery).build());

        // Kiểm tra ràng buộc trọng lượng
        int currentCapacity = 0;
        for (int i = 0; i < cloneRoute.size(); i++) {
            currentCapacity += cloneRoute.get(i).getValue().getLoad();
            if (currentCapacity > vehicle.getCapacity()) return false;
        }

        // Ràng buộc khung thời gian
        double finishServiceTime = 0;
        double arrivalTime = 0;
        for (int i = 0; i < cloneRoute.size(); i++) {
            if (i == 0) {
                // Di chuyển đi lần đầu
                arrivalTime = Location.depot.distance(cloneRoute.get(i).getValue());
                finishServiceTime += Math.max(arrivalTime, cloneRoute.get(i).getValue().getLTW())
                        + cloneRoute.get(i).getValue().getServiceTime();
            } else {
                // Di chuyển giữa các điểm
                arrivalTime = finishServiceTime + cloneRoute.get(i - 1).getValue().distance(cloneRoute.get(i).getValue());
                finishServiceTime = Math.max(arrivalTime, cloneRoute.get(i).getValue().getLTW()) + cloneRoute.get(i).getValue().getServiceTime();
            }

            if (arrivalTime > cloneRoute.get(i).getValue().getUTW()) return false;
        }

        // Kiểm tra time window của depot và điểm cuối (quay về)
        arrivalTime += finishServiceTime +
                cloneRoute.get(cloneRoute.size() - 1).getValue().distance(Location.depot);
        if (arrivalTime > cloneRoute.get(0).getValue().getUTW()) return false;

        return true;
    }
}
