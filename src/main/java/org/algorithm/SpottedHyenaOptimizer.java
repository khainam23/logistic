package org.algorithm;

import java.util.ArrayList;
import java.util.List;

public class SpottedHyenaOptimizer extends AAlgorithm {
    @Override
    public List<Vehicle> optimize(List<List<Vehicle>> populations, Object... args) {
        List<List<Vehicle>> backupPopulations = new ArrayList<>(populations);
        // Step 1: Khởi tạo các tham số
        double h = (double) args[0];
        double B = (double) args[1];
        double E = (double) args[2];
        int N = (int) args[3];

        // Step 2: Tính toán giá trị tối ưu của các lời giải
        List<Double> fitnesses = populations.parallelStream()
                .map(this::calculateFitness)
                .toList();
        int indPrey = fitnesses.indexOf(fitnesses.stream().min(Double::compare).get());
        List<Vehicle> prey = populations.get(indPrey);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            for (List<Vehicle> solution : populations) {
                // Sử dụng thuật toán hoán đổi dựa trên B và E
                solution = swapOperation(solution, prey, B, E);
            }
        }

        return prey;
    }

    public List<Vehicle> swapOperation(List<Vehicle> heyna, List<Vehicle> prey, double B, double E) {
        int indVehicleHeyna = rand.nextInt(heyna.size());
        while(!prey.contains(heyna.get(indVehicleHeyna))) {
            indVehicleHeyna = rand.nextInt(heyna.size());
        }
        Vehicle vehicle = heyna.get(indVehicleHeyna);
        List<Location> way = vehicle.getWay();
        // Số lần thực hiện swap
        for (int i = 0; i < (int) Math.ceil(B); i++) {
            if(E > 1) {
                // Swap random
                int ind1 = rand.nextInt(way.size());
                int ind2 = rand.nextInt(way.size());
                while (ind1 == ind2)
                    ind2 = rand.nextInt(way.size());
                Location temp = way.get(ind1);
                way.set(ind1, way.get(ind2));
                way.set(ind2, temp);
            } else {
                // Swap theo sự giống nhau của prey
                List<Location> preyWay = prey.get(prey.indexOf(vehicle)).getWay();

            }
        }
        return heyna;
    }

}
