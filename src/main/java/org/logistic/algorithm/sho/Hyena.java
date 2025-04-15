package org.logistic.algorithm.sho;

import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.logistic.algorithm.Agent;
import org.logistic.model.Solution;

/**
 * Đối tượng Hyena trong thuật toán Spotted Hyena Optimizer
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Hyena extends Agent {
    public Hyena(Solution solution, double fitness) {
        super(solution, fitness);
    }
}
