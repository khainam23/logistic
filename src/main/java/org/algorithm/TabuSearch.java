package org.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TabuSearch {

    private ArrayList<Location> locations;
    private int[][] distances;
    private int capacity;
    private int[] successfulRoute;
    private ArrayList<Route> tabuList = new ArrayList<Route>();
    ArrayList<Route> neighbourhood = new ArrayList<Route>();
    Route initialSolution;
    Route globalBest;
    Route localBest;
    Route finalSolution;
    Route bestOpNotValid;
    
    boolean valid;
    int cost;
    long timeOfFirstValidSolution;
    int numberOfConstraintsBroken;
    Result result;
    
    public void setResult(boolean valid, int cost, long timeOfFirstValidSolution, int numberOfConstraintsBroken) {
    	result = new Result(valid, cost, timeOfFirstValidSolution, numberOfConstraintsBroken);
    }
    
    public Result getResult() {
    	return result;
    }

    public TabuSearch(ArrayList<Location> locations2, int[][] distances, int capacity) {
        this.locations = locations2;
        this.distances = distances;
        this.capacity = capacity;
    }

    public Route getFeasableRoute() {

        for (int i = 0; i < locations.size(); i++) {
            locations.get(i).resetServiced();
        }

        int visits = 0;
        int time = 0;
        int[] arrivalTime = new int[locations.size()];
        int previousIndex = 0;
        int currentCapacity = 0;
        successfulRoute = new int[locations.size()];
        successfulRoute[0] = 0;//Start at depot
        while (visits < locations.size() - 1) {
            int index = 0;
            double lowestUpperBound = Double.MAX_VALUE;
            for (int i = 1; i < locations.size(); i++) {
                if (isFeasibleLocation(locations.get(i), currentCapacity)) {
                    double upperBound = locations.get(i).getUTW();
                    if (i % 2 == 0)
                        upperBound *= upperBound;
                    else
                        upperBound *= Math.min(locations.get(i + 1).getUTW(), upperBound);
                    if (upperBound < lowestUpperBound) {
                        lowestUpperBound = upperBound;
                        index = i;
                    }
                }
            }
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
        Route route = new Route(successfulRoute, locations, distances, capacity);

        return route;
    }

    private boolean isFeasibleLocation(Location location, int currentCapacity) {
        return (location.isServiceable() && !location.isServiced() && currentCapacity + location.getLoad() <= capacity);
    }

    private int getRandom(int min, int max) {
        Random rn = new Random();
        return rn.nextInt((max - min) + 1) + min;
    }

    public Route newPDRearrangeOperator(Route route1, int pick1, int drop1) {
        int[] newRoute = new int[locations.size()];

        ArrayList<Integer> shifted = new ArrayList<Integer>();

        for (int i = 0; i < route1.getRoute().length; i++) {
            if (route1.getRoute()[i] != pick1 && route1.getRoute()[i] != drop1) {
                shifted.add(route1.getRoute()[i]);
            }
        }

        boolean terminate = true;
        while (terminate) {

            int inputPick = getRandom(1, shifted.size() - 1);
            int inputDrop = getRandom(inputPick + 1, shifted.size());

            shifted.add(inputPick, pick1);
            shifted.add(inputDrop, drop1);
            int currentCapacity = 0;
            for (int i = 1; i < shifted.size(); i++) {
                currentCapacity += locations.get(shifted.get(i)).getLoad();
                if (currentCapacity > capacity) {
                    shifted.clear();
                    for (int j = 0; j < route1.getRoute().length; j++) {
                        if (route1.getRoute()[j] != pick1 && route1.getRoute()[j] != drop1) {
                            shifted.add(route1.getRoute()[j]);
                        }
                    }
                    break;
                } else if (currentCapacity == 0 && i == shifted.size() - 1) {
                    terminate = false;
                } else if (currentCapacity != 0 && i == shifted.size() - 1) {
                    shifted.remove(inputPick);
                    shifted.remove(inputDrop);
                    break;
                }
            }

            for (int i = 0; i < shifted.size(); i++) {
                newRoute[i] = shifted.get(i);
            }
        }
        Route newRoutes = new Route(newRoute, locations, distances, capacity);
        return newRoutes;
    }

    public void main(Route route) {
        initialSolution = route;
        finalSolution = route;
        globalBest = route;
        bestOpNotValid = route;
        Route BestCandidate = route;
        int itr = 0;
        
        if(route.isValid()) {
        	timeOfFirstValidSolution=-1;
        }

        ArrayList<Route> tabuList = new ArrayList<Route>();
        tabuList.add(globalBest);

        long startTime = System.currentTimeMillis();
        while (itr < 1000 && System.currentTimeMillis() - startTime < 60000) {

            if (CheckList(globalBest) == false) {

                BestCandidate = doTabuSearch(globalBest);

                if (BestCandidate.getFitnessValue() < finalSolution.getFitnessValue()) {
                    globalBest = BestCandidate;
                    if (BestCandidate.isValid()) {
                        finalSolution = BestCandidate;
                        System.out.println("New Found Fitness " + globalBest.getFitnessValue());

                        System.out.print("0 ");
                        for (int k = 1; k < globalBest.getRoute().length; k++) {
                            System.out.print(globalBest.getRoute()[k] + " ");
                        }
                        System.out.println();
                        System.out.println("Iteration:" + itr + " " + globalBest.isValid());

                        System.out.println();
                        
                        if(timeOfFirstValidSolution == 0){
                        	timeOfFirstValidSolution=System.currentTimeMillis() - startTime;
                        }
                        	
              
                    } else if (BestCandidate.getFitnessValue() < bestOpNotValid.getFitnessValue()) {
                        bestOpNotValid = BestCandidate;
                        System.out.println("New Found Invalid Fitness " + bestOpNotValid.getFitnessValue());

                        System.out.print("0 ");
                        for (int k = 1; k < bestOpNotValid.getRoute().length; k++) {
                            System.out.print(bestOpNotValid.getRoute()[k] + " ");
                        }
                        System.out.println();
                        System.out.println("Iteration:" + itr + " " + bestOpNotValid.isValid());

                        System.out.println();
                        

                    }

                }
                tabuList.add(globalBest);

            } else {
                if (tabuList.size() > 0) {
                    globalBest = tabuList.get(0);
                    tabuList.remove(0);
                } else {
                    if (itr % 10 == 0) {
                        globalBest = getFeasableRoute();
                    }

                }
            }

            itr++;

        }

        if (finalSolution.isValid()) {
            System.out.println();
            System.out.println("--------------------------------------------");
            System.out.println("Optimized Fitness " + finalSolution.getFitnessValue());
            System.out.print("0 ");

            for (int k = 1; k < finalSolution.getRoute().length; k++) {
                System.out.print(finalSolution.getRoute()[k] + " ");

            }

            System.out.println();
            System.out.println(finalSolution.isValid());
            
            if(timeOfFirstValidSolution==-1) {
            	timeOfFirstValidSolution=0;
            }
            
            setResult(finalSolution.isValid(), finalSolution.getFitnessValue(), timeOfFirstValidSolution, countConstraintBreaks(finalSolution.getRoute()));

        } else {
            System.out.println();
            System.out.println("--------------------------------------------");
            System.out.println("Optimized Invalid Fitness " + bestOpNotValid.getFitnessValue());
            System.out.print("0 ");
            for (int k = 1; k < bestOpNotValid.getRoute().length; k++) {
                System.out.print(bestOpNotValid.getRoute()[k] + " ");
                capacity += locations.get(k).getLoad();

            }

            System.out.println();
            System.out.println(bestOpNotValid.isValid());

            
            setResult(bestOpNotValid.isValid(), bestOpNotValid.getFitnessValue(), -1, countConstraintBreaks(bestOpNotValid.getRoute()));

        }


        if (System.currentTimeMillis() - startTime >= 120000) {
            System.out.println("Two minute has past, the program has terminated");
        }

    }

    public boolean CheckList(Route route) {
        boolean contains = false;
        for (int i = 0; i < tabuList.size(); i++) {
            if (route.getFitnessValue() == tabuList.get(i).getFitnessValue()) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public Route doTabuSearch(Route route) {
        Route bestRoute = route;

        for (int i = 0; i < 2000; i++) {
        	
        	for (int h = 1; h < (locations.size() - 1 / 2); h += 2) {
            
        	  Route newRoute = newPDRearrangeOperator(route, h, h + 1);
              neighbourhood.add(newRoute);
              
        	}
//            if (neighbourhood.size() > 2) {
//
//                Route unionRoute1 = doUnionCrossover(neighbourhood.get(getRandom(0, neighbourhood.size() - 1)), neighbourhood.get(getRandom(0, neighbourhood.size() - 1)));
//                neighbourhood.add(unionRoute1);
//
//            } else if (neighbourhood.size() == 0) {
//                for (int h = 1; h < (locations.size() - 1 / 2); h += 2) {
//                    Route newRoute = newPDRearrangeOperator(route, h, h + 1);
//                    neighbourhood.add(newRoute);
//                    Route neighbour = neighbourhood.get(getRandom(0, neighbourhood.size() - 1));
//                    for (int j = 1; j < (locations.size() - 1 / 2); j += 2) {
//                        neighbour = newPDRearrangeOperator(neighbour, j, j + 1);
//                        if (neighbour.getFitnessValue() < newRoute.getFitnessValue()) {
//                            newRoute = neighbour;
//                        }
//                    }
//                }
//            }
        }
        for (int i = 0; i < neighbourhood.size(); i++) {
            if (neighbourhood.get(i).getFitnessValue() < bestRoute.getFitnessValue()) {
                bestRoute = neighbourhood.get(i);
            }
        }
        return bestRoute;
    }

    public Route doUnionCrossover(Route p1, Route p2) {
        int[] newRoute = new int[locations.size()];

        Map<Integer, Integer> chosen = new HashMap<Integer, Integer>();

        newRoute[0] = 0;
        int counter1 = 1;
        int counter2 = 1;

        for (int i = 1; i < newRoute.length; i++) {
            int P1orP2 = getRandom(1, 2);
            if (P1orP2 == 1) {
                if (!chosen.containsKey(p1.getRoute()[counter1])) {
                    newRoute[i] = p1.getRoute()[counter1];
                    chosen.put(p1.getRoute()[counter1], 0);
                    counter1++;
                } else {
                    counter1++;
                    i--;
                }
            } else {
                if (!chosen.containsKey(p2.getRoute()[counter2])) {
                    newRoute[i] = p2.getRoute()[counter2];
                    chosen.put(p2.getRoute()[counter2], 0);
                    counter2++;
                } else {
                    counter2++;
                    i--;
                }
            }
        }
        Route route = new Route(newRoute, locations, distances, capacity);
        return route;
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
            if (currentLoad > capacity)
                count++;// capacity constraint
        }
        return count;
    }
//	public Route doMergeCrossOver2(Route p1, Route p2) {
//		int randomGene1 = getRandom(1, p1.getRoute().length-1);
//		int randomGene2 = getRandom(1, p1.getRoute().length-1);
//		ArrayList<Integer> inverted = new ArrayList<Integer>();
//		int [] newRoute = new int [locations.size()];
//		while(randomGene1 == randomGene2) {
//			randomGene1 = getRandom(1,p1.getRoute().length-1);
//		}
//		
//		if(randomGene1 > randomGene2) {
//		
//			for(int i = randomGene1; i>= randomGene2; i--) {
//				inverted.add(p1.getRoute()[i]);
//			}
//			
//			for(int i = 0;i < newRoute.length;i++) {
//				if(i < randomGene2 || i > randomGene1) {
//					newRoute[i] = p1.getRoute()[i];
//				}else {
//					newRoute[i] =inverted.get(0);
//					inverted.remove(0);
//				}
//			}
//		}else if(randomGene1 < randomGene2){
//	
//			for(int i = randomGene2; i>= randomGene1; i--) {
//				inverted.add( p1.getRoute()[i]);
//			}
//			for(int i = 0;i < newRoute.length;i++) {
//				if(i < randomGene1 || i > randomGene2) {
//					newRoute[i] =  p1.getRoute()[i];
//				}else {
//					newRoute[i] =inverted.get(0);
//					inverted.remove(0);
//				}
//			}
//		}
//	
//		Route route = new Route(newRoute,locations,distances,capacity);
////		System.out.println();
////		System.out.println("Fitness " + route.getFitnessValue());
////		System.out.print("0 ");
////			int time = 0;
////			for(int k = 1; k < route.getRoute().length;k++) {
////				System.out.print(route.getRoute()[k] + " ");
////				time+=distances[route.getRoute()[k-1]][route.getRoute()[k]];
////				if(time < locations.get(route.getRoute()[k]).getLTW()){
////					time=locations.get(route.getRoute()[k]).getLTW();
////				}				
////			}
////			System.out.println();
////			System.out.println(route.checkValidity());
//		return route;
//	}
//	
//	public Route MX1(Route p1, Route p2) {
//		int [] newRoute = new int [locations.size()];
//		int[] precedenceVector = new int[locations.size()];
//
//    	for(int i = 0;i < locations.size();i++) {
//    		precedenceVector[i] = locations.get(i).getLTW();
//    	}
//
//    	for(int i = 0; i < precedenceVector.length;i++) {
//    		for(int k = i+1;k < precedenceVector.length;k++) {
//    			if(precedenceVector[k] < precedenceVector[i]) {
//    				int tem = precedenceVector[k];
//    				precedenceVector[k] = precedenceVector[i];
//    				precedenceVector[i] = tem;
//    				
//    			}
//    		}
//    	}
//    	 	
//    	for(int i = 0; i < precedenceVector.length;i++) {
//    		for(int k = 0; k < locations.size();k++) {
//    			if(precedenceVector[i] == locations.get(k).getLTW()) {
//    				precedenceVector[i] = k;
//    			}
//    		}
//    	}
//		
//		
//		int[] tempP1 = new int[p1.getRoute().length];
//		int[] tempP2 = new int[p2.getRoute().length];
//		
//		for(int i = 0;i < p1.getRoute().length;i++) {
//			tempP1[i] = p1.getRoute()[i];
//			tempP2[i] = p2.getRoute()[i];
//		}
//		
//		for(int i = 0;i < tempP1.length;i++) {
//			if(tempP1[i] ==tempP2[i]) {
//				newRoute[i] = tempP1[i];
//			}else {
//				int startIndex = 1;
//		
//				while(startIndex < precedenceVector.length) {
//					if(precedenceVector[startIndex] == tempP1[i]) {
//						for(int k = i;k <tempP2.length;k++) {
//							if(tempP1[i] ==  tempP2[k]) {
//								tempP2[k] = tempP2[i];
//							}
//						}
//						
//						newRoute[i] = tempP1[i];
//						
//						break;
//					}else if(precedenceVector[startIndex] == tempP2[i]) {
//						for(int k = i;k < tempP1.length;k++) {
//							if(tempP2[i] ==  tempP1[k]) {
//								tempP1[k] = tempP1[i];
//							}
//						}
//						
//						newRoute[i] = tempP2[i];
//						
//						break;
//					}else {
//						startIndex++;
//					}
//				}
//			}
//		}		
//		Route route = new Route(newRoute,locations,distances,capacity);
//		System.out.println();
//		System.out.println("Fitness " + route.getFitnessValue());
//		System.out.print("0 ");
//			int time = 0;
//			
//			for(int k = 1; k < route.getRoute().length;k++) {
//				System.out.print(route.getRoute()[k] + " ");
//				time+=distances[route.getRoute()[k-1]][route.getRoute()[k]];
//				if(time < locations.get(route.getRoute()[k]).getLTW()){
//					time=locations.get(route.getRoute()[k]).getLTW();
//				}				
//			}
//			System.out.println();
//			System.out.println(route.checkValidity());
//		
//
//		return route;
//	}
//		
}
