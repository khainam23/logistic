package org.algorithm;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Random;

/**
 * Các bước thực hiện:
 * + Khởi tạo các điểm bằng cách xáo trộn điểm ban đầu (hiện đang sử dụng Swap Operator)
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SHO {
    final ArrayList<Location> locations;
    final int initialPopulation;
    final double[] lowerBounds;
    final double[] upperBounds;
    final int maxIterations;
    /**
     * B và E là 2 hằng số điều chỉnh trong qúa trình tối ưu.
     * + B: Quyết định không gian tìm kiếm (Nhỏ: gần; Lớn: Xa)
     * + E: Hệ số dừng
     * h : Mức độ gắn kết
     * N: Số lượng linh cẩu
     */
    final double h, B, E, N; // Hyperparameters of SHO
    final int maxCapacity;
    double bestFitness;
    ArrayList<Location> bestSolution;

    public ArrayList<Location> optimize() {
        // Step 1: Khởi tạo quần thể ban đầu
        ArrayList<ArrayList<Location>> population = initializePopulation();

        // Step 2: Khởi tạo tham số (đã có ở constructor)

        // Step 3: Tính giá trị cho tối trên từng lời giải tìm thấy
        ArrayList<Double> fitnessValues = calculateFitness(population);
        bestSolution = population.get(0); // Cá thể đầu có giá trị tốt nhất
        bestFitness = fitnessValues.get(0); // Cá thể đầu có giá trị tốt nhất

        for (int iter = 0; iter < maxIterations; iter++) {
            // Step 4: Explore the best search agent in the given search space
            updateBestSolution(population, fitnessValues);

            // Step 5: Define the group of optimal solutions (clustering)
            ArrayList<ArrayList<Location>> clusters = clusterSolutions(population);

            // Step 6: Update the positions of search agents
            updatePositions(population, clusters);

            // Step 7: Check boundaries
            adjustBoundaries(population);

            // Step 8: Calculate updated fitness values
            fitnessValues = calculateFitness(population);

            // Step 9: Update the best solution if found
            updateBestSolution(population, fitnessValues);

            // Step 10: Check stopping criterion
            if (stoppingCriterion(population)) break;
        }

        // Step 11: Return the best solution found
        return bestSolution;
    }

    private ArrayList<ArrayList<Location>> initializePopulation() {
        Random random = new Random();
        ArrayList<ArrayList<Location>> population = new ArrayList<>(); // Khởi tạo các vị trí

        for (int i = 0; i < initialPopulation; i++) {
            ArrayList<Location> route = new ArrayList<>(locations); // Vị trí được cho ban đầu
            int currentLoad = 0;
            int currentTime = 0;

            // Xáo trộn các điểm (Swap)
            java.util.Collections.shuffle(route, random);

            // Thiết lập các thông số
            for (Location loc : route) {
                if (currentTime < loc.getLTW()) {
                    currentTime = loc.getLTW(); // Chờ đến thời điểm có thể phục vụ
                }
                if (currentTime > loc.getUTW() || currentLoad + loc.getLoad() > maxCapacity) {
                    loc.setServiceable(false); // Đánh dấu không thể phục vụ
                } else {
                    currentLoad += loc.getLoad();
                    currentTime += Math.sqrt(loc.getX() * loc.getX() + loc.getY() * loc.getY()); // Giả sử thời gian di chuyển là khoảng cách euclid
                }
            }
            population.add(route);
        }
        return population;
    }

    // Tính giá trị tối ưu
    private ArrayList<Double> calculateFitness(ArrayList<ArrayList<Location>> population) {
        ArrayList<Double> fitnessValues = new ArrayList<>();
        for (ArrayList<Location> individual : population) {
            fitnessValues.add(evaluateFitness(individual));
        }
        return fitnessValues;
    }

    // Tính giá trị tối ưu trên lời giải
    private double evaluateFitness(ArrayList<Location> route) {
        double fitness = 0.0;
        double currentTime = 0;
        int currentLoad = 0;

        for (int i = 0; i < route.size() - 1; i++) {
            Location current = route.get(i);
            Location next = route.get(i + 1);

            // Tính khoảng cách Euclidean
            double distance = Math.sqrt(Math.pow(current.getX() - next.getX(), 2) + Math.pow(current.getY() - next.getY(), 2));
            fitness += distance;

            // Cập nhật thời gian và kiểm tra ràng buộc thời gian
            currentTime += distance;
            if (currentTime < next.getLTW()) {
                currentTime = next.getLTW(); // Chờ đến thời điểm có thể phục vụ
            }
            if (currentTime > next.getUTW()) {
                fitness += 1000; // Phạt nếu không đáp ứng được thời gian
            }

            // Cập nhật tải trọng và kiểm tra ràng buộc
            currentLoad += next.getLoad();
            if (currentLoad < 0 || currentLoad > maxCapacity) {
                fitness += 1000; // Phạt nếu vượt tải trọng
            }
        }

        return fitness;
    }


    private void updateBestSolution(ArrayList<ArrayList<Location>> population, ArrayList<Double> fitnessValues) {
        for (int i = 0; i < population.size(); i++) {
            if (fitnessValues.get(i) < bestFitness) {
                bestFitness = fitnessValues.get(i);
                bestSolution = population.get(i);
            }
        }
    }

    private ArrayList<ArrayList<Location>> clusterSolutions(ArrayList<ArrayList<Location>> population) {
        return new ArrayList<>(population);
    }

    private void updatePositions(ArrayList<ArrayList<Location>> population, ArrayList<ArrayList<Location>> clusters) {
        Random random = new Random();
        for (ArrayList<Location> individual : population) {
            for (Location loc : individual) {
                if (!loc.isServiceable()) continue; // Chỉ cập nhật địa điểm có thể phục vụ

                double r1 = random.nextDouble();
                double r2 = random.nextDouble();

                // Công thức cập nhật
                double exploration = E * r1 * (bestSolution.get(loc.getX()).getX() - loc.getX());
                double convergence = N * r2 * (loc.getX() - bestSolution.get(loc.getX()).getX());
                loc.setX((int) (loc.getX() + h * exploration + B * convergence));

                exploration = E * r1 * (bestSolution.get(loc.getY()).getY() - loc.getY());
                convergence = N * r2 * (loc.getY() - bestSolution.get(loc.getY()).getY());
                loc.setY((int) (loc.getY() + h * exploration + B * convergence));
            }
        }
    }

    private void adjustBoundaries(ArrayList<ArrayList<Location>> population) {
        for (ArrayList<Location> individual : population) {
            for (int i = 0; i < individual.size(); i++) {
                Location loc = individual.get(i);

                // Giới hạn tọa độ x
                loc.setX(Math.max((int) lowerBounds[0], Math.min(loc.getX(), (int) upperBounds[0])));

                // Giới hạn tọa độ y
                loc.setY(Math.max((int) lowerBounds[1], Math.min(loc.getY(), (int) upperBounds[1])));

                // Giới hạn thời gian LTW và UTW
                loc.setLTW(Math.max((int) lowerBounds[2], Math.min(loc.getLTW(), (int) upperBounds[2])));
                loc.setUTW(Math.max((int) lowerBounds[3], Math.min(loc.getUTW(), (int) upperBounds[3])));
            }
        }
    }

    private boolean stoppingCriterion(ArrayList<ArrayList<Location>> population) {
        for (ArrayList<Location> route : population) {
            for (Location loc : route) {
                if (!loc.isServiced() && loc.isServiceable()) {
                    return false;
                }
            }
        }
        return true;
    }
}
