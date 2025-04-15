package org.logistic.util;

import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

/**
 * Tiện ích in thông tin cho ứng dụng
 */
public class PrintUtil {
    private static PrintUtil instance;
    private static final String SEPARATOR = "*".repeat(10);

    /**
     * Constructor riêng tư để ngăn khởi tạo trực tiếp
     */
    private PrintUtil() {
        // Private constructor
    }

    /**
     * Lấy instance của PrintUtil (Singleton pattern)
     * 
     * @return Instance của PrintUtil
     */
    public static synchronized PrintUtil getInstance() {
        if (instance == null) {
            instance = new PrintUtil();
        }
        return instance;
    }

    /**
     * In danh sách các giải pháp
     * 
     * @param solutions Mảng các giải pháp cần in
     */
    public void printSolutions(Solution[] solutions) {
        if (solutions == null) {
            System.out.println("Solutions: null");
            return;
        }
        
        System.out.println("Solutions (count: " + solutions.length + "):");
        for (int i = 0; i < solutions.length; i++) {
            System.out.println("Solution #" + (i + 1) + ":");
            printSolution(solutions[i]);
        }
    }

    /**
     * In thông tin của một giải pháp
     * 
     * @param solution Giải pháp cần in
     */
    public void printSolution(Solution solution) {
        if (solution == null) {
            System.out.println("Solution: null");
            return;
        }
        
        System.out.println("Solution (fitness: " + solution.getFitness() + "):");
        printRoutes(solution.getRoutes());
        System.out.println(SEPARATOR);
    }

    /**
     * In danh sách các tuyến đường
     * 
     * @param routes Mảng các tuyến đường cần in
     */
    public void printRoutes(Route[] routes) {
        if (routes == null) {
            System.out.println("Routes: null");
            return;
        }

        System.out.println("Routes (count: " + routes.length + "):");
        for (int i = 0; i < routes.length; i++) {
            System.out.println((i + 1) + ". " + routes[i]);
        }
    }

    /**
     * In thông tin của một vị trí
     * 
     * @param location Vị trí cần in
     */
    public void printLocation(Location location) {
        if (location == null) {
            System.out.println("Location: null");
            return;
        }

        System.out.println("Location:");
        printLocationDetails(location);
    }

    /**
     * In danh sách các vị trí
     * 
     * @param locations Mảng các vị trí cần in
     */
    public void printLocations(Location[] locations) {
        if (locations == null) {
            System.out.println("Locations array: null");
            return;
        }

        if (locations.length == 0) {
            System.out.println("Locations array: empty");
            return;
        }

        System.out.println("Locations array (size: " + locations.length + "):");
        for (int i = 0; i < locations.length; i++) {
            System.out.println("Location #" + i + ":");
            if (locations[i] == null) {
                System.out.println("  null");
            } else {
                printLocationDetails(locations[i]);
            }
            System.out.println(); // Thêm dòng trống để dễ đọc
        }
    }
    
    /**
     * In chi tiết của một vị trí
     * 
     * @param location Vị trí cần in chi tiết
     */
    private void printLocationDetails(Location location) {
        System.out.println("  Point: (" + location.getPoint().getX() + ", " + location.getPoint().getY() + ")");
        System.out.println("  Service Time Pick: " + location.getServiceTimePick());
        System.out.println("  Service Time Deliver: " + location.getServiceTimeDeliver());
        System.out.println("  Demand Pick: " + location.getDemandPick());
        System.out.println("  Demand Deliver: " + location.getDemandDeliver());
        System.out.println("  Time Window: [" + location.getLtw() + ", " + location.getUtw() + "]");
        System.out.println("  Is Pick: " + location.isPick());
        System.out.println("  Is Deliver: " + location.isDeliver());
    }
}
