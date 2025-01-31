package org.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.model.Location;
import org.model.Vehicle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class GenerateData {
    List<Location> locations;
    int[][] distances;
    int capacity;
    List<List<Vehicle>> solutions;

    /**
     * @param n     - Số lượng điểm location
     * @param width - Khung thời gian
     */
    public void generateLocations(int n, int width) {
        if (n < 5)
            throw new RuntimeException("[GenerateData > generateLocations()] Number of tasks must be greater than 4!");

        long startTime = System.currentTimeMillis();

        // Các tham số ràng buộc
        final int MIN_CORD = 1;
        final int MAX_CORD = 100;
        final int MIN_CAPACITY = 10;
        final int MAX_CAPACITY = 100;

        // Khởi tạo các biến sử dụng chính
        capacity = getRandom(MIN_CAPACITY, MAX_CAPACITY);
        locations = new ArrayList<>();
        distances = new int[n][n];
        int[] successfulRoute = new int[n];
        solutions = new ArrayList<>();

        // Add n tasks
        Location depot = new Location(0, new Point(0, 0));
        locations.add(depot);
        for (int i = 1; i < n; i++) {
            // Generate location
            int x = getRandom(MIN_CORD, MAX_CORD);
            int y = getRandom(MIN_CORD, MAX_CORD);

            // Generate load and service time
            int demandPick = getRandom(MIN_CAPACITY, capacity);
            int demandDeliver = getRandom(MIN_CAPACITY, capacity);
            int serviceTime = (demandPick + demandDeliver) * 60;

            Location location = new Location(i, new Point(x, y), demandPick, demandDeliver, serviceTime);
            locations.add(location);
        }

        // Tính khoảng cách
        for (int row = 0; row < distances.length; row++) {
            for (int col = 0; col < distances.length; col++) {
                distances[row][col] = (int) locations.get(row).distance(locations.get(col));
            }
        }

        // Khởi tạo các điểm ràng buộc
        int visits = 0;
        int time = 0;
        int[] arrivalTime = new int[locations.size()];
        int previousIndex = 0;
        int currentCapacity = 0;
        successfulRoute[0] = 0;
        while (visits < locations.size() - 1) {
            int index = getRandom(1, locations.size() - 1);//random location ignoring the depot
            if (!locations.get(index).isServiced()) {
                if (currentCapacity + locations.get(index).getLoad() <= capacity) {
                    currentCapacity += locations.get(index).getLoad();
                    locations.get(index).setServiced(true);
                    time += distances[previousIndex][index];
                    previousIndex = index;
                    arrivalTime[index] = time;
                    successfulRoute[visits + 1] = index;
                    visits++;
                }
            }
        }

        // Tính thời gian trung bình tìm kiếm ra giải pháp
        int averageTime = time / visits;

        // Tạo time phù hợp cho lộ trình
        for (int i = 1; i < locations.size(); i++) {
            int w = getRandom(1, width) * averageTime * 10;
            locations.get(i).setUTW(arrivalTime[i] + w);
            locations.get(i).setLTW(arrivalTime[i] - w);
            if (locations.get(i).getLTW() < 0) {// Use a different formula if LTW goes below 0
                w = getRandom(1, arrivalTime[i]);
                locations.get(i).setLTW(arrivalTime[i] - w);
            }
        }

        resetLocation();
        System.out.println("Finish generate locations: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void resetLocation() {
        locations.forEach(Location::reset);
    }

    public void printData() {
        System.out.println("Distances by Euclid:");
        int maxWidth = 0;
        for (int[] row : distances) {
            for (int num : row) {
                maxWidth = Math.max(maxWidth, String.valueOf(num).length());
            }
        }

        for (int[] row : distances) {
            for (int num : row) {
                System.out.printf("%" + (maxWidth + 2) + "d", num);
            }
            System.out.println();
        }
        System.out.println("-".repeat(maxWidth));
        System.out.println("Locations:");
        locations.forEach(Location::print);
    }

    public static void main(String[] args) {
        GenerateData generateData = new GenerateData();
        generateData.generateLocations(10, 10);
        generateData.printData();
    }

    // Lấy ngẫu nhiên giá trị trong một khoảng
    private int getRandom(int min, int max) {
        Random rn = new Random();
        return rn.nextInt((max - min) + 1) + min;
    }

    /**
     * Có khả năng tạo dữ liệu về bài toán VRPSPDTW giả
     */
}
