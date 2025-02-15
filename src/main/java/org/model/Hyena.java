package org.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class Hyena {
    List<Route> solution;
    double fitness;

    /**
     * Đại diện cho thuật toán Spoted Hyena Optimizer.
     * Mỗi một hyena đại diện cho một giải pháp trong bài toán.
     */
}
