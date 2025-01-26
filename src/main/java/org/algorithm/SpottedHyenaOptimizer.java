package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Location;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends Algorithm {
    @Override
    public Result run(List<Location> locations) {
        return null;
    }

    public double[][] init(int searchAgents, int dimension, double[] upperbound, double[] lowerbound) {
        double[][] pos = new double[searchAgents][dimension];

        if (upperbound.length == 1 && lowerbound.length == 1) {
            double ub = upperbound[0];
            double lb = lowerbound[0];
            for (int i = 0; i < searchAgents; i++) {
                for (int j = 0; j < dimension; j++) {
                    pos[i][j] = rd.nextDouble() * (ub - lb) + lb;
                }
            }
        } else {
            for (int i = 0; i < dimension; i++) {
                double ub_i = upperbound[i];
                double lb_i = lowerbound[i];
                for (int j = 0; j < searchAgents; j++) {
                    pos[j][i] = rd.nextDouble() * (ub_i - lb_i) + lb_i;
                }
            }
        }
        return pos;
    }

    public int noh(double[] bestHyenaFitness) {
        double min = 0.5;
        double max = 1.0;
        double M = min + (max - min) * rd.nextDouble();
        M += bestHyenaFitness[0];

        int count = 0;
        for (int i = 1; i < bestHyenaFitness.length; i++) {
            if (M >= bestHyenaFitness[i]) {
                count++;
            }
        }
        return count;
    }

    public double[] sho(int N, int maxIterations, double[] lowerbound, double[] upperbound, int dimension, FitnessFunction fitness) {
        double[][] hyenaPos = init(N, dimension, upperbound, lowerbound);
        double[] convergenceCurve = new double[maxIterations];
        double[] bestHyenaPos = new double[dimension];
        double bestHyenaScore = Double.MAX_VALUE;

        Random rand = new Random();
        int iteration = 1;

        while (iteration <= maxIterations) {
            double[] hyenaFitness = new double[N];
            for (int i = 0; i < N; i++) {
                // Ensure positions are within bounds
                for (int j = 0; j < dimension; j++) {
                    if (hyenaPos[i][j] > upperbound[j]) {
                        hyenaPos[i][j] = upperbound[j];
                    } else if (hyenaPos[i][j] < lowerbound[j]) {
                        hyenaPos[i][j] = lowerbound[j];
                    }
                }
                hyenaFitness[i] = fitness.calculate(hyenaPos[i]);
            }

            // Sort fitness and positions
            double[] sortedFitness = hyenaFitness.clone();
            Arrays.sort(sortedFitness);

            double[][] sortedPopulation = new double[N][dimension];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (hyenaFitness[j] == sortedFitness[i]) {
                        sortedPopulation[i] = hyenaPos[j].clone();
                        break;
                    }
                }
            }

            // Update best solutions
            if (iteration == 1) {
                bestHyenaScore = sortedFitness[0];
                bestHyenaPos = sortedPopulation[0].clone();
            }

            int NOH = noh(sortedFitness);

            double a = 5 - iteration * (5.0 / maxIterations);
            double CV = 0;

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < dimension; j++) {
                    double newPos = 0;
                    for (int k = 0; k < NOH; k++) {
                        double r1 = rand.nextDouble();
                        double r2 = rand.nextDouble();
                        double var1 = 2 * a * r1 - a;
                        double var2 = 2 * r2;

                        double distanceToHyena = Math.abs(var2 * sortedPopulation[k][j] - hyenaPos[i][j]);
                        newPos += sortedPopulation[k][j] - var1 * distanceToHyena;
                    }
                    hyenaPos[i][j] = newPos / (NOH + 1);
                }
            }

            convergenceCurve[iteration - 1] = bestHyenaScore;
            iteration++;
        }

        return new double[]{bestHyenaScore, convergenceCurve[maxIterations - 1]};
    }

    public interface FitnessFunction {
        double calculate(double[] position);
    }
}
