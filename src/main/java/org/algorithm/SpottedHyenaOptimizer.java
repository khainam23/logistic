package org.algorithm;

import java.util.*;

public class SpottedHyenaOptimizer {
    static Map<TypeFunctionalRandom, IFunctionalRandom> mapFunctional;

    // Đưa vào constructor khi có
    static {
        mapFunctional = new HashMap<>();
        mapFunctional.put(TypeFunctionalRandom.SWAP_OPERATOR, Collections::shuffle);
    }

    public double[][] init(int searchAgents, int dimension, double[] upperbound, double[] lowerbound) {
        Random rand = new Random();
        int boundary = upperbound.length;
        double[][] pos = new double[searchAgents][dimension];

        if (boundary == 1) {
            for (int i = 0; i < searchAgents; i++) {
                for (int j = 0; j < dimension; j++) {
                    pos[i][j] = rand.nextDouble() * (upperbound[0] - lowerbound[0]) + lowerbound[0];
                }
            }
        } else if (boundary > 1) {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < searchAgents; j++) {
                    pos[j][i] = rand.nextDouble() * (upperbound[i] - lowerbound[i]) + lowerbound[i];
                }
            }
        }

        return pos;
    }

    public int noh(double[] bestHyenaFitness) {
        double min = 0.5;
        double max = 1;
        int count = 0;
        Random rand = new Random();

        double M = rand.nextDouble() * (max - min) + min;
        M = M + bestHyenaFitness[0];

        for (int i = 1; i < bestHyenaFitness.length; i++) {
            if (M >= bestHyenaFitness[i]) {
                count++;
            }
        }

        return count;
    }

    // Hàm fitness cần phải được định nghĩa trước khi sử dụng
    public double fitness(double[] position) {
        // Thực thi hàm fitness ở đây
        return 0.0;
    }

    public Object[] sho(int N, int maxIterations, double[] lowerbound, double[] upperbound, int dimension) {
        double[][] hyenaPos = init(N, dimension, upperbound, lowerbound);
        double[] convergenceCurve = new double[maxIterations];
        int iteration = 0;
        double[] bestHyenaScore = new double[1];
        double[][] bestHyenaPos = new double[1][dimension];

        while (iteration < maxIterations) {
            double[] hyenaFitness = new double[N];

            // Tính fitness cho các hyena
            for (int i = 0; i < N; i++) {
                // Kiểm tra giới hạn của hyena
                for (int j = 0; j < dimension; j++) {
                    if (hyenaPos[i][j] > upperbound[j]) hyenaPos[i][j] = upperbound[j];
                    if (hyenaPos[i][j] < lowerbound[j]) hyenaPos[i][j] = lowerbound[j];
                }

                hyenaFitness[i] = fitness(hyenaPos[i]);
            }

            double[] fitnessSorted = Arrays.copyOf(hyenaFitness, N);
            Arrays.sort(fitnessSorted);
            int[] sortedIndices = new int[N];
            for (int i = 0; i < N; i++) {
                sortedIndices[i] = i;
            }
            Arrays.sort(sortedIndices);

            double[][] sortedPopulation = new double[N][dimension];
            for (int i = 0; i < N; i++) {
                sortedPopulation[i] = hyenaPos[sortedIndices[i]];
            }

            // Lưu hyena tốt nhất
            bestHyenaPos[0] = sortedPopulation[0];
            bestHyenaScore[0] = fitnessSorted[0];

            // Sử dụng hàm `noh` để tính toán số lượng hyena có thể "săn"
            int NOH = noh(fitnessSorted);

            // Cập nhật các hyena
            double a = 5 - iteration * (5.0 / maxIterations);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < dimension; j++) {
                    double CV = 0;
                    for (int k = 0; k < NOH; k++) {
                        Random rand = new Random();
                        double r1 = rand.nextDouble();
                        double r2 = rand.nextDouble();
                        double Var1 = 2 * a * r1 - a;
                        double Var2 = 2 * r2;
                        double distanceToHyena = Math.abs(Var2 * sortedPopulation[k][j] - hyenaPos[i][j]);
                        CV += sortedPopulation[k][j] - Var1 * distanceToHyena;
                    }
                    hyenaPos[i][j] = CV / (NOH + 1);
                }
            }

            convergenceCurve[iteration] = bestHyenaScore[0];
            iteration++;
        }

        return new Object[]{bestHyenaScore[0], bestHyenaPos[0], convergenceCurve};
    }

    enum TypeFunctionalRandom {
        // Hoán đổi từng phần tử
        SWAP_OPERATOR,
        // Hoán đổi một nhóm phần tử
        SWAP_SEQUENCE,
        PD_SHIFT,
        PD_EXCHANGE,
        PD_REARRANGE
    }

    @FunctionalInterface
    interface IFunctionalRandom {
        void method(ArrayList<Location> hyena);
    }
}
