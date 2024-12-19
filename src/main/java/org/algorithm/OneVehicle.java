package org.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class OneVehicle {
    private Vehicle vehicle;

    public void optimize() {
        ArrayList<Location> target = vehicle.getWay();
        double maxLoad = vehicle.getMaxLoad();
        int sizePopulations = 100;
        int Max_Iteration = 100;
        final Random rd = new Random();

        // Sử dụng SA tạo các giá trị
        ArrayList<ArrayList<Location>> populations = sa();

        // Các giá trị cơ bản
        int i = 0;
        double h = h(i, Max_Iteration);
        double B = B(rd.nextDouble());
        double E = E(h, rd.nextDouble());
        int N = sizePopulations;
        List<Double> fitnesses = populations.parallelStream()
                .map(OneVehicle::fitness).toList();
        ArrayList<Location> prey = populations.get(fitnesses.indexOf(fitnesses.stream().max(Double::compareTo)));

        i = 1;
        while (i < Max_Iteration) {

            ++i;
        }
    }

    public static double fitness(ArrayList<Location> locations) {
        double fitness = 0; // Kết quả điểm
        double currentTime = 0; // Đánh giá cho thời gian
        double currentLoad = 0; // Đánh giá cho trọng tải
        final int bonus = 100; // Giá trị giả định nếu có thưởng / phạt

        // Triển khai tính điểm
        for (int i = 0; i < locations.size() - 1; i++) {
            Location currentLoc = locations.get(i);
            Location nextLoc = locations.get(i + 1);

            // Giả định khoảng cách 2 điểm chính là thời gian di chuyển
            double tempTime = currentLoc.calculatorDistance(nextLoc);

            // Cập nhật thời gian
            currentTime += tempTime;
            if (currentTime < currentLoc.getLTW()) {
                // Đến trước thời gian cần giao -> Thưởng
                fitness += bonus;
            }
            if (currentTime > currentLoc.getUTW()) {
                // Đến trễ hơn thời gian cần giao -> Phạt
                fitness -= bonus;
            }

            // Cập nhật tải trọng
            currentLoad += currentLoc.getLoadPickup();
            if (currentLoad < 0 || currentLoad > vehicle.getMaxLoad()) {
                // Số hàng phải thả nhiều hơn số hàng hiện có -> Phạt
                // Số hàng phải lấy lấy hơn giá trị chứa -> Phạt
                fitness -= bonus;
            }

            fitness += currentTime;
        }
        return fitness;
    }

    private double E(double h, double rd2) {
        return 2 * h * rd2 - h;
    }

    public double B(double rd1) {
        return 2 * rd1;
    }

    public int h(int i, int max) {
        return 5 - (i * (5 / max));
    }

    public ArrayList<ArrayList<Location>> sa() {
        return null;
    }
}
