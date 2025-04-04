package org.logistic.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.io.File;
import java.util.List;

public class WriteLog {
    private static WriteLog instance;
    private static final String LOG_FILE = "src/main/resources/SpottedHyenaOptimizer.log";
    private PrintWriter writer;

    private WriteLog() {
        try {
            File logDir = new File("src/main/resources");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            writer = new PrintWriter(new FileWriter(LOG_FILE, false));
        } catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
    }

    public static synchronized WriteLog getInstance() {
        if (instance == null) {
            instance = new WriteLog();
        }
        return instance;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void log(String message) {
        if (writer != null) {
            writer.println(String.format("[%s] %s", getCurrentTimestamp(), message));
            writer.flush();
        }
    }

    public void logIterationInfo(int currentIteration, int maxIterations, double bestFitness) {
        log(String.format("Iteration %d/%d - Best fitness value: %.2f", 
            currentIteration, maxIterations, bestFitness));
    }

    public void logInitialInfo(double initialFitness) {
        log(String.format("Starting SHO optimization process..."));
        log(String.format("Initial best fitness value: %.2f", initialFitness));
    }

    public void logInitialSolution(List<List<Integer>> routes, double[][] distances) {
        log("Initial Solution Details:");
        double totalDistance = 0;
        for (int i = 0; i < routes.size(); i++) {
            log(String.format("Vehicle %d - Route: %s, Distance: %.2f", 
                i + 1, routes.get(i).toString(), distances[i][0]));
            totalDistance += distances[i][0];
        }
        log(String.format("Initial Total Distance: %.2f", totalDistance));
    }

    public void logVehicleRoute(int vehicleIndex, int[] route, double distance) {
        log(String.format("Vehicle %d - Route: %s, Distance: %.2f", 
            vehicleIndex + 1, Arrays.toString(route), distance));
    }

    public void logTotalDistance(double totalDistance) {
        log(String.format("Total Distance: %.2f", totalDistance));
    }

    public void logFinalResult(double bestFitness) {
        log("Optimization completed.");
        log(String.format("Final optimized fitness value: %.2f", bestFitness));
    }

    public void logFinalResult(double bestFitness, List<List<Integer>> routes, double[][] distances) {
        log("\nOptimization completed. Final Solution Details:");
        double totalDistance = 0;
        for (int i = 0; i < routes.size(); i++) {
            log(String.format("Vehicle %d - Route: %s, Distance: %.2f", 
                i + 1, routes.get(i).toString(), distances[i][0]));
            totalDistance += distances[i][0];
        }
        log(String.format("Total Distance: %.2f", totalDistance));
        log(String.format("Final Fitness Value: %.2f", bestFitness));
        log(String.format("Final Optimized Fitness Value: %.2f", bestFitness));
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}