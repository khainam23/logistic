package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;

/**
 * Tiện ích kiểm tra các điều kiện ràng buộc cho tuyến đường
 */
public class CheckConditionUtil {
    private static CheckConditionUtil instance;

    private CheckConditionUtil() {
        // Private constructor để ngăn khởi tạo trực tiếp
    }

    /**
     * Lấy instance của CheckConditionUtil (Singleton pattern)
     * 
     * @return Instance của CheckConditionUtil
     */
    public static synchronized CheckConditionUtil getInstance() {
        if (instance == null) {
            instance = new CheckConditionUtil();
        }
        return instance;
    }

    /**
     * Kiểm tra xem tuyến đường có hợp lệ không
     * 
     * Hợp lệ là khi:
     * - Trọng tải không xảy ra mâu thuẫn (không xuất hiện số âm trong quá trình)
     * - Thời gian giao phải khớp với cửa sổ thời gian
     *
     * @param route Tuyến đường cần kiểm tra
     * @param locations Mảng các vị trí
     * @param maxPayload Trọng tải tối đa của phương tiện
     * @param currTarget Trọng tải hiện tại
     * @return true nếu tuyến đường hợp lệ, false nếu không
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
                
                // Nếu vượt quá cửa sổ thời gian trên, tuyến đường không hợp lệ
                if (serviceTime > nextLoc.getUtw()) {
                    return false;
                }
            }

            // Kiểm tra giao hàng
            if (currLoc.isDeliver()) {
                targetPayload -= currLoc.getDemandDeliver();
                // Nếu trọng tải âm, tuyến đường không hợp lệ
                if (targetPayload < 0) {
                    return false;
                }
            }

            // Kiểm tra lấy hàng
            if (currLoc.isPick()) {
                targetPayload += currLoc.getDemandPick();
                // Nếu vượt quá trọng tải tối đa, tuyến đường không hợp lệ
                if (targetPayload > maxPayload) {
                    return false;
                }
            }
        }

        // Tuyến đường hợp lệ nếu đã vượt qua tất cả các kiểm tra
        return true;
    }
}
