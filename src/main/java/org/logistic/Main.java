package org.logistic;

import org.logistic.algorithm.sho.SimulatedAnnealing;
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
    public static void main(String[] args) throws URISyntaxException {
        // Thiết lập công cụ
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        PrintUtil printUtil = PrintUtil.getInstance();
        CheckConditionUtil checkConditionUtil = CheckConditionUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        // Đọc dữ liệu
        String dataLocation = "data/vrptw/src/c101.txt";
        String dataSolution = "data/vrptw/solution/c101.txt";
        Location[] locations = readData(rdff, dataLocation, ReadDataFromFile.ProblemType.VRPTW);
        Route[] routes = readSolution(rdff, dataSolution);

        // Tạo giải pháp đầu tiên
        Solution mainSolution = new Solution(routes, fitnessUtil.calculatorFitness(routes, locations));

        //  Tạo đa giải pháp
        SimulatedAnnealing sa = new SimulatedAnnealing(mainSolution, writeLogUtil);
        Solution[] solutions = sa.run(fitnessUtil, checkConditionUtil, locations, routes[0].getMaxPayload());

        // In ra kết quả
        printUtil.printSolutions(solutions);

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