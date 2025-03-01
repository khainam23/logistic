package org.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.model.Location;
import org.model.Pair;
import org.model.Route;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class GenerateData {
    List<Location> locations;
    int[][] distances;
    int capacity;
    int[] successfulRoute; // 1 giải pháp của dạng TSP
    final Random rd = new Random();

    // Dùng để tạo ra một lời giải (không tối ưu)
    public List<Route> generateSolution() {
        System.out.println("!!! Auto set vehicle [2 - 4]");
        int numVehicle = getRandom(2, 4);
        System.out.println("Vehicle is: " + numVehicle);

        if (locations == null) {
            System.out.println("!!! Auto set location with n = 8, width = 100");
            generateLocations(8, 100);
        }

        List<Integer> storeIndLoc = new ArrayList<>();
        for (int i = 1; i < successfulRoute.length; i++) {
            storeIndLoc.add(successfulRoute[i]);
        }

        List<Route> routes = new ArrayList<>(numVehicle);
        for (int i = 0; i < numVehicle; i++) {
            int sizeRoute = i == numVehicle - 1 ? storeIndLoc.size() : storeIndLoc.size() / (numVehicle - i);
            List<Pair<Integer, Location>> indLoc = new ArrayList<>();
            for (int j = 0; j < sizeRoute; j++) {
                int tempInd = storeIndLoc.remove(getRandom(0, storeIndLoc.size() - 1));
                indLoc.add(Pair.of(tempInd, locations.get(tempInd))); // Loại bỏ khiến kích thước thay đổi liên tục
            }
            routes.add(Route.builder().indLoc(indLoc).build());
        }
        routes.forEach(Route::print);
        return routes;
    }

    private void handleRoute(List<Pair<Integer, Location>> route) {

    }

    public static void main(String[] args) {
        GenerateData generateData = new GenerateData();
        generateData.generateSolution();
    }

    /**
     * Tạo ra các điểm location giả
     *
     * @param n     - Số lượng điểm location
     * @param width - Khung thời gian
     */
    private void generateLocations(int n, int width) {
        if (n < 5)
            throw new RuntimeException("[GenerateData > generateLocations()] Number of tasks must be greater than 4!");

        // Các tham số ràng buộc
        final int MIN_CORD = 1;
        final int MAX_CORD = 100;
        final int MIN_CAPACITY = 10;
        final int MAX_CAPACITY = 100;

        // Khởi tạo các biến sử dụng chính
        capacity = getRandom(MIN_CAPACITY, MAX_CAPACITY);
        locations = new ArrayList<>();
        distances = new int[n][n];
        successfulRoute = new int[n];

        // Add n tasks
        locations.add(Location.depot);
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
        int time = 0;
        int visits = 0;
        int[] arrivalTime = new int[locations.size()];
        int previousIndex = 0;
        int currentCapacity = 0;
        successfulRoute[0] = 0;
        long startTime = System.currentTimeMillis();
        while (visits < locations.size() - 1) {
            if (System.currentTimeMillis() - startTime > 60000) {
                System.out.println("So long generate locations !!!");
                printData();
                return;
            }
            int index = getRandom(1, locations.size() - 1);//random location ignoring the depot
            if (!locations.get(index).isServiced()) {
                if (currentCapacity + locations.get(index).getLoad() <= capacity) {
                    currentCapacity += locations.get(index).getLoad();
                    locations.get(index).setServiced(true);
                    time += locations.get(previousIndex).distance(locations.get(index));
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
            if (locations.get(i).getLTW() < 0) { // Use a different formula if LTW goes below 0
                w = getRandom(1, arrivalTime[i]);
                locations.get(i).setLTW(arrivalTime[i] - w);
            }
        }

        resetLocation();
        printData();
        System.out.println("Finish generate locations: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void resetLocation() {
        locations.forEach(Location::reset);
    }

    private void printData() {
        System.out.println("Capacity by generate: " + capacity);
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
        System.out.println("Success Route:" + Arrays.toString(successfulRoute));
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
