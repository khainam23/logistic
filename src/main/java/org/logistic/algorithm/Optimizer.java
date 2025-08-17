package org.logistic.algorithm;

import org.logistic.model.DistanceTime;
import org.logistic.model.Location;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

/**
 * Interface for all optimization algorithms
 */
public interface Optimizer {
    /**
     * Run the optimization algorithm
     * 
     * @param initialSolutions Initial solutions to start with
     * @param fitnessUtil Utility for calculating fitness
     * @param checkConditionUtil Utility for checking conditions
     * @param locations Array of locations
     * @return The best solution found
     */
    Solution run(Solution[] initialSolutions, 
                FitnessUtil fitnessUtil,
                CheckConditionUtil checkConditionUtil, 
                Location[] locations);

    /**
     * Run the optimization algorithm with DistanceTime information
     * 
     * @param initialSolutions Initial solutions to start with
     * @param fitnessUtil Utility for calculating fitness
     * @param checkConditionUtil Utility for checking conditions
     * @param locations Array of locations
     * @param distanceTimes Array of distance-time information
     * @return The best solution found
     */
    default Solution run(Solution[] initialSolutions, 
                        FitnessUtil fitnessUtil,
                        CheckConditionUtil checkConditionUtil, 
                        Location[] locations,
                        DistanceTime[] distanceTimes) {
        // Default implementation fallback to original method
        return run(initialSolutions, fitnessUtil, checkConditionUtil, locations);
    }
}