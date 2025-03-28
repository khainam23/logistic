package org.logistic.data;

import lombok.Getter;
import org.logistic.model.Location;
import org.logistic.model.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ReadDataFromFile {
    private List<Location> locations;

    public ReadDataFromFile() {
        this.locations = new ArrayList<>();
    }

    public void dataOfVrptw(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getPath())));
            String line;
            int count = 0;
            String name;
            String[] infoVehicle;
            locations = new ArrayList<>();
            while((line = reader.readLine()) != null) {
                // Tên
                if(count == 0) {
                    name = line;
                } else if(count == 4){
                    infoVehicle = line.trim().split("\\s+");
                } else if(count >= 9) {
                    String[] parts = line.trim().split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int demand = Integer.parseInt(parts[3]);
                    int ltw = Integer.parseInt(parts[4]);
                    int utw = Integer.parseInt(parts[5]);
                    int service = Integer.parseInt(parts[6]);
                    Location location = Location.builder()
                            .point(new Point(x, y))
                            .serviceTimePick(service)
                            .serviceTimeDeliver(service)
                            .demandDeliver(demand)
                            .demandPick(demand)
                            .ltw(ltw)
                            .utw(utw)
                            .build();

                    locations.add(location);
                }

                ++count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dataOfPdptw(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getPath())));
            String line;
            int count = 0;
            String[] infoVehicle;
            locations = new ArrayList<>();
            while((line = reader.readLine()) != null) {
                if(count == 0){
                    infoVehicle = line.trim().split("\\s+");
                } else if(count >= 1) {
                    String[] parts = line.trim().split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    int task = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    int demand = Integer.parseInt(parts[4]);
                    int ltw = Integer.parseInt(parts[5]);
                    int utw = Integer.parseInt(parts[6]);
                    int service = Integer.parseInt(parts[7]);
                    Location location = Location.builder()
                            .point(new Point(x, y))
                            .serviceTimePick(service)
                            .serviceTimeDeliver(service)
                            .demandDeliver(demand)
                            .demandPick(demand)
                            .ltw(ltw)
                            .utw(utw)
                            .build();

                    locations.add(location);
                }

                ++count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}