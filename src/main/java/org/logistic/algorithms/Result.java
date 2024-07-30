package org.logistic.algorithms;

public class Result {
    private boolean valid;
    private int cost;
    private long timeOfFirstValidSolution;
    private int numberOfConstraintsBroken;

    public Result(boolean valid, int cost, long timeOfFirstValidSolution, int numberOfConstraintsBroken) {
        this.valid = valid;
        this.cost = cost;
        this.timeOfFirstValidSolution = timeOfFirstValidSolution;
        this.numberOfConstraintsBroken = numberOfConstraintsBroken;
    }

    public boolean isValid() {
        return valid;
    }

    public int getCost() {
        return cost;
    }

    public long getTimeOfFirstValidSolution() {
        return timeOfFirstValidSolution;
    }

    public int getNumberOfConstraintsBroken() {
        return numberOfConstraintsBroken;
    }
}
