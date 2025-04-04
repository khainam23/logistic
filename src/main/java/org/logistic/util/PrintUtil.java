package org.logistic.util;

import org.logistic.model.Location;

public class PrintUtil {
    public static void printLocation(Location location) {
        if (location == null) {
            System.out.println("Location: null");
            return;
        }
        
        System.out.println("Location:");
        System.out.println("  Point: (" + location.getPoint().getX() + ", " + location.getPoint().getY() + ")");
        System.out.println("  Service Time Pick: " + location.getServiceTimePick());
        System.out.println("  Service Time Deliver: " + location.getServiceTimeDeliver());
        System.out.println("  Demand Pick: " + location.getDemandPick());
        System.out.println("  Demand Deliver: " + location.getDemandDeliver());
        System.out.println("  Time Window: [" + location.getLtw() + ", " + location.getUtw() + "]");
        System.out.println("  Is Pick: " + location.isPick());
        System.out.println("  Is Deliver: " + location.isDeliver());
    }

    public static void printLocations(Location[] locations) {
        if (locations == null) {
            System.out.println("Locations array: null");
            return;
        }
        
        if (locations.length == 0) {
            System.out.println("Locations array: empty");
            return;
        }
        
        System.out.println("Locations array (size: " + locations.length + "):");
        for (int i = 0; i < locations.length; i++) {
            System.out.println("Location #" + i + ":");
            if (locations[i] == null) {
                System.out.println("  null");
            } else {
                System.out.println("  Point: (" + locations[i].getPoint().getX() + ", " + locations[i].getPoint().getY() + ")");
                System.out.println("  Service Time Pick: " + locations[i].getServiceTimePick());
                System.out.println("  Service Time Deliver: " + locations[i].getServiceTimeDeliver());
                System.out.println("  Demand Pick: " + locations[i].getDemandPick());
                System.out.println("  Demand Deliver: " + locations[i].getDemandDeliver());
                System.out.println("  Time Window: [" + locations[i].getLtw() + ", " + locations[i].getUtw() + "]");
                System.out.println("  Is Pick: " + locations[i].isPick());
                System.out.println("  Is Deliver: " + locations[i].isDeliver());
            }
            System.out.println(); // Add empty line for better readability
        }
    }
}
