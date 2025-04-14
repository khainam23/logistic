package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.WriteLogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer {
    // Các tham số của thuật toán
    static final int MAX_ITERATIONS = 100;
    static final int CLUSTER_SIZE = 5; // Kích thước cụm linh cẩu
    static final double A_DECREASE_FACTOR = 0.01; // Tốc độ giảm của hệ số A

    final Random random;
    final WriteLogUtil writeLogUtil;

    // Danh sách các linh cẩu
    List<Hyena> population;
    List<List<Hyena>> clusters; // Các cụm linh cẩu
    Hyena bestHyena; // Linh cẩu có fitness tốt nhất

    // Các tham số khác
    Location[] locations;
    FitnessUtil fitnessUtil;
    CheckConditionUtil checkConditionUtil;
    int currentTarget;

    public SpottedHyenaOptimizer(WriteLogUtil writeLogUtil) {
        this.random = new Random();
        this.writeLogUtil = writeLogUtil;
        this.writeLogUtil.setLogFilePath(WriteLogUtil.PathLog.SHO.getPath());
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

        writeLogUtil.info("Initialized population with " + population.size() + " hyenas");
        writeLogUtil.info("Best initial hyena fitness: " + bestHyena.getFitness());
    }

    /**
     * Phân cụm quần thể linh cẩu
     */
    private void formClusters() {
        clusters.clear();

        // Sắp xếp quần thể theo fitness
        population.sort((h1, h2) -> Double.compare(h1.getFitness(), h2.getFitness()));

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

        // Số chiều (số tuyến đường)
        int dimensions = newSolution.getRoutes().length;

        // Tính toán vector B và E
        double[] B = calculateBVector(dimensions);
        double[] E = calculateEVector(dimensions, a);

        // Cập nhật từng tuyến đường (từng chiều)
        for (int i = 0; i < dimensions; i++) {
            // Pha 1: Tìm kiếm (dựa vào vector B)
            if (Math.abs(B[i]) > 1) {
                // Khám phá: thực hiện biến đổi ngẫu nhiên
                applyRandomOperation(newSolution.getRoutes()[i]);
            }
            // Pha 2 & 3: Bao vây và tấn công (dựa vào vector E)
            else {
                // Tính toán khoảng cách đến giải pháp tốt nhất
                double D = calculateRouteDistance(newSolution.getRoutes()[i], bestSolution.getRoutes()[i]);

                // Cập nhật vị trí theo công thức SHO
                if (Math.abs(E[i]) < 1) {
                    // Khai thác: di chuyển về phía giải pháp tốt nhất
                    learnFromBestRoute(newSolution.getRoutes()[i], bestSolution.getRoutes()[i], D, E[i]);
                }
            }

            // Kiểm tra tính khả thi
            if (!checkConditionUtil.isInsertionFeasible(newSolution.getRoutes()[i], locations,
                    newSolution.getRoutes()[i].getMaxPayload(), currentTarget)) {
                newSolution.getRoutes()[i] = currentSolution.getRoutes()[i].copy();
            }
        }

        // Tính toán fitness mới
        double newFitness = fitnessUtil.calculatorFitness(newSolution.getRoutes(), locations);
        newSolution.setFitness(newFitness);

        // Cập nhật nếu tốt hơn
        if (newFitness < hyena.getFitness()) {
            hyena.setSolution(newSolution);
            hyena.setFitness(newFitness);

            // Cập nhật linh cẩu tốt nhất
            if (newFitness < bestHyena.getFitness()) {
                bestHyena = new Hyena(newSolution.copy(), newFitness);
                writeLogUtil.info("New best solution found with fitness: " + newFitness);
            }
        }
    }

    /**
     * Tính toán khoảng cách giữa hai tuyến đường
     */
    private double calculateRouteDistance(org.logistic.model.Route route1, org.logistic.model.Route route2) {
        int[] way1 = route1.getIndLocations();
        int[] way2 = route2.getIndLocations();
        int minLength = Math.min(way1.length, way2.length);

        double distance = 0;
        for (int i = 0; i < minLength; i++) {
            distance += Math.abs(way1[i] - way2[i]);
        }
        return distance / minLength;
    }

    /**
     * Học hỏi từ tuyến đường tốt nhất với công thức SHO
     */
    private void learnFromBestRoute(org.logistic.model.Route targetRoute,
                                    org.logistic.model.Route bestRoute,
                                    double D, double E) {
        int[] targetWay = targetRoute.getIndLocations();
        int[] bestWay = bestRoute.getIndLocations();
        int minLength = Math.min(targetWay.length, bestWay.length);

        for (int i = 0; i < minLength; i++) {
            // Công thức SHO: newPosition = bestPosition - E * D
            double newPos = bestWay[i] - E * D;

            // Làm tròn và điều chỉnh nếu cần
            int adjustedPos = (int) Math.round(newPos);
            if (adjustedPos >= 0 && adjustedPos < targetWay.length) {
                targetWay[i] = adjustedPos;
            }
        }
    }

    /**
     * Áp dụng toán tử ngẫu nhiên cho khám phá
     */
    private void applyRandomOperation(org.logistic.model.Route route) {
        int operator = random.nextInt(3);
        switch (operator) {
            case 0 -> applySwapOperator(route);
            case 1 -> applyInsertOperator(route);
            case 2 -> applyReverseOperator(route);
        }
    }

    private void applySwapOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);

        // Đảm bảo pos1 khác pos2
        while (pos1 == pos2) {
            pos2 = random.nextInt(way.length);
        }

        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;

    }

    // Toán tử Insert: di chuyển một điểm đến vị trí mới trong tuyến đường
    private void applyInsertOperator(Route route) {
        // Chọn một điểm để di chuyển (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos = random.nextInt(way.length);

        // Chọn vị trí mới để chèn điểm (không bao gồm depot ở đầu và cuối)
        int insertPos = random.nextInt(way.length);
        int posVal = way[Math.max(insertPos, pos)];

        for (int i = Math.min(insertPos, pos); i <= Math.max(insertPos, pos); i++) {
            int tempVal = way[i];
            way[i] = posVal;
            posVal = tempVal;
        }
    }

    // Toán tử Reverse: đảo ngược thứ tự của một đoạn trong tuyến đường
    private void applyReverseOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên (không bao gồm depot ở đầu và cuối)
        int[] way = route.getIndLocations();
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);

        // Đảm bảo pos1 < pos2
        if (pos1 > pos2) {
            int temp = pos1;
            pos1 = pos2;
            pos2 = temp;
        }

        // Đảo ngược đoạn từ pos1 đến pos2
        while (pos1 < pos2) {
            int temp = way[pos1];
            way[pos1] = way[pos2];
            way[pos2] = temp;
            pos1++;
            pos2--;
        }

    }

    /**
     * Chạy thuật toán SHO cải tiến
     */
    public Solution run(Solution[] initialSolutions, FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, Location[] locations,
                        int currentTarget) {
        this.fitnessUtil = fitnessUtil;
        this.checkConditionUtil = checkConditionUtil;
        this.locations = locations;
        this.currentTarget = currentTarget;

        writeLogUtil.info("Starting Improved Spotted Hyena Optimizer");
        writeLogUtil.info("Max iterations: " + MAX_ITERATIONS);

        // Khởi tạo quần thể
        initialize(initialSolutions);

        // Vòng lặp chính
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Hệ số a giảm tuyến tính từ 2 về 0
            double a = 2 * (1 - (double) iteration / MAX_ITERATIONS);

            // Cập nhật vị trí từng linh cẩu
            for (Hyena hyena : population) {
                updateHyenaPosition(hyena, a);
            }

            // Cập nhật cụm định kỳ
            if (iteration % 10 == 0) {
                formClusters();
                diversifyClusters();
            }

            writeLogUtil.info("Iteration " + iteration + ", Best fitness: " + bestHyena.getFitness());
        }

        writeLogUtil.info("Improved SHO completed");
        writeLogUtil.info("Best solution fitness: " + bestHyena.getFitness());

        return bestHyena.getSolution();
    }

    /**
     * Đa dạng hóa các cụm
     */
    private void diversifyClusters() {
        for (List<Hyena> cluster : clusters) {
            // Chọn linh cẩu tốt nhất trong cụm
            Hyena bestInCluster = cluster.stream()
                    .min((h1, h2) -> Double.compare(h1.getFitness(), h2.getFitness()))
                    .orElse(null);

            if (bestInCluster != null) {
                // Tạo các linh cẩu mới từ linh cẩu tốt nhất cụm
                for (Hyena hyena : cluster) {
                    if (hyena != bestInCluster) {
                        Solution newSolution = createDiversifiedSolution(bestInCluster.getSolution());
                        double newFitness = fitnessUtil.calculatorFitness(newSolution.getRoutes(), locations);
                        hyena.setSolution(newSolution);
                        hyena.setFitness(newFitness);
                    }
                }
            }
        }
    }

    /**
     * Tạo giải pháp đa dạng hóa từ giải pháp gốc
     */
    private Solution createDiversifiedSolution(Solution original) {
        Solution newSolution = original.copy();

        // Áp dụng nhiều toán tử biến đổi
        for (org.logistic.model.Route route : newSolution.getRoutes()) {
            int operations = 1 + random.nextInt(3);
            for (int i = 0; i < operations; i++) {
                applyRandomOperation(route);
            }
        }

        return newSolution;
    }
}