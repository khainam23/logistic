package org.logistic.algorithm.sho;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class Hyena {
    Solution solution; // Lời giải hiện tại
    double fitness; // Giá trị fitness của lời giải
    
    public Hyena copy() {
        return new Hyena(this.solution.copy(), this.fitness);
    }
}
