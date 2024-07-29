package org.logistic.evalution;

import java.util.Arrays;

public class Objfun {
    public static double[] objfun(double[][] soln, double[][] train_data, double[] train_target, double[][] test_data, double[] test_target) {
        int dim;
        int v;
        double[][] fitn;

        if (soln.length > 0 && soln[0].length > 0) {
            dim = soln[0].length;   // length of soln
            v = soln.length;  // No. of soln
            fitn = new double[v][1];
        } else {
            dim = soln.length;
            v = 1;
            fitn = new double[1][1];
        }

        for (int i = 0; i < v; i++) {
            double[] sol = new double[dim];
            if (soln.length > 0 && soln[0].length > 0) {
                for (int j = 0; j < dim; j++) {
                    sol[j] = Math.round(soln[i][j]);
                }
            } else {
                for (int j = 0; j < dim; j++) {
                    sol[j] = Math.round(soln[j]);
                }
            }

            boolean[] act = new boolean[test_target.length];
            boolean[] pred;
            int net;

            // Thay thế train_nn với phiên bản tương ứng trong Java
            pred = train_nn(train_data, train_target, test_data, (int) sol[0]);
            act = Arrays.stream(test_target).mapToInt(value -> (int) value).mapToObj(value -> value != 0).toArray(boolean[]::new);

            // Thay thế evaln với phiên bản tương ứng trong Java
            double[] eval = evaln(pred, act);
            fitn[i][0] = 1 / (eval[4] + eval[7]);
        }

        return Arrays.stream(fitn).mapToDouble(arr -> arr[0]).toArray();
    }

    // Placeholder methods for train_nn and evaln
    // Bạn cần thay thế chúng bằng phiên bản Java tương ứng

    public static boolean[] train_nn(double[][] train_data, double[] train_target, double[][] test_data, int sol) {
        // Implement train_nn logic here
        return new boolean[test_data.length]; // Placeholder return
    }

    public static double[] evaln(boolean[] pred, boolean[] act) {
        // Implement evaln logic here
        return new double[8]; // Placeholder return
    }
}
