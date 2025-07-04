package org.logistic.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.logistic.Main.Algorithm;
import org.logistic.Main.ExportType;
import org.logistic.algorithm.sa.SimulatedAnnealing;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

public class RLUtil {
    
    /**
     * Lấy đường dẫn thư mục exports tự động
     */
    private static String getExportsDirectory() {
        // Lấy thư mục hiện tại của project
        String currentDir = System.getProperty("user.dir");
        File exportsDir = new File(currentDir, "exports");
        
        // Tạo thư mục exports nếu chưa tồn tại
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }
        
        return exportsDir.getAbsolutePath();
    }
    
    /**
     * Xử lý tăng cường.
     * 
     * + Chạy các thuật toán trong thư mục như bình thường
     * + Nhưng lặp qua các epoch, epoch sau sẽ lấy thông tin solution tốt nhất từ epoch trước đó làm init solution
     * + Mỗi epoch có iterator lần thực hiện, mỗi lần chạy tất cả 4 thuật toán
     * + Mỗi iterator ghi file riêng và cập nhật global best solution
     * + Cuối cùng ghi global best solution tổng thể
     * 
     * @param epoch số epoch cần thực hiện
     * @param iterator số iterator trong mỗi epoch (số lần chạy tất cả 4 thuật toán)
     * @param iterations số iterations cho mỗi lần chạy thuật toán
     */
    public static void processRL(String srcDirectory, String solutionDirectory,
            ReadDataFromFile rdff, FitnessUtil fitnessUtil, PrintUtil printUtil,
            CheckConditionUtil checkConditionUtil, ReadDataFromFile.ProblemType problemType,
            FitnessStrategy strategy, ExportType exportType, int iterations, boolean parallelEnabled, int iterator, int epoch) {

        System.out.println("\n=== BẮT ĐẦU XỬ LÝ TĂNG CƯỜNG (RL) ===");
        System.out.println("Thư mục src: " + srcDirectory);
        System.out.println("Thư mục solution: " + solutionDirectory);
        System.out.println("Số epoch: " + epoch);
        System.out.println("Số vòng lặp mỗi epoch: " + iterator);
        System.out.println("Số iterations mỗi thuật toán: " + iterations);
        System.out.println("Chế độ song song: " + (parallelEnabled ? "BẬT" : "TẮT"));

        // Lưu trữ kết quả của từng epoch
        List<EpochResult> epochResults = new ArrayList<>();
        
        // Xử lý từng file trong thư mục
        rdff.processAllFilesInDirectory(srcDirectory, solutionDirectory, problemType,
                (locations, routes, fileName) -> {
                    try {
                        System.out.println("\n=== XỬ LÝ FILE: " + fileName + " ===");
                        
                        // Khởi tạo solution ban đầu cho epoch đầu tiên
                        Solution currentBestSolution = new Solution(routes, 
                            fitnessUtil.calculatorFitness(routes, locations, parallelEnabled));
                        
                        List<EpochResult> fileEpochResults = new ArrayList<>();
                        
                        // Chạy qua từng epoch
                        for (int currentEpoch = 1; currentEpoch <= epoch; currentEpoch++) {
                            System.out.println("\n--- EPOCH " + currentEpoch + "/" + epoch + " cho file " + fileName + " ---");
                            
                            // Tạo initial solutions cho epoch hiện tại
                            Solution[] initialSolutions;
                            if (currentEpoch == 1) {
                                // Epoch đầu tiên: sử dụng SA để tạo population từ solution gốc
                                SimulatedAnnealing sa = new SimulatedAnnealing(currentBestSolution);
                                initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil,
                                        locations, routes[0].getMaxPayload());
                            } else {
                                // Epoch tiếp theo: sử dụng best solution từ epoch trước để tạo population
                                SimulatedAnnealing sa = new SimulatedAnnealing(currentBestSolution);
                                initialSolutions = sa.runAndGetPopulation(fitnessUtil, checkConditionUtil,
                                        locations, routes[0].getMaxPayload());
                                System.out.println("Sử dụng solution tốt nhất từ epoch trước (fitness: " + 
                                    currentBestSolution.getFitness() + ") làm cơ sở cho epoch " + currentEpoch);
                            }
                            
                            Solution epochBestSolution = null;
                            List<IteratorResult> epochIteratorResults = new ArrayList<>();
                            
                            // Chạy iterator lần trong mỗi epoch
                            for (int iter = 1; iter <= iterator; iter++) {
                                System.out.println("\n  --- Iterator " + iter + "/" + iterator + " trong epoch " + currentEpoch + " ---");
                                
                                // Chạy tất cả các thuật toán cho iterator hiện tại
                                Map<Algorithm, Solution> iteratorResults = runAllOptimizersForIterator(
                                    initialSolutions, fitnessUtil, checkConditionUtil, locations,
                                    routes[0].getMaxPayload(), iterations, parallelEnabled);
                                
                                // Tìm solution tốt nhất trong iterator này
                                Solution iteratorBestSolution = findBestSolution(iteratorResults);
                                
                                // Lưu kết quả iterator
                                epochIteratorResults.add(new IteratorResult(iter, iteratorBestSolution, iteratorResults));
                                
                                // Ghi file cho iterator này
                                if (iteratorBestSolution != null) {
                                    writeIteratorResultToFile(fileName, currentEpoch, iter, iteratorBestSolution, exportType);
                                }
                                
                                // Cập nhật best solution trong epoch
                                if (iteratorBestSolution != null && 
                                    (epochBestSolution == null || iteratorBestSolution.getFitness() < epochBestSolution.getFitness())) {
                                    epochBestSolution = iteratorBestSolution.copy();
                                    System.out.println("  ✓ Cập nhật best solution trong epoch: fitness = " + epochBestSolution.getFitness());
                                }
                                
                                // Cập nhật global best solution
                                if (iteratorBestSolution != null && 
                                    (currentBestSolution == null || iteratorBestSolution.getFitness() < currentBestSolution.getFitness())) {
                                    currentBestSolution = iteratorBestSolution.copy();
                                    System.out.println("  ✓✓ Cập nhật GLOBAL best solution: fitness = " + currentBestSolution.getFitness());
                                }
                                
                                System.out.println("  Hoàn thành iterator " + iter + 
                                    " - Best fitness: " + (iteratorBestSolution != null ? iteratorBestSolution.getFitness() : "N/A"));
                            }
                            
                            // Global best đã được cập nhật trong từng iterator
                            
                            // Lưu kết quả epoch với thông tin chi tiết về các iterator
                            EpochResult epochResult = new EpochResult(currentEpoch, fileName, 
                                epochBestSolution != null ? epochBestSolution.copy() : null, epochIteratorResults);
                            fileEpochResults.add(epochResult);
                            
                            System.out.println("Hoàn thành epoch " + currentEpoch + 
                                " - Best fitness: " + (epochBestSolution != null ? epochBestSolution.getFitness() : "N/A"));
                        }
                        
                        // Lưu kết quả của file này
                        epochResults.addAll(fileEpochResults);
                        
                        // In kết quả tổng kết cho file
                        printFileRLResults(fileName, fileEpochResults, currentBestSolution);
                        
                        System.out.println("=== HOÀN THÀNH XỬ LÝ FILE: " + fileName + " ===\n");
                        
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý RL cho file " + fileName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
        
        // Ghi kết quả ra file riêng cho từng file input
        writeRLResultsToFiles(epochResults, exportType);
        
        // Ghi global best solution cho tất cả các file
        writeGlobalBestSolution(epochResults, exportType);
        
        System.out.println("\n=== HOÀN THÀNH XỬ LÝ TĂNG CƯỜNG (RL) ===");
    }
    
    /**
     * Chạy tất cả các thuật toán cho một iterator trong epoch
     */
    private static Map<Algorithm, Solution> runAllOptimizersForIterator(
            Solution[] initialSolutions, FitnessUtil fitnessUtil, 
            CheckConditionUtil checkConditionUtil, Location[] locations, 
            int maxPayload, int iterations, boolean parallelEnabled) {
        
        Map<Algorithm, Solution> results = new HashMap<>();
        Algorithm[] algorithms = Algorithm.values();
        
        for (Algorithm algorithm : algorithms) {
            try {
                System.out.println("    Chạy thuật toán: " + algorithm);
                
                org.logistic.algorithm.Optimizer optimizer = ExecutionUtil.createOptimizer(algorithm);
                if (optimizer == null) {
                    System.err.println("    Không thể tạo optimizer cho thuật toán: " + algorithm);
                    results.put(algorithm, null);
                    continue;
                }
                
                // Chạy thuật toán một lần cho iterator này
                Solution currentSolution = optimizer.run(initialSolutions, fitnessUtil, 
                        checkConditionUtil, locations, maxPayload);
                
                results.put(algorithm, currentSolution);
                
                if (currentSolution != null) {
                    System.out.println("    " + algorithm + " - fitness: " + currentSolution.getFitness());
                } else {
                    System.out.println("    " + algorithm + " - không có solution");
                }
                
            } catch (Exception e) {
                System.err.println("    Lỗi khi chạy thuật toán " + algorithm + ": " + e.getMessage());
                results.put(algorithm, null);
            }
        }
        
        return results;
    }
    
    /**
     * Tìm solution tốt nhất từ kết quả các thuật toán
     */
    private static Solution findBestSolution(Map<Algorithm, Solution> results) {
        Solution bestSolution = null;
        
        for (Solution solution : results.values()) {
            if (solution != null && (bestSolution == null || solution.getFitness() < bestSolution.getFitness())) {
                bestSolution = solution;
            }
        }
        
        return bestSolution;
    }
    
    /**
     * In kết quả RL cho một file
     */
    private static void printFileRLResults(String fileName, List<EpochResult> epochResults, Solution finalBestSolution) {
        System.out.println("\n=== KẾT QUẢ RL CHO FILE: " + fileName + " ===");
        
        for (EpochResult epochResult : epochResults) {
            System.out.printf("Epoch %d: Best fitness = %.2f\n", 
                epochResult.epochNumber, 
                epochResult.bestSolution != null ? epochResult.bestSolution.getFitness() : Double.MAX_VALUE);
            
            // In chi tiết từng iterator trong epoch
            if (epochResult.iteratorResults != null && !epochResult.iteratorResults.isEmpty()) {
                for (IteratorResult iterResult : epochResult.iteratorResults) {
                    System.out.printf("  Iterator %d: Best fitness = %.2f", 
                        iterResult.iteratorNumber,
                        iterResult.bestSolution != null ? iterResult.bestSolution.getFitness() : Double.MAX_VALUE);
                    
                    // In kết quả từng thuật toán trong iterator
                    if (iterResult.algorithmResults != null && !iterResult.algorithmResults.isEmpty()) {
                        System.out.print(" [");
                        boolean first = true;
                        for (Map.Entry<Algorithm, Solution> entry : iterResult.algorithmResults.entrySet()) {
                            if (!first) System.out.print(", ");
                            System.out.printf("%s: %.2f", 
                                entry.getKey().name(),
                                entry.getValue() != null ? entry.getValue().getFitness() : Double.MAX_VALUE);
                            first = false;
                        }
                        System.out.print("]");
                    }
                    System.out.println();
                }
            }
            System.out.println(); // Dòng trống giữa các epoch
        }
        
        System.out.printf("Solution cuối cùng tốt nhất: fitness = %.2f\n", 
            finalBestSolution != null ? finalBestSolution.getFitness() : Double.MAX_VALUE);
    }
    
    /**
     * Ghi kết quả RL ra file riêng cho từng epoch của từng file input - chỉ ghi routes
     */
    private static void writeRLResultsToFiles(List<EpochResult> epochResults, ExportType exportType) {
        if (exportType == ExportType.NONE) {
            return;
        }
        
        // Ghi file riêng cho từng epoch của từng file input
        for (EpochResult epochResult : epochResults) {
            try {
                // Tạo tên file output dựa trên tên file input và epoch number
                String baseFileName = epochResult.fileName;
                if (baseFileName.contains(".")) {
                    baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
                }
                String outputFileName = String.format("%s%srl_results_%s_epoch%d.txt", 
                    getExportsDirectory(), File.separator, baseFileName, epochResult.epochNumber);
                
                FileWriter writer = new FileWriter(outputFileName);
                
                // Ghi routes của solution tốt nhất trong epoch - chỉ routes, không có điểm số
                if (epochResult.bestSolution != null && epochResult.bestSolution.getRoutes() != null) {
                    Route[] routes = epochResult.bestSolution.getRoutes();
                    int routeNumber = 1;
                    
                    for (Route route : routes) {
                        if (route != null && route.getIndLocations() != null && route.getIndLocations().length > 0) {
                            writer.write("Route " + routeNumber + ": ");
                            
                            // Ghi các điểm trong route, bỏ qua điểm 0 (depot)
                            boolean first = true;
                            for (int location : route.getIndLocations()) {
                                if (location != 0) { // Bỏ qua điểm 0
                                    if (!first) {
                                        writer.write(" ");
                                    }
                                    writer.write(String.valueOf(location));
                                    first = false;
                                }
                            }
                            writer.write("\n");
                            routeNumber++;
                        }
                    }
                } else {
                    writer.write("Không có solution hợp lệ\n");
                }
                
                writer.close();
                System.out.println("Đã ghi kết quả RL routes ra file: " + outputFileName);
                
            } catch (IOException e) {
                System.err.println("Lỗi khi ghi file kết quả RL cho " + epochResult.fileName + 
                    " epoch " + epochResult.epochNumber + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Ghi kết quả của một iterator ra file
     */
    private static void writeIteratorResultToFile(String fileName, int epochNumber, int iteratorNumber, 
                                                 Solution solution, ExportType exportType) {
        if (exportType == ExportType.NONE) {
            return;
        }
        
        try {
            // Tạo tên file output
            String baseFileName = fileName;
            if (baseFileName.contains(".")) {
                baseFileName = baseFileName.substring(0, baseFileName.lastIndexOf('.'));
            }
            String outputFileName = String.format("%s%srl_iter_%s_epoch%d_iter%d.txt", 
                getExportsDirectory(), File.separator, baseFileName, epochNumber, iteratorNumber);
            
            FileWriter writer = new FileWriter(outputFileName);
            
            // Ghi routes của solution - chỉ routes, không có điểm số
            if (solution != null && solution.getRoutes() != null) {
                Route[] routes = solution.getRoutes();
                int routeNumber = 1;
                
                for (Route route : routes) {
                    if (route != null && route.getIndLocations() != null && route.getIndLocations().length > 0) {
                        writer.write("Route " + routeNumber + ": ");
                        
                        // Ghi các điểm trong route, bỏ qua điểm 0 (depot)
                        boolean first = true;
                        for (int location : route.getIndLocations()) {
                            if (location != 0) { // Bỏ qua điểm 0
                                if (!first) {
                                    writer.write(" ");
                                }
                                writer.write(String.valueOf(location));
                                first = false;
                            }
                        }
                        writer.write("\n");
                        routeNumber++;
                    }
                }
            } else {
                writer.write("Không có solution hợp lệ\n");
            }
            
            writer.close();
            System.out.println("    Đã ghi kết quả iterator ra file: " + outputFileName);
            
        } catch (IOException e) {
            System.err.println("    Lỗi khi ghi file kết quả iterator cho " + fileName + 
                " epoch " + epochNumber + " iter " + iteratorNumber + ": " + e.getMessage());
        }
    }
    
    /**
     * Ghi global best solution ra file
     */
    private static void writeGlobalBestSolution(List<EpochResult> epochResults, ExportType exportType) {
        if (exportType == ExportType.NONE || epochResults.isEmpty()) {
            return;
        }
        
        // Tìm global best solution từ tất cả các epoch và file
        Solution globalBestSolution = null;
        String globalBestFileName = "";
        int globalBestEpoch = 0;
        
        for (EpochResult epochResult : epochResults) {
            if (epochResult.bestSolution != null) {
                if (globalBestSolution == null || 
                    epochResult.bestSolution.getFitness() < globalBestSolution.getFitness()) {
                    globalBestSolution = epochResult.bestSolution;
                    globalBestFileName = epochResult.fileName;
                    globalBestEpoch = epochResult.epochNumber;
                }
            }
        }
        
        if (globalBestSolution == null) {
            System.out.println("Không có global best solution để ghi");
            return;
        }
        
        try {
            String outputFileName = getExportsDirectory() + File.separator + "rl_global_best.txt";
            FileWriter writer = new FileWriter(outputFileName);
            
            // Ghi thông tin global best
            writer.write("Global Best Solution\n");
            writer.write("File: " + globalBestFileName + "\n");
            writer.write("Epoch: " + globalBestEpoch + "\n");
            writer.write("Fitness: " + globalBestSolution.getFitness() + "\n");
            writer.write("Routes:\n");
            
            // Ghi routes
            if (globalBestSolution.getRoutes() != null) {
                Route[] routes = globalBestSolution.getRoutes();
                int routeNumber = 1;
                
                for (Route route : routes) {
                    if (route != null && route.getIndLocations() != null && route.getIndLocations().length > 0) {
                        writer.write("Route " + routeNumber + ": ");
                        
                        // Ghi các điểm trong route, bỏ qua điểm 0 (depot)
                        boolean first = true;
                        for (int location : route.getIndLocations()) {
                            if (location != 0) { // Bỏ qua điểm 0
                                if (!first) {
                                    writer.write(" ");
                                }
                                writer.write(String.valueOf(location));
                                first = false;
                            }
                        }
                        writer.write("\n");
                        routeNumber++;
                    }
                }
            }
            
            writer.close();
            System.out.println("Đã ghi Global Best Solution ra file: " + outputFileName);
            System.out.println("Global Best - File: " + globalBestFileName + ", Epoch: " + globalBestEpoch + 
                             ", Fitness: " + globalBestSolution.getFitness());
            
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi Global Best Solution: " + e.getMessage());
        }
    }
    
    /**
     * Lớp lưu trữ kết quả của một iterator
     */
    private static class IteratorResult {
        int iteratorNumber;
        Solution bestSolution;
        Map<Algorithm, Solution> algorithmResults;
        
        public IteratorResult(int iteratorNumber, Solution bestSolution, 
                             Map<Algorithm, Solution> algorithmResults) {
            this.iteratorNumber = iteratorNumber;
            this.bestSolution = bestSolution;
            this.algorithmResults = new HashMap<>(algorithmResults);
        }
    }
    
    /**
     * Lớp lưu trữ kết quả của một epoch
     */
    private static class EpochResult {
        int epochNumber;
        String fileName;
        Solution bestSolution;
        List<IteratorResult> iteratorResults;
        
        public EpochResult(int epochNumber, String fileName, Solution bestSolution, 
                          List<IteratorResult> iteratorResults) {
            this.epochNumber = epochNumber;
            this.fileName = fileName;
            this.bestSolution = bestSolution;
            this.iteratorResults = new ArrayList<>(iteratorResults);
        }
    }
}
