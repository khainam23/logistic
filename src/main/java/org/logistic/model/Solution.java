package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.logistic.util.FitnessUtil;

import java.util.Arrays;
import java.util.Objects;

@Data
@Getter
@Setter
@AllArgsConstructor
public class Solution {
    Route[] routes; // Các tuyến đường sẽ đi
    double fitness; // Điểm được đánh giá của giải pháp

    public Solution copy() {
        Route[] newRoutes = new Route[this.routes.length];
        for (int i = 0; i < this.routes.length; i++) {
            newRoutes[i] = routes[i].copy();
        }
        return new Solution(newRoutes, this.fitness);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Double.compare(fitness, solution.fitness) == 0 && Objects.deepEquals(routes, solution.routes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(routes), fitness);
    }
}
