package org.logistic.data;

import lombok.Getter;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Route;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Lớp đọc dữ liệu từ file
 */
@Getter
public class ReadDataFromFile {
    Location[] locations;
    Route[] routes;
    int maxCapacity;
    Random rd = new Random();

    /**
     * Enum định nghĩa các loại bài toán
     */
    public enum ProblemType {
        VRPTW,  // Vehicle Routing Problem with Time Windows
        PDPTW,  // Pickup and Delivery Problem with Time Windows
        // Thêm các loại bài toán khác ở đây
    }

    /**
     * Đọc dữ liệu của bài toán
     * 
     * @param filePath Đường dẫn file
     * @param problemType Loại bài toán
     */
    public void dataOfProblem(String filePath, ProblemType problemType) {
        try {
            switch (problemType) {
                case VRPTW:
                    readData(filePath, false);
                    break;
                case PDPTW:
                    readData(filePath, true);
                    break;
                // Xử lý các loại bài toán khác ở đây
                default:
                    throw new IllegalArgumentException("Unsupported problem type: " + problemType);
            }
        } catch (Exception e) {
            System.err.println("Error reading problem data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đọc dữ liệu từ file
     * 
     * @param filePath Đường dẫn file
     * @param isPdptw Có phải là bài toán PDPTW không
     * @throws Exception Nếu có lỗi khi đọc file
     */
    private void readData(String filePath, boolean isPdptw) throws Exception {
        // Thử đọc từ classpath trước
        Path path;
        try {
            path = Paths.get(Objects.requireNonNull(
                    ReadDataFromFile.class.getClassLoader().getResource(filePath)).toURI());
        } catch (Exception e) {
            // Nếu không tìm thấy trong classpath, thử đọc từ đường dẫn tuyệt đối
            path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IOException("File not found: " + filePath);
            }
        }

        List<Location> locationList = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                // Đọc dung lượng tối đa
                if(count == (isPdptw ? 0 : 4)) {
                    String[] parts = line.trim().split("\\s+");
                    maxCapacity = Integer.parseInt(parts[1]);
                    System.out.println("Max capacity: " + maxCapacity);
                }

                // Đọc thông tin địa điểm
                if (count >= (isPdptw ? 1 : 9)) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < (isPdptw ? 8 : 7)) {
                        continue; // Bỏ qua dòng không đủ dữ liệu
                    }

                    try {
                        int id = Integer.parseInt(parts[0]);
                        int x = Integer.parseInt(parts[isPdptw ? 2 : 1]);
                        int y = Integer.parseInt(parts[isPdptw ? 3 : 2]);
                        int demand = Integer.parseInt(parts[isPdptw ? 4 : 3]);
                        int ltw = Integer.parseInt(parts[isPdptw ? 5 : 4]);
                        int utw = Integer.parseInt(parts[isPdptw ? 6 : 5]);
                        int service = Integer.parseInt(parts[isPdptw ? 7 : 6]);

                        Location location = Location.builder()
                                .point(new Point(x, y))
                                .serviceTimePick(0)
                                .serviceTimeDeliver(service)
                                .ltw(ltw)
                                .utw(utw)
                                .build();

                        // Xử lý dữ liệu theo loại bài toán
                        if(isPdptw) {
                            if(demand < 0) {
                                location.setPick(true);
                                location.setDemandPick(Math.abs(demand));
                            } else {
                                location.setDeliver(true);
                                location.setDemandDeliver(demand);
                            }
                        } else {
                            // vrptw
                            location.setDeliver(true);
                            location.setDemandDeliver(demand);
                        }

                        locationList.add(location);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line " + count + ": " + line);
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                count++;
            }
            
            // Chuyển List<Location> thành mảng
            locations = locationList.toArray(new Location[0]);
            System.out.println("Read " + locations.length + " locations from " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    /**
     * Đọc giải pháp từ file
     * 
     * @param filePath Đường dẫn file
     * @throws Exception Nếu có lỗi khi đọc file
     */
    public void readSolution(String filePath) throws Exception {
        // Thử đọc từ classpath trước
        Path path;
        try {
            path = Paths.get(Objects.requireNonNull(
                    ReadDataFromFile.class.getClassLoader().getResource(filePath)).toURI());
        } catch (Exception e) {
            // Nếu không tìm thấy trong classpath, thử đọc từ đường dẫn tuyệt đối
            path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IOException("File not found: " + filePath);
            }
        }

        List<Route> routeList = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (count >= 7 && line.contains(":")) {
                    try {
                        String[] parts = line.substring(line.indexOf(':') + 1).trim().split("\\s+");
                        int[] indLocs = new int[parts.length];
                        for (int i = 0; i < parts.length; ++i) {
                            indLocs[i] = Integer.parseInt(parts[i]);
                        }

                        Route route = new Route(indLocs, maxCapacity);
                        
                        // Tính khoảng cách nếu đã có locations
                        if (locations != null) {
                            route.calculateDistance(locations);
                        }

                        routeList.add(route);
                    } catch (Exception e) {
                        System.err.println("Error parsing route at line " + count + ": " + line);
                        System.err.println("Error: " + e.getMessage());
                    }
                }
                count++;
            }
            
            // Chuyển List<Route> thành mảng
            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " routes from " + filePath);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
    
    /**
     * Đọc dữ liệu từ file với đường dẫn tuyệt đối
     * 
     * @param filePath Đường dẫn file tuyệt đối
     * @param problemType Loại bài toán
     */
    public void dataOfProblemFromAbsolutePath(String filePath, ProblemType problemType) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            
            List<Location> locationList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int count = 0;
                boolean isPdptw = (problemType == ProblemType.PDPTW);
                
                while ((line = reader.readLine()) != null) {
                    // Đọc dung lượng tối đa
                    if(count == (isPdptw ? 0 : 4)) {
                        String[] parts = line.trim().split("\\s+");
                        maxCapacity = Integer.parseInt(parts[1]);
                    }

                    // Đọc thông tin địa điểm
                    if (count >= (isPdptw ? 1 : 9)) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length < (isPdptw ? 8 : 7)) {
                            continue; // Bỏ qua dòng không đủ dữ liệu
                        }

                        try {
                            int id = Integer.parseInt(parts[0]);
                            int x = Integer.parseInt(parts[isPdptw ? 2 : 1]);
                            int y = Integer.parseInt(parts[isPdptw ? 3 : 2]);
                            int demand = Integer.parseInt(parts[isPdptw ? 4 : 3]);
                            int ltw = Integer.parseInt(parts[isPdptw ? 5 : 4]);
                            int utw = Integer.parseInt(parts[isPdptw ? 6 : 5]);
                            int service = Integer.parseInt(parts[isPdptw ? 7 : 6]);

                            Location location = Location.builder()
                                    .point(new Point(x, y))
                                    .serviceTimePick(0)
                                    .serviceTimeDeliver(service)
                                    .ltw(ltw)
                                    .utw(utw)
                                    .build();

                            // Xử lý dữ liệu theo loại bài toán
                            if(isPdptw) {
                                if(demand < 0) {
                                    location.setPick(true);
                                    location.setDemandPick(Math.abs(demand));
                                } else {
                                    location.setDeliver(true);
                                    location.setDemandDeliver(demand);
                                }
                            } else {
                                // vrptw
                                location.setDeliver(true);
                                location.setDemandDeliver(demand);
                            }

                            locationList.add(location);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing line " + count + ": " + line);
                        }
                    }
                    count++;
                }
                
                // Chuyển List<Location> thành mảng
                locations = locationList.toArray(new Location[0]);
                System.out.println("Read " + locations.length + " locations from " + filePath);
            }
            
        } catch (Exception e) {
            System.err.println("Error reading problem data from absolute path: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Đọc giải pháp từ file với đường dẫn tuyệt đối
     * 
     * @param filePath Đường dẫn file tuyệt đối
     */
    public void readSolutionFromAbsolutePath(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File not found: " + filePath);
            }
            
            List<Route> routeList = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int count = 0;
                
                while ((line = reader.readLine()) != null) {
                    if (count >= 7 && line.contains(":")) {
                        try {
                            String[] parts = line.substring(line.indexOf(':') + 1).trim().split("\\s+");
                            int[] indLocs = new int[parts.length];
                            for (int i = 0; i < parts.length; ++i) {
                                indLocs[i] = Integer.parseInt(parts[i]);
                            }

                            Route route = new Route(indLocs, maxCapacity);
                            
                            // Tính khoảng cách nếu đã có locations
                            if (locations != null) {
                                route.calculateDistance(locations);
                            }

                            routeList.add(route);
                        } catch (Exception e) {
                            System.err.println("Error parsing route at line " + count + ": " + line);
                        }
                    }
                    count++;
                }
                
                // Chuyển List<Route> thành mảng
                routes = routeList.toArray(new Route[0]);
                System.out.println("Read " + routes.length + " routes from " + filePath);
            }
            
        } catch (Exception e) {
            System.err.println("Error reading solution from absolute path: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
