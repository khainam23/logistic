package org.logistic.util;

import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích để xuất dữ liệu ra các định dạng khác nhau như Excel, TXT, CSV
 */
public class ExportDataUtil {
    private static ExportDataUtil instance;
    private String exportDirectory;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Enum định nghĩa các định dạng xuất file được hỗ trợ
     */
    public enum ExportFormat {
        EXCEL(".xlsx"),
        CSV(".csv"),
        TXT(".txt");
        
        private final String extension;
        
        ExportFormat(String extension) {
            this.extension = extension;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * Enum định nghĩa các đường dẫn mặc định cho các loại xuất dữ liệu
     */
    public enum ExportPath {
        SOLUTION("exports/solutions"),
        LOCATION("exports/locations"),
        ROUTE("exports/routes");
        
        private final String path;
        
        ExportPath(String path) {
            this.path = path;
        }
        
        public String getPath() {
            return path;
        }
    }
    
    /**
     * Constructor riêng tư để đảm bảo singleton
     */
    private ExportDataUtil() {
        this.exportDirectory = "exports";
        initExportDirectory();
    }
    
    /**
     * Lấy instance của ExportDataUtil (Singleton pattern)
     * 
     * @return Instance của ExportDataUtil
     */
    public static synchronized ExportDataUtil getInstance() {
        if (instance == null) {
            instance = new ExportDataUtil();
        }
        return instance;
    }
    
    /**
     * Thiết lập thư mục xuất dữ liệu
     * 
     * @param exportDirectory Đường dẫn thư mục xuất dữ liệu
     */
    public void setExportDirectory(String exportDirectory) {
        this.exportDirectory = exportDirectory;
        initExportDirectory();
    }
    
    /**
     * Khởi tạo thư mục xuất dữ liệu nếu chưa tồn tại
     */
    private void initExportDirectory() {
        File directory = new File(exportDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Xuất giải pháp ra file với định dạng được chỉ định
     * 
     * @param solution Giải pháp cần xuất
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @param filePath Đường dẫn file (không bao gồm phần mở rộng)
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportSolution(Solution solution, Location[] locations, ExportFormat format, String filePath) {
        try {
            String fullPath = filePath + format.getExtension();
            
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(filePath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            switch (format) {
                case EXCEL -> exportToExcel(solution, locations, fullPath);
                case CSV -> exportToCSV(solution, locations, fullPath);
                case TXT -> exportToTXT(solution, locations, fullPath);
            }
            
            System.out.println("Đã xuất dữ liệu thành công vào file: " + fullPath);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất dữ liệu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xuất giải pháp ra file với định dạng được chỉ định và tên file tự động
     * 
     * @param solution Giải pháp cần xuất
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @param directory Thư mục chứa file
     * @param prefix Tiền tố tên file
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportSolution(Solution solution, Location[] locations, ExportFormat format, String directory, String prefix) {
        // Tạo tên file với timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String fileName = directory + "/" + prefix + "_" + timestamp;
        return exportSolution(solution, locations, format, fileName);
    }
    
    /**
     * Xuất giải pháp ra file Excel
     */
    private void exportToExcel(Solution solution, Location[] locations, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet cho thông tin tổng quan
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, solution);
            
            // Tạo sheet cho từng tuyến đường
            for (int i = 0; i < solution.getRoutes().length; i++) {
                Sheet routeSheet = workbook.createSheet("Route " + (i + 1));
                createRouteSheet(routeSheet, solution.getRoutes()[i], locations);
            }
            
            // Ghi workbook ra file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Tạo sheet tổng quan cho file Excel
     */
    private void createSummarySheet(Sheet sheet, Solution solution) {
        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Solution Summary");
        
        // Tạo thông tin tổng quan
        Row fitnessRow = sheet.createRow(1);
        fitnessRow.createCell(0).setCellValue("Fitness");
        fitnessRow.createCell(1).setCellValue(solution.getFitness());
        
        Row routesRow = sheet.createRow(2);
        routesRow.createCell(0).setCellValue("Number of Routes");
        routesRow.createCell(1).setCellValue(solution.getRoutes().length);
        
        // Tạo bảng tóm tắt các tuyến đường
        Row routeHeaderRow = sheet.createRow(4);
        routeHeaderRow.createCell(0).setCellValue("Route");
        routeHeaderRow.createCell(1).setCellValue("Locations");
        routeHeaderRow.createCell(2).setCellValue("Distance");
        routeHeaderRow.createCell(3).setCellValue("Payload");
        
        for (int i = 0; i < solution.getRoutes().length; i++) {
            Route route = solution.getRoutes()[i];
            Row routeRow = sheet.createRow(5 + i);
            routeRow.createCell(0).setCellValue("Route " + (i + 1));
            routeRow.createCell(1).setCellValue(route.getIndLocations().length);
            routeRow.createCell(2).setCellValue(route.getDistance());
            routeRow.createCell(3).setCellValue(route.getMaxPayload());
        }
        
        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Tạo sheet chi tiết cho từng tuyến đường
     */
    private void createRouteSheet(Sheet sheet, Route route, Location[] locations) {
        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Location ID");
        headerRow.createCell(1).setCellValue("X");
        headerRow.createCell(2).setCellValue("Y");
        headerRow.createCell(3).setCellValue("Demand");
        headerRow.createCell(4).setCellValue("Ready Time");
        headerRow.createCell(5).setCellValue("Due Time");
        headerRow.createCell(6).setCellValue("Service Time");
        
        // Tạo dữ liệu cho từng địa điểm trong tuyến đường
        int[] locationIndices = route.getIndLocations();
        for (int i = 0; i < locationIndices.length; i++) {
            int locationIndex = locationIndices[i];
            if (locationIndex >= locations.length) continue; // Bỏ qua nếu chỉ số không hợp lệ
            
            Location location = locations[locationIndex];
            Row locationRow = sheet.createRow(i + 1);
            locationRow.createCell(0).setCellValue(locationIndex);
            locationRow.createCell(1).setCellValue(location.getX());
            locationRow.createCell(2).setCellValue(location.getY());
            locationRow.createCell(3).setCellValue(location.getDemand());
            locationRow.createCell(4).setCellValue(location.getReadyTime());
            locationRow.createCell(5).setCellValue(location.getDueTime());
            locationRow.createCell(6).setCellValue(location.getServiceTime());
        }
        
        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Xuất giải pháp ra file CSV
     */
    private void exportToCSV(Solution solution, Location[] locations, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Ghi thông tin tổng quan
            writer.writeNext(new String[]{"Solution Summary"});
            writer.writeNext(new String[]{"Fitness", String.valueOf(solution.getFitness())});
            writer.writeNext(new String[]{"Number of Routes", String.valueOf(solution.getRoutes().length)});
            writer.writeNext(new String[]{});
            
            // Ghi thông tin từng tuyến đường
            for (int i = 0; i < solution.getRoutes().length; i++) {
                Route route = solution.getRoutes()[i];
                writer.writeNext(new String[]{"Route " + (i + 1)});
                writer.writeNext(new String[]{"Distance", String.valueOf(route.getDistance())});
                writer.writeNext(new String[]{"Payload", String.valueOf(route.getMaxPayload())});
                
                // Header cho các địa điểm
                writer.writeNext(new String[]{"Location ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"});
                
                // Dữ liệu cho từng địa điểm trong tuyến đường
                int[] locationIndices = route.getIndLocations();
                for (int locationIndex : locationIndices) {
                    if (locationIndex >= locations.length) continue; // Bỏ qua nếu chỉ số không hợp lệ
                    
                    Location location = locations[locationIndex];
                    writer.writeNext(new String[]{
                        String.valueOf(locationIndex),
                        String.valueOf(location.getX()),
                        String.valueOf(location.getY()),
                        String.valueOf(location.getDemand()),
                        String.valueOf(location.getReadyTime()),
                        String.valueOf(location.getDueTime()),
                        String.valueOf(location.getServiceTime())
                    });
                }
                
                writer.writeNext(new String[]{});
            }
        }
    }
    
    /**
     * Xuất giải pháp ra file TXT
     */
    private void exportToTXT(Solution solution, Location[] locations, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Thông tin tổng quan
        lines.add("Solution Summary");
        lines.add("Fitness: " + solution.getFitness());
        lines.add("Number of Routes: " + solution.getRoutes().length);
        lines.add("");
        
        // Thông tin từng tuyến đường
        for (int i = 0; i < solution.getRoutes().length; i++) {
            Route route = solution.getRoutes()[i];
            lines.add("Route " + (i + 1));
            lines.add("Distance: " + route.getDistance());
            lines.add("Payload: " + route.getMaxPayload());
            lines.add("Locations: " + route.getIndLocations().length);
            
            // Danh sách các địa điểm
            lines.add("Location Path: ");
            StringBuilder path = new StringBuilder();
            for (int locationIndex : route.getIndLocations()) {
                path.append(locationIndex).append(" -> ");
            }
            if (path.length() > 4) {
                path.setLength(path.length() - 4); // Xóa " -> " cuối cùng
            }
            lines.add(path.toString());
            
            // Chi tiết từng địa điểm
            lines.add("Location Details:");
            lines.add(String.format("%-10s %-10s %-10s %-10s %-12s %-10s %-12s", 
                    "ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"));
            
            for (int locationIndex : route.getIndLocations()) {
                if (locationIndex >= locations.length) continue; // Bỏ qua nếu chỉ số không hợp lệ
                
                Location location = locations[locationIndex];
                lines.add(String.format("%-10d %-10.2f %-10.2f %-10d %-12d %-10d %-12d",
                        locationIndex,
                        location.getX(),
                        location.getY(),
                        location.getDemand(),
                        location.getReadyTime(),
                        location.getDueTime(),
                        location.getServiceTime()));
            }
            
            lines.add("");
        }
        
        // Ghi ra file
        Files.write(Paths.get(filePath), lines);
    }
    
    /**
     * Xuất giải pháp ra file với định dạng được chỉ định và đường dẫn mặc định
     * 
     * @param solution Giải pháp cần xuất
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportSolution(Solution solution, Location[] locations, ExportFormat format) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filePath = ExportPath.SOLUTION.getPath() + "/solution_" + timestamp;
        return exportSolution(solution, locations, format, filePath);
    }
    
    /**
     * Xuất dữ liệu vị trí ra file với định dạng được chỉ định
     * 
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @param filePath Đường dẫn file (không bao gồm phần mở rộng)
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportLocations(Location[] locations, ExportFormat format, String filePath) {
        try {
            String fullPath = filePath + format.getExtension();
            
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(filePath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            switch (format) {
                case EXCEL -> exportLocationsToExcel(locations, fullPath);
                case CSV -> exportLocationsToCSV(locations, fullPath);
                case TXT -> exportLocationsToTXT(locations, fullPath);
            }
            
            System.out.println("Đã xuất dữ liệu vị trí thành công vào file: " + fullPath);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất dữ liệu vị trí: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xuất dữ liệu vị trí ra file với định dạng được chỉ định và đường dẫn mặc định
     * 
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportLocations(Location[] locations, ExportFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = ExportPath.LOCATION.getPath() + "/locations_" + timestamp;
        return exportLocations(locations, format, filePath);
    }
    
    /**
     * Xuất dữ liệu vị trí ra file Excel
     */
    private void exportLocationsToExcel(Location[] locations, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Locations");
            
            // Tạo header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Location ID");
            headerRow.createCell(1).setCellValue("X");
            headerRow.createCell(2).setCellValue("Y");
            headerRow.createCell(3).setCellValue("Demand");
            headerRow.createCell(4).setCellValue("Ready Time");
            headerRow.createCell(5).setCellValue("Due Time");
            headerRow.createCell(6).setCellValue("Service Time");
            
            // Tạo dữ liệu cho từng địa điểm
            for (int i = 0; i < locations.length; i++) {
                Location location = locations[i];
                Row locationRow = sheet.createRow(i + 1);
                locationRow.createCell(0).setCellValue(i);
                locationRow.createCell(1).setCellValue(location.getX());
                locationRow.createCell(2).setCellValue(location.getY());
                locationRow.createCell(3).setCellValue(location.getDemand());
                locationRow.createCell(4).setCellValue(location.getReadyTime());
                locationRow.createCell(5).setCellValue(location.getDueTime());
                locationRow.createCell(6).setCellValue(location.getServiceTime());
            }
            
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Ghi workbook ra file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Xuất dữ liệu vị trí ra file CSV
     */
    private void exportLocationsToCSV(Location[] locations, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Header
            writer.writeNext(new String[]{"Location ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"});
            
            // Dữ liệu cho từng địa điểm
            for (int i = 0; i < locations.length; i++) {
                Location location = locations[i];
                writer.writeNext(new String[]{
                    String.valueOf(i),
                    String.valueOf(location.getX()),
                    String.valueOf(location.getY()),
                    String.valueOf(location.getDemand()),
                    String.valueOf(location.getReadyTime()),
                    String.valueOf(location.getDueTime()),
                    String.valueOf(location.getServiceTime())
                });
            }
        }
    }
    
    /**
     * Xuất dữ liệu vị trí ra file TXT
     */
    private void exportLocationsToTXT(Location[] locations, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Header
        lines.add("Locations Data");
        lines.add(String.format("%-10s %-10s %-10s %-10s %-12s %-10s %-12s", 
                "ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"));
        
        // Dữ liệu cho từng địa điểm
        for (int i = 0; i < locations.length; i++) {
            Location location = locations[i];
            lines.add(String.format("%-10d %-10.2f %-10.2f %-10d %-12d %-10d %-12d",
                    i,
                    location.getX(),
                    location.getY(),
                    location.getDemand(),
                    location.getReadyTime(),
                    location.getDueTime(),
                    location.getServiceTime()));
        }
        
        // Ghi ra file
        Files.write(Paths.get(filePath), lines);
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file với định dạng được chỉ định
     * 
     * @param routes Mảng các tuyến đường
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @param filePath Đường dẫn file (không bao gồm phần mở rộng)
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportRoutes(Route[] routes, Location[] locations, ExportFormat format, String filePath) {
        try {
            String fullPath = filePath + format.getExtension();
            
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(filePath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            switch (format) {
                case EXCEL -> exportRoutesToExcel(routes, locations, fullPath);
                case CSV -> exportRoutesToCSV(routes, locations, fullPath);
                case TXT -> exportRoutesToTXT(routes, locations, fullPath);
            }
            
            System.out.println("Đã xuất dữ liệu tuyến đường thành công vào file: " + fullPath);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất dữ liệu tuyến đường: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file với định dạng được chỉ định và đường dẫn mặc định
     * 
     * @param routes Mảng các tuyến đường
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportRoutes(Route[] routes, Location[] locations, ExportFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = ExportPath.ROUTE.getPath() + "/routes_" + timestamp;
        return exportRoutes(routes, locations, format, filePath);
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file với định dạng được chỉ định, đường dẫn và tiền tố tùy chỉnh
     * 
     * @param routes Mảng các tuyến đường
     * @param locations Mảng các địa điểm
     * @param format Định dạng xuất file
     * @param directory Thư mục chứa file
     * @param prefix Tiền tố tên file
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportRoutes(Route[] routes, Location[] locations, ExportFormat format, String directory, String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = directory + "/" + prefix + "_" + timestamp;
        return exportRoutes(routes, locations, format, filePath);
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file Excel
     */
    private void exportRoutesToExcel(Route[] routes, Location[] locations, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet tổng quan
            Sheet summarySheet = workbook.createSheet("Summary");
            Row headerRow = summarySheet.createRow(0);
            headerRow.createCell(0).setCellValue("Route");
            headerRow.createCell(1).setCellValue("Locations");
            headerRow.createCell(2).setCellValue("Distance");
            headerRow.createCell(3).setCellValue("Payload");
            
            for (int i = 0; i < routes.length; i++) {
                Route route = routes[i];
                Row routeRow = summarySheet.createRow(i + 1);
                routeRow.createCell(0).setCellValue("Route " + (i + 1));
                routeRow.createCell(1).setCellValue(route.getIndLocations().length);
                routeRow.createCell(2).setCellValue(route.getDistance());
                routeRow.createCell(3).setCellValue(route.getMaxPayload());
            }
            
            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < 4; i++) {
                summarySheet.autoSizeColumn(i);
            }
            
            // Tạo sheet cho từng tuyến đường
            for (int i = 0; i < routes.length; i++) {
                Sheet routeSheet = workbook.createSheet("Route " + (i + 1));
                createRouteSheet(routeSheet, routes[i], locations);
            }
            
            // Ghi workbook ra file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file CSV
     */
    private void exportRoutesToCSV(Route[] routes, Location[] locations, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Ghi thông tin tổng quan
            writer.writeNext(new String[]{"Routes Summary"});
            writer.writeNext(new String[]{"Number of Routes", String.valueOf(routes.length)});
            writer.writeNext(new String[]{});
            
            // Ghi thông tin từng tuyến đường
            for (int i = 0; i < routes.length; i++) {
                Route route = routes[i];
                writer.writeNext(new String[]{"Route " + (i + 1)});
                writer.writeNext(new String[]{"Distance", String.valueOf(route.getDistance())});
                writer.writeNext(new String[]{"Payload", String.valueOf(route.getMaxPayload())});
                
                // Header cho các địa điểm
                writer.writeNext(new String[]{"Location ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"});
                
                // Dữ liệu cho từng địa điểm trong tuyến đường
                int[] locationIndices = route.getIndLocations();
                for (int locationIndex : locationIndices) {
                    if (locationIndex >= locations.length) continue; // Bỏ qua nếu chỉ số không hợp lệ
                    
                    Location location = locations[locationIndex];
                    writer.writeNext(new String[]{
                        String.valueOf(locationIndex),
                        String.valueOf(location.getX()),
                        String.valueOf(location.getY()),
                        String.valueOf(location.getDemand()),
                        String.valueOf(location.getReadyTime()),
                        String.valueOf(location.getDueTime()),
                        String.valueOf(location.getServiceTime())
                    });
                }
                
                writer.writeNext(new String[]{});
            }
        }
    }
    
    /**
     * Xuất dữ liệu tuyến đường ra file TXT
     */
    private void exportRoutesToTXT(Route[] routes, Location[] locations, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Thông tin tổng quan
        lines.add("Routes Summary");
        lines.add("Number of Routes: " + routes.length);
        lines.add("");
        
        // Thông tin từng tuyến đường
        for (int i = 0; i < routes.length; i++) {
            Route route = routes[i];
            lines.add("Route " + (i + 1));
            lines.add("Distance: " + route.getDistance());
            lines.add("Payload: " + route.getMaxPayload());
            lines.add("Locations: " + route.getIndLocations().length);
            
            // Danh sách các địa điểm
            lines.add("Location Path: ");
            StringBuilder path = new StringBuilder();
            for (int locationIndex : route.getIndLocations()) {
                path.append(locationIndex).append(" -> ");
            }
            if (path.length() > 4) {
                path.setLength(path.length() - 4); // Xóa " -> " cuối cùng
            }
            lines.add(path.toString());
            
            // Chi tiết từng địa điểm
            lines.add("Location Details:");
            lines.add(String.format("%-10s %-10s %-10s %-10s %-12s %-10s %-12s", 
                    "ID", "X", "Y", "Demand", "Ready Time", "Due Time", "Service Time"));
            
            for (int locationIndex : route.getIndLocations()) {
                if (locationIndex >= locations.length) continue; // Bỏ qua nếu chỉ số không hợp lệ
                
                Location location = locations[locationIndex];
                lines.add(String.format("%-10d %-10.2f %-10.2f %-10d %-12d %-10d %-12d",
                        locationIndex,
                        location.getX(),
                        location.getY(),
                        location.getDemand(),
                        location.getReadyTime(),
                        location.getDueTime(),
                        location.getServiceTime()));
            }
            
            lines.add("");
        }
        
        // Ghi ra file
        Files.write(Paths.get(filePath), lines);
    }
    
    /**
     * Tạo tên file với timestamp
     * 
     * @param directory Thư mục chứa file
     * @param prefix Tiền tố tên file
     * @return Đường dẫn file đầy đủ (không bao gồm phần mở rộng)
     */
    private String createFileNameWithTimestamp(String directory, String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return directory + "/" + prefix + "_" + timestamp;
    }
    
    /**
     * Đảm bảo thư mục tồn tại
     * 
     * @param directory Đường dẫn thư mục
     */
    private void ensureDirectoryExists(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Xuất dữ liệu tùy chỉnh ra file
     * 
     * @param data Dữ liệu cần xuất (dạng chuỗi)
     * @param format Định dạng xuất file
     * @param filePath Đường dẫn file (không bao gồm phần mở rộng)
     * @return true nếu xuất thành công, false nếu có lỗi
     */
    public boolean exportCustomData(String data, ExportFormat format, String filePath) {
        try {
            String fullPath = filePath + format.getExtension();
            
            // Tạo thư mục nếu chưa tồn tại
            File directory = new File(filePath).getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            // Ghi dữ liệu ra file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath))) {
                writer.write(data);
            }
            
            System.out.println("Đã xuất dữ liệu tùy chỉnh thành công vào file: " + fullPath);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi xuất dữ liệu tùy chỉnh: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Đóng tất cả các tài nguyên nếu cần
     * Phương thức này có thể được gọi khi ứng dụng kết thúc
     */
    public void close() {
        // Hiện tại không có tài nguyên cần đóng
        // Nhưng giữ phương thức này để tương thích với WriteLogUtil
        System.out.println("ExportDataUtil đã đóng tất cả các tài nguyên");
    }
}
