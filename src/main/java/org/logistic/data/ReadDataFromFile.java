package org.logistic.data;

import lombok.Getter;
import org.logistic.model.Location;
import org.logistic.model.Point;
import org.logistic.model.Route;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Getter
public class ReadDataFromFile {
    Location[] locations;
    Route[] routes;
    int maxCapacity;
    Random rd = new Random();

    public enum ProblemType {
        VRPTW,
        PDPTW,
        // Thêm các thuật toán khác ở đây
    }

    public void dataOfProblem(String filePath, ProblemType problemType) {
        try {
            switch (problemType) {
                case VRPTW:
                    readData(filePath, false);
                    break;
                case PDPTW:
                    readData(filePath, true);
                    break;
                // Xử lý các thuật toán khác ở đây
                default:
                    throw new IllegalArgumentException("Unsupported problem type: " + problemType);
            }
        } catch (URISyntaxException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void readData(String filePath, boolean isPdptw) throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(
                ReadDataFromFile.class.getClassLoader().getResource(filePath)).toURI());

        List<Location> locationList = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if(count == (isPdptw ? 0 : 4)) {
                    String[] parts = line.trim().split("\\s+");
                    maxCapacity = Integer.parseInt(parts[1]);
                }

                if (count >= (isPdptw ? 1 : 9)) {
                    String[] parts = line.trim().split("\\s+");

                    int id = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[isPdptw ? 2 : 1]);
                    int y = Integer.parseInt(parts[isPdptw ? 3 : 2]);
                    int demand = Integer.parseInt(parts[isPdptw ? 4 : 3]);
                    int ltw = Integer.parseInt(parts[isPdptw ? 5 : 4]);
                    int utw = Integer.parseInt(parts[isPdptw ? 6 : 5]);
                    int service = Integer.parseInt(parts[isPdptw ? 7 : 6]);

                    Location location = Location.builder()
                            .point(new Point(x, y))
                            .serviceTimePick(0)
                            .serviceTimeDeliver(service)
                            .ltw(ltw)
                            .utw(utw)
                            .build();

                    // Nếu đây là giải pháp của pdptw
                    if(isPdptw) {
                        if(demand < 0) {
                            location.setPick(true);
                            location.setDemandPick(Math.abs(demand));
                        } else {
                            location.setDeliver(true);
                            location.setDemandDeliver(demand);
                        }
                    } else {
                        // vrptw
                        location.setDeliver(true);
                        location.setDemandDeliver(demand);
                    }

                    locationList.add(location);
                }
                count++;
            }
            // Chuyển List<Location> thành mảng
            locations = locationList.toArray(new Location[0]);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    public void readSolution(String filePath) throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(
                ReadDataFromFile.class.getClassLoader().getResource(filePath)).toURI());

        List<Route> routeList = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (count >= 7) {
                    String[] parts = line.substring(line.indexOf(':') + 1).trim().split("\\s+");
                    int[] indLocs = new int[parts.length];
                    for (int i = 0; i < parts.length; ++i) {
                        indLocs[i] = Integer.parseInt(parts[i]);
                    }

                    Route route = new Route(indLocs, maxCapacity);

                    routeList.add(route);
                }
                count++;
            }
            // Chuyển List<Route> thành mảng
            routes = routeList.toArray(new Route[0]);

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}
