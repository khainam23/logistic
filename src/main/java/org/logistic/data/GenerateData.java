package org.logistic.data;

import java.util.Random;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Vehicle;

public class GenerateData {
    private static final int MIN_X = 0;
    private static final int MAX_X = 100;
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 100;
    private static final int MIN_DEMAND = 1;
    private static final int MAX_DEMAND = 20;
    private static final int MIN_SERVICE_TIME = 5;
    private static final int MAX_SERVICE_TIME = 30;
    private static final int MIN_TIME_WINDOW = 0;
    private static final int MAX_TIME_WINDOW = 480; // 8 hours in minutes
    private static final Random random = new Random();

    public static Location[] generateLocations(int numPickupPoints) {
        Location[] locations = new Location[numPickupPoints * 2]; // For both pickup and delivery points
        
        for (int i = 0; i < numPickupPoints; i++) {
            // Generate pickup point
            Point pickupPoint = generateRandomPoint();
            int demand = generateRandomDemand();
            int serviceTime = generateRandomServiceTime();
            int[] pickupTimeWindow = generateTimeWindow();
            
            locations[i] = Location.builder()
                    .point(pickupPoint)
                    .serviceTimePick(serviceTime)
                    .serviceTimeDeliver(0)
                    .demandPick(demand)
                    .demandDeliver(0)
                    .ltw(pickupTimeWindow[0])
                    .utw(pickupTimeWindow[1])
                    .isPick(true)
                    .isDeliver(false)
                    .build();
            
            // Generate corresponding delivery point
            Point deliveryPoint = generateRandomPoint();
            int deliveryServiceTime = generateRandomServiceTime();
            int[] deliveryTimeWindow = generateTimeWindow(pickupTimeWindow[1]);
            
            locations[numPickupPoints + i] = Location.builder()
                    .point(deliveryPoint)
                    .serviceTimePick(0)
                    .serviceTimeDeliver(deliveryServiceTime)
                    .demandPick(0)
                    .demandDeliver(-demand) // Negative demand for delivery
                    .ltw(deliveryTimeWindow[0])
                    .utw(deliveryTimeWindow[1])
                    .isPick(false)
                    .isDeliver(true)
                    .build();
        }
        
        return locations;
    }
    
    private static Point generateRandomPoint() {
        int x = random.nextInt(MAX_X - MIN_X + 1) + MIN_X;
        int y = random.nextInt(MAX_Y - MIN_Y + 1) + MIN_Y;
        return new Point(x, y);
    }
    
    private static int generateRandomDemand() {
        return random.nextInt(MAX_DEMAND - MIN_DEMAND + 1) + MIN_DEMAND;
    }
    
    private static int generateRandomServiceTime() {
        return random.nextInt(MAX_SERVICE_TIME - MIN_SERVICE_TIME + 1) + MIN_SERVICE_TIME;
    }
    
    private static int[] generateTimeWindow() {
        int start = random.nextInt(MAX_TIME_WINDOW - MIN_TIME_WINDOW + 1) + MIN_TIME_WINDOW;
        int end = start + random.nextInt(MAX_TIME_WINDOW - start + 1);
        return new int[]{start, end};
    }
    
    private static int[] generateTimeWindow(int minStart) {
        int start = random.nextInt(MAX_TIME_WINDOW - minStart + 1) + minStart;
        int end = start + random.nextInt(MAX_TIME_WINDOW - start + 1);
        return new int[]{start, end};
    }
    
    public static Vehicle[] generateVehicles(int numVehicles, Point depot, int capacity) {
        Vehicle[] vehicles = new Vehicle[numVehicles];
        for (int i = 0; i < numVehicles; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setPoint(depot);
            vehicle.setCapacity(capacity);
            vehicle.setRoute(new int[0]); // Empty route initially
            vehicles[i] = vehicle;
        }
        return vehicles;
    }
}
