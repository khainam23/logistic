package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class CheckConditionUtil {
    private static CheckConditionUtil checkConditionUtil;

    private CheckConditionUtil() {}

    public static CheckConditionUtil getInstance() {
        if (checkConditionUtil == null)
            checkConditionUtil = new CheckConditionUtil();
        return checkConditionUtil;
    }

    /**
     * Kiểm tra xem tuyến đường có hợp lệ.
     * Hợp lệ là khi:
     * + Trọng tải không xảy ra mâu thuẫn (giá trị trong quá trình không xuất hiện số âm)
     * + Thời gian giao phải khớp
     *
     * @param route
     * @return
     */
    public boolean isInsertionFeasible(Route route, Location[] locations, int maxPayload, int currTarget) {
        int[] indLocations = route.getIndLocations();
        int targetPayload = currTarget;
        int serviceTime = 0;
        int length = indLocations.length;

        for (int i = 0; i < length; i++) {
            Location currLoc = locations[indLocations[i]];

            // Kiểm tra có vi phạm thời gian tối đa
            if (i < length - 1) {
                Location nextLoc = locations[indLocations[i + 1]];
                serviceTime += currLoc.totalServiceTime() + currLoc.distance(nextLoc);
                if (serviceTime > nextLoc.getUtw()) return false;
            }

            // Cần kiểm tra có nhận hàng trước
            if (currLoc.isDeliver()) {
                targetPayload -= currLoc.getDemandDeliver();
                if (targetPayload < 0) return false;
            }

            // Lấy hàng nếu có
            if (currLoc.isPick()) {
                targetPayload += currLoc.getDemandPick();
                if (targetPayload > maxPayload) return false;
            }
        }

        return true;
    }
}
