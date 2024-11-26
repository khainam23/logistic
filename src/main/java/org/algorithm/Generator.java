package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Generator {
    int[][] distances; // Khoảng cách giữa các điểm
    ArrayList<Location> locations; // Thông tin tại mỗi điểm
    int[] successfulRoute; // Cạnh tốt nên đi
    int capacity; // Tải trọng

    /**
     * Số nhiệm vụ = n (số lượng địa điểm)
     * Tạo ra khoảng thời gian di chuyển = width:
     * + LTW = T - width
     * + UTW = T - width
     * @param n
     * @param width
     */
    public void generateData(int n, int width) {
        // Các hằng số
        final int MIN_COORD = 1; // Giá trị tối thiểu của tọa độ x hoặc y
        final int MAX_COORD = 100; // Giá trị tối đa của tọa độ x hoặc y
        final int MIN_CAPACITY = 1; // Trọng tải tối thiểu
        final int MAX_CAPACITY = 10; // Trọng tải tối đa
        capacity = getRandom(MIN_CAPACITY, MAX_CAPACITY); // Dung lượng tối đa

        // Danh sách thông tin của các vị trí
        locations = new ArrayList<>();
        // Bắt đầu tại vị trí khởi tạo
        Location depot = new Location(true, 0, 0, 0);
        depot.setServiceable(false);
        depot.setServiced(true);
        locations.add(depot);

        // Thêm các vị trí
        for (int i = 0; i < n; i++) {
            // Tạo ra các vị trí lấy hàng (depot)
            int Px = getRandom(MIN_COORD, MAX_COORD);
            int Py = getRandom(MIN_COORD, MAX_COORD);
            int load = getRandom(1, capacity);
            locations.add(new Location(true, Px, Py, load));
            // Tạo ra các vị trí thả hàng (dropoff)
            int Dx = getRandom(MIN_COORD, MAX_COORD);
            int Dy = getRandom(MIN_COORD, MAX_COORD);
            // Dấu - thể hiện sẽ nhận bao nhiêu
            locations.add(new Location(false, Dx, Dy, -load));
        }

        // Tính khoảng cách điểm
        distances = new int[locations.size()][locations.size()];
        for (int rows = 0; rows < distances.length; rows++) {
            for (int cols = 0; cols < distances.length; cols++) {
                distances[rows][cols] = (int)
                        // Khoảng cách euclid
                        Math.sqrt(
                                Math.pow(locations.get(cols).getY() - locations.get(rows).getY(), 2) +
                                Math.pow(locations.get(cols).getX() - locations.get(rows).getX(), 2)
                        );
            }
        }

        // Tạo ngẫu nhiên các tuyến đường thỏa mãn điều kiện ràng buộc
        int visits = 0;
        int time = 0;
        int[] arrivalTime = new int[locations.size()]; // Ghi lại thời gian tới từng điểm
        int previousIndex = 0;
        int currentCapacity = 0;
        successfulRoute = new int[locations.size()]; // Các nhánh có thể đi
        successfulRoute[0] = 0;// Nơi bắt đầu nhận
        while (visits < locations.size() - 1) {
            int index = getRandom(1, locations.size() - 1);// Vị trí ngẫu nhiên không là vị trí xuất phát hoặc kho
            if (locations.get(index).isServiceable() && !locations.get(index).isServiced()) { // Chưa thăm và có thể ghé
                if (currentCapacity + locations.get(index).getLoad() <= capacity) { // Trọng tải hiện tại không được vượt quá trọng tải xe có thể mang
                    currentCapacity += locations.get(index).getLoad();
                    locations.get(index).setServiced(true);
                    time += distances[previousIndex][index];
                    previousIndex = index;
                    arrivalTime[index] = time;
                    successfulRoute[visits + 1] = index; // Nối nhánh
                    if (locations.get(index).isPickup()) {
                        locations.get(index + 1).setServiceable(true);
                    }
                    visits++;
                }
            }
        }

        // Thời gian trung bình
        int averageTime = time / visits;

        // Cập nhật thời gian
        for (int i = 1; i < locations.size(); i++) {
            int w = getRandom(1, width) * averageTime * 10;
            locations.get(i).setUTW(arrivalTime[i] + w);
            locations.get(i).setLTW(arrivalTime[i] - w);
            if (locations.get(i).getLTW() < 0) {
                // Thời gian không thể bé hơn 0
                w = getRandom(1, arrivalTime[i]);
                locations.get(i).setLTW(arrivalTime[i] - w);
            }
        }

        // Khởi động lại việc có thể nhận và lấy hàng
        resetServiced(locations);
    }

    // Lấy ngẫu nhiên trong đoạn min -> max
    private int getRandom(int min, int max) {
        final Random rn = new Random();
        return rn.nextInt((max - min) + 1) + min;
    }

    // In ma trận
    public void printMatrix(int[][] di) {
        for (int cols = 0; cols < di.length; cols++)
            System.out.print("\t" + cols);

        for (int rows = 0; rows < di.length; rows++) {
            System.out.print("\n" + rows);
            for (int cols = 0; cols < di.length; cols++) {
                System.out.print("\t" + di[rows][cols]);
            }
        }
        System.out.println("");
    }

    public void printLocations(ArrayList<Location> lo) {
        System.out.println(String.format("%8s %11s %6s %6s %6s %10s %13s", "Pickup:", "(x, y):", "Load:", "LTW:", "UTW:", "Serviced:", "Serviceable:"));
        for (int i = 0; i < lo.size(); i++) {
            System.out.println(lo.get(i).toString());
        }
    }

    public void printRoute(ArrayList<Location> lo, int[] ri) {
        resetServiced(lo);
        System.out.println("\nRoute:");
        System.out.println(String.format("%8s %8s %11s %6s %6s %6s %10s %13s %15s %15s", "Index:", "Pickup:", "(x, y):", "Load:", "LTW:", "UTW:", "Serviced:", "Serviceable:", "Current Load: ", "Current Time:"));
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < ri.length; i++) {
            String index = String.format("%8s ", ri[i]);
            currentLoad += lo.get(ri[i]).getLoad();
            currentTime += distances[previousIndex][ri[i]];
            previousIndex = ri[i];
            //Show if the vehicle capacity is exceeded
            String cl = "";
            if (currentLoad > capacity)
                cl = String.format("%15s", currentLoad + " > " + capacity);
            else
                cl = String.format("%15s", currentLoad);
            String ct = String.format("%15s", currentTime);
            lo.get(ri[i]).setServiced(true);
            if (lo.get(ri[i]).isPickup())
                lo.get(ri[i] + 1).setServiceable(true);
            System.out.println(index + lo.get(ri[i]).toString() + cl + ct);
        }
    }

    public ArrayList<Location> resetServiced(ArrayList<Location> lo) {
        for (int i = 1; i < lo.size(); i++) {
            lo.get(i).resetServiced();
        }
        return lo;
    }
}
