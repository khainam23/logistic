package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Hyena;
import org.model.Location;
import org.model.Pair;
import org.model.Route;
import org.utils.CheckConstraint;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends Algorithm {
    final int POPULATION_SIZE = 20;
    int indBestHyena;
    List<Hyena> population;
    Set<Double> bestScoreHyena;

    public SpottedHyenaOptimizer(List<Location> locations, List<Route> solution) {
        super(locations, solution);
    }

    @Override
    public Result optimizer() {
        sho();
        Hyena bestHyena = population.get(indBestHyena);
        System.out.println("Best scores: " + bestScoreHyena);
        return new Result("Spotted Hyena Optimizer", bestHyena.getSolution(), bestHyena.getFitness(), firstTimeSolution);
    }

    /**
     * Hàm khởi chạy của thuật toán
     */
    private void sho() {
        // Khởi tạo quần thể
        population = new ArrayList<>();
        initialPopulation();
        bestScoreHyena = new HashSet<>(); // Ghi nhận các giá trị tốt
        List<Double> fitness;

        // Lựa chọn các giá trị khởi tạo
        double h = 5;
        double B = 2;
        double E = 2;
        int N = 2;

        // Giới hạn số lần tìm kiếm
        for (int i = 0; i < MAX_ITERATOR; i++) {
            // Tính giá trị fitness
            fitness = calculatorFitness();

            // Tìm nhóm tối ưu
            List<Hyena> cluster = findCluster(fitness, N);

            // Cập nhật vị trí
            for (int j = 0; j < POPULATION_SIZE; j++) {
                updatePosition(cluster, population.get(j), E, B);
            }

            // Kiểm tra điều kiện

            // Cập nhật tham số
            h = 5 - (i * (5 / MAX_ITERATOR));
            B = 2 * rd.nextDouble();
            E = 2 * h * rd.nextDouble() - h;
            N = noh(fitness);
        }
    }

    /**
     * Thực hiện tính fitness của từng hyena và cập nhật vị trí tối ưu tốt nhất hiện có
     *
     * @return
     */
    private List<Double> calculatorFitness() {
        List<Double> fitness = new ArrayList<>();
        double bestFitness = Double.MAX_VALUE;
        for (int i = 0; i < POPULATION_SIZE; ++i) {
            Hyena hyena = population.get(i);
            double fitnessOfHyena = fitness(hyena.getSolution());
            if (fitnessOfHyena < bestFitness) {
                indBestHyena = i;
                bestFitness = fitnessOfHyena;
            }
            fitness.add(fitnessOfHyena);
        }
        bestScoreHyena.add(bestFitness);
        return fitness;
    }

    /**
     * Thực hiện cập nhật các cá thể
     *
     * @param cluster
     * @param hyena
     * @param E
     * @param B
     */
    private void updatePosition(List<Hyena> cluster, Hyena hyena, double E, double B) {
        List<Route> routes = hyena.getSolution();
        if (E > 1) {
            // Tình huống cần tìm giải pháp mới
            if (B < 1) {
                int indR1 = rd.nextInt(routes.size());
                int indR2 = rd.nextInt(routes.size());
                while (indR1 == indR2)
                    indR2 = rd.nextInt(routes.size());
                swapOperator(routes.get(indR1), routes.get(indR2));
            }
        } else {
            // Thay đổi giữa trên giải pháp tốt hiện biết
            if (B > 1) {
                int indRdGoodHyena = rd.nextInt(cluster.size()); // Lấy ngẫu nhiên hyena tốt
                List<Route> goodSolution = cluster.get(indRdGoodHyena).getSolution();
                int indRdGoodRoute = rd.nextInt(goodSolution.size()); // Lấy ngẫu nhiên giải pháp tốt hyena
                int indRdNormalRoute = rd.nextInt(routes.size()); // Lấy ngẫu nhiên giải pháp thông thường
                Route goodRoute = goodSolution.get(indRdGoodRoute);
                Route normalRoute = routes.get(indRdNormalRoute);
                swapOperator(goodRoute, normalRoute);
            }
        }
    }

    /**
     * Thực hiện swap theo các giải pháp tốt nhất
     */
    private void swapOperator(Route r1, Route r2) {
        // Lấy cặp giải pháp trong good solution
        List<Pair<Integer, Location>> listGoodInd = getRdPD(r1);
        int i1 = listGoodInd.get(0).getKey();
        int i2 = listGoodInd.get(1).getKey();
        if (r2.contains(listGoodInd.get(0)) &&
                r2.contains(listGoodInd.get(1))) {
            // Đã tồn tại cặp này trong giải pháp
            // Thực hiện 2 opt trên cặp này
            twoOpt(r2, i1, i2);
        } else {
            // Thêm vào
            if (CheckConstraint.getInstance()
                    .isInsertionFeasible(vehicle, listGoodInd.get(0).getValue(), listGoodInd.get(1).getValue(), i1, i2)
            ) {
                // Thêm vào được trên tuyến đường hiện tại
                r2.set(i1, listGoodInd.get(0));
                r2.set(i2, listGoodInd.get(1));
            }

            // Nếu không xem như hyena này đã đi quá xa và không cần quan tâm đến
        }
    }

    private void twoOpt(Route route, int i1, int i2) {
        if (CheckConstraint.getInstance().isInsertionFeasible(
                vehicle, route.get(i2).getValue(), route.get(i1).getValue(), i2, i1
        )) {
            // Nếu có thể đảo
            Pair<Integer, Location> tempPair = route.get(i1);
            route.set(i1, route.get(i2));
            route.set(i2, tempPair);
        }
        // Không thì không can thiệp vào giải pháp của hyena
    }

    /**
     * Đếm các giá trị được cho là tối ưu
     *
     * @param fitness
     * @return
     */
    public int noh(List<Double> fitness) {
        double M = rd.nextDouble(0.5, 1) + fitness.get(indBestHyena);
        int count = 0;
        for (Double Ph : fitness)
            if (Ph < M)
                count++;
        return count;
    }

    /**
     * Tìm nhóm tối ưu
     *
     * @param fitness
     * @param N
     * @return
     */
    private List<Hyena> findCluster(List<Double> fitness, int N) {
        List<Hyena> cluster = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Double> sortedFitness = new ArrayList<>(fitness);
        sortedFitness.sort(Comparator.naturalOrder()); // Sắp xếp tăng dần

        // Ghi lại vị trí trong population
        for (int i = 0; i < sortedFitness.size(); i++) {
            indices.add(fitness.indexOf(sortedFitness.get(i)));
        }

        // Lấy các giá trị được xem là nhóm tối ưu
        for (int i = 0; i < N; i++) {
            cluster.add(population.get(indices.get(i)));
        }
        return cluster;
    }

    private void initialPopulation() {
        //Random Initialization
        List<Hyena> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Route> solutionOfHyena = (List<Route>) cloneList(solution);
            int ind = rd.nextInt(solutionOfHyena.size());
            Collections.shuffle(solutionOfHyena.get(ind).getIndLoc());
            population.add(new Hyena(solutionOfHyena, fitness(solutionOfHyena)));
        }
        this.population = population;
    }

    private List<?> cloneList(List<?> solution) {
        return new ArrayList<>(solution);
    }

    /**
     * Spotted Hyena Optimizer
     * Bài toán lấy ý tưởng từ việc săn mồi của đàn linh cẩu.
     */
}
