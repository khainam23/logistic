package org.logistic.evaluation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Result {
    public static void main(String[] args) throws IOException {
        double[][][] Eval = loadEval("Eval_all.npy");

        String[] rowHead = {"Accuracy", "Sensitivity", "Specificity", "Precision", "FPR", "FNR", "NPV", "FDR", "F1_score", "MCC"};
        String[] columnHead = {"Term", "ANN", "PSO.ANN", "GWO.ANN", "SHO.ANN"};

        // First Dataset
        System.out.println("----------------------------------- DataSet 1 -----------------------------------");
        printTable(Eval[0][4], rowHead, columnHead);

        // Second Dataset
        System.out.println("----------------------------------- DataSet 2 -----------------------------------");
        printTable(Eval[1][4], rowHead, columnHead);

        // Third Dataset
        System.out.println("----------------------------------- DataSet 3 -----------------------------------");
        printTable(Eval[2][4], rowHead, columnHead);

        // Plotting datasets
        plotDataset(Eval[0], "Results/Result001.png");
        plotDataset(Eval[1], "Results/Result002.png");
        plotDataset(Eval[2], "Results/Result003.png");
    }

    private static double[][][] loadEval(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        double[][][] data = npyToArray(bytes);
        return data;
    }

    private static double[][][] npyToArray(byte[] bytes) {
        // Implement the method to convert npy byte array to a Java 3D array
        // You can use a library like npy-java
        return new double[0][][]; // Placeholder
    }

    private static void printTable(double[][] data, String[] rowHead, String[] columnHead) {
        for (int i = 0; i < rowHead.length; i++) {
            System.out.print(rowHead[i] + "\t");
            for (int j = 0; j < columnHead.length - 1; j++) {
                System.out.print(data[i][j + 4] + "\t");
            }
            System.out.println();
        }
    }

    private static void plotDataset(double[][] data, String outputPath) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String[] categories = {"35", "45", "55", "65", "75", "85"};
        String[] seriesNames = {"ANN [17]", "PSO.ANN [18]", "GWO.ANN [19]", "SHO.ANN"};

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                dataset.addValue(data[i][j][4] * 100, seriesNames[i], categories[j]);
            }
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Accuracy vs Learning Percentage",
                "Learning Percentage",
                "Accuracy (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        int width = 640;    // Width of the image
        int height = 480;   // Height of the image

        ChartUtils.saveChartAsPNG(new File(outputPath), barChart, width, height);
    }
}
