package org.logistic.evaluation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class PlotResult {
    public static void main(String[] args) {
        plotGraph();
    }

    public static void plotGraph() {
        for (int i = 0; i < 4; i++) {
            double[][] Error_eval = loadErrorEval("Error_eval" + (i + 1) + ".npy");

            switch (i) {
                case 0:
                    System.out.println("-----------------------Testcase: 1--------------------------------------");
                    break;
                case 1:
                    System.out.println("-----------------------Testcase: 2--------------------------------------");
                    break;
                case 2:
                    System.out.println("-----------------------Testcase: 3--------------------------------------");
                    break;
                case 3:
                    System.out.println("-----------------------Testcase: 4--------------------------------------");
                    break;
            }

            double[][] Ev = Error_eval[3];  // Only the learning percentage 75
            double[][] Eval = Arrays.copyOfRange(Ev, 0, Ev.length);

            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Algorithm+Classifier Analysis %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            String[] Ev1 = {"PSO", "GWO", "WOA", "DHOA", "Proposed"};
            printEvaluation(Ev1, Eval);

            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Classifier Analysis %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            String[] Ev2 = {"Fuzzy", "NN", "SVM", "KNN", "Fuzzy+NN", "Proposed"};
            printEvaluation(Ev2, Eval);

            String[] lnn = {"Accuracy", "Sensitivity", "Specificity", "Precision", "FPR", "FNR", "NPV", "FDR", "F1-Score", "MCC"};
            int[] x = {35, 55, 65, 75, 85};
            double[][] val = new double[5][5];
            int[] vn = {0, 3, 4, 5, 8};

            for (int j = 0; j < vn.length; j++) {
                for (int k = 0; k < 5; k++) {
                    Ev = Error_eval[k];
                    Eval = Arrays.copyOfRange(Ev, 0, Ev.length);
                    double[][] data = Arrays.copyOfRange(Eval, 0, 5);

                    for (int m = 0; m < 5; m++) {
                        val[m][k] = vn[j] == 9 ? data[m][vn[j]] : data[m][vn[j]] * 100;
                    }
                }

                plotGraph(x, val, lnn[vn[j]], "Learning Percentage", "perfalg_" + i + "-" + j + ".png");
            }

            val = new double[6][5];
            for (int j = 0; j < vn.length; j++) {
                for (int k = 0; k < 5; k++) {
                    Ev = Error_eval[k];
                    Eval = Arrays.copyOfRange(Ev, 0, Ev.length);
                    double[][] data = {Eval[6], Eval[7], Eval[8], Eval[5], Eval[9], Eval[4]};

                    for (int m = 0; m < 6; m++) {
                        val[m][k] = vn[j] == 9 ? data[m][vn[j]] : data[m][vn[j]] * 100;
                    }
                }

                plotGraph(x, val, lnn[vn[j]], "Learning Percentage", "perfcls_" + i + "-" + j + ".png");
            }
        }
    }

    private static double[][] loadErrorEval(String filename) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(filename));
            // convert the byte array to double array
            // This step needs proper implementation based on the .npy file format
            return new double[10][10]; // replace with actual conversion
        } catch (IOException e) {
            e.printStackTrace();
            return new double[0][0];
        }
    }

    private static void printEvaluation(String[] names, double[][] Eval) {
        System.out.println("Accuracy  Sensitivity  Specificity  Precision  FPR  FNR  NPV  FDR   F1-Score  MCC");
        for (int n = 0; n < names.length; n++) {
            System.out.print(names[n] + " ");
            for (int m = 0; m < 10; m++) {
                System.out.print(Eval[n][m] + " ");
            }
            System.out.println();
        }
    }

    private static void plotGraph(int[] x, double[][] val, String yLabel, String xLabel, String fileName) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        String[] labels = {"PSO-FNN", "GWO-FNN", "WOA-FNN", "DHOA-FNN", "O-DHOA-FNN"};
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN};

        for (int i = 0; i < val.length; i++) {
            XYSeries series = new XYSeries(labels[i]);
            for (int j = 0; j < x.length; j++) {
                series.add(x[j], val[i][j]);
            }
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                yLabel,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < val.length; i++) {
            renderer.setSeriesPaint(i, colors[i]);
        }
        plot.setRenderer(renderer);

        try {
            ChartPanel chartPanel = new ChartPanel(chart);
            JFrame frame = new JFrame();
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setVisible(true);

            // Save chart as PNG file
            // ChartUtilities.saveChartAsPNG(new File(fileName), chart, 800, 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
