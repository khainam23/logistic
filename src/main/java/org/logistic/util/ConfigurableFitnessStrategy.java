package org.logistic.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

/**
 * Strategy có thể cấu hình để tính fitness
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigurableFitnessStrategy implements FitnessStrategy {
    double alpha;
    double beta;
    double gamma;
    double delta;
    boolean useDistance;
    boolean useServiceTime;
    boolean useWaitingTime;
    boolean useVehicleCount;

    @Builder
    public ConfigurableFitnessStrategy(
            double alpha, double beta, double gamma, double delta,
            boolean useDistance, boolean useServiceTime, boolean useWaitingTime, boolean useVehicleCount) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.delta = delta;
        this.useDistance = useDistance;
        this.useServiceTime = useServiceTime;
        this.useWaitingTime = useWaitingTime;
        this.useVehicleCount = useVehicleCount;
    }

    @Override
    public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
        double fitness = 0.0;
        
        if (useDistance) {
            fitness += alpha * totalDistances;
        }
        
        if (useServiceTime) {
            fitness += beta * totalServiceTime;
        }
        
        if (useWaitingTime) {
            fitness += gamma * totalWaitingTime;
        }
        
        if (useVehicleCount) {
            fitness += delta * numberVehicle;
        }
        
        return fitness;
    }
}