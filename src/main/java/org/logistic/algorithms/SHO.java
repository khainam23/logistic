package org.logistic.algorithms;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import java.util.Random;

public class SHO {
    private static final Random random = new Random();

    public static Object[] sho(
            RealMatrix positions,
            FunctionEval feval,
            RealMatrix lb,
            RealMatrix ub,
            int maxIter,
            RealVector valIn,
            RealVector valTar,
            RealMatrix testDat,
            RealVector testTar) {

        int n = positions.getRowDimension();
        int dim = positions.getColumnDimension();
        RealVector ubVector = ub.getRowVector(0);
        RealVector lbVector = lb.getRowVector(0);

        RealVector alphaPos = new ArrayRealVector(dim);
        double alphaScore = Double.POSITIVE_INFINITY;

        RealVector[] convergenceCurve = new RealVector[maxIter + 1];
        for (int i = 0; i < convergenceCurve.length; i++) {
            convergenceCurve[i] = new ArrayRealVector(dim);
        }

        int l = 0;
        long startTime = System.currentTimeMillis();
        double[] newFitness = new double[n];

        while (l <= maxIter) {
            for (int i = 0; i < n; i++) {
                RealVector pos = positions.getRowVector(i);

                // Ensure positions stay within bounds
                for (int j = 0; j < dim; j++) {
                    if (pos.getEntry(j) > ubVector.getEntry(j)) {
                        pos.setEntry(j, ubVector.getEntry(j));
                    } else if (pos.getEntry(j) < lbVector.getEntry(j)) {
                        pos.setEntry(j, lbVector.getEntry(j));
                    }
                }

                // Calculate fitness
                newFitness[i] = feval.evaluate(pos, valIn, valTar, testDat, testTar);

                // Update leader
                if (newFitness[i] < alphaScore) {
                    alphaScore = newFitness[i];
                    alphaPos = pos.copy();
                }
            }

            double a = 5 - l * (5.0 / maxIter);

            RealVector indn;
            if (l == 0) {
                indn = new ArrayRealVector(dim);
            } else {
                indn = new ArrayRealVector(newFitness);
                for (int i = 0; i < indn.getDimension(); i++) {
                    if (indn.getEntry(i) < alphaScore) {
                        indn.setEntry(i, 1.0);
                    } else {
                        indn.setEntry(i, 0.0);
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                int h = indn.getEntry(i) == 1.0 ? random.nextInt(n - 5) + 1 : 1;

                for (int j = 0; j < dim; j++) {
                    RealVector x1 = new ArrayRealVector(h);
                    for (int c = 0; c < h; c++) {
                        double r1 = random.nextDouble();
                        double r2 = random.nextDouble();

                        double a1 = 2 * a * r1 - a;
                        double c1 = 2 * r2;

                        double dAlpha = Math.abs(c1 * alphaPos.getEntry(j) - positions.getEntry(i, j));
                        x1.setEntry(c, alphaPos.getEntry(j) - a1 * dAlpha);
                    }
                    positions.setEntry(i, j, x1.getL1Distance(new ArrayRealVector(dim)) / dim);
                }
            }

            convergenceCurve[l] = new ArrayRealVector(new double[]{alphaScore});
            l++;
        }

        double bestFit = convergenceCurve[maxIter - 1].getEntry(0);
        long elapsedTime = System.currentTimeMillis() - startTime;

        return new Object[]{bestFit, convergenceCurve, alphaPos, elapsedTime};
    }
}
