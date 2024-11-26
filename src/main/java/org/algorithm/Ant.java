package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ant {
    final double alpha = 1;
    ArrayList<Location> locations;
    double[][] distances;
    double[][] pheromones;
    double[] lowerBounds;
    double[] upperBounds;
    int maxCapacity;
    double initialPheromone;
    int[] routeIndex;
    double[] penaltyCosts;
    double[] waitingCosts;
    int currentIndex = 0;
    double cost = 0;
    int time = 0;
    int currentLoad = 0;
    boolean validRoute = true;

    public Ant() {
        this.cost = -1;
    }

    public Ant(ArrayList<Location> locations, int[][] distances, double[][] pheromones, int maxCapacity, double initialPheromone) {
        this.locations = locations;
        lowerBounds = new double[locations.size()];
        upperBounds = new double[locations.size()];
        //Convert distances to doubles to make it easier to use the distance inverse later
        this.distances = new double[distances.length][distances.length];
        for (int rows = 0; rows < distances.length; rows++) {
            for (int cols = 0; cols < distances.length; cols++) {
                this.distances[rows][cols] = distances[rows][cols];
            }
            lowerBounds[rows] = locations.get(rows).getLTW();
            upperBounds[rows] = locations.get(rows).getUTW();
        }
        this.pheromones = pheromones;
        this.maxCapacity = maxCapacity;
        this.initialPheromone = initialPheromone;
        routeIndex = new int[locations.size()];
        addLocationIndex(0);//starting at depot
        penaltyCosts = new double[locations.size()];
        waitingCosts = new double[locations.size()];
        resetPenaltyAndWaitingCosts();
    }

    public Ant(ArrayList<Location> locations, int[] routeIndex, double[][] distances, double[][] pheromones, double[] penaltyCosts, double[] waitingCosts, int maxCapacity, double initialPheromone, double cost, int time, int currentLoad, int currentIndex, boolean validRoute) {
        this.locations = locations;
        this.routeIndex = routeIndex;
        this.distances = distances;
        this.penaltyCosts = penaltyCosts;
        this.waitingCosts = waitingCosts;
        this.pheromones = pheromones;
        this.maxCapacity = maxCapacity;
        this.initialPheromone = initialPheromone;
        this.cost = cost;
        this.time = time;
        this.currentLoad = currentLoad;
        this.currentIndex = currentIndex;
        this.validRoute = validRoute;
    }

    public void addLocationIndex(int index) {
        routeIndex[currentIndex] = index;
        currentIndex++;
    }

    public int[] getRouteIndex() {
        return routeIndex;
    }

    public void setRouteIndex(int[] routeIndex) {
        this.routeIndex = routeIndex;
    }

    public double[] getPenaltyCosts() {
        return penaltyCosts;
    }

    public double[] getWaitingCosts() {
        return waitingCosts;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int nextLocation(int previousIndex) {
        //Get the pheromones and inverse distances dealing with the previousIndex
        double[] pheros = pheromones[previousIndex];
        double[] dists = distances[previousIndex];
        //Multiply the pheromones and the inverse distances
        //Alpha and beta are relative importance of pheromones and visibility of ant, respectively
        double[] fitness = new double[pheros.length];
        double fitnessSum = 0;
        for (int i = 1; i < locations.size(); i++) {
            if (isFeasibleLocation(locations.get(i))) {
                fitness[i] = (pheros[i] * alpha) * (1 / (dists[i] + lowerBounds[i] * upperBounds[i] * upperBounds[i]));
                //fitness[i] = (1 / upperBounds[i]);
                fitnessSum += fitness[i];
            } else {
                fitness[i] = 0;
            }
        }

        //Choose path based on roulette wheel selection
        double[] probability = new double[fitness.length];
        probability[0] = 0;
        for (int i = 1; i < fitness.length; i++) {
            probability[i] = probability[i - 1] + (fitness[i] / fitnessSum);
        }

        double random = new Random().nextDouble();
        int index = 0;
        for (int i = 1; i < fitness.length; i++) {
            if (random < probability[i]) {
                index = i;
                break;
            }
        }
        //We have found the index of the next location to visit
        //Add index to our route
        addLocationIndex(index);
        // Keep track of time:
        // Add [travel time]
        time += distances[previousIndex][index];
        //[waiting time if the ant arrives early: LTW - time]
        waitingCosts[index] = Math.max(0, locations.get(index).getLTW() - time);
        //[penalty cost if the ant arrives late]
        if (time > locations.get(index).getUTW())
            penaltyCosts[index] = Math.max(100, 10 * (time - locations.get(index).getUTW()));
        else
            penaltyCosts[index] = 0;
        //If the ant was late, the route is no longer valid
        if (penaltyCosts[index] > 0)
            validRoute = false;
        // Keep track of running cost:
        cost += distances[previousIndex][index]
                + waitingCosts[index]//waiting time
                + penaltyCosts[index];//penalty
        //Add waiting costs to time
        time += waitingCosts[index];
        //keep track of currentLoad
        currentLoad += locations.get(index).getLoad();


        locations.get(index).setServiced(true);
        if (locations.get(index).isPickup()) {
            locations.get(index + 1).setServiceable(true);
        }
        return index;
    }

    boolean isFeasibleLocation(Location location) {
        //A feasible location must satisfy three constraints:
        //1: Precedence constraint
        //2: Cannot be visited more than once
        //3: Expected load cannot exceed maxCapacity
        return (location.isServiceable() && !location.isServiced() && currentLoad + location.getLoad() <= maxCapacity);
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    //replace pheromones with best global solution
    //and reset all variables
    public void resetAnt(double[][] pheromones) {
        this.pheromones = pheromones;
        currentIndex = 1;
        cost = 0;
        time = 0;
        currentLoad = 0;
        validRoute = true;
        resetLocations();
        resetRouteIndex();
        resetPenaltyAndWaitingCosts();
    }

    public void resetLocations() {
        for (int i = 1; i < locations.size(); i++)
            locations.get(i).resetServiced();
    }

    public void resetRouteIndex() {
        for (int i = 1; i < routeIndex.length; i++)
            routeIndex[i] = 0;
    }

    void resetPenaltyAndWaitingCosts() {
        for (int i = 0; i < penaltyCosts.length; i++) {
            penaltyCosts[i] = 0;
            waitingCosts[i] = 0;
        }
    }

    public boolean isValidRoute() {
        return validRoute;
    }

    //Copy by value, not by reference
    public Ant copy() {
        //Only need to individually copy the arrays and lists
        //locations, routeIndex, distances and pheromones
        int[] routeIndexCopy = new int[routeIndex.length];
        double[] penaltyCostsCopy = new double[penaltyCosts.length];
        double[] waitingCostsCopy = new double[waitingCosts.length];
        ArrayList<Location> locationsCopy = new ArrayList<>();
        double[][] distancesCopy = new double[distances.length][distances.length];
        double[][] pheromonesCopy = new double[pheromones.length][pheromones.length];
        for (int i = 0; i < locations.size(); i++) {
            locationsCopy.add(locations.get(i));
            routeIndexCopy[i] = routeIndex[i];
            waitingCostsCopy[i] = waitingCosts[i];
            penaltyCostsCopy[i] = penaltyCosts[i];
            for (int j = 0; j < locations.size(); j++) {
                distancesCopy[i][j] = distances[i][j];
                pheromonesCopy[i][j] = pheromones[i][j];
            }
        }

        return new Ant(locationsCopy, routeIndexCopy, distancesCopy, pheromonesCopy, penaltyCostsCopy, waitingCostsCopy, maxCapacity, initialPheromone, cost, time, currentLoad, currentIndex, validRoute);
    }

}


