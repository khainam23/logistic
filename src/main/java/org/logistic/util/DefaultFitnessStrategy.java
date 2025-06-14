package org.logistic.util;

/**
 * Strategy mặc định để tính fitness
 */
public class DefaultFitnessStrategy implements FitnessStrategy {
    private final double alpha = 1.0;
    private final double beta = 1.0;
    private final double gamma = 1.0;
    private final double delta = 1.0;

    @Override
    public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
        return alpha * totalDistances + beta * totalServiceTime +
                gamma * totalWaitingTime + delta * numberVehicle;
    }
}