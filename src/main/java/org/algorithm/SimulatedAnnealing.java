package org.algorithm;

import org.data.Result;
import org.model.Route;

import java.util.List;

public class SimulatedAnnealing extends Algorithm {
    @Override
    public Result optimizer() {
        return null;
    }

    // Tối ưu giải pháp ban đầu bằng thuật toán Simulated annealing
    public List<Route> SA(List<Route> routes) {
        List<Route> bestSolution = routes;
        double oldCost = fitness(bestSolution);
        double T = 1.0;
        double Tmin = 0.00001;
        double alpha = 0.9;
        while (T > Tmin) {
            for (int i = 0; i < 100; i++) {
                List<Route> neighbor = neighborForSA(bestSolution);
                double newCost = fitness(neighbor);
                double ap = Math.exp((oldCost - newCost) / T);
                if (ap > rd.nextDouble()) {
                    bestSolution = neighbor;
                    oldCost = newCost;
                }
            }
            T *= alpha;
        }
        return bestSolution;
    }

    /**
     * Tìm ra một giải pháp gần với giải pháp tốt nhất hiện có
     *
     * @param bestSolution
     * @return
     */
    private List<Route> neighborForSA(List<Route> bestSolution) {
        return null;
    }
}
