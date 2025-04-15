package org.logistic.algorithm.gwo;

import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.logistic.algorithm.Agent;
import org.logistic.model.Solution;

/**
 * Đối tượng Wolf trong thuật toán Grey Wolf Optimizer
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wolf extends Agent {
    public Wolf(Solution solution, double fitness) {
        super(solution, fitness);
    }
}