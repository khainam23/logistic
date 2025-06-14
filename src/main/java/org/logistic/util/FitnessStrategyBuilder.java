package org.logistic.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Builder để tạo và cấu hình FitnessStrategy
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FitnessStrategyBuilder {
    double alpha = 1.0;
    double beta = 1.0;
    double gamma = 1.0;
    double delta = 1.0;
    boolean useDistance = true;
    boolean useServiceTime = true;
    boolean useWaitingTime = true;
    boolean useVehicleCount = true;

    public FitnessStrategyBuilder withAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public FitnessStrategyBuilder withBeta(double beta) {
        this.beta = beta;
        return this;
    }

    public FitnessStrategyBuilder withGamma(double gamma) {
        this.gamma = gamma;
        return this;
    }

    public FitnessStrategyBuilder withDelta(double delta) {
        this.delta = delta;
        return this;
    }

    public FitnessStrategyBuilder useDistance(boolean use) {
        this.useDistance = use;
        return this;
    }

    public FitnessStrategyBuilder useServiceTime(boolean use) {
        this.useServiceTime = use;
        return this;
    }

    public FitnessStrategyBuilder useWaitingTime(boolean use) {
        this.useWaitingTime = use;
        return this;
    }

    public FitnessStrategyBuilder useVehicleCount(boolean use) {
        this.useVehicleCount = use;
        return this;
    }

    public FitnessStrategy build() {
        return ConfigurableFitnessStrategy.builder()
                .alpha(alpha)
                .beta(beta)
                .gamma(gamma)
                .delta(delta)
                .useDistance(useDistance)
                .useServiceTime(useServiceTime)
                .useWaitingTime(useWaitingTime)
                .useVehicleCount(useVehicleCount)
                .build();
    }
}