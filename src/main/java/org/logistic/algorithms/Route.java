package org.logistic.algorithms;

import java.util.ArrayList;

public class Route {
    private int[] route;
    private int fitnessValue;
    private ArrayList<Location> locations;
    private int[][] distances;
    private int maxCapacity;
    private boolean valid;


    public Route(int[] route, ArrayList<Location> locations, int[][] distances, int maxCapacity) {
        this.route = route;
        this.maxCapacity = maxCapacity;
        this.locations = locations;
        this.distances = distances;
        this.fitnessValue = calculateFitness();
        this.valid = calculateValid();
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public int[][] getDistances() {
        return distances;
    }

    public void setDistances(int[][] distances) {
        this.distances = distances;
    }

    public void setRoute(int[] route) {
        this.route = route;
    }

    public void setFitnessValue(int fitnessValue) {
        this.fitnessValue = fitnessValue;
    }

    public Route() {

    }

    public int calculateFitness() {
        int fitness = 0;
        int penalty = 0;
        int time = 0;
        int capacity = 0;

        for (int i = 0; i < route.length - 1; i++) {
            time += distances[route[i]][route[i + 1]];
            capacity += locations.get(i).getLoad();
            if (time < locations.get(route[i + 1]).getLTW()) {
                time = locations.get(route[i + 1]).getLTW();
            }
            if (time > locations.get(route[i + 1]).getUTW()) {
                penalty += Math.max(100, 10 * (time - locations.get(route[i]).getUTW()));
            }
            if (capacity > maxCapacity) {
                penalty += Math.max(100, 10 * (capacity - maxCapacity));
            }
        }
        fitness = penalty + time;
        return fitness;
    }

    public int getFitnessValue() {
        return fitnessValue;
    }

    public boolean isValid() {
        return valid;
    }

    public int[] getRoute() {
        return route;
    }

    private boolean calculateValid() {
        boolean valid = true;
        resetLocations();
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = locations.get(route[i]);
            currentLoad += lo.getLoad();
            currentTime += distances[previousIndex][route[i]];
            previousIndex = route[i];
            locations.get(route[i]).setServiced(true);
            if (lo.isPickup())
                locations.get(route[i] + 1).setServiceable(true);
            //add any waiting time
            currentTime += Math.max(0, lo.getLTW() - currentTime);

            if (currentTime > lo.getUTW() || currentLoad > maxCapacity) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    public void resetLocations() {
        for (int i = 1; i < locations.size(); i++)
            locations.get(i).resetServiced();
    }

}
