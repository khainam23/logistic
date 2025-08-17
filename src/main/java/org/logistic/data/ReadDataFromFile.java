package org.logistic.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.logistic.model.DistanceTime;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Route;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadDataFromFile {
    Location[] locations;
    Route[] routes;
    DistanceTime[] distanceTimes;
    double maxCapacity;

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum ProblemType {
        VRPTW(false, 3, 6, 0, 1, 2, 3, 4, 5, 6),
        PDPTW(true, 0, 1, 2, 3, 4, 5, 6, 7, 8),
        VRPSPDTW_LIU_TANG_YAO(true, 7, 9, 0, 1, 2, 3, 4, 5, 6),
        VRPSPDTW_WANG_CHEN(true, 3, 6, 0, 1, 2, 3, 4, 5, 6, 7, 8);

        boolean isPickupDelivery;
        int capacityLineIndex;
        int dataStartLineIndex;
        int[] columnIndices;

        ProblemType(boolean isPickupDelivery, int capacityLineIndex, int dataStartLineIndex, int... columnIndices) {
            this.isPickupDelivery = isPickupDelivery;
            this.capacityLineIndex = capacityLineIndex;
            this.dataStartLineIndex = dataStartLineIndex;
            this.columnIndices = columnIndices;
        }

        public int getColumnIndex(int field) {
            return (field < 0 || field >= columnIndices.length) ? -1 : columnIndices[field];
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SolutionFormat {
        int headerLines = 0; // Changed from 5 to 0 to work with PDPTW files
        String routePrefix = ":";
        String delimiter = "\\s+";
    }

    public void readProblemData(String filePath, ProblemType problemType) {
        try {
            readProblemDataFromPath(resolveFilePath(filePath), problemType);
        } catch (Exception e) {
            System.err.println("Error reading problem data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void readSolution(String filePath) {
        readSolution(filePath, new SolutionFormat());
    }

    public void readSolution(String filePath, ProblemType problemType) {
        try {
            switch (problemType) {
                case VRPTW:
                    readVRPTWSolution(resolveFilePath(filePath));
                    break;
                case PDPTW:
                    readPDPTWSolution(resolveFilePath(filePath));
                    break;
                case VRPSPDTW_LIU_TANG_YAO:
                    readVRPSPDTWLiuTangYaoSolution(resolveFilePath(filePath));
                    break;
                case VRPSPDTW_WANG_CHEN:
                    readVRPSPDTWWangChenSolution(resolveFilePath(filePath));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported problem type: " + problemType);
            }
        } catch (Exception e) {
            System.err.println("Error reading solution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void readSolution(String filePath, SolutionFormat format) {
        try {
            readSolutionFromPath(resolveFilePath(filePath), format);
        } catch (Exception e) {
            System.err.println("Error reading solution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Path resolveFilePath(String filePath) throws IOException {
        try {
            URL resource = ReadDataFromFile.class.getClassLoader().getResource(filePath);
            if (resource != null)
                return Paths.get(resource.toURI());
        } catch (Exception ignored) {
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path))
            throw new IOException("File not found: " + filePath);
        return path;
    }

    private File resolveDirectoryPath(String directoryPath) {
        try {
            URL url = ReadDataFromFile.class.getClassLoader().getResource(directoryPath);
            if (url != null)
                return new File(url.toURI());
        } catch (Exception ignored) {
        }
        return new File(directoryPath);
    }

    private void readProblemDataFromPath(Path path, ProblemType problemType) throws IOException {
        switch (problemType) {
            case VRPTW:
                readVRPTWData(path, problemType);
                break;
            case PDPTW:
                readPDPTWData(path, problemType);
                break;
            case VRPSPDTW_LIU_TANG_YAO:
                readVRPSPDTWLiuTangYaoData(path);
                break;
            case VRPSPDTW_WANG_CHEN:
                readVRPSPDTWWangChenData(path, problemType);
                break;
            default:
                throw new IllegalArgumentException("Unsupported problem type: " + problemType);
        }
    }

    private void readVRPTWData(Path path, ProblemType problemType) throws IOException {
        List<Location> locationList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (count == problemType.getCapacityLineIndex()) {
                    maxCapacity = Integer.parseInt(line.trim().split("\\s+")[1]);
                    System.out.println("Max capacity: " + maxCapacity);
                }

                if (count >= problemType.getDataStartLineIndex()) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 7) {
                        count++;
                        continue;
                    }

                    try {
                        int[] indices = new int[7];
                        for (int i = 0; i < 7; i++) {
                            indices[i] = problemType.getColumnIndex(i);
                            if (indices[i] < 0) {
                                throw new IllegalArgumentException("Invalid column indices for " + problemType);
                            }
                        }

                        int x = Integer.parseInt(parts[indices[1]]);
                        int y = Integer.parseInt(parts[indices[2]]);
                        int demand = Integer.parseInt(parts[indices[3]]);
                        int ltw = Integer.parseInt(parts[indices[4]]);
                        int utw = Integer.parseInt(parts[indices[5]]);
                        int service = Integer.parseInt(parts[indices[6]]);

                        Location location = Location.builder()
                                .point(new Point(x, y))
                                .serviceTimePick(0)
                                .serviceTimeDeliver(service)
                                .ltw(ltw)
                                .utw(utw)
                                .build();

                        location.setDeliver(true);
                        location.setDemandDeliver(demand);

                        locationList.add(location);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing VRPTW line " + count + ": " + line);
                    }
                }
                count++;
            }

            locations = locationList.toArray(new Location[0]);
            System.out.println("Read " + locations.length + " VRPTW locations from " + path);
        }
    }

    private void readPDPTWData(Path path, ProblemType problemType) throws IOException {
        List<Location> locationList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (count == problemType.getCapacityLineIndex()) {
                    maxCapacity = Integer.parseInt(line.trim().split("\\s+")[1]);
                    System.out.println("Max capacity: " + maxCapacity);
                }

                if (count >= problemType.getDataStartLineIndex()) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 8) {
                        count++;
                        continue;
                    }

                    try {
                        int[] indices = new int[7];
                        for (int i = 0; i < 7; i++) {
                            indices[i] = problemType.getColumnIndex(i);
                            if (indices[i] < 0) {
                                throw new IllegalArgumentException("Invalid column indices for " + problemType);
                            }
                        }

                        int x = Integer.parseInt(parts[indices[1]]);
                        int y = Integer.parseInt(parts[indices[2]]);
                        int demand = Integer.parseInt(parts[indices[3]]);
                        int ltw = Integer.parseInt(parts[indices[4]]);
                        int utw = Integer.parseInt(parts[indices[5]]);
                        int service = Integer.parseInt(parts[indices[6]]);

                        Location location = Location.builder()
                                .point(new Point(x, y))
                                .serviceTimePick(0)
                                .serviceTimeDeliver(service)
                                .ltw(ltw)
                                .utw(utw)
                                .build();

                        if (demand < 0) {
                            location.setPick(true);
                            location.setDemandPick(Math.abs(demand));
                        } else {
                            location.setDeliver(true);
                            location.setDemandDeliver(demand);
                        }

                        locationList.add(location);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing PDPTW line " + count + ": " + line);
                    }
                }
                count++;
            }

            locations = locationList.toArray(new Location[0]);
            System.out.println("Read " + locations.length + " PDPTW locations from " + path);
        }
    }

    private void readVRPSPDTWLiuTangYaoData(Path path) throws IOException {
        List<Location> locationList = new ArrayList<>();
        List<DistanceTime> distanceTimeList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean inNodeSection = false;
            boolean inDistanceTimeSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Đọc capacity từ dòng CAPACITY
                if (line.startsWith("CAPACITY")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        maxCapacity = Double.parseDouble(parts[1].trim());
                        System.out.println("Max capacity: " + maxCapacity);
                    }
                    continue;
                }

                // Bắt đầu đọc dữ liệu node
                if (line.equals("NODE_SECTION")) {
                    inNodeSection = true;
                    inDistanceTimeSection = false;
                    continue;
                }

                // Bắt đầu đọc DISTANCETIME_SECTION
                if (line.equals("DISTANCETIME_SECTION")) {
                    inNodeSection = false;
                    inDistanceTimeSection = true;
                    continue;
                }

                // Kết thúc đọc dữ liệu
                if (line.equals("EOF") || line.startsWith("DEMAND_SECTION") || line.startsWith("DEPOT_SECTION")) {
                    break;
                }

                // Đọc dữ liệu node: [ID],[delivery],[pickup],[start_time],[end_time],[service_time]
                if (inNodeSection && !line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        try {
                            int nodeId = Integer.parseInt(parts[0]);
                            double dDemand = Double.parseDouble(parts[1]);
                            double pDemand = Double.parseDouble(parts[2]);
                            int ltw = Integer.parseInt(parts[3]);
                            int utw = Integer.parseInt(parts[4]);
                            int service = Integer.parseInt(parts[5]);

                            Location location = Location.builder()
                                    .point(new Point(0, 0)) // Tọa độ sẽ được tính từ distance matrix
                                    .serviceTimePick(0)
                                    .serviceTimeDeliver(service)
                                    .ltw(ltw)
                                    .utw(utw)
                                    .build();

                            if (pDemand > 0) {
                                location.setPick(true);
                                location.setDemandPick(pDemand);
                            }
                            if (dDemand > 0) {
                                location.setDeliver(true);
                                location.setDemandDeliver(dDemand);
                            }
                            if (pDemand == 0 && dDemand == 0) {
                                location.setDeliver(false);
                                location.setPick(false);
                                location.setDemandDeliver(0);
                                location.setDemandPick(0);
                            }

                            locationList.add(location);
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi phân tích dòng NODE Liu Tang Yao: " + line);
                        }
                    }
                }

                // Đọc dữ liệu DISTANCETIME_SECTION
                if (inDistanceTimeSection && !line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        try {
                            int fromNode = Integer.parseInt(parts[0].trim());
                            int toNode = Integer.parseInt(parts[1].trim());
                            double distance = Double.parseDouble(parts[2].trim());
                            double travelTime = Double.parseDouble(parts[3].trim());

                            DistanceTime distanceTime = DistanceTime.builder()
                                    .fromNode(fromNode)
                                    .toNode(toNode)
                                    .distance(distance)
                                    .travelTime(travelTime)
                                    .build();

                            distanceTimeList.add(distanceTime);
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi phân tích dòng DISTANCETIME Liu Tang Yao: " + line);
                        }
                    }
                }
            }

            locations = locationList.toArray(new Location[0]);
            distanceTimes = distanceTimeList.toArray(new DistanceTime[0]);
            
            System.out.println("Đã đọc " + locations.length + " location VRPSPDTW Liu Tang Yao từ " + path);
            System.out.println("Đã đọc " + distanceTimes.length + " thông tin khoảng cách-thời gian từ DISTANCETIME_SECTION");
        }
    }

    private void readVRPSPDTWWangChenData(Path path, ProblemType problemType) throws IOException {
        List<Location> locationList = new ArrayList<>();
        List<DistanceTime> distanceTimeList = new ArrayList<>();
        boolean readingNodes = false;
        boolean inDistanceTimeSection = false;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // Đọc CAPACITY
                if (line.startsWith("CAPACITY")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        maxCapacity = (int) Double.parseDouble(parts[1].trim());
                        System.out.println("Max capacity: " + maxCapacity);
                    }
                    continue;
                }

                // Bắt đầu đọc NODE_SECTION
                if (line.equals("NODE_SECTION")) {
                    readingNodes = true;
                    inDistanceTimeSection = false;
                    continue;
                }

                // Bắt đầu DISTANCETIME_SECTION
                if (line.equals("DISTANCETIME_SECTION")) {
                    readingNodes = false;
                    inDistanceTimeSection = true;
                    continue;
                }

                // Kết thúc đọc dữ liệu
                if (line.equals("EOF") || line.startsWith("DEMAND_SECTION") || line.startsWith("DEPOT_SECTION")) {
                    break;
                }

                if (readingNodes) {
                    // Định dạng: [ID],[delivery],[pickup],[start_time],[end_time],[service_time]
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        try {
                            int nodeId = Integer.parseInt(parts[0].trim());
                            double dDemand = Double.parseDouble(parts[1].trim());
                            double pDemand = Double.parseDouble(parts[2].trim());
                            int readyTime = Integer.parseInt(parts[3].trim());
                            int dueDate = Integer.parseInt(parts[4].trim());
                            int serviceTime = Integer.parseInt(parts[5].trim());

                            Location location = Location.builder()
                                    .point(new Point(0, 0)) // Tọa độ sẽ được tính từ distance matrix
                                    .serviceTimePick(0)
                                    .serviceTimeDeliver(serviceTime)
                                    .ltw(readyTime)
                                    .utw(dueDate)
                                    .build();

                            if (pDemand > 0) {
                                location.setPick(true);
                                location.setDemandPick(pDemand);
                            }
                            if (dDemand > 0) {
                                location.setDeliver(true);
                                location.setDemandDeliver(dDemand);
                            }
                            if (pDemand == 0 && dDemand == 0) {
                                location.setDeliver(false);
                                location.setPick(false);
                                location.setDemandDeliver(0);
                                location.setDemandPick(0);
                            }

                            locationList.add(location);
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi định dạng dòng NODE Wang Chen: " + line);
                        }
                    }
                }

                // Đọc dữ liệu DISTANCETIME_SECTION
                if (inDistanceTimeSection && !line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        try {
                            int fromNode = Integer.parseInt(parts[0].trim());
                            int toNode = Integer.parseInt(parts[1].trim());
                            double distance = Double.parseDouble(parts[2].trim());
                            double travelTime = Double.parseDouble(parts[3].trim());

                            DistanceTime distanceTime = DistanceTime.builder()
                                    .fromNode(fromNode)
                                    .toNode(toNode)
                                    .distance(distance)
                                    .travelTime(travelTime)
                                    .build();

                            distanceTimeList.add(distanceTime);
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi phân tích dòng DISTANCETIME Wang Chen: " + line);
                        }
                    }
                }
            }
        }

        locations = locationList.toArray(new Location[0]);
        distanceTimes = distanceTimeList.toArray(new DistanceTime[0]);
        
        System.out.println("Đã đọc " + locations.length + " Location từ: " + path);
        System.out.println("Đã đọc " + distanceTimes.length + " thông tin khoảng cách-thời gian từ DISTANCETIME_SECTION");
    }

    private void readVRPTWSolution(Path path) throws IOException {
        List<Route> routeList = new ArrayList<>();
        SolutionFormat format = new SolutionFormat();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (count >= format.getHeaderLines() && line.contains(format.getRoutePrefix())) {
                    try {
                        String routeData = line
                                .substring(line.indexOf(format.getRoutePrefix()) + format.getRoutePrefix().length())
                                .trim();
                        if (routeData == null || routeData.isEmpty())
                            continue;
                        String[] parts = routeData.split(format.getDelimiter());
                        if (parts == null)
                            continue;
                        int[] indLocs = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();

                        if (indLocs.length > 1) {
                            Route route = new Route(indLocs, maxCapacity);
                            if (locations != null) {
                                if (distanceTimes != null && distanceTimes.length > 0) {
                                    route.calculateDistance(locations, distanceTimes);
                                } else {
                                    route.calculateDistance(locations);
                                }
                            }
                            routeList.add(route);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing VRPTW route at line " + count + ": " + line);
                    }
                }
                count++;
            }

            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " VRPTW routes from " + path);
        }
    }

    private void readPDPTWSolution(Path path) throws IOException {
        List<Route> routeList = new ArrayList<>();
        SolutionFormat format = new SolutionFormat();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (count >= format.getHeaderLines() && line.contains(format.getRoutePrefix())) {
                    try {
                        String routeData = line.substring(line.indexOf(format.getRoutePrefix()) + 1).trim();
                        String[] parts = routeData.split(format.getDelimiter());
                        int[] indLocs = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();

                        Route route = new Route(indLocs, maxCapacity);
                        if (locations != null) {
                            if (distanceTimes != null && distanceTimes.length > 0) {
                                route.calculateDistance(locations, distanceTimes);
                            } else {
                                route.calculateDistance(locations);
                            }
                        }
                        routeList.add(route);
                    } catch (Exception e) {
                        System.err.println("Error parsing PDPTW route at line " + count + ": " + line);
                    }
                }
                count++;
            }

            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " PDPTW routes from " + path);
        }
    }

    private void readVRPSPDTWLiuTangYaoSolution(Path path) throws IOException {
        List<Route> routeList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Route")) {
                    try {
                        // Xử lý định dạng Liu Tang Yao: "Route 1: 185"
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            String routeData = parts[1].trim();
                            if (!routeData.isEmpty()) {
                                String[] nodeStrings = routeData.split("\\s+");
                                List<Integer> nodeList = new ArrayList<>();
                                nodeList.add(0); // Thêm depot đầu
                                for (String nodeStr : nodeStrings) {
                                    if (!nodeStr.trim().isEmpty()) {
                                        nodeList.add(Integer.parseInt(nodeStr.trim()));
                                    }
                                }
                                nodeList.add(0); // Thêm depot cuối

                                int[] indLocs = nodeList.stream().mapToInt(Integer::intValue).toArray();
                                Route route = new Route(indLocs, maxCapacity);
                                if (locations != null) {
                                    if (distanceTimes != null && distanceTimes.length > 0) {
                                        route.calculateDistance(locations, distanceTimes);
                                    } else {
                                        route.calculateDistance(locations);
                                    }
                                }
                                routeList.add(route);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing Liu Tang Yao route at line " + count + ": " + line);
                    }
                }
                count++;
            }

            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " VRPSPDTW Liu Tang Yao routes from " + path);
        }
    }

    private void readVRPSPDTWWangChenSolution(Path path) throws IOException {
        List<Route> routeList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Route")) {
                    try {
                        // Xử lý định dạng Wang Chen: "Route 1: 20 29 46 48"
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            String routeData = parts[1].trim();
                            if (!routeData.isEmpty()) {
                                String[] nodeStrings = routeData.split("\\s+");
                                List<Integer> nodeList = new ArrayList<>();
                                nodeList.add(0); // Thêm depot đầu

                                for (String nodeStr : nodeStrings) {
                                    if (!nodeStr.trim().isEmpty()) {
                                        nodeList.add(Integer.parseInt(nodeStr.trim()));
                                    }
                                }
                                nodeList.add(0); // Thêm depot cuối

                                int[] indLocs = nodeList.stream().mapToInt(Integer::intValue).toArray();
                                Route route = new Route(indLocs, maxCapacity);
                                if (locations != null) {
                                    if (distanceTimes != null && distanceTimes.length > 0) {
                                        route.calculateDistance(locations, distanceTimes);
                                    } else {
                                        route.calculateDistance(locations);
                                    }
                                }
                                routeList.add(route);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing VRPSPDTW Wang Chen route: " + line);
                        e.printStackTrace();
                    }
                }
            }

            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " VRPSPDTW Wang Chen routes from " + path);
        }
    }

    private void readSolutionFromPath(Path path, SolutionFormat format) throws IOException {
        List<Route> routeList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (count >= format.getHeaderLines() && line.contains(format.getRoutePrefix())) {
                    try {
                        String routeData = line.substring(line.indexOf(format.getRoutePrefix()) + 1).trim();
                        String[] parts = routeData.split(format.getDelimiter());
                        int[] indLocs = Arrays.stream(parts).mapToInt(Integer::parseInt).toArray();

                        if (indLocs.length >= 1) {
                            Route route = new Route(indLocs, maxCapacity);
                            if (locations != null) {
                                if (distanceTimes != null && distanceTimes.length > 0) {
                                    route.calculateDistance(locations, distanceTimes);
                                } else {
                                    route.calculateDistance(locations);
                                }
                            }
                            routeList.add(route);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing route at line " + count + ": " + line);
                    }
                }
                count++;
            }

            routes = routeList.toArray(new Route[0]);
            System.out.println("Read " + routes.length + " routes from " + path);
        }
    }

    private List<File> getFilesInDirectory(String directoryPath, String extension) {
        File directory = resolveDirectoryPath(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory not found: " + directoryPath);
            return new ArrayList<>();
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0)
            return new ArrayList<>();

        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().endsWith(extension))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    private File getMatchingSolutionFile(File srcFile, String solutionDir) {
        String baseName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf('.'));
        File solutionDirectory = resolveDirectoryPath(solutionDir);

        if (!solutionDirectory.exists() || !solutionDirectory.isDirectory())
            return null;

        File[] matchingFiles = solutionDirectory.listFiles(
                file -> file.isFile() && file.getName().startsWith(baseName) && file.getName().endsWith(".txt"));

        if (matchingFiles != null && matchingFiles.length > 0) {
            Arrays.sort(matchingFiles, Comparator.comparing(File::getName, Comparator.comparingInt(String::length)));
            return matchingFiles[0];
        }
        return null;
    }

    public void processAllFilesInDirectory(String srcDirPath, String solutionDirPath,
            ProblemType problemType, FileProcessCallback callback) {
        List<File> srcFiles = getFilesInDirectory(srcDirPath, ".txt");
        if (srcFiles.isEmpty()) {
            System.err.println("No source files found in directory: " + srcDirPath);
            return;
        }

        for (File srcFile : srcFiles) {
            try {
                locations = null;
                routes = null;

                readProblemData(srcFile.getAbsolutePath(), problemType);
                if (locations == null || locations.length == 0)
                    continue;

                File solutionFile = getMatchingSolutionFile(srcFile, solutionDirPath);
                if (solutionFile == null)
                    continue;

                readSolution(solutionFile.getAbsolutePath());
                if (routes == null || routes.length == 0)
                    continue;

                callback.process(locations, routes, srcFile.getName());
                System.gc();

            } catch (Exception e) {
                System.err.println("Error processing file " + srcFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public interface FileProcessCallback {
        void process(Location[] locations, Route[] routes, String fileName);
    }
}