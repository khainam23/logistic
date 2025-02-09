package org.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.model.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ChangeSolution {
    static Random rd = new Random();

    public static List<Route> swapOperation(List<Route> routes) {
        List<Route> newRoute = new ArrayList<>(routes);
        int ind = rd.nextInt(newRoute.size()); // Chọn ngẫu nhiên phương tiện
        Collections.shuffle(newRoute.get(ind).getIndLoc()); // Hoán đổi vị trí di chuyển của một phương tiện
        return newRoute;
    }

    /**
     * Dùng thực hiện các công việc biến đổi giải pháp.
     * Swap Operation:
     */
}
