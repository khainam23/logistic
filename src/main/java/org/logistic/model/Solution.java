package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.logistic.util.FitnessUtil;

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
}
