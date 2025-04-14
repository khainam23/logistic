package org.logistic.algorithm.aco;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class Ant {
    Solution solution; // Lời giải hiện tại của kiến
    double fitness; // Giá trị fitness của lời giải
}