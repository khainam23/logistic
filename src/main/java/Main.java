import org.algorithm.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    private static final String PATH = Objects.requireNonNull(Main.class.getClassLoader().getResource("")).getPath();

    //These are the variables which can be changed
    private static final int REQUESTS = 50;//Any value from: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50
    private static final boolean READ_FROM_DATASET = true;//Test on the same dataset used in the report, or generate new dataset
    private static final int ITERATIONS = 1;//Number of times to retest the same algorithm on the same dataset. Note that each test takes up to 1 minute.
    private static final int ALGORITHM = 0;//Choose which algorithm to test. 0 = Ant Colony, 1 = Cuckoo Search, 2 = Tabu Search


    //Do not change anything below this line
    private static ArrayList<Location> locations = new ArrayList<Location>();
    private static int[][] distances = new int[REQUESTS * 2 + 1][REQUESTS * 2 + 1];
    private static int capacity = 0;

    public static void main(String[] args) {
        //Check a valid algorithm was chosen:
        if(ALGORITHM>=0 && ALGORITHM<=2) {
            if (READ_FROM_DATASET) {
                //Only values from 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 are allowed
                if (REQUESTS > 0 && REQUESTS < 51 && REQUESTS % 5 == 0) {
                    //Test the same dataset used in the report
                    readFromFile();
                } else {
                    System.out.println("Error, incorrect REQUEST number." +
                            "\nPlease use one of the following: 5, 10, 15, 20, 25, 30, 35, 40, 45, 50.\n");
                    System.exit(0);
                }
            } else {
                //Generate new dataset
                Generator g = new Generator();
                g.generateData(REQUESTS, 10);
                locations = g.getLocations();
                distances = g.getDistances();
                capacity = g.getCapacity();
            }


            Result[] results = new Result[ITERATIONS];

            //Running the tests
            if (ALGORITHM == 0) {
                // Ant Colony Optimization
                for (int i = 0; i < ITERATIONS; i++) {
                    System.out.println("Ant Colony Optimization:");
                    AntColonyOptimization ACO = new AntColonyOptimization(locations, distances, capacity);
                    results[i] = ACO.go();
                }
            } else if (ALGORITHM == 1) {
                //Cuckoo Search
                for (int i = 0; i < ITERATIONS; i++) {
                    CuckooSearch mycuckoo = new CuckooSearch(locations, distances, capacity);
                    /* ("numberofnests/population" recommended 15-25, "numberofIterations" recommended 500-1000 *but sometimes up to 2000 is fine
                     *  , The running time in milliseconds can be set) default is 1 minute = 60 000 ms
                     */
                    results[i] = mycuckoo.cuckooSearch(25, 2000, 60000);
                    System.out.printf("Valid: %b  cost: %d  Time Of First Valid Solution: %s ms   Number Of Broken Constraints: %d", results[i].isValid(),
                            results[i].getCost(), "" + results[i].getTimeOfFirstValidSolution(), results[i].getNumberOfConstraintsBroken());
                    System.out.println();
                }
            } else if (ALGORITHM == 2) {
                // Tabu Search
                for (int i = 0; i < ITERATIONS; i++) {
                    TabuSearch tabu = new TabuSearch(locations, distances, capacity);
                    System.out.println("Obtaining Initial Solution");
                    Route route = tabu.getFeasableRoute();
                    System.out.println("Begin Fitness " + route.getFitnessValue());
                    for (int k = 0; k < route.getRoute().length; k++) {
                        System.out.print(route.getRoute()[k] + " ");
                    }
                    System.out.println();
                    System.out.println(route.isValid());
                    System.out.println("--------------------------------------------");

                    tabu.main(route);
                    results[i] = tabu.getResult();
                    System.out.println("Valid : " + tabu.getResult().isValid() + " | Cost : " + tabu.getResult().getCost() + " | Time of First Valid Solution : " + tabu.getResult().getTimeOfFirstValidSolution() + " | Number of constraints broken : " + tabu.getResult().getNumberOfConstraintsBroken());
                }
            }

            //Print the best and average results
            printResults(results);
        }else{
            System.out.println("Error, incorrect ALGORITHM number." +
                    "\nPlease use one of the following: 0: Ant Colony, 1: Cuckoo Search, 2: Tabu Search.\n");
            System.exit(0);
        }
    }

    private static void printResults(Result[] r) {
        int countValid = 0;
        int costTotal = 0;
        int bestCost = Integer.MAX_VALUE;
        long firstTimeTotal = 0;
        long firstTimeBest = Integer.MAX_VALUE;
        int constraintTotal = 0;
        int constraintBest = Integer.MAX_VALUE;
        for (int i = 0; i < r.length; i++) {
            if (r[i].isValid()) {
                countValid++;
                int cost = r[i].getCost();
                costTotal += cost;
                if (cost < bestCost)
                    bestCost = cost;
                long firstTime = r[i].getTimeOfFirstValidSolution();
                firstTimeTotal += firstTime;
                if (firstTime < firstTimeBest)
                    firstTimeBest = firstTime;

            }
            int constraint = r[i].getNumberOfConstraintsBroken();
            constraintTotal += constraint;
            if (constraint < constraintBest)
                constraintBest = constraint;
        }
        System.out.println(constraintTotal);
        double averageCost = -1;
        long averageFirstTime = -1;
        if (countValid > 0) {
            averageCost = ((double) costTotal) / countValid;
            averageFirstTime = firstTimeTotal / countValid;
        } else {
            bestCost = -1;
            firstTimeBest = -1;
        }

        double averageConstraint = 0;
        if (countValid < r.length) {
            averageConstraint = ((double) constraintTotal) / ((double) (r.length - countValid));
        }

        System.out.println();
        System.out.println(String.format("Valid: %s/%s", countValid, r.length));
        System.out.println(String.format("Best cost: %s", bestCost));
        System.out.println(String.format("Average cost: %s", averageCost));
        System.out.println(String.format("Best time to first valid solution: %sms", firstTimeBest));
        System.out.println(String.format("Average time to first valid solution: %sms", averageFirstTime));
        System.out.println(String.format("Fewest constraints broken: %s", constraintBest));
        System.out.println(String.format("Average constrains broken: %s", averageConstraint));
    }

    public static void readFromFile() {
        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(PATH + REQUESTS + "locations.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                Location location = new Location(data[0].equals("true"), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                location.setLTW(Integer.parseInt(data[4]));
                location.setUTW(Integer.parseInt(data[5]));
                locations.add(location);
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(PATH + REQUESTS + "distances.csv"));
            String row;
            int j = 0;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                for (int i = 0; i < data.length; i++) {
                    distances[i][j] = Integer.parseInt(data[i]);
                }
                j++;
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(PATH + REQUESTS + "capacity.csv"));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                capacity = Integer.parseInt(data[0]);
            }
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

