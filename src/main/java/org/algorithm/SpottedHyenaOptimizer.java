package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Hyena;
import org.model.Location;
import org.model.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends Algorithm {
    final int POPULATION_SIZE = 20;
    int indBestHyena;
    List<Hyena> population;
    List<Double> bestScoreHyena;

    public SpottedHyenaOptimizer(List<Location> locations, List<Route> solution) {
        super(locations, solution);
    }

    @Override
    public Result optimizer() {
        sho();
        Hyena bestHyena = population.get(indBestHyena);
        return new Result("Spotted Hyena Optimizer", bestHyena.getSolution(), bestHyena.getFitness(), firstTimeSolution);
    }

    /**
     * Hàm khởi chạy của thuật toán
     */
    private void sho() {
        // Khởi tạo quần thể
        population = new ArrayList<>();
        initialPopulation();
        bestScoreHyena = new ArrayList<>(); // Ghi nhận các giá trị tốt
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
        if (E > 1) {
            // Tình huống cần tìm giải pháp mới
            if (B < 1) {
                swapRoutev1(hyena.getSolution());
            }
        } else {
            // Thay đổi giữa trên giải pháp tốt hiện biết
            if (B > 1) {
                swapRoutev2(cluster, hyena.getSolution());
            }
        }
    }

    /**
     * Thực hiện swap theo các giải pháp tốt nhất
     *
     * @param routes
     */
    private void swapRoutev2(List<Hyena> cluster, List<Route> routes) {
        int i = rd.nextInt(routes.size());
        Route r1 = routes.get(i);
        Route r2 = cluster.get(indBestHyena % cluster.size()).getSolution().get(i);
        int minSize = Math.min(r1.size(), r2.size());
        int sInd = rd.nextInt(minSize);
        int indR1 = r1.indexOf(r2.get(sInd));
        if (indR1 != -1) {
            int ranInd = rd.nextInt(r1.size());
            int temp = r1.get(indR1);
            r1.set(indR1, r1.get(ranInd));
            r1.set(ranInd, temp);
        }
        // Nếu hyena hiện tại quá khác xa giải pháp tốt thì bỏ nó
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
     * Thực hiện swap tìm giải pháp mới
     *
     * @param routes
     */
    private void swapRoutev1(List<Route> routes) {
        int i1 = rd.nextInt(routes.size());
        int i2 = rd.nextInt(routes.size());
        while (i1 == i2) { // Đảm bảo không trùng
            i2 = rd.nextInt(routes.size());
        }
        Route r1 = routes.get(i1);
        Route r2 = routes.get(i2);
        int randInd = rd.nextInt(rd.nextDouble() > 0.5 ? r1.size() : r2.size());
        if (r1.get(randInd) == null) {
            // Tình huống Hyena 2 có giải pháp dài hơn
            r1.add(r2.remove(randInd));
        } else if (r2.get(randInd) == null) {
            // Tình huống Hyena 1 có giải pháp dài hơn
            r2.add(r1.remove(randInd));
        } else {
            // Tình huống 2 Hyena có giải pháp bằng nhau 
            Integer temp = r1.get(randInd);
            r1.set(randInd, r2.get(randInd));
            r2.set(randInd, temp);
        }
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

        // Lấy cấc giá trị được xem là nhóm tối ưu
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
