package org.algorithm; /**
 *
 */

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class CuckooSearch {
    // Generator getData = new Generator();
    ArrayList<Location> location;
    int[][] distances;
    int capacity;
    double pa = 0.25;
    double lambda = 1.5;
    Random random = new Random();

    public CuckooSearch(ArrayList<Location> loc, int[][] distance, int cap) {
        location = loc;
        distances = distance;
        capacity = cap;
    }
    //-------------------------------------CUCKOO SEARCH ALGO-------------------------------------------------

    public Result cuckooSearch(int numberOfNests, int numberOfIterations, int runningTime) {
        long startTime = System.currentTimeMillis();
        long firstSolutionTime = System.currentTimeMillis();
        //---------------------------Begin Algorithm--------------------------------
        int iter = 0;
        int[][] nests = new int[numberOfNests][location.size()];
        // Initialization of nests
        for (int i = 0; i < nests.length; i++) {
            nests[i] = initialNest();
        }
        nests = sortNests(nests);
        int[] bestSolution = nests[0];
        boolean isFirstValidSolution = isValidRoute(bestSolution);
        if (isFirstValidSolution) {
            firstSolutionTime = System.currentTimeMillis();
        }
        System.out.printf("[Iteration  %d ]: %b   %d   %s \n", iter, isValidRoute(bestSolution), sumOfDistance(bestSolution), Arrays.toString(bestSolution));

        // start iteration
        iter = 1;
        while (iter < numberOfIterations && System.currentTimeMillis() - startTime < runningTime) {
            // Get the cuckoo via Levy Flights
            int[] cuckooNest = getNestLevy(nests, numberOfNests);
            /*
             *  Randomly choose a nest from the population to compare with cuckoo in fitness
             *  if successful the cuckoo replaces the nest
             * */
            int randomNestIndex = random.nextInt(numberOfNests);
            if (fitnessFunction(cuckooNest) < fitnessFunction(nests[randomNestIndex])) {
                nests[randomNestIndex] = cuckooNest;
            }
            /*
             *  remove the worst performing  nests with probability pa = 25 from the population
             *  the new replacement nests are created via levy flights
             */
            int probability = (int) (numberOfNests * pa);
            for (int i = numberOfNests - probability; i < numberOfNests; i++) {
                nests[i] = getNestLevy(nests, numberOfNests);
            }
            nests = sortNests(nests);
            //printSolutions(nests);
            int[] newBestSolution = nests[0];
            if (fitnessFunction(newBestSolution) < fitnessFunction(bestSolution)) {
                bestSolution = newBestSolution;
                if (isValidRoute(bestSolution) && isFirstValidSolution == false) {
                    isFirstValidSolution = true;
                    firstSolutionTime = System.currentTimeMillis();
                }
                System.out.printf("[Iteration  %d ]: %b   %d   %s \n", iter, isValidRoute(bestSolution), sumOfDistance(bestSolution), Arrays.toString(bestSolution));
            }
            iter++;
        }

        //-----------------------------Stop Algorithm----------------------------------
        boolean isValid = isValidRoute(nests[0]);
        int cost = sumOfDistance(nests[0]);
        long timeOfFirstValidSolution = firstSolutionTime - startTime;
        if (isFirstValidSolution == false) {
            timeOfFirstValidSolution = -1;
        }
        int numberOfConstraintsBroken = countConstraintBreaks(nests[0]);
        return new Result(isValid, cost, timeOfFirstValidSolution, numberOfConstraintsBroken);
    }

    //------------------------------------HELPER FUNCTIONS-------------------------------------------------
    // for initializing the nest via random four point swap looped between 0 - 10 times
    int[] initialNest() {
        int[] nest = new int[location.size()];
        List<Integer> pickup = new ArrayList<>();
        for (int i = 0; i < nest.length; i++) {
            nest[i] = i;
            if (i % 2 != 0 && i != 0) {
                pickup.add(i);
            }
        }
        //nest = randomRoute(nest);
        int repeats = random.nextInt(10);
        for (int i = 0; i < repeats; i++) {
            int indexPick1 = random.nextInt(pickup.size());
            int indexPick2 = random.nextInt(pickup.size());
            int indexD1 = indexDelivery(nest, pickup.get(indexPick1));
            int indexD2 = indexDelivery(nest, pickup.get(indexPick2));
            int indexP1 = indexPickup(nest, nest[indexD1]);
            int indexP2 = indexPickup(nest, nest[indexD2]);
            nest = swapLocations(nest, indexP1, indexP2);
            nest = swapLocations(nest, indexD1, indexD2);
        }


        return nest;
    }

    // get cuckoo via levy flight
    int[] getNestLevy(int[][] nests, int numberOfNests) {
        int[] nest = nests[random.nextInt(numberOfNests)];
        if (LevyFlight() < 0.48) {
            nest = sixPointSwapSearch(nest);
            //System.out.println("six point swap");
        } else if (0.48 <= LevyFlight() && LevyFlight() < 0.97) {
            nest = fourPointSwapSearch(nest);
            //System.out.println("four point swap");
        } else {
            nest = twoPointSwapSearch(nest);
            //System.out.println("Two point swap");
        }
        return nest;
    }

    // Levy Flight with u as a random number (0, variance) and v a random number [0.0001, 0.9999]
    public double LevyFlight() {
        double u = randomFunct(variance());
        double v = randomFunct(0.9999);
        return u / Math.pow(v, lambda);
    }

    // Two location points search of the cuckoo solution
    int[] twoPointSwapSearch(int[] cuckooNest) {
        int[] bestRoute = cuckooNest.clone();
        int fitbest = fitnessFunction(bestRoute);
        // generate all solutions and choose the best one.
        for (int i = 2; i < cuckooNest.length; i++) {
            if ((cuckooNest[i] % 2 == 0 && cuckooNest[i - 1] != cuckooNest[i] - 1) || (cuckooNest[i] % 2 != 0 && cuckooNest[i - 1] % 2 != 0)
                    || (cuckooNest[i] % 2 != 0 && cuckooNest[i - 1] % 2 == 0)) {
                int[] currentRoute = swapLocations(cuckooNest.clone(), i - 1, i);
                int fitCurrent = fitnessFunction(currentRoute);
                if (fitCurrent <= fitbest) {
                    bestRoute = currentRoute;
                    fitbest = fitCurrent;
                }
            }
        }
        return bestRoute;
    }

    // four locations swap search
    int[] fourPointSwapSearch(int[] cuckooNest) {
        int[] pickup = new int[(cuckooNest.length - 1) / 2];
        int p = 0;
        for (int i = 1; i < cuckooNest.length; i++) {
            if (cuckooNest[i] % 2 != 0) {
                pickup[p] = cuckooNest[i];
                p++;
            }
        }
        int[] bestRoute = cuckooNest.clone();
        int fitbest = fitnessFunction(bestRoute);
        for (int index1 = 0; index1 < pickup.length; index1++) {
            int indexD1 = indexDelivery(cuckooNest, pickup[index1]);
            int indexP1 = indexPickup(cuckooNest, cuckooNest[indexD1]);
            for (int index2 = 0; index2 < pickup.length; index2++) {
                if (index1 != index2 && index2 > index1) {
                    int indexD2 = indexDelivery(cuckooNest.clone(), pickup[index2]);
                    int indexP2 = indexPickup(cuckooNest.clone(), cuckooNest[indexD2]);
                    int[] currentRoute = swapLocations(cuckooNest.clone(), indexP1, indexP2);
                    currentRoute = swapLocations(currentRoute, indexD1, indexD2);
                    int fitCurrent = fitnessFunction(currentRoute);
                    if (fitCurrent <= fitbest) {
                        bestRoute = currentRoute;
                        fitbest = fitCurrent;
                    }
                }
            }
        }
        return bestRoute;
    }

    // six locations points swap search
    int[] sixPointSwapSearch(int[] cuckooNest) {
        int[] pickup = new int[(cuckooNest.length - 1) / 2];
        int p = 0;
        for (int i = 1; i < cuckooNest.length; i++) {
            if (cuckooNest[i] % 2 != 0) {
                pickup[p] = cuckooNest[i];
                p++;
            }
        }
        int[] bestRoute = cuckooNest.clone();
        int fitbest = fitnessFunction(bestRoute);
        for (int index1 = 0; index1 < pickup.length; index1++) {
            int indexD1 = indexDelivery(cuckooNest, pickup[index1]);
            int indexP1 = indexPickup(cuckooNest, cuckooNest[indexD1]);
            for (int index2 = 0; index2 < pickup.length; index2++) {
                if (index1 != index2 && index2 > index1) {
                    int indexD2 = indexDelivery(cuckooNest.clone(), pickup[index2]);
                    int indexP2 = indexPickup(cuckooNest.clone(), cuckooNest[indexD2]);
                    int[] currentRoute = swapLocations(cuckooNest.clone(), indexP1, indexP2);
                    currentRoute = swapLocations(currentRoute, indexD1, indexD2);
                    for (int index3 = 0; index3 < pickup.length; index3++) {
                        if (index2 != index3 && index2 > index3) {
                            int indexD3 = indexDelivery(cuckooNest, pickup[index3]);
                            int indexP3 = indexPickup(cuckooNest, cuckooNest[indexD3]);
                            currentRoute = swapLocations(currentRoute, indexP2, indexP3);
                            currentRoute = swapLocations(currentRoute, indexD2, indexD3);
                            int fitCurrent = fitnessFunction(currentRoute);
                            if (fitCurrent <= fitbest) {
                                bestRoute = currentRoute;
                                fitbest = fitCurrent;
                            }
                        }
                    }
                }
            }
        }
        return bestRoute;
    }

    //	 swap elements of array
    int[] swapLocations(int[] route, int i, int j) {
        List<Integer> rout = toList(route.clone());
        Collections.swap(rout, i, j);
        return toArray(rout);
    }

    // the fitness function of the nest
    public int fitnessFunction2(int[] route) {
        return (int) (0.05 * sumOfDistance(route) + 0.95 * penaltyFunction(route));
    }

    // the fitness function of the nest
    int fitnessFunction(int[] route) {
        if (isValidRoute(route)) {
            return fitnessFunction2(route);
        }
        return fitnessFunction2(route) + 1000;
    }

    // Calculate the total route distance
    int sumOfDistance(int[] nestRoute) {
        int sum = 0;
        for (int i = 1; i < nestRoute.length; i++) {
            sum += distances[nestRoute[i]][nestRoute[i - 1]];
            if (sum < location.get(nestRoute[i]).getLTW()) {
                int waitingTime = location.get(nestRoute[i]).getLTW() - sum;
                sum += waitingTime;
            }
        }
        return sum;
    }

    //Calculate the capacity penalty of the route
    int capacityPenalty(int[] nestRoute) {
        int penalty = 0;
        int sumOfCapacity = 0;
        for (int loc : nestRoute) {
            if (loc == 0) continue;
            sumOfCapacity += location.get(loc).getLoad();
            if (sumOfCapacity > capacity) penalty += sumOfCapacity - capacity;
        }
        return penalty;
    }

    //Calculate the time penalty for the arriving late to a location
    int timeDelayPenalty(int[] nestRoute) {
        int time = 0;
        int penalty = 0;
        for (int i = 1; i < nestRoute.length; i++) {
            time += distances[nestRoute[i]][nestRoute[i - 1]];
            if (time > location.get(nestRoute[i]).getUTW()) {
                penalty = Math.max(100, 10 * (location.get(nestRoute[i]).getUTW() - time));
            }
        }
        return penalty;
    }

    // sum of the time and capacity penalties
    int penaltyFunction(int[] nestRoute) {
        return timeDelayPenalty(nestRoute) + capacityPenalty(nestRoute);
    }


    // gets index of delivery given a pickup location 
    int indexDelivery(int[] cuckoo, int pickup) {
        int index = -1;
        for (int i = 1; i < cuckoo.length; i++) {
            if (cuckoo[i] == pickup + 1) {
                return i;
            }
        }
        return index;
    }

    // get index of pickup given a delivery location
    int indexPickup(int[] cuckoo, int delivery) {
        int index = -1;
        for (int i = 1; i < cuckoo.length; i++) {
            if (cuckoo[i] == delivery - 1) {
                return i;
            }
        }
        return index;
    }

    // gamma function calculation for the levy distribution
    double logGamma(double x) {
        double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173 / (x + 0) - 86.50532033 / (x + 1)
                + 24.01409822 / (x + 2) - 1.231739516 / (x + 3)
                + 0.00120858003 / (x + 4) - 0.00000536382 / (x + 5);
        return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
    }

    double gamma(double x) {
        return Math.exp(logGamma(x));
    }

    // variance for the levy distribution
    double variance() {
        double variance = ((gamma(1 + lambda) / gamma(0.5 * (1 + lambda))) *
                (Math.sin(Math.PI * 0.5 * lambda) / (0.5 * Math.pow(2, 0.5 * (lambda - 1)))));
        return Math.pow(variance, 1 / lambda);
    }

    // uniform random number generator returns doubles between 0.0001-var for the Levy distribution
    double randomFunct(double var) {
        return random.nextDouble() * (var - 0.0001) + (0.0001);
    }

    // list to array
    int[] toArray(List<Integer> ar) {
        int[] array = new int[ar.size()];
        for (int i = 0; i < ar.size(); i++) array[i] = (int) ar.get(i);
        return array;
    }

    // array to list
    List<Integer> toList(int[] arr) {
        List<Integer> al = new ArrayList<Integer>();
        for (int i = 0; i < arr.length; i++) al.add(arr[i]);
        return al;
    }

    // sort nests in descending order of fitness
    int[][] sortNests(int[][] nests) {
        int[][] sortedNests = new int[nests.length][nests[0].length];
        List<MyNests> set = new ArrayList<MyNests>();
        // map the nest fitness with the index
        for (int i = 0; i < nests.length; i++) {
            set.add(new MyNests(nests[i], fitnessFunction(nests[i])));

        }
        Collections.sort(set);
        for (int i = 0; i < nests.length; i++) {
            sortedNests[i] = set.get(i).getNest();
        }
        return sortedNests;
    }

    void printSolutions(int[][] nests) {
        for (int[] nest : nests) {
            System.out.println(Arrays.toString(nest));
        }
        System.out.println("--------------------------------------------------\n");
    }

    //-----------------------------------------------------------------------------------
    public void resetLocations() {
        for (int i = 1; i < location.size(); i++)
            location.get(i).resetServiced();
    }

    public boolean isValidRoute(int[] route) {
        boolean valid = true;
        resetLocations();
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = location.get(route[i]);
            currentLoad += lo.getLoad();
            currentTime += distances[previousIndex][route[i]];
            previousIndex = route[i];
            location.get(route[i]).setServiced(true);
            if (lo.isPickup())
                location.get(route[i] + 1).setServiceable(true);
            //add any waiting time
            currentTime += Math.max(0, lo.getLTW() - currentTime);
            //Invalid if over capacity or upper time limit
            if (currentTime > lo.getUTW() || currentLoad > capacity) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    // count constraint broken
    int countConstraintBreaks(int[] route) {
        int count = 0;
        resetLocations();
        int currentLoad = 0;
        int currentTime = 0;
        int previousIndex = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = location.get(route[i]);
            currentLoad += lo.getLoad();
            currentTime += distances[previousIndex][route[i]];
            previousIndex = route[i];
            if (!location.get(route[i]).isServiceable() && i > 0)
                count++;//precedence constraint
            if (location.get(route[i]).isServiced() && i > 0)
                count++;//only visit each location once
            location.get(route[i]).setServiced(true);
            if (lo.isPickup())
                location.get(route[i] + 1).setServiceable(true);
            //add any waiting time
            currentTime += Math.max(0, lo.getLTW() - currentTime);
            if (currentTime > lo.getUTW())
                count++;//upper time window constraint
            if (currentLoad > capacity)
                count++;// capacity constraint
        }
        return count;
    }

    /*
     *  This is to map the fitness with a nest index ie nests[index]
     *  this will be used for sorting
     */
    public class MyNests implements Comparable<MyNests> {
        int fitness = 1000000;
        int[] nest;

        MyNests(int[] nest, int fitness) {
            this.fitness = fitness;
            this.nest = nest;
        }

        public int getFitness() {
            return fitness;
        }

        public int[] getNest() {
            return nest;
        }

        public void setNest(int[] nest) {
            this.nest = nest;
        }

        public int compareTo(MyNests n) {
            if (fitness < n.fitness) return -1;
            if (fitness > n.fitness) return 1;
            if (fitness == n.fitness) return 0;
            return 0;
        }
    }
}
