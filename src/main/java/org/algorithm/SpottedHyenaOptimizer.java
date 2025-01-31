package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Hyena;

import java.util.Arrays;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer extends Algorithm {
    Hyena bestHyena;

    @Override
    public Result run() {
        FitnessEvaluate fitness = new FitnessEvaluate() {};

        sho(100, locations.size(), fitness);

        return null;
    }

    public double[][] init(int searchAgents, int dimension) {
        double[][] pos = new double[searchAgents][dimension];

        if (upperBounds.length == 1 && lowerBounds.length == 1) {
            double ub = upperBounds[0];
            double lb = lowerBounds[0];
            for (int i = 0; i < searchAgents; i++) {
                for (int j = 0; j < dimension; j++) {
                    pos[i][j] = rd.nextDouble() * (ub - lb) + lb; // Khởi tạo vị trí ngẫu hiên của các hyena
                }
            }
        } else {
            // Trường hợp là không gian lớn hơn
            for (int i = 0; i < dimension; i++) {
                double ub_i = upperBounds[i];
                double lb_i = lowerBounds[i];
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

    public void sho(int N, int dimension, FitnessEvaluate fitness) {
        double[][] hyenaPos = init(N, dimension);
        double[] convergenceCurve = new double[MAX_ITERATOR];
        double[] bestHyenaPos = new double[dimension];
        double bestHyenaScore = Double.MAX_VALUE;

        Random rand = new Random();
        int iteration = 1;

        while (iteration <= MAX_ITERATOR) {
            double[] hyenaFitness = new double[N];
            for (int i = 0; i < N; i++) {
                //  Đảm bảo rằng không có hyena nào ra khỏi giới hạn
                for (int j = 0; j < dimension; j++) {
                    if (hyenaPos[i][j] > upperBounds[j]) {
                        hyenaPos[i][j] = upperBounds[j];
                    } else if (hyenaPos[i][j] < lowerBounds[j]) {
                        hyenaPos[i][j] = lowerBounds[j];
                    }
                }
                // Tính fitness của từng hyena
                hyenaFitness[i] = fitness.calculateSHO(hyenaPos[i]);
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

            double h = 5 - iteration * (5.0 / MAX_ITERATOR);
            double CV = 0;

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < dimension; j++) {
                    double newPos = 0;
                    for (int k = 0; k < NOH; k++) {
                        double r1 = rand.nextDouble();
                        double r2 = rand.nextDouble();
                        double E = 2 * h * r1 - h;
                        double B = 2 * r2;

                        double distanceToHyena = Math.abs(E * sortedPopulation[k][j] - hyenaPos[i][j]);
                        newPos += sortedPopulation[k][j] - B * distanceToHyena;
                    }
                    hyenaPos[i][j] = newPos / (NOH + 1);
                }
            }

            convergenceCurve[iteration - 1] = bestHyenaScore;
            iteration++;
        }
    }
}
