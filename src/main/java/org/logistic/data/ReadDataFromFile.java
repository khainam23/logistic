package org.logistic.data;

import lombok.Getter;
import org.logistic.model.Location;
import org.logistic.model.Point;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ReadDataFromFile {
    private final List<Location> locations = new ArrayList<>();

    public void dataOfVrptw(String filePath) {
        readFile(filePath, false);
    }

    public void dataOfPdptw(String filePath) {
        readFile(filePath, true);
    }

    private void readFile(String filePath, boolean isPdptw) {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).getPath());

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (count >= (isPdptw ? 1 : 9)) {
                    String[] parts = line.trim().split("\\s+");

                    int id = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[isPdptw ? 2 : 1]);
                    int y = Integer.parseInt(parts[isPdptw ? 3 : 2]);
                    int demand = Integer.parseInt(parts[isPdptw ? 4 : 3]);
                    int ltw = Integer.parseInt(parts[isPdptw ? 5 : 4]);
                    int utw = Integer.parseInt(parts[isPdptw ? 6 : 5]);
                    int service = Integer.parseInt(parts[isPdptw ? 7 : 6]);

                    locations.add(Location.builder()
                            .point(new Point(x, y))
                            .serviceTimePick(service)
                            .serviceTimeDeliver(service)
                            .demandDeliver(demand)
                            .demandPick(demand)
                            .ltw(ltw)
                            .utw(utw)
                            .build());
                }
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}
