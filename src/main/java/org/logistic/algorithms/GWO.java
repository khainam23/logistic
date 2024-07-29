package org.logistic.algorithms;
import java.util.Random;

public class GWO {
    public static Object[] gwo(double[][] Positions, String fobj, double[][] Lb, double[][] Ub, int Max_iter, double[] val_in, double[] val_tar, double[][] test_dat, double[] test_tar) {
        int N = Positions.length;
        int dim = Positions[0].length;
        double[] ub = Ub[1];
        double[] lb = Lb[1];

        // initialize alpha, beta, and delta_pos
        double[] Alpha_pos = new double[dim];
        double Alpha_score = Double.POSITIVE_INFINITY;

        double[] Beta_pos = new double[dim];
        double Beta_score = Double.POSITIVE_INFINITY;

        double[] Delta_pos = new double[dim];
        double Delta_score = Double.POSITIVE_INFINITY;

        double[] Convergence_curve = new double[Max_iter + 1];
        int l = 0;
        long startTime = System.currentTimeMillis();

        Random rn = new Random();

        while (l <= Max_iter) {
            for (int i = 0; i < N; i++) {
                // Return back the search agents that go beyond the boundaries of the search space
                for (int j = 0; j < dim; j++) {
                    if (Positions[i][j] > ub[j]) {
                        Positions[i][j] = ub[j];
                    } else if (Positions[i][j] < lb[j]) {
                        Positions[i][j] = lb[j];
                    }
                }

                // Calculate objective function for each search agent
                double fitness = feval(fobj, Positions[i], val_in, val_tar, test_dat, test_tar);

                // Update the leader
                if (fitness < Alpha_score) {
                    Alpha_score = fitness;  // Update alpha
                    Alpha_pos = Positions[i].clone();
                }

                if (Alpha_score < fitness && fitness < Beta_score) {
                    Beta_score = fitness;
                    Beta_pos = Positions[i].clone();
                }

                if (Alpha_score < fitness && fitness < Beta_score) {
                    if (fitness < Delta_score) {
                        Delta_score = fitness; // Update delta
                        Delta_pos = Positions[i].clone();
                    }
                }
            }

            double a = 2 - l * (2.0 / Max_iter);  // a decreases linearly from 2 to 0

            // Update the Position of search agents including omegas
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < dim; j++) {
                    double r1 = rn.nextDouble();
                    double r2 = rn.nextDouble();

                    double A1 = 2 * a * r1 - a;
                    double C1 = 2 * r2;

                    double D_alpha = Math.abs(C1 * Alpha_pos[j] - Positions[i][j]);
                    double X1 = Alpha_pos[j] - A1 * D_alpha;

                    r1 = rn.nextDouble();
                    r2 = rn.nextDouble();

                    double A2 = 2 * a * r1 - a;
                    double C2 = 2 * r2;

                    double D_beta = Math.abs(C2 * Beta_pos[j] - Positions[i][j]);
                    double X2 = Beta_pos[j] - A2 * D_beta;

                    r1 = rn.nextDouble();
                    r2 = rn.nextDouble();

                    double A3 = 2 * a * r1 - a;
                    double C3 = 2 * r2;

                    double D_delta = Math.abs(C3 * Delta_pos[j] - Positions[i][j]);
                    double X3 = Delta_pos[j] - A3 * D_delta;

                    Positions[i][j] = (X1 + X2 + X3) / 3;
                }
            }

            Convergence_curve[l] = Alpha_score;
            l++;
        }

        double best_fit = Convergence_curve[Max_iter - 1];
        long elapsedTime = System.currentTimeMillis() - startTime;
        return new Object[]{best_fit, Convergence_curve, Alpha_pos, elapsedTime / 1000.0};
    }

    // Placeholder for the feval function
    public static double feval(String fobj, double[] position, double[] val_in, double[] val_tar, double[][] test_dat, double[] test_tar) {
        // Implement the function evaluation logic here
        return 0.0; // Replace with actual evaluation
    }
}
