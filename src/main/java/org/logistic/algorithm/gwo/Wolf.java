package org.logistic.algorithm.gwo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class Wolf {
    Solution solution; // Lời giải hiện tại
    double fitness; // Giá trị fitness của lời giải
}