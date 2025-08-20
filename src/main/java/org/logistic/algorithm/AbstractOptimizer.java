package org.logistic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.logistic.model.DistanceTime;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Lớp trừu tượng cung cấp các phương thức chung cho các thuật toán tối ưu hóa
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractOptimizer implements Optimizer {

    // Các tham số chung
    final Random random = new Random();

    // Các tham số được thiết lập trong quá trình chạy
    Location[] locations;
    DistanceTime[] distanceTimes;
    FitnessUtil fitnessUtil;
    CheckConditionUtil checkConditionUtil;

    /**
     * Khởi tạo optimizer
     */
    public AbstractOptimizer() {
        // Constructor không tham số
    }

    /**
     * Override phương thức run với DistanceTime
     */
    @Override
    public Solution run(Solution[] initialSolutions,
            FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil,
            Location[] locations,
            DistanceTime[] distanceTimes) {
        this.distanceTimes = distanceTimes;
        return run(initialSolutions, fitnessUtil, checkConditionUtil, locations);
    }

    /**
     * Áp dụng toán tử hoán đổi (swap) cho một tuyến đường
     *
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applySwapOperator(Route route) {
        int[] way = route.getIndLocations();
        if (way.length < 2) {
            return; // Không thể hoán đổi nếu chỉ có 1 phần tử hoặc ít hơn
        }

        // Chọn hai vị trí ngẫu nhiên khác nhau
        int pos1 = random.nextInt(way.length);
        int pos2;
        do {
            pos2 = random.nextInt(way.length);
        } while (pos1 == pos2);

        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;
    }

    /**
     * Áp dụng toán tử PD-Shift: Di chuyển một điểm từ một tuyến đường sang tuyến
     * đường khác
     *
     * @param routes Mảng các tuyến đường cần áp dụng toán tử
     */
    protected void applyPdShift(Route[] routes) {
        if (routes.length < 2) {
            return; // Cần ít nhất 2 tuyến đường để thực hiện shift
        }

        // Chọn ngẫu nhiên 2 tuyến đường khác nhau
        int routeIndex1 = random.nextInt(routes.length);
        int routeIndex2;
        do {
            routeIndex2 = random.nextInt(routes.length);
        } while (routeIndex1 == routeIndex2);

        Route route1 = routes[routeIndex1];
        Route route2 = routes[routeIndex2];

        int[] way1 = route1.getIndLocations();
        int[] way2 = route2.getIndLocations();

        // Kiểm tra nếu một trong hai tuyến đường không có điểm nào
        if (way1.length == 0 || way2.length == 0) {
            return;
        }

        // Lưu lại bản sao của các tuyến đường gốc để khôi phục nếu cần
        int[] originalWay1 = way1.clone();
        int[] originalWay2 = way2.clone();

        // Chọn một điểm ngẫu nhiên từ tuyến đường 1 để di chuyển sang tuyến đường 2
        int posToMove = random.nextInt(way1.length);
        int locationToMove = way1[posToMove];

        // Chọn vị trí ngẫu nhiên trên tuyến đường 2 để chèn điểm
        int insertPos = way2.length > 0 ? random.nextInt(way2.length + 1) : 0;

        // Tạo mảng mới cho tuyến đường 1 (loại bỏ điểm được di chuyển)
        int[] newWay1 = new int[way1.length - 1];
        for (int i = 0, j = 0; i < way1.length; i++) {
            if (i != posToMove) {
                newWay1[j++] = way1[i];
            }
        }

        // Tạo mảng mới cho tuyến đường 2 (thêm điểm mới)
        int[] newWay2 = new int[way2.length + 1];
        for (int i = 0; i < insertPos; i++) {
            newWay2[i] = way2[i];
        }
        newWay2[insertPos] = locationToMove;
        for (int i = insertPos; i < way2.length; i++) {
            newWay2[i + 1] = way2[i];
        }

        // Cập nhật các tuyến đường
        route1.setIndLocations(newWay1);
        route2.setIndLocations(newWay2);

        // Đảm bảo giá trị không vượt quá giới hạn
        validateLocationIndices(newWay1);
        validateLocationIndices(newWay2);

        // Kiểm tra tính khả thi
        if (locations != null) {
            if (!checkConditionUtil.isInsertionFeasible(route1, locations, route1.getMaxPayload()) ||
                    !checkConditionUtil.isInsertionFeasible(route2, locations, route2.getMaxPayload())) {
                // Khôi phục lại nếu không khả thi
                route1.setIndLocations(originalWay1);
                route2.setIndLocations(originalWay2);
                return;
            }

            // Cập nhật khoảng cách
            if (distanceTimes != null && distanceTimes.length > 0) {
                route1.calculateDistance(locations, distanceTimes);
                route2.calculateDistance(locations, distanceTimes);
            } else {
                route1.calculateDistance(locations);
                route2.calculateDistance(locations);
            }
        }
    }

    /**
     * Áp dụng toán tử PD-Exchange: Trao đổi các điểm giữa hai tuyến đường
     *
     * @param routes Mảng các tuyến đường cần áp dụng toán tử
     */
    protected void applyPdExchange(Route[] routes) {
        if (routes.length < 2) {
            return; // Cần ít nhất 2 tuyến đường để thực hiện exchange
        }

        // Chọn ngẫu nhiên 2 tuyến đường khác nhau
        int routeIndex1 = random.nextInt(routes.length);
        int routeIndex2;
        do {
            routeIndex2 = random.nextInt(routes.length);
        } while (routeIndex1 == routeIndex2);

        Route route1 = routes[routeIndex1];
        Route route2 = routes[routeIndex2];

        int[] way1 = route1.getIndLocations();
        int[] way2 = route2.getIndLocations();

        // Kiểm tra nếu một trong hai tuyến đường không có điểm nào
        if (way1.length == 0 || way2.length == 0) {
            return;
        }

        // Chọn một điểm ngẫu nhiên từ mỗi tuyến đường để trao đổi
        int pos1 = random.nextInt(way1.length);
        int pos2 = random.nextInt(way2.length);

        // Trao đổi hai điểm
        int temp = way1[pos1];
        way1[pos1] = way2[pos2];
        way2[pos2] = temp;

        // Cập nhật khoảng cách nếu có thông tin về locations
        if (locations != null) {
            if (distanceTimes != null && distanceTimes.length > 0) {
                route1.calculateDistance(locations, distanceTimes);
                route2.calculateDistance(locations, distanceTimes);
            } else {
                route1.calculateDistance(locations);
                route2.calculateDistance(locations);
            }
        }
    }

    /**
     * Áp dụng toán tử ngẫu nhiên cho khám phá
     *
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applyRandomOperation(Route route) {
        if (!route.isUse())
            return;
        int operator = random.nextInt(2);
        switch (operator) {
            case 0 -> applySwapOperator(route);
            case 1 -> applySwapSequence(route);
        }
    }

    /**
     * Áp dụng toán tử hoán đổi chuỗi cho một tuyến đường
     *
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applySwapSequence(Route route) {
        int[] way = route.getIndLocations();
        if (way.length < 2) {
            return; // Không thể hoán đổi nếu chỉ có 1 phần tử hoặc ít hơn
        }

        int n = random.nextInt(10) + 1;
        for (int i = 0; i < n; i++) {
            applySwapOperator(route);
        }
    }

    /**
     * Áp dụng toán tử PD-Rearrange: Sắp xếp lại các điểm trong tuyến đường theo 3
     * cách
     *
     * @param routes Mảng các tuyến đường cần áp dụng toán tử
     */
    protected void applyPdRearrange(Route[] routes) {
        if (routes.length < 1) {
            return; // Cần ít nhất 1 tuyến đường để thực hiện rearrange
        }

        // Chọn ngẫu nhiên 1 tuyến đường
        int routeIndex = random.nextInt(routes.length);
        Route route = routes[routeIndex];

        int[] way = route.getIndLocations();

        // Kiểm tra nếu tuyến đường không có đủ điểm để sắp xếp lại
        if (way.length < 3) {
            return;
        }

        // Chọn ngẫu nhiên một đoạn để sắp xếp lại
        int startPos = random.nextInt(way.length - 2);
        int endPos = startPos + 2 + random.nextInt(Math.min(5, way.length - startPos - 2));
        int segmentLength = endPos - startPos + 1;

        // Chọn ngẫu nhiên một trong 3 cách sắp xếp
        int method = random.nextInt(3);

        if (method == 0) {
            // Cách 1: Đảo ngược đoạn
            int left = startPos;
            int right = endPos;
            while (left < right) {
                int temp = way[left];
                way[left] = way[right];
                way[right] = temp;
                left++;
                right--;
            }
        } else if (method == 1) {
            // Cách 2: Xoay vòng đoạn
            int rotateBy = 1 + random.nextInt(segmentLength - 1);
            int[] segment = new int[segmentLength];

            // Sao chép đoạn cần xoay
            for (int i = 0; i < segmentLength; i++) {
                segment[i] = way[startPos + i];
            }

            // Xoay vòng đoạn
            for (int i = 0; i < segmentLength; i++) {
                way[startPos + i] = segment[(i + rotateBy) % segmentLength];
            }
        } else {
            // Cách 3: Sắp xếp ngẫu nhiên đoạn
            for (int i = 0; i < segmentLength; i++) {
                int j = random.nextInt(segmentLength);
                int temp = way[startPos + i];
                way[startPos + i] = way[startPos + j];
                way[startPos + j] = temp;
            }
        }

        // Cập nhật khoảng cách nếu có thông tin về locations
        if (locations != null) {
            if (distanceTimes != null && distanceTimes.length > 0) {
                route.calculateDistance(locations, distanceTimes);
            } else {
                route.calculateDistance(locations);
            }
        }
    }

    /**
     * Áp dụng toán tử ngẫu nhiên cho nhiều tuyến đường
     *
     * @param routes Mảng các tuyến đường cần áp dụng toán tử
     */
    protected void applyRandomMultiRouteOperation(Route[] routes) {
        if (routes.length < 2) {
            return; // Cần ít nhất 2 tuyến đường để thực hiện các toán tử đa tuyến
        }

        // Bỏ qua route không sử dụng
        List<Route> usableRoutes = new ArrayList<>();
        for (Route route : routes) {
            if (route.isUse()) {
                usableRoutes.add(route);
            }
        }
        Route[] filterRoute = usableRoutes.toArray(new Route[0]);

        int operator = random.nextInt(3);
        switch (operator) {
            case 0 -> applyPdShift(filterRoute);
            case 1 -> applyPdExchange(filterRoute);
            case 2 -> applyPdRearrange(filterRoute);
        }

        // Loại bỏ các route rỗng
        for (int i = 0; i < routes.length; i++) {
            if (routes[i].getIndLocations().length == 0) {
                routes[i].setUse(false);
            }
        }

    }

    /**
     * Thiết lập các tham số chung cho thuật toán
     *
     * @param fitnessUtil        Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations          Mảng các vị trí
     * @param currentTarget      Trọng tải hiện tại
     */
    protected void setupParameters(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil,
            Location[] locations) {
        this.fitnessUtil = fitnessUtil;
        this.checkConditionUtil = checkConditionUtil;
        this.locations = locations;
    }

    /**
     * Đảm bảo các chỉ số vị trí không vượt quá giới hạn
     *
     * @param indices Mảng các chỉ số cần kiểm tra
     */
    protected void validateLocationIndices(int[] indices) {
        if (locations == null) {
            return;
        }

        int maxLocationIndex = locations.length - 1;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] > maxLocationIndex) {
                indices[i] = maxLocationIndex;
            }
        }
    }
}