package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.algorithm.AbstractOptimizer;
import org.logistic.algorithm.Agent;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Thuật toán Spotted Hyena Optimizer
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends AbstractOptimizer {
    // Các tham số của thuật toán
    static final int MAX_ITERATIONS = 1000;
    static final int CLUSTER_SIZE = 5; // Kích thước cụm linh cẩu

    // Danh sách các linh cẩu
    List<Hyena> population;
    List<List<Hyena>> clusters; // Các cụm linh cẩu
    Hyena bestHyena; // Linh cẩu có fitness tốt nhất

    /**
     * Khởi tạo thuật toán Spotted Hyena Optimizer
     */
    public SpottedHyenaOptimizer() {
        super();
    }

    /**
     * Khởi tạo quần thể linh cẩu từ các giải pháp ban đầu
     */

    private void initialize(Solution[] initialSolutions) {
        population = new ArrayList<>();
        clusters = new ArrayList<>();

        // Khởi tạo quần thể từ các giải pháp ban đầu
        for (Solution solution : initialSolutions) {
            Hyena hyena = new Hyena(solution.copy(), solution.getFitness());
            population.add(hyena);

            // Cập nhật linh cẩu tốt nhất
            if (bestHyena == null || hyena.getFitness() < bestHyena.getFitness()) {
                bestHyena = hyena;
            }
        }

        // Phân cụm quần thể
        formClusters();
    }

    /**
     * Phân cụm quần thể linh cẩu
     */
    private void formClusters() {
        clusters.clear();

        // Sắp xếp quần thể theo fitness
        population.sort(Comparator.comparingDouble(Agent::getFitness));

        // Tạo các cụm
        for (int i = 0; i < population.size(); i += CLUSTER_SIZE) {
            int end = Math.min(i + CLUSTER_SIZE, population.size());
            clusters.add(new ArrayList<>(population.subList(i, end)));
        }
    }

    /**
     * Tính toán vector B (hệ số điều chỉnh khoảng cách)
     */
    private double[] calculateBVector(int dimensions) {
        double[] B = new double[dimensions];
        for (int i = 0; i < dimensions; i++) {
            B[i] = 2 * (1 - random.nextDouble()); // Ngẫu nhiên trong [0,2]
        }
        return B;
    }

    /**
     * Tính toán vector E (hệ số điều chỉnh bao vây)
     */
    private double[] calculateEVector(int dimensions, double a) {
        double[] E = new double[dimensions];
        double h = 2 * a * random.nextDouble() - a; // [-a, a]
        for (int i = 0; i < dimensions; i++) {
            E[i] = h * (2 * random.nextDouble() - 1); // Ngẫu nhiên trong [-h,h]
        }
        return E;
    }

    /**
     * Cập nhật vị trí của linh cẩu theo 3 pha của SHO
     */
    private void updateHyenaPosition(Hyena hyena, double a) {
        Solution currentSolution = hyena.getSolution();
        Solution bestSolution = bestHyena.getSolution();

        // Tạo giải pháp mới
        Solution newSolution = currentSolution.copy();
        Route[] routes = newSolution.getRoutes();

        // Số chiều (số tuyến đường)
        int dimensions = routes.length;

        // Tính toán vector B và E
        double[] B = calculateBVector(dimensions);
        double[] E = calculateEVector(dimensions, a);

        // Cập nhật từng tuyến đường (từng chiều)
        for (int i = 0; i < dimensions; i++) {
            // Pha 1: Tìm kiếm (dựa vào vector B)
            if (B[i] > 1) {
                // Khám phá: thực hiện biến đổi ngẫu nhiên
                // Áp dụng các toán tử đa tuyến với xác suất 10%
                if (random.nextDouble() < 0.5) {
                    applyRandomMultiRouteOperation(routes);

                    // Kiểm tra tính khả thi sau khi áp dụng toán tử đa tuyến
                    for (int j = 0; j < dimensions; j++) {
                        if (!checkConditionUtil.isInsertionFeasible(routes[j], locations,
                                routes[j].getMaxPayload())) {
                            routes[j] = currentSolution.getRoutes()[j].copy();
                        }
                    }
                } else {
                    applyRandomOperation(routes[i]);
                }
            }
            // Pha 2 & 3: Bao vây và tấn công (dựa vào vector E)
            else {
                // Tính toán khoảng cách đến giải pháp tốt nhất
                double D = calculateRouteDistance(routes[i], bestSolution.getRoutes()[i]);

                // Cập nhật vị trí theo công thức SHO
                if (Math.abs(E[i]) < 1) {
                    // Khai thác: di chuyển về phía giải pháp tốt nhất
                    learnFromBestRoute(routes[i], bestSolution.getRoutes()[i], D, E[i]);
                }
            }

            // Kiểm tra tính khả thi
            if (!checkConditionUtil.isInsertionFeasible(routes[i], locations,
                    routes[i].getMaxPayload())) {
                routes[i] = currentSolution.getRoutes()[i].copy();
            }
        }

        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(routes, locations);
        newSolution.setFitness(newFitness);

        // Cập nhật nếu tốt hơn
        if (newFitness < hyena.getFitness()) {
            hyena.setSolution(newSolution);
            hyena.setFitness(newFitness);

            // Cập nhật linh cẩu tốt nhất
            if (newFitness < bestHyena.getFitness()) {
                bestHyena = new Hyena(newSolution.copy(), newFitness);
                System.out.println("New best solution found with fitness: " + newFitness);
            }
        }
    }

    /**
     * Tính toán khoảng cách giữa hai tuyến đường
     * Sử dụng số lượng điểm khác nhau và độ tương đồng về thứ tự
     */
    private double calculateRouteDistance(Route route1, Route route2) {
        int[] way1 = route1.getIndLocations();
        int[] way2 = route2.getIndLocations();

        if (way1.length == 0 && way2.length == 0) {
            return 0.0;
        }

        // Tính toán số điểm chung
        int commonPoints = 0;
        for (int loc1 : way1) {
            for (int loc2 : way2) {
                if (loc1 == loc2) {
                    commonPoints++;
                    break;
                }
            }
        }

        // Tính toán độ tương đồng về thứ tự (cho các điểm chung)
        double orderSimilarity = 0.0;
        int minLength = Math.min(way1.length, way2.length);
        if (minLength > 0) {
            int samePositions = 0;
            for (int i = 0; i < minLength; i++) {
                if (way1[i] == way2[i]) {
                    samePositions++;
                }
            }
            orderSimilarity = (double) samePositions / minLength;
        }

        // Kết hợp các yếu tố để tính khoảng cách
        int maxLength = Math.max(way1.length, way2.length);
        double structuralDistance = (maxLength - commonPoints) / (double) maxLength;
        double orderDistance = 1.0 - orderSimilarity;

        // Trọng số: 60% cấu trúc, 40% thứ tự
        return 0.6 * structuralDistance + 0.4 * orderDistance;
    }

    /**
     * Học hỏi từ tuyến đường tốt nhất với công thức SHO
     * Thực hiện swap và reorder các điểm dựa trên best route
     */
    private void learnFromBestRoute(Route targetRoute, Route bestRoute, double D, double E) {
        Route tempRoute = targetRoute.copy();
        int[] targetWay = targetRoute.getIndLocations();
        int[] bestWay = bestRoute.getIndLocations();

        if (targetWay.length <= 1 || bestWay.length <= 1) {
            return;
        }

        // Tính toán số lượng thay đổi dựa trên công thức SHO
        double intensity = Math.abs(E * D);
        int numSwaps = Math.max(1, (int) Math.round(intensity * targetWay.length / 2));
        numSwaps = Math.min(numSwaps, targetWay.length / 2);

        // Thực hiện swap các điểm để học hỏi từ best route
        for (int swap = 0; swap < numSwaps; swap++) {
            // Chọn ngẫu nhiên một điểm từ best route
            int bestIndex = random.nextInt(bestWay.length);
            int bestLocationId = bestWay[bestIndex];

            // Tìm điểm này trong target route
            int targetIndex = -1;
            for (int i = 0; i < targetWay.length; i++) {
                if (targetWay[i] == bestLocationId) {
                    targetIndex = i;
                    break;
                }
            }

            // Nếu tìm thấy, di chuyển nó về vị trí tương tự như trong best route
            if (targetIndex != -1) {
                // Tính vị trí mục tiêu dựa trên tỷ lệ vị trí trong best route
                double relativePos = (double) bestIndex / bestWay.length;
                int newTargetIndex = (int) Math.round(relativePos * (targetWay.length - 1));
                newTargetIndex = Math.max(0, Math.min(newTargetIndex, targetWay.length - 1));

                // Thực hiện swap nếu vị trí khác nhau
                if (targetIndex != newTargetIndex) {
                    int temp = targetWay[targetIndex];
                    targetWay[targetIndex] = targetWay[newTargetIndex];
                    targetWay[newTargetIndex] = temp;
                }
            }
        }

        // Kiểm tra ràng buộc
        targetRoute.setIndLocations(targetWay);
        if (!checkConditionUtil.isInsertionFeasible(targetRoute, locations, targetRoute.getMaxPayload())) {
            targetRoute.setIndLocations(tempRoute.getIndLocations());
        }
    }

    /**
     * Chạy thuật toán SHO cải tiến
     */
    @Override
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
            CheckConditionUtil checkConditionUtil, Location[] locations) {
        // Thiết lập các tham số từ lớp cha
        setupParameters(fitnessUtil, checkConditionUtil, locations);

        // Khởi tạo quần thể
        initialize(initialSolutions);

        // Vòng lặp chính
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Hệ số a giảm tuyến tính từ 5 về 0
            double a = 5 * (1 - (double) iteration / MAX_ITERATIONS);

            // Cập nhật vị trí từng linh cẩu
            for (Hyena hyena : population) {
                updateHyenaPosition(hyena, a);
            }

            // Cập nhật cụm định kỳ
            if (iteration % 10 == 0) {
                formClusters();
            }
        }

        return bestHyena.getSolution();
    }
}