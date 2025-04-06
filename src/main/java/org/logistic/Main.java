package org.logistic;

import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.FitnessUtil;
import org.logistic.util.WriteLogUtil;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        // Thiết lập công cụ
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();
        ReadDataFromFile rdff = new ReadDataFromFile();

        // Thiết lập đường dẫn
        String dataVrptw = "data/vrptw/src/c101.txt";
        String solutionVrptw = "data/vrptw/solution/c101.txt";
//        String dataPdptw = "data/pdptw/src/lc101.txt";
//        String solutionPdptw = "data/pdptw/solution/lc101.txt";

        // Lấy dữ liệu
        Location[] locations = readData(rdff, dataVrptw, ReadDataFromFile.ProblemType.VRPTW);
        Route[] routes = readSolution(rdff, solutionVrptw);


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