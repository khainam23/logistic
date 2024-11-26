package org.algorithm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Kết quả của điểm
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
public class Result {
    boolean valid; // Thỏa điều kiện?
    int cost; // Chi phí
    long timeOfFirstValidSolution; // Thời gian tại điểm tìm ra lời giải hợp lệ đầu tiên
    int numberOfConstraintsBroken; // Số lượng ràng buộc không thảo mãn
}
