package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;
import org.logistic.util.FitnessUtil;
import org.logistic.model.Vehicle;
import org.logistic.util.CheckConditionUtil;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.util.WriteLogUtil;

import java.util.Arrays;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpottedHyenaOptimizer {
    // Fixed algorithm parameters
    static final int POPULATION_SIZE = 50;
    static final int MAX_ITERATIONS = 500;
    static final double ENCIRCLING_COEFFICIENT = 1.5;

    // Dynamic parameters used in the algorithm
    Hyena[] hyenas;
    Hyena bestHyena;
    Random random;
    int currentIteration;

    // Initialization parameters
    Location[] locations;
    Vehicle[] vehicles;

    public SpottedHyenaOptimizer(Location[] locations, Vehicle[] vehicles) {
        this.random = new Random();
        this.currentIteration = 1; // Loop is numbered starting from 1
        this.locations = locations;
        this.vehicles = vehicles;
    }

    public Vehicle[] getOptimizedVehicles() {
        return this.vehicles;
    }

    public Solution optimize(Solution initialSolution) {
        System.out.println("\n===== STARTING SPOTTED HYENA OPTIMIZER ALGORITHM =====\n");
        System.out.println("Population size: " + POPULATION_SIZE);
        System.out.println("Maximum iterations: " + MAX_ITERATIONS);
        System.out.println("Encircling coefficient: " + ENCIRCLING_COEFFICIENT);
        System.out.println();
        
        // Initialize initial population using SA
        System.out.println("Initializing initial population using Simulated Annealing...");
        Solution[] initialPopulation = initPopulation(initialSolution);
        hyenas = new Hyena[POPULATION_SIZE];
        System.out.println("Initial population initialized with " + POPULATION_SIZE + " individuals.");

        // Calculate initial coefficients
        double h = ENCIRCLING_COEFFICIENT - (currentIteration * (ENCIRCLING_COEFFICIENT / MAX_ITERATIONS));
        double E = 2 * h * random.nextDouble() - h;
        double B = 2 * random.nextDouble();
        
        System.out.println("\nInitial coefficients:");
        System.out.println("h = " + h);
        System.out.println("E = " + E);
        System.out.println("B = " + B);

        // Convert Solutions to Hyenas
        System.out.println("\nConverting Solutions to Hyenas...");
        for (int i = 0; i < POPULATION_SIZE; i++) {
            double fitness = FitnessUtil.fitness(initialPopulation[i], this.locations);
            hyenas[i] = new Hyena(initialPopulation[i], fitness);
            if (i < 5 || i > POPULATION_SIZE - 6) { // Only print first 5 and last 5 individuals to avoid too much information
                System.out.println("Hyena #" + i + ": Fitness = " + fitness);
            } else if (i == 5) {
                System.out.println("...");
            }
        }

        // Find the best initial Hyena
        System.out.println("\nFinding the best initial Hyena...");
        updateBestHyena();
        System.out.println("Best initial Hyena has fitness = " + bestHyena.getFitness());

        // Log information about the initial solution
        Solution currentBestSolution = bestHyena.getSolution();
        WriteLogUtil.getInstance().logInitialInfo(bestHyena.getFitness());
        WriteLogUtil.getInstance().logInitialSolution(currentBestSolution.getRoutes(), currentBestSolution.getDistances());

        // Main loop of the SHO algorithm
        System.out.println("\n===== STARTING MAIN LOOP =====\n");

        while (!isTerminationCriteriaMet()) {
            System.out.println("\n----- Iteration #" + currentIteration + " / " + MAX_ITERATIONS + " -----");
            WriteLogUtil.getInstance().logIterationInfo(currentIteration, MAX_ITERATIONS, bestHyena.getFitness());
            for (Hyena hyena : hyenas) {
                // Assumed distance between hyena and prey
                double D = Math.abs(B * bestHyena.getFitness() - hyena.getFitness());

                // Update Hyena position
                Solution newSolution = updatePosition(hyena.getSolution(), bestHyena.getSolution(), E, D);
                Hyena newHyena = new Hyena(newSolution, FitnessUtil.fitness(newSolution, this.locations));

                // Update if the new position is better for this hyena
                if (newHyena.getFitness() < hyena.getFitness()) {
                    hyena.setSolution(newSolution);
                    hyena.setFitness(newHyena.getFitness());
                }
            }

            // Update coefficients
            h = ENCIRCLING_COEFFICIENT - (currentIteration * (ENCIRCLING_COEFFICIENT / MAX_ITERATIONS));
            E = 2 * h * random.nextDouble() - h;
            B = 2 * random.nextDouble();
            
            System.out.println("New coefficients: h = " + h + ", E = " + E + ", B = " + B);

            // Update best Hyena
            double oldBestFitness = bestHyena.getFitness();
            updateBestHyena();
            
            // Print information about the best Hyena
            if (bestHyena.getFitness() < oldBestFitness) {
                System.out.println("Found a better solution! New fitness = " + bestHyena.getFitness() + 
                                   " (improvement " + (oldBestFitness - bestHyena.getFitness()) + ")");
            } else {
                System.out.println("Current best fitness = " + bestHyena.getFitness());
            }

            currentIteration++;
        }

        System.out.println("\n===== END OF MAIN LOOP =====\n");
        System.out.println("Completed " + (currentIteration - 1) + " iterations.");
        System.out.println("Final best fitness = " + bestHyena.getFitness());
        
        // Update routes for vehicles from the optimal solution
        System.out.println("\n===== ROUTE ALLOCATION FOR VEHICLES =====\n");
        Solution finalBestSolution = bestHyena.getSolution();
        double[][] bestDistances = finalBestSolution.getDistances();
        boolean[] assignedPoints = new boolean[bestDistances.length];
        
        System.out.println("Number of points to allocate: " + bestDistances.length);
        System.out.println("Number of available vehicles: " + vehicles.length);
        
        // Calculate distance from each point to the depot
        double[] depotDistances = new double[bestDistances.length];
        Point depot = vehicles[0].getPoint();
        for (int i = 0; i < bestDistances.length; i++) {
            depotDistances[i] = calculateDistance(locations[i], new Location(depot, 0));
        }
        
        // Allocate points to vehicles based on distance to depot
        int pointsPerVehicle = bestDistances.length / vehicles.length;
        int remainingPoints = bestDistances.length % vehicles.length;
        
        System.out.println("Average points per vehicle: " + pointsPerVehicle + 
                           (remainingPoints > 0 ? " (" + remainingPoints + " first vehicles will have 1 additional point)" : ""));
        
        // Sort points by distance to depot
        Integer[] sortedPoints = new Integer[bestDistances.length];
        for (int i = 0; i < bestDistances.length; i++) {
            sortedPoints[i] = i;
        }
        Arrays.sort(sortedPoints, (a, b) -> Double.compare(depotDistances[a], depotDistances[b]));
        
        // Allocate points to each vehicle
        int currentIndex = 0;
        for (int i = 0; i < vehicles.length; i++) {
            int numPoints = pointsPerVehicle + (i < remainingPoints ? 1 : 0);
            if (numPoints == 0) {
                vehicles[i].setRoute(new int[0]);
                continue;
            }
            
            int[] route = new int[numPoints];
            int routeIndex = 0;
            
            // Get points for this vehicle
            for (int j = 0; j < numPoints && currentIndex < sortedPoints.length; j++) {
                route[routeIndex++] = sortedPoints[currentIndex++];
                assignedPoints[route[routeIndex - 1]] = true;
            }
            
            // Optimize route for this vehicle
            if (routeIndex > 0) {
                int[] optimizedRoute = new int[routeIndex];
                boolean[] visited = new boolean[routeIndex];
                
                // Start from the point closest to the depot
                double minDepotDist = Double.MAX_VALUE;
                int firstPoint = 0;
                for (int j = 0; j < routeIndex; j++) {
                    double dist = calculateDistance(locations[route[j]], new Location(depot, 0));
                    if (dist < minDepotDist) {
                        minDepotDist = dist;
                        firstPoint = j;
                    }
                }
                
                optimizedRoute[0] = route[firstPoint];
                visited[firstPoint] = true;
                int currentPoint = firstPoint;
                
                // Build route based on nearest point principle
                for (int j = 1; j < routeIndex; j++) {
                    double minDistance = Double.MAX_VALUE;
                    int nextPoint = -1;
                    
                    for (int k = 0; k < routeIndex; k++) {
                        if (!visited[k]) {
                            double distance = calculateDistance(locations[route[currentPoint]], locations[route[k]]);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nextPoint = k;
                            }
                        }
                    }
                    
                    if (nextPoint != -1) {
                        optimizedRoute[j] = route[nextPoint];
                        visited[nextPoint] = true;
                        currentPoint = nextPoint;
                    }
                }
                
                vehicles[i].setRoute(optimizedRoute);
                
                // Update distance matrix for the new route
                for (int j = 0; j < optimizedRoute.length - 1; j++) {
                    bestDistances[optimizedRoute[j]][i] = calculateDistance(locations[optimizedRoute[j]], locations[optimizedRoute[j + 1]]);
                }
            }
        }
        
        // Print information about vehicle routes
        System.out.println("\n===== VEHICLE ROUTE INFORMATION =====\n");
        for (int i = 0; i < vehicles.length; i++) {
            System.out.println("Vehicle #" + i + ":");
            int[] route = vehicles[i].getRoute();
            if (route.length == 0) {
                System.out.println("  No points");
            } else {
                System.out.print("  Route: ");
                for (int j = 0; j < route.length; j++) {
                    System.out.print(route[j]);
                    if (j < route.length - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println();
                
                // Print detailed information about points in the route
                System.out.println("  Point details:");
                for (int j = 0; j < route.length; j++) {
                    Location location = locations[route[j]];
                    System.out.println("    Point #" + route[j] + ": (" + 
                                       location.getPoint().getX() + ", " + 
                                       location.getPoint().getY() + ")");
                }
            }
            System.out.println();
        }
        
        // Log information about the final optimal solution
        WriteLogUtil.getInstance().logFinalResult(bestHyena.getFitness(), finalBestSolution.getRoutes(), finalBestSolution.getDistances());
        
        System.out.println("\n===== END OF SPOTTED HYENA OPTIMIZER ALGORITHM =====\n");
        
        return finalBestSolution;
    }

    // Initialize initial population
    private Solution[] initPopulation(Solution solution) {
        SimulatedAnnealing sa = new SimulatedAnnealing(solution);
        return sa.run();
    }

    private void updateBestHyena() {
        Hyena oldBest = bestHyena;
        bestHyena = Arrays.stream(hyenas)
                .min((h1, h2) -> Double.compare(h1.getFitness(), h2.getFitness()))
                .orElse(hyenas[0]);
        
        // Print information about the 5 Hyenas with the best fitness
        Hyena[] sortedHyenas = Arrays.copyOf(hyenas, hyenas.length);
        Arrays.sort(sortedHyenas, (h1, h2) -> Double.compare(h1.getFitness(), h2.getFitness()));
        
        System.out.println("Top 5 Hyenas with best fitness:");
        for (int i = 0; i < Math.min(5, sortedHyenas.length); i++) {
            System.out.println("  #" + (i+1) + ": Fitness = " + sortedHyenas[i].getFitness());
        }
    }

    private boolean isTerminationCriteriaMet() {
        return currentIteration >= MAX_ITERATIONS;
    }

    private Solution updatePosition(Solution currentSolution, Solution bestSolution, double E, double D) {
        Solution newSolution = currentSolution.copy();
        double[][] distances = newSolution.getDistances();
        
        // Don't print detailed information for each individual to avoid too much information
        
        // Update routes for vehicles
        for (int i = 0; i < vehicles.length; i++) {
            int[] currentRoute = vehicles[i].getRoute();
            if (currentRoute.length > 0) {
                // Update distance matrix for the current route
                for (int j = 0; j < currentRoute.length - 1; j++) {
                    distances[currentRoute[j]][i] = calculateDistance(locations[currentRoute[j]], locations[currentRoute[j + 1]]);
                }
            }
        }
        
        int rows = distances.length;
        int cols = distances[0].length;

        // Calculate change range based on D
        int changeRange = (int) Math.max(1, Math.min(5, D));

        // Probability of applying transformation operators based on encircling coefficient E
        double operatorProbability = Math.abs(E);

        if (operatorProbability > 0.5) { // Exploration phase - Strong changes
            // Perform random swaps of points in the route
            for (int k = 0; k < changeRange; k++) {
                int vehicleIndex = random.nextInt(vehicles.length);
                Vehicle vehicle = vehicles[vehicleIndex];
                int[] route = vehicle.getRoute();
                
                if (route.length > 1) {
                    // Choose two random positions in the route to swap
                    int pos1 = random.nextInt(route.length);
                    int pos2 = random.nextInt(route.length);
                    
                    // Check feasibility of the swap
                    Location loc1 = locations[route[pos1]];
                    Location loc2 = locations[route[pos2]];
                    
                    if (CheckConditionUtil.isInsertionFeasible(loc1, loc2, route[pos1], route[pos2], vehicle)) {
                        // Swap points in the route
                        int temp = route[pos1];
                        route[pos1] = route[pos2];
                        route[pos2] = temp;
                        
                        // Update distance matrix
                        updateDistanceMatrix(distances, route, vehicleIndex);
                    }
                }
            }
        } else { // Exploitation phase - Slight changes
            // Local optimization by adjusting the order of points in the route
            for (int i = 0; i < vehicles.length; i++) {
                Vehicle vehicle = vehicles[i];
                int[] route = vehicle.getRoute();
                
                if (route.length > 2) {
                    // Choose a sub-segment in the route to optimize
                    int start = random.nextInt(route.length - 2);
                    int end = Math.min(start + 2, route.length - 1);
                    
                    // Try reversing the sub-segment to improve distance
                    double currentDistance = calculateRouteDistance(route, start, end, distances);
                    int[] tempRoute = route.clone();
                    reverseSubRoute(tempRoute, start, end);
                    double newDistance = calculateRouteDistance(tempRoute, start, end, distances);
                    
                    if (newDistance < currentDistance) {
                        // If reversing improves distance, apply the change
                        System.arraycopy(tempRoute, 0, route, 0, route.length);
                        updateDistanceMatrix(distances, route, i);
                    }
                }
            }
        }

        return newSolution;
    }

    private void updateDistanceMatrix(double[][] distances, int[] route, int vehicleIndex) {
        // Update distance matrix based on the new route
        for (int i = 0; i < route.length - 1; i++) {
            distances[route[i]][vehicleIndex] = calculateDistance(locations[route[i]], locations[route[i + 1]]);
        }
    }

    private double calculateDistance(Location loc1, Location loc2) {
        Point p1 = loc1.getPoint();
        Point p2 = loc2.getPoint();
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }

    private double calculateRouteDistance(int[] route, int start, int end, double[][] distances) {
        double totalDistance = 0;
        for (int i = start; i < end; i++) {
            totalDistance += distances[route[i]][route[i + 1]];
        }
        return totalDistance;
    }

    private void reverseSubRoute(int[] route, int start, int end) {
        while (start < end) {
            int temp = route[start];
            route[start] = route[end];
            route[end] = temp;
            start++;
            end--;
        }
    }
}
