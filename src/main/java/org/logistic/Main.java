package org.logistic;

import org.logistic.algorithm.Optimizer;
import org.logistic.algorithm.aco.AntColonyOptimization;
import org.logistic.algorithm.gwo.GreyWolfOptimizer;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.*;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    enum Algorithm {SHO, ACO, GWO, SA}
    enum ExportType {NONE, EXCEL, CSV, TXT, ALL}

    // Cấu trúc việc chạy chương trình
    public static void main(String[] args) {
        // Thiết lập thuật toán mặc định và định dạng xuất mặc định
        Algorithm algorithm = Algorithm.SHO;
        ExportType exportType = ExportType.EXCEL;
        String dataLocation = "data/pdptw/src/lc101.txt";
        String dataSolution = "data/pdptw/solution/lc101.txt";
        
        // Kiểm tra tham số dòng lệnh để chọn thuật toán
        if (args.length > 0) {
            try {
                algorithm = Algorithm.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Thuật toán không hợp lệ. Sử dụng SHO, ACO, GWO hoặc SA.");
                System.out.println("Sử dụng thuật toán mặc định: " + algorithm);
            }
        }
        
        // Kiểm tra tham số dòng lệnh để chọn định dạng xuất
        if (args.length > 1) {
            try {
                exportType = ExportType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Định dạng xuất không hợp lệ. Sử dụng NONE, EXCEL, CSV, TXT hoặc ALL.");
                System.out.println("Sử dụng định dạng xuất mặc định: " + exportType);
            }
        }
        
        // Kiểm tra tham số dòng lệnh để chọn file dữ liệu
        if (args.length > 2) {
            dataLocation = args[2];
            System.out.println("Sử dụng file dữ liệu: " + dataLocation);
        }
        
        // Kiểm tra tham số dòng lệnh để chọn file giải pháp
        if (args.length > 3) {
            dataSolution = args[3];
            System.out.println("Sử dụng file giải pháp: " + dataSolution);
        }
         
        // Thiết lập công cụ
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();
        ExportDataUtil exportData = ExportDataUtil.getInstance();

        // Đọc dữ liệu
        System.out.println("Đang đọc dữ liệu từ file: " + dataLocation);
        Location[] locations = readData(rdff, dataLocation, ReadDataFromFile.ProblemType.PDPTW);
        if (locations.length == 0) {
            System.err.println("Không thể đọc dữ liệu từ file: " + dataLocation);
            return;
        }
        
        System.out.println("Đang đọc giải pháp từ file: " + dataSolution);
        Route[] routes = readSolution(rdff, dataSolution);
        if (routes.length == 0) {
            System.err.println("Không thể đọc giải pháp từ file: " + dataSolution);
            return;
        }

        // Tạo giải pháp đầu tiên
        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));

        // Tạo đa giải pháp sử dụng SimulatedAnnealing
        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
        Solution[] initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil, locations, routes[0].getMaxPayload());
        
        // Tạo đối tượng thuật toán tối ưu hóa dựa trên lựa chọn
        Optimizer optimizer;
        
        switch (algorithm) {
            case ACO:
                // Sử dụng thuật toán Ant Colony Optimization
                System.out.println("Đang chạy thuật toán Ant Colony Optimization (ACO)...");
                optimizer = new AntColonyOptimization(writeLogUtil);
                break;

            case GWO:
                // Sử dụng thuật toán Grey Wolf Optimizer
                System.out.println("Đang chạy thuật toán Grey Wolf Optimizer (GWO)...");
                optimizer = new GreyWolfOptimizer(writeLogUtil);
                break;
                
            case SA:
                // Sử dụng thuật toán Simulated Annealing
                System.out.println("Đang chạy thuật toán Simulated Annealing (SA)...");
                optimizer = sa; // Sử dụng lại đối tượng SA đã tạo
                break;

            case SHO:
            default:
                // Sử dụng thuật toán Spotted Hyena Optimizer
                System.out.println("Đang chạy thuật toán Spotted Hyena Optimizer (SHO)...");
                optimizer = new SpottedHyenaOptimizer(writeLogUtil);
                break;
        }
        
        // Chạy thuật toán tối ưu hóa đã chọn
        Solution optimizedSolution = optimizer.run(
            initialSolutions, 
            fitnessUtil, 
            checkConditionUtil, 
            locations, 
            routes[0].getMaxPayload()
        );
        
        writeLogUtil.info(algorithm + " completed with fitness: " + optimizedSolution.getFitness());

        // In ra kết quả tối ưu
        printUtil.printSolution(optimizedSolution);
        writeLogUtil.info("Final optimized solution fitness: " + optimizedSolution.getFitness());
        
        // Xuất dữ liệu nếu được yêu cầu
        if (exportType != ExportType.NONE) {
            // Tạo thư mục exports nếu chưa tồn tại
            String exportDir = "exports";
            new File(exportDir).mkdirs();
            
            // Tạo tên file dựa trên thuật toán và thời gian
            String filePrefix = algorithm.toString().toLowerCase();
            
            System.out.println("Đang xuất dữ liệu...");
            
            // Xuất dữ liệu theo định dạng được chọn
            if (exportType == ExportType.ALL || exportType == ExportType.EXCEL) {
                System.out.println("Xuất ra file Excel...");
                exportData.exportSolution(optimizedSolution, locations, ExportDataUtil.ExportFormat.EXCEL, 
                        exportDir + "/" + filePrefix, filePrefix);
            }
            
            if (exportType == ExportType.ALL || exportType == ExportType.CSV) {
                System.out.println("Xuất ra file CSV...");
                exportData.exportSolution(optimizedSolution, locations, ExportDataUtil.ExportFormat.CSV, 
                        exportDir + "/" + filePrefix, filePrefix);
            }
            
            if (exportType == ExportType.ALL || exportType == ExportType.TXT) {
                System.out.println("Xuất ra file TXT...");
                exportData.exportSolution(optimizedSolution, locations, ExportDataUtil.ExportFormat.TXT, 
                        exportDir + "/" + filePrefix, filePrefix);
            }
            
            // Xuất dữ liệu vị trí và tuyến đường nếu xuất tất cả
            if (exportType == ExportType.ALL) {
                System.out.println("Xuất dữ liệu vị trí...");
                exportData.exportLocations(locations, ExportDataUtil.ExportFormat.EXCEL, 
                        exportDir + "/locations");
                
                System.out.println("Xuất dữ liệu tuyến đường...");
                exportData.exportRoutes(optimizedSolution.getRoutes(), locations, ExportDataUtil.ExportFormat.EXCEL, 
                        exportDir + "/routes");
            }
            
            System.out.println("Xuất dữ liệu hoàn tất. Các file được lưu trong thư mục: " + exportDir);
        }

        // Đóng các util nếu có
        writeLogUtil.close();
    }


    /**
     * Đọc dữ liệu từ file
     * 
     * @param rdff Đối tượng ReadDataFromFile
     * @param filePath Đường dẫn file
     * @param problemType Loại bài toán
     * @return Mảng các địa điểm
     */
    public static Location[] readData(ReadDataFromFile rdff, String filePath, ReadDataFromFile.ProblemType problemType) {
        try {
            // Thử đọc từ classpath
            rdff.dataOfProblem(filePath, problemType);
            
            // Nếu không có dữ liệu, thử đọc từ đường dẫn tuyệt đối
            if (rdff.getLocations() == null || rdff.getLocations().length == 0) {
                rdff.dataOfProblemFromAbsolutePath(filePath, problemType);
            }
            
            return rdff.getLocations();
        } catch (Exception e) {
            System.err.println("Error reading data: " + e.getMessage());
            e.printStackTrace();
            return new Location[0];
        }
    }

    /**
     * Đọc giải pháp từ file
     * 
     * @param rdff Đối tượng ReadDataFromFile
     * @param filePath Đường dẫn file
     * @return Mảng các tuyến đường
     */
    public static Route[] readSolution(ReadDataFromFile rdff, String filePath) {
        try {
            // Thử đọc từ classpath
            rdff.readSolution(filePath);
            
            // Nếu không có dữ liệu, thử đọc từ đường dẫn tuyệt đối
            if (rdff.getRoutes() == null || rdff.getRoutes().length == 0) {
                rdff.readSolutionFromAbsolutePath(filePath);
            }
            
            // Tính khoảng cách cho mỗi tuyến đường
            if (rdff.getRoutes() != null && rdff.getLocations() != null) {
                for (Route route : rdff.getRoutes()) {
                    route.calculateDistance(rdff.getLocations());
                }
            }
            
            return rdff.getRoutes();
        } catch (Exception e) {
            System.err.println("Error reading solution: " + e.getMessage());
            e.printStackTrace();
            return new Route[0];
        }
    }
}