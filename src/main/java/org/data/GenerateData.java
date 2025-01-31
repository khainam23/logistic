package org.data;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.model.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenerateData {
    final int MAX_ITERATOR = 100; // Ràng buộc tìm kiếm
    List<Location> locations;

    /**
     * @param n     - Số lượng điểm location
     * @param width - Khung thời gian
     */
    public void generateLocations(int n, int width) {
        if (n < 5)
            throw new RuntimeException("[GenerateData > generateLocations()] Number of tasks must be greater than 4!");

        // Các tham số ràng buộc
        final int MIN_CORD = 1;
        final int MAX_CORD = 100;
        final int MIN_CAPACITY = 1;
        final int MAX_CAPACITY = 10;

        // Khởi tạo các biến sử dụng chính
        int capacity = getRandom(MIN_CAPACITY, MAX_CAPACITY);
        locations = new ArrayList<>();
        int[][] distances = new int[n][n];
        int[] successfulRoute = new int[n];

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

        //Average time of randomly generated route
        int averageTime = time / visits;

        //Create time windows which suite the route
        for (int i = 1; i < locations.size(); i++) {
            int w = getRandom(1, width) * averageTime * 10;
            locations.get(i).setUTW(arrivalTime[i] + w);
            locations.get(i).setLTW(arrivalTime[i] - w);
            if (locations.get(i).getLTW() < 0) {// Use a different formula if LTW goes below 0
                w = getRandom(1, arrivalTime[i]);
                locations.get(i).setLTW(arrivalTime[i] - w);
            }
        }
    }

    public static void main(String[] args) {
        GenerateData generateData = new GenerateData();
        generateData.generateLocations(10, 10);
        System.out.println(generateData.locations);
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
