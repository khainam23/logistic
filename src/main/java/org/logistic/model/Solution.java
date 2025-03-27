package org.logistic.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
public class Solution {
    double[][] distances;

    public Solution(double[][] distances) {
        this.distances = distances;
    }

    public double[][] getDistances() {
        return distances;
    }

    public Solution copy() {
        int rows = this.distances.length;
        int cols = this.distances[0].length;
        double[][] newDistances = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newDistances[i][j] = this.distances[i][j];
            }
        }
        return new Solution(newDistances);
    }

    public List<List<Integer>> getRoutes() {
        List<List<Integer>> routes = new ArrayList<>();
        int n = distances.length;
        boolean[] visited = new boolean[n];
        visited[0] = true; // Đánh dấu depot đã được thăm

        List<Integer> currentRoute = new ArrayList<>();
        currentRoute.add(0); // Bắt đầu từ depot

        for (int i = 1; i < n; i++) {
            if (!visited[i]) {
                int currentPoint = i;
                visited[i] = true;
                currentRoute.add(i);

                // Tìm điểm tiếp theo gần nhất
                while (true) {
                    double minDistance = Double.MAX_VALUE;
                    int nextPoint = -1;

                    for (int j = 1; j < n; j++) {
                        if (!visited[j] && distances[currentPoint][j] > 0 && distances[currentPoint][j] < minDistance) {
                            minDistance = distances[currentPoint][j];
                            nextPoint = j;
                        }
                    }

                    if (nextPoint == -1) break;

                    currentPoint = nextPoint;
                    visited[nextPoint] = true;
                    currentRoute.add(nextPoint);
                }

                currentRoute.add(0); // Kết thúc tại depot
                routes.add(new ArrayList<>(currentRoute));
                currentRoute.clear();
                currentRoute.add(0);
            }
        }

        return routes;
    }
}
