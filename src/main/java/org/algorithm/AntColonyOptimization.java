package org.algorithm;

import java.util.ArrayList;
import java.util.Arrays;

public class AntColonyOptimization {

    private Ant[] ants;
    private ArrayList<Location> locations;
    private int[][] distances;
    private double[][] pheromones;
    private int maxCapacity;
    final private double evaporation = 0.7;
    private double totalCost = 0;

    //Constants
    final private int m = 100; //number of ants
    final private int MAX_ITERATIONS = 10000;
    final private double INITIAL_PHEROMONE = 4;

    public AntColonyOptimization(ArrayList<Location> locations, int[][] distances, int maxCapacity) {
        this.locations = locations;
        this.distances = distances;
        this.maxCapacity = maxCapacity;
        //Initialise pheromones to INITIAL_PHEROMONE
        pheromones = new double[distances.length][distances.length];
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones.length; j++) {
                pheromones[i][j] = INITIAL_PHEROMONE;
            }
        }
        //Create array of m ants and pass the locations
        ants = new Ant[m];
        for (int k = 0; k < ants.length; k++)
            ants[k] = new Ant(locations, distances, pheromones, maxCapacity, INITIAL_PHEROMONE);
    }

    public Result go() {
        long firstValidRouteTime = -1;
        long startTime = System.currentTimeMillis();
        Ant bestGlobalAnt = new Ant();
        Ant bestIterAnt = new Ant();
        boolean newBestAnt = false;
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (System.currentTimeMillis() - startTime < 60000) {
                for (int k = 0; k < ants.length; k++) {
                    ants[k].resetLocations();
                    int previousIndex = 0;
                    int nextIndex = 0;
                    while (ants[k].getCurrentIndex() < locations.size()) {
                        nextIndex = ants[k].nextLocation(previousIndex);
                        previousIndex = nextIndex;
                    }

                    totalCost += ants[k].getCost();
                    //Keep track of the best ant and the best solution
                    if (k == 0) {//Very first ant is bestGlobalAnt by default
                        bestIterAnt = ants[k].copy();
                        if (i == 0) {
                            bestGlobalAnt = ants[k].copy();
                            newBestAnt = true;
                        }
                    } else {//All other ants need to check if they are better
                        if (ants[k].getCost() < bestIterAnt.getCost() && (!bestIterAnt.isValidRoute() || ants[k].isValidRoute())) {
                            bestIterAnt = ants[k].copy();
                        }
                    }
                }

                //apply 2-opt to best iteration ant
                applyTwoOpt(bestIterAnt);

                //After all ants from one iteration
                if (bestIterAnt.getCost() < bestGlobalAnt.getCost() && (!bestGlobalAnt.isValidRoute() || bestIterAnt.isValidRoute())) {
                    bestGlobalAnt = bestIterAnt.copy();
                    newBestAnt = true;
                }

                if (newBestAnt && firstValidRouteTime == -1 && bestGlobalAnt.isValidRoute())
                    firstValidRouteTime = System.currentTimeMillis() - startTime;

                //Update the global pheromones
                updateGlobalPheromone(bestIterAnt);
                for (int k = 0; k < ants.length; k++) {
                    ants[k].resetAnt(pheromones);
                }
                if (newBestAnt) {
                    if (i == 0)
                        System.out.println();
                    System.out.print(String.format("[Iteration %4s ]: ", i));
                    System.out.print(String.format("%5s", bestGlobalAnt.isValidRoute()));
                    System.out.print(String.format("%13s\t", bestGlobalAnt.getCost()));
                    for (int index : bestGlobalAnt.getRouteIndex())
                        System.out.print(String.format("%3s", index));
                    System.out.println();
                    double max = 0;
                    double min = 1;
                    for (int rows = 0; rows < pheromones.length; rows++) {
                        for (int cols = 0; cols < pheromones.length; cols++) {
                            if (pheromones[rows][cols] > max)
                                max = pheromones[rows][cols];
                            else if (pheromones[rows][cols] < min)
                                min = pheromones[rows][cols];
                        }
                    }
                    newBestAnt = false;
                }

            }
        }

        boolean valid = isValidRoute(bestGlobalAnt.getRouteIndex());
        System.out.println("Valid: " + valid);

        int count = countConstraintBreaks(bestGlobalAnt.getRouteIndex());

        return new Result(valid, (int) bestGlobalAnt.getCost(), firstValidRouteTime, count);
    }


    public void updateGlobalPheromone(Ant bestAnt) {
        for (int rows = 0; rows < pheromones.length; rows++) {
            for (int cols = 0; cols < pheromones.length; cols++) {
                pheromones[rows][cols] *= (1 - evaporation);
            }
        }

        int[] routeIndex = bestAnt.getRouteIndex();
        double[] penaltyCosts = bestAnt.getPenaltyCosts();
        for (int i = 0; i < routeIndex.length - 1; i++) {
            pheromones[routeIndex[i]][routeIndex[i + 1]] += (m * 1000) / bestAnt.getCost();

        }

        double max = (m * 1000) / bestAnt.getCost();
        double min = max / (2 * locations.size());
        for (int rows = 0; rows < pheromones.length; rows++) {
            for (int cols = 0; cols < pheromones.length; cols++) {
                if (pheromones[rows][cols] < min) {
                    pheromones[rows][cols] = min;
                } else if (pheromones[rows][cols] > max) {
                    pheromones[rows][cols] = max;
                }
            }
        }
    }

    private boolean isValidRoute(int[] route) {
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

    private int countConstraintBreaks(int[] route) {
        int count = 0;
        resetLocations();
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = locations.get(route[i]);
            currentLoad += lo.getLoad();
            currentTime += distances[previousIndex][route[i]];
            previousIndex = route[i];
            if (!locations.get(route[i]).isServiceable() && i > 0)
                count++;//precedence constraint
            if (locations.get(route[i]).isServiced() && i > 0)
                count++;//only visit each location once
            locations.get(route[i]).setServiced(true);
            if (lo.isPickup())
                locations.get(route[i] + 1).setServiceable(true);
            //add any waiting time
            currentTime += Math.max(0, lo.getLTW() - currentTime);
            if (currentTime > lo.getUTW())
                count++;//upper time window constraint
            if (currentLoad > maxCapacity)
                count++;// capacity constraint
        }
        return count;
    }

    private boolean applyTwoOpt(Ant ant) {
        int[] route = new int[ant.getRouteIndex().length];
        for (int i = 0; i < route.length; i++)
            route[i] = ant.getRouteIndex()[i];
        double bestCost = ant.getCost();
        int numberOfNodesToSwap = 3;
        int[] r;
        double cost;
        boolean improvement = false;
        boolean continueLoop = true;
        while (continueLoop) {
            continueLoop = false;
            loop:
            for (int i = 1; i <= numberOfNodesToSwap - 1; i++) {
                for (int k = i + 1; k <= numberOfNodesToSwap; k++) {
                    r = twoOptSwap(route, i, k);
                    cost = calculateRouteCost(r);
                    if (cost < bestCost) {
                        System.arraycopy(r, 0, route, 0, route.length);
                        System.out.println("2OPT Improvement: " + bestCost + " --- " + cost);
                        System.out.println(Arrays.toString(route));
                        bestCost = cost;
                        improvement = true;
                        continueLoop = true;
                        break loop;
                    }
                }
            }
        }
        if (improvement) {
            ant.setRouteIndex(route);
            ant.setCost(bestCost);
        }
        return improvement;
    }

    private int[] twoOptSwap(int[] route, int i, int k) {
        int[] r = new int[route.length];
        for (int x = 0; x < i; x++)
            r[x] = route[x];
        int index = i;
        for (int x = k; x >= i; x--) {
            r[index] = route[x];
            index++;
        }
        for (int x = k + 1; x < route.length; x++)
            r[x] = route[x];
        return r;
    }

    private double calculateRouteCost(int[] route) {
        resetLocations();
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        double penaltyCosts = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = locations.get(route[i]);
            currentLoad += lo.getLoad();
            currentTime += distances[previousIndex][route[i]];
            previousIndex = route[i];
            if (locations.get(route[i]).isServiceable() || i == 0) {
                locations.get(route[i]).setServiced(true);
                if (lo.isPickup())
                    locations.get(route[i] + 1).setServiceable(true);
                //add any waiting time
                currentTime += Math.max(0, lo.getLTW() - currentTime);
                if (currentTime > locations.get(route[i]).getUTW())
                    penaltyCosts += Math.max(100, 10 * (currentTime - locations.get(route[i]).getUTW()));
                if (currentLoad > maxCapacity) {
                    penaltyCosts += 10000 * (currentLoad - maxCapacity);
                }
            } else
                penaltyCosts += 100000;
        }
        return penaltyCosts + currentTime;
    }

}
