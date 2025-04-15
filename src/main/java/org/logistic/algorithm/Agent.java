package org.logistic.algorithm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Solution;

/**
 * Lớp cơ sở cho các đối tượng trong thuật toán tối ưu hóa
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class Agent {
    Solution solution; // Lời giải hiện tại
    double fitness; // Giá trị fitness của lời giải
}