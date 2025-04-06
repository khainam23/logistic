package org.logistic;

import lombok.Locked;
import org.logistic.algorithm.sho.SpottedHyenaOptimizer;
import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Solution;
import org.logistic.model.Vehicle;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.PrintUtil;
import org.logistic.util.WriteLogUtil;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        // Thiết lập công cụ
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();
        WriteLogUtil writeLogUtil = WriteLogUtil.getInstance();


    }
//
//    public static void testRealDataVrptw() throws URISyntaxException {
//        String data = "data/vrptw/src/c101.txt";
//
//        ReadDataFromFile readDataVRPTW = new ReadDataFromFile();
//        readDataVRPTW.dataOfVrptw(data);
//    }
//
//    public static void testRealDataPdptw() throws URISyntaxException {
//        String data = "data/pdptw/src/lc101.txt";
//
//        ReadDataFromFile readDataPdptw = new ReadDataFromFile();
//        readDataPdptw.dataOfPdptw(data);
//
//
//    }
}