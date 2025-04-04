package org.logistic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Solution {
    Route[] routes; // Các tuyến đường sẽ đi
    double fitness; // Điểm được đánh giá của giải pháp

    public int countVehicles() {
        return routes.length;
    }
}
