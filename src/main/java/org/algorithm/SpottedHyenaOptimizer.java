package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Hyena;
import org.model.Route;
import org.util.ChangeSolution;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends Algorithm {
    final int SIZE_POPULATION = 100;

    @Override
    public Result optimizer() {
        return null;
    }

    public Hyena sho() {
        // Step 1: Khởi tạo ban đầu
        List<Hyena> population = initializePopulation();

        // Step 2: Lựa chọn các giá trị cho thuật toán
            // Vòng lặp tối đa đã được định nghĩa ở class Algorithm
        double h = 5;
        double B = 2;
        double E = 1;
        int N = 2; // Ban đầu cứ lựa giả định

        // Step 3: Tính giá trị fitness trên mỗi hyena
        // (đã được tính trong quá trình khởi tạo giải pháp)
        double[] fitness = new double[SIZE_POPULATION];
        for (int i = 0; i < population.size(); i++) {
            fitness[i] = population.get(i).getFitness();
        }

        // Step 4: Hyena tốt nhất hiện tại - dựa vào fitness
        double bestScoreHyena = fitness[0];
        int indBestHyena = 0;
        for (int i = 1; i < fitness.length; i++) {
            if(bestScoreHyena >= fitness[i]) {
                indBestHyena = i;
                bestScoreHyena = fitness[i];
            }
        }

        // Spotted Hyena Optimizer
        for (int i = 0; i < MAX_ITERATOR; i++) {
            // Step 5: Xác định nhóm cá thể tối ưu
            List<Hyena> clusterHyena = clusterHyenas(N);


            // Step 6: Cập nhật vị trí của các hyena
            updatePosition();

            // Step 7: Kiểm soát giới hạn
            checkBoundary();

            // Step 8: Tính lại fitness và cập nhật best heyna nếu có
            for (int j = 0; j < fitness.length; j++) {
                fitness[j] = fitness(population.get(j).getRoutes());
                if(bestScoreHyena >= fitness[j]) {
                    bestScoreHyena = fitness[j];
                    indBestHyena = j;
                }
            }

            // Step 9: Cập nhật nhóm các cá thể tối ưu

            // Step 10: Kiểm tra các điều kiện khác
        }

        // Step 11: Return về best hyena tìm được
        return population.get(indBestHyena);
    }

    /**
     * Khởi tạo các giải pháp (hyena)
     */
    private List<Hyena> initializePopulation() {
        List<Hyena> hyenas = new ArrayList<>();
        for (int i = 0; i < SIZE_POPULATION; i++) {
            List<Route> newSolution = ChangeSolution.swapOperation(solution);
            Hyena hyena = new Hyena(newSolution, fitness(newSolution));
            hyenas.add(hyena);
        }
        return hyenas;
    }

    /**
     * Đếm các hyena vượt qua điều kiện test.
     */
    private int noh(List<Double> bestHyenaFitness) {
        double min = 0.5;
        double max = 1.0;
        double M = min + (max - min) * rd.nextDouble();
        M += bestHyenaFitness.get(0);

        int count = 0;
        for (int i = 0; i < bestHyenaFitness.size(); i++) {
            if(M >= bestHyenaFitness.get(0))
                ++count;
        }
        return count;
    }

    /**
     * Tìm nhóm tối ưu. Sử dụng Eqs (8) và (9)
     * @return
     */
    private List<Hyena> clusterHyenas(int N) {
        return null;
    }

    private void updatePosition() {

    }

    private void checkBoundary() {

    }
}
