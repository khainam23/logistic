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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

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
    
    /**
     * Lấy danh sách các file trong thư mục
     * 
     * @param directoryPath Đường dẫn thư mục
     * @param extension Phần mở rộng của file (ví dụ: ".txt")
     * @return Danh sách các file
     */
    private List<File> getFilesInDirectory(String directoryPath, String extension) {
        File directory;
        
        try {
            // Thử tìm thư mục từ classpath
            java.net.URL url = ReadDataFromFile.class.getClassLoader().getResource(directoryPath);
            if (url != null) {
                directory = new File(url.toURI());
            } else {
                // Nếu không tìm thấy trong classpath, thử đường dẫn tuyệt đối
                directory = new File(directoryPath);
            }
        } catch (Exception e) {
            // Nếu có lỗi, thử đường dẫn tuyệt đối
            directory = new File(directoryPath);
        }
        
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory not found or not a directory: " + directoryPath);
            System.err.println("Absolute path: " + directory.getAbsolutePath());
            return new ArrayList<>();
        }
        
        System.out.println("Reading files from directory: " + directory.getAbsolutePath());
        
        // Lọc và sắp xếp các file theo tên
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("No files found in directory: " + directory.getAbsolutePath());
            return new ArrayList<>();
        }
        
        return Arrays.stream(files)
                .filter(file -> file.isFile() && file.getName().endsWith(extension))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy file solution tương ứng với file src
     * 
     * @param srcFile File src
     * @param solutionDir Thư mục chứa các file solution
     * @return File solution tương ứng hoặc null nếu không tìm thấy
     */
    private File getMatchingSolutionFile(File srcFile, String solutionDir) {
        String srcName = srcFile.getName();
        String baseName = srcName.substring(0, srcName.lastIndexOf('.'));
        
        // Tìm file solution có tên bắt đầu giống với file src
        File solutionDirectory;
        
        try {
            // Thử tìm thư mục từ classpath
            java.net.URL url = ReadDataFromFile.class.getClassLoader().getResource(solutionDir);
            if (url != null) {
                solutionDirectory = new File(url.toURI());
            } else {
                // Nếu không tìm thấy trong classpath, thử đường dẫn tuyệt đối
                solutionDirectory = new File(solutionDir);
            }
        } catch (Exception e) {
            // Nếu có lỗi, thử đường dẫn tuyệt đối
            solutionDirectory = new File(solutionDir);
        }
        
        if (!solutionDirectory.exists() || !solutionDirectory.isDirectory()) {
            System.err.println("Solution directory not found or not a directory: " + solutionDir);
            System.err.println("Absolute path: " + solutionDirectory.getAbsolutePath());
            return null;
        }
        
        System.out.println("Looking for solution file matching '" + baseName + "' in: " + solutionDirectory.getAbsolutePath());
        
        File[] matchingFiles = solutionDirectory.listFiles(
                file -> file.isFile() && file.getName().startsWith(baseName) && file.getName().endsWith(".txt"));
        
        if (matchingFiles != null && matchingFiles.length > 0) {
            // Sắp xếp để lấy file ngắn nhất (thường là file không có hậu tố)
            Arrays.sort(matchingFiles, Comparator.comparing(File::getName, 
                    Comparator.comparingInt(String::length)));
            System.out.println("Found matching solution file: " + matchingFiles[0].getName());
            return matchingFiles[0];
        }
        
        System.err.println("No matching solution file found for: " + baseName);
        return null;
    }
    
    /**
     * Đọc tất cả các file trong thư mục src và solution tương ứng
     * 
     * @param srcDirPath Đường dẫn thư mục chứa file src
     * @param solutionDirPath Đường dẫn thư mục chứa file solution
     * @param problemType Loại bài toán
     * @param callback Callback để xử lý sau khi đọc mỗi cặp file
     */
    public void processAllFilesInDirectory(String srcDirPath, String solutionDirPath, 
                                          ProblemType problemType, FileProcessCallback callback) {
        System.out.println("Attempting to process files from:");
        System.out.println("Source directory: " + srcDirPath);
        System.out.println("Solution directory: " + solutionDirPath);
        
        // Kiểm tra xem đường dẫn có phải là đường dẫn tương đối từ resources không
        if (!new File(srcDirPath).isAbsolute()) {
            // Thử tìm trong resources
            try {
                java.net.URL resourceUrl = ReadDataFromFile.class.getClassLoader().getResource(srcDirPath);
                if (resourceUrl != null) {
                    System.out.println("Found source directory in resources: " + resourceUrl.getPath());
                } else {
                    System.out.println("Source directory not found in resources, using as-is: " + srcDirPath);
                }
            } catch (Exception e) {
                System.out.println("Error checking resources: " + e.getMessage());
            }
        }
        
        List<File> srcFiles = getFilesInDirectory(srcDirPath, ".txt");
        if (srcFiles.isEmpty()) {
            System.err.println("No source files found in directory: " + srcDirPath);
            return;
        }
        
        System.out.println("Found " + srcFiles.size() + " source files in directory: " + srcDirPath);
        
        // Xử lý từng file src và solution tương ứng
        for (File srcFile : srcFiles) {
            try {
                // Reset dữ liệu trước khi đọc file mới
                locations = null;
                routes = null;
                
                // Đọc file src
                String srcPath = srcFile.getAbsolutePath();
                System.out.println("\nProcessing source file: " + srcFile.getName() + " (" + srcPath + ")");
                dataOfProblemFromAbsolutePath(srcPath, problemType);
                
                if (locations == null || locations.length == 0) {
                    System.err.println("Failed to read locations from: " + srcPath);
                    continue;
                }
                
                System.out.println("Successfully read " + locations.length + " locations from: " + srcFile.getName());
                
                // Tìm và đọc file solution tương ứng
                File solutionFile = getMatchingSolutionFile(srcFile, solutionDirPath);
                if (solutionFile == null) {
                    System.err.println("No matching solution file found for: " + srcFile.getName());
                    continue;
                }
                
                String solutionPath = solutionFile.getAbsolutePath();
                System.out.println("Processing solution file: " + solutionFile.getName() + " (" + solutionPath + ")");
                readSolutionFromAbsolutePath(solutionPath);
                
                if (routes == null || routes.length == 0) {
                    System.err.println("Failed to read routes from: " + solutionPath);
                    continue;
                }
                
                System.out.println("Successfully read " + routes.length + " routes from: " + solutionFile.getName());
                
                // Gọi callback để xử lý dữ liệu
                callback.process(locations, routes, srcFile.getName());
                
                // Giải phóng bộ nhớ sau khi xử lý xong
                System.gc();
                
            } catch (Exception e) {
                System.err.println("Error processing file " + srcFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Interface callback để xử lý sau khi đọc mỗi cặp file
     */
    public interface FileProcessCallback {
        void process(Location[] locations, Route[] routes, String fileName);
    }
}
