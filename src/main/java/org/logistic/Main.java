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
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.PrintUtil;
import org.logistic.util.WriteLogUtil;

import java.net.URISyntaxException;

public class Main {
    enum Algorithm {SHO, ACO, GWO, SA}

    // Cấu trúc việc chạy chương trình
    public static void main(String[] args) throws URISyntaxException {
        // Thiết lập thuật toán mặc định
        Algorithm algorithm = Algorithm.SHO;
        
        // Kiểm tra tham số dòng lệnh để chọn thuật toán
        if (args.length > 0) {
            try {
                algorithm = Algorithm.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Thuật toán không hợp lệ. Sử dụng SHO, ACO, GWO hoặc SA.");
                System.out.println("Sử dụng thuật toán mặc định: " + algorithm);
            }
        }
        
        // Thiết lập công cụ
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        // Đọc dữ liệu
        String dataLocation = "data/pdptw/src/lc101.txt";
        String dataSolution = "data/pdptw/solution/lc101.txt";
        Location[] locations = readData(rdff, dataLocation, ReadDataFromFile.ProblemType.PDPTW);
        Route[] routes = readSolution(rdff, dataSolution);

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

        // Đóng các util nếu có
        writeLogUtil.close();
    }


    public static Location[] readData(ReadDataFromFile rdff, String filePath, ReadDataFromFile.ProblemType problemType) throws URISyntaxException {
        rdff.dataOfProblem(filePath, problemType);
        return rdff.getLocations();
    }

    public static Route[] readSolution(ReadDataFromFile rdff, String filePath) throws URISyntaxException {
        rdff.readSolution(filePath);
        return rdff.getRoutes();
    }
}