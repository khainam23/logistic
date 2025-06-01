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
import java.util.Random;
import java.util.stream.Collectors;

import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Route;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Lớp đọc dữ liệu từ file
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadDataFromFile {
    Location[] locations;
    Route[] routes;
    int maxCapacity;
    Random rd = new Random();

    /**
     * Enum định nghĩa các loại bài toán và cấu hình đọc dữ liệu tương ứng
     */
    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum ProblemType {
        // Định nghĩa chỉ số cột: [id, x, y, demand, ltw, utw, service, pickup_idx (chỉ cho PDPTW)]
        VRPTW(false, 4, 9, 0, 1, 2, 3, 4, 5, 6),  // Vehicle Routing Problem with Time Windows
        PDPTW(true, 0, 1, 2, 3, 4, 5, 6, 7, 8);   // Pickup and Delivery Problem with Time Windows
        
        boolean isPickupDelivery;
        int capacityLineIndex;
        int dataStartLineIndex;
        int[] columnIndices; // [id, x, y, demand, ltw, utw, service, pickup_idx (chỉ cho PDPTW)]
        
        ProblemType(boolean isPickupDelivery, int capacityLineIndex, int dataStartLineIndex, 
                   int... columnIndices) {
            this.isPickupDelivery = isPickupDelivery;
            this.capacityLineIndex = capacityLineIndex;
            this.dataStartLineIndex = dataStartLineIndex;
            this.columnIndices = columnIndices;
        }
        
        public int getColumnIndex(int field) {
            if (field < 0 || field >= columnIndices.length) {
                System.err.println("Warning: Trying to access column index " + field + 
                                  " but only " + columnIndices.length + " indices are defined.");
                return -1;
            }
            return columnIndices[field];
        }
    }
    
    /**
     * Cấu hình định dạng file giải pháp
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SolutionFormat {
        int headerLines = 5;
        String routePrefix = ":";
        String delimiter = "\\s+";
    }

    /**
     * Đọc dữ liệu của bài toán
     * 
     * @param filePath Đường dẫn file
     * @param problemType Loại bài toán
     */
    public void readProblemData(String filePath, ProblemType problemType) {
        try {
            Path path = resolveFilePath(filePath);
            readProblemDataFromPath(path, problemType);
        } catch (Exception e) {
            System.err.println("Error reading problem data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đọc giải pháp từ file
     * 
     * @param filePath Đường dẫn file
     */
    public void readSolution(String filePath) {
        readSolution(filePath, new SolutionFormat());
    }
    
    /**
     * Đọc giải pháp từ file với định dạng tùy chỉnh
     * 
     * @param filePath Đường dẫn file
     * @param format Định dạng file giải pháp
     */
    public void readSolution(String filePath, SolutionFormat format) {
        try {
            Path path = resolveFilePath(filePath);
            readSolutionFromPath(path, format);
        } catch (Exception e) {
            System.err.println("Error reading solution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Giải quyết đường dẫn file, hỗ trợ cả classpath và đường dẫn tuyệt đối
     * 
     * @param filePath Đường dẫn file
     * @return Path đã giải quyết
     * @throws IOException Nếu không tìm thấy file
     */
    private Path resolveFilePath(String filePath) throws IOException {
        // Thử đọc từ classpath trước
        try {
            URL resource = ReadDataFromFile.class.getClassLoader().getResource(filePath);
            if (resource != null) {
                return Paths.get(resource.toURI());
            }
        } catch (Exception e) {
            // Bỏ qua lỗi và thử đường dẫn tuyệt đối
        }
        
        // Thử đọc từ đường dẫn tuyệt đối
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        return path;
    }
    
    /**
     * Giải quyết đường dẫn thư mục, hỗ trợ cả classpath và đường dẫn tuyệt đối
     * 
     * @param directoryPath Đường dẫn thư mục
     * @return File đã giải quyết
     */
    private File resolveDirectoryPath(String directoryPath) {
        // Thử tìm thư mục từ classpath
        try {
            URL url = ReadDataFromFile.class.getClassLoader().getResource(directoryPath);
            if (url != null) {
                return new File(url.toURI());
            }
        } catch (Exception e) {
            // Bỏ qua lỗi và thử đường dẫn tuyệt đối
        }
        
        // Thử đường dẫn tuyệt đối
        return new File(directoryPath);
    }
    
    /**
     * Đọc dữ liệu bài toán từ Path
     * 
     * @param path Đường dẫn file
     * @param problemType Loại bài toán
     * @throws IOException Nếu có lỗi khi đọc file
     */
    private void readProblemDataFromPath(Path path, ProblemType problemType) throws IOException {
        List<Location> locationList = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                // Đọc dung lượng tối đa
                if (count == problemType.getCapacityLineIndex()) {
                    String[] parts = line.trim().split("\\s+");
                    maxCapacity = Integer.parseInt(parts[1]);
                    System.out.println("Max capacity: " + maxCapacity);
                }

                // Đọc thông tin địa điểm
                if (count >= problemType.getDataStartLineIndex()) {
                    String[] parts = line.trim().split("\\s+");
                    int minColumns = problemType.isPickupDelivery() ? 8 : 7;
                    
                    if (parts.length < minColumns) {
                        count++;
                        continue; // Bỏ qua dòng không đủ dữ liệu
                    }

                    try {
                        // Lấy các chỉ số cột
                        int idIdx = problemType.getColumnIndex(0);
                        int xIdx = problemType.getColumnIndex(1);
                        int yIdx = problemType.getColumnIndex(2);
                        int demandIdx = problemType.getColumnIndex(3);
                        int ltwIdx = problemType.getColumnIndex(4);
                        int utwIdx = problemType.getColumnIndex(5);
                        int serviceIdx = problemType.getColumnIndex(6);
                        
                        // Kiểm tra các chỉ số cột hợp lệ
                        if (idIdx < 0 || xIdx < 0 || yIdx < 0 || demandIdx < 0 || 
                            ltwIdx < 0 || utwIdx < 0 || serviceIdx < 0) {
                            System.err.println("Error: Invalid column indices for problem type " + problemType);
                            throw new IllegalArgumentException("Invalid column indices configuration");
                        }
                        
                        // Đọc dữ liệu từ các cột
                        // int id = Integer.parseInt(parts[idIdx]);
                        int x = Integer.parseInt(parts[xIdx]);
                        int y = Integer.parseInt(parts[yIdx]);
                        int demand = Integer.parseInt(parts[demandIdx]);
                        int ltw = Integer.parseInt(parts[ltwIdx]);
                        int utw = Integer.parseInt(parts[utwIdx]);
                        int service = Integer.parseInt(parts[serviceIdx]);

                        Location location = Location.builder()
                                .point(new Point(x, y))
                                .serviceTimePick(0)
                                .serviceTimeDeliver(service)
                                .ltw(ltw)
                                .utw(utw)
                                .build();

                        // Xử lý dữ liệu theo loại bài toán
                        if (problemType.isPickupDelivery()) {
                            if (demand < 0) {
                                location.setPick(true);
                                location.setDemandPick(Math.abs(demand));
                            } else {
                                location.setDeliver(true);
                                location.setDemandDeliver(demand);
                            }
                        } else {
                            // VRPTW và các loại bài toán khác
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
            System.out.println("Read " + locations.length + " locations from " + path);
        }
    }
    
    /**
     * Đọc giải pháp từ Path
     * 
     * @param path Đường dẫn file
     * @param format Định dạng file giải pháp
     * @throws IOException Nếu có lỗi khi đọc file
     */
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
            System.out.println("Read " + routes.length + " routes from " + path);
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
        File directory = resolveDirectoryPath(directoryPath);
        
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
        
        File solutionDirectory = resolveDirectoryPath(solutionDir);
        
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
                readProblemData(srcPath, problemType);
                
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
                readSolution(solutionPath);
                
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