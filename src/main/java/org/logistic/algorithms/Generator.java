package org.logistic.algorithms;

import java.util.ArrayList;
import java.util.Random;

public class Generator {

    private int[][] distances;
    private ArrayList<Location> locations;
    private int[] successfulRoute;
    private int capacity;

    // Number of tasks = n
    // Width of time window = width
    public void generateData(int n, int width) {
        // Constant values
        final int MIN_COORD = 1;
        final int MAX_COORD = 100;
        final int MIN_CAPACITY = 1;
        final int MAX_CAPACITY = 10;
        capacity = getRandom(MIN_CAPACITY, MAX_CAPACITY);

        //List of locations
        locations = new ArrayList<>();
        //Starting location/depot
        Location depot = new Location(true, 0, 0, 0);
        depot.setServiceable(false);
        depot.setServiced(true);
        locations.add(depot);

        //Add n locations
        for (int i = 0; i < n; i++) {
            // Generate Pickup location and load
            int Px = getRandom(MIN_COORD, MAX_COORD);
            int Py = getRandom(MIN_COORD, MAX_COORD);
            int load = getRandom(1, capacity);
            locations.add(new Location(true, Px, Py, load));
            // Generate corresponding Dropoff location and load
            int Dx = getRandom(MIN_COORD, MAX_COORD);
            int Dy = getRandom(MIN_COORD, MAX_COORD);
            // Same load as corresponding pickup, but negative
            locations.add(new Location(false, Dx, Dy, -load));
        }


        // Calculate distances matrix
        distances = new int[locations.size()][locations.size()];
        for (int rows = 0; rows < distances.length; rows++) {
            for (int cols = 0; cols < distances.length; cols++) {
                distances[rows][cols] = (int) Math.sqrt(Math.pow(locations.get(cols).getY() - locations.get(rows).getY(), 2) + Math.pow(locations.get(cols).getX() - locations.get(rows).getX(), 2));
            }
        }


        // Randomly generate route which satisfies the precedence constraint
        int visits = 0;
        int time = 0;
        int[] arrivalTime = new int[locations.size()];
        int previousIndex = 0;
        int currentCapacity = 0;
        successfulRoute = new int[locations.size()];
        successfulRoute[0] = 0;//Start at depot
        while (visits < locations.size() - 1) {
            int index = getRandom(1, locations.size() - 1);//random location ignoring the depot
            if (locations.get(index).isServiceable() && !locations.get(index).isServiced()) {
                if (currentCapacity + locations.get(index).getLoad() <= capacity) {
                    currentCapacity += locations.get(index).getLoad();
                    locations.get(index).setServiced(true);
                    time += distances[previousIndex][index];
                    previousIndex = index;
                    arrivalTime[index] = time;
                    successfulRoute[visits + 1] = index;
                    if (locations.get(index).isPickup()) {
                        locations.get(index + 1).setServiceable(true);
                    }
                    visits++;
                }
            }
        }


        //Average time of randomly generated route
        int averageTime = time / visits;

        //Create time windows which suite the route
        for (int i = 1; i < locations.size(); i++) {
            int w = getRandom(1, width) * averageTime * 10;
            locations.get(i).setUTW(arrivalTime[i] + w);
            locations.get(i).setLTW(arrivalTime[i] - w);
            if (locations.get(i).getLTW() < 0) {// Use a different formula if LTW goes below 0
                w = getRandom(1, arrivalTime[i]);
                locations.get(i).setLTW(arrivalTime[i] - w);
            }
        }

        //Reset the serviced and serviceable booleans
        resetServiced(locations);

    }

    // Get random int in range
    private int getRandom(int min, int max) {
        Random rn = new Random();
        return rn.nextInt((max - min) + 1) + min;
    }

    public void printMatrix(int[][] di) {
        for (int cols = 0; cols < di.length; cols++)
            System.out.print("\t" + cols);

        for (int rows = 0; rows < di.length; rows++) {
            System.out.print("\n" + rows);
            for (int cols = 0; cols < di.length; cols++) {
                System.out.print("\t" + di[rows][cols]);
            }
        }
        System.out.println("");
    }

    public void printLocations(ArrayList<Location> lo) {
        System.out.println(String.format("%8s %11s %6s %6s %6s %10s %13s", "Pickup:", "(x, y):", "Load:", "LTW:", "UTW:", "Serviced:", "Serviceable:"));
        for (int i = 0; i < lo.size(); i++) {
            System.out.println(lo.get(i).toString());
        }
    }

    public void printRoute(ArrayList<Location> lo, int[] ri) {
        resetServiced(lo);
        System.out.println("\nRoute:");
        System.out.println(String.format("%8s %8s %11s %6s %6s %6s %10s %13s %15s %15s", "Index:", "Pickup:", "(x, y):", "Load:", "LTW:", "UTW:", "Serviced:", "Serviceable:", "Current Load: ", "Current Time:"));
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < ri.length; i++) {
            String index = String.format("%8s ", ri[i]);
            currentLoad += lo.get(ri[i]).getLoad();
            currentTime += distances[previousIndex][ri[i]];
            previousIndex = ri[i];
            //Show if the vehicle capacity is exceeded
            String cl = "";
            if (currentLoad > capacity)
                cl = String.format("%15s", currentLoad + " > " + capacity);
            else
                cl = String.format("%15s", currentLoad);
            String ct = String.format("%15s", currentTime);
            lo.get(ri[i]).setServiced(true);
            if (lo.get(ri[i]).isPickup())
                lo.get(ri[i] + 1).setServiceable(true);
            System.out.println(index + lo.get(ri[i]).toString() + cl + ct);
        }
    }

    public ArrayList<Location> resetServiced(ArrayList<Location> lo) {
        for (int i = 1; i < lo.size(); i++) {
            lo.get(i).resetServiced();
        }
        return lo;
    }

    public int[][] getDistances() {
        return distances;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public int[] getSuccessfulRoute() {
        return successfulRoute;
    }

    public int getCapacity() {
        return capacity;
    }

}
