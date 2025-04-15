package org.logistic.algorithm.aco;

import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.logistic.algorithm.Agent;
import org.logistic.model.Solution;

/**
 * Đối tượng Ant trong thuật toán Ant Colony Optimization
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ant extends Agent {
    public Ant(Solution solution, double fitness) {
        super(solution, fitness);
    }
}