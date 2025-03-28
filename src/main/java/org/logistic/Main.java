package org.logistic;

import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.data.GenerateData;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Solution;
import org.logistic.model.Vehicle;
import org.logistic.util.WriteLog;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        testRealDataPdptw();
    }

    public static void test() {
        // Tạo dữ liệu giả
        int numPickupPoints = 5; // Số điểm đón hàng
        int numVehicles = 3; // Số xe
        int vehicleCapacity = 50; // Sức chứa của mỗi xe

        // Tạo điểm kho (depot)
        Point depot = new Point(0, 0);

        // Tạo các điểm đón và trả hàng
        Location[] locations = GenerateData.generateLocations(numPickupPoints);

        // Tạo đội xe
        Vehicle[] vehicles = GenerateData.generateVehicles(numVehicles, depot, vehicleCapacity);

        // Tính ma trận khoảng cách
        int totalPoints = locations.length;
        double[][] distances = new double[totalPoints][totalPoints];
        for (int i = 0; i < totalPoints; i++) {
            for (int j = 0; j < totalPoints; j++) {
                Point p1 = locations[i].getPoint();
                Point p2 = locations[j].getPoint();
                distances[i][j] = Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
            }
        }

        // Tạo giải pháp ban đầu
        Solution initialSolution = new Solution(distances);

        // Khởi tạo và chạy thuật toán SHO
        SpottedHyenaOptimizer optimizer = new SpottedHyenaOptimizer(locations, vehicles);
        Solution optimizedSolution = optimizer.optimize(initialSolution);

        // Ghi kết quả vào log
        WriteLog logger = WriteLog.getInstance();
        logger.log("\nOptimized Results:");
        Vehicle[] optimizedVehicles = optimizer.getOptimizedVehicles();
        for (int i = 0; i < optimizedVehicles.length; i++) {
            int[] route = optimizedVehicles[i].getRoute();

            // Tính quãng đường cho xe này
            double vehicleDistance = 0;
            if (route.length > 0) {
                // Từ kho đến điểm đầu tiên
                vehicleDistance += Math.sqrt(Math
                        .pow(optimizedVehicles[i].getPoint().getX() - locations[route[0]].getPoint().getX(), 2) +
                        Math.pow(optimizedVehicles[i].getPoint().getY() - locations[route[0]].getPoint().getY(), 2));

                // Giữa các điểm
                for (int j = 0; j < route.length - 1; j++) {
                    vehicleDistance += distances[route[j]][route[j + 1]];
                }

                // Từ điểm cuối về kho
                vehicleDistance += Math.sqrt(Math
                        .pow(optimizedVehicles[i].getPoint().getX()
                                - locations[route[route.length - 1]].getPoint().getX(), 2)
                        +
                        Math.pow(optimizedVehicles[i].getPoint().getY()
                                - locations[route[route.length - 1]].getPoint().getY(), 2));
            }
            logger.logVehicleRoute(i, route, vehicleDistance);
        }

        // Tính tổng quãng đường
        double totalDistance = 0;
        for (Vehicle vehicle : optimizedVehicles) {
            int[] route = vehicle.getRoute();
            if (route.length > 0) {
                // Thêm khoảng cách từ kho đến điểm đầu tiên
                totalDistance += Math
                        .sqrt(Math.pow(vehicle.getPoint().getX() - locations[route[0]].getPoint().getX(), 2) +
                                Math.pow(vehicle.getPoint().getY() - locations[route[0]].getPoint().getY(), 2));

                // Tính khoảng cách giữa các điểm
                for (int i = 0; i < route.length - 1; i++) {
                    totalDistance += distances[route[i]][route[i + 1]];
                }

                // Thêm khoảng cách từ điểm cuối về kho
                totalDistance += Math.sqrt(Math
                        .pow(vehicle.getPoint().getX() - locations[route[route.length - 1]].getPoint().getX(), 2) +
                        Math.pow(vehicle.getPoint().getY() - locations[route[route.length - 1]].getPoint().getY(), 2));
            }
        }
        logger.logTotalDistance(totalDistance);
        logger.close();
    }

    public static void testRealDataVrptw() throws URISyntaxException {
        String data = "data/vrptw/src/c101.txt";

        ReadDataFromFile readDataVRPTW = new ReadDataFromFile();
        readDataVRPTW.dataOfVrptw(data);

        System.out.println(readDataVRPTW.getLocations());
    }

    public static void testRealDataPdptw() throws URISyntaxException {
        String data = "data/pdptw/src/lc101.txt";

        ReadDataFromFile readDataPdptw = new ReadDataFromFile();
        readDataPdptw.dataOfPdptw(data);

        System.out.println(readDataPdptw.getLocations());
    }
}