package org.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Result {
    String name; // Tên thuật toán
    double fitness; // Điểm của thuật toán này
    double firstTimeSolution; // Thời gian tìm ra lời giải đầu tiên
    double totalDistance; // Tổng quảng đường di chuyển
    double totalTime; // Tổng thời gian di chuyển
    double totalTimeServices; // Tổng thời gian dịch vụ
    /**
     * Là phần tử đi lưu trữ các thông tin để sau này xem xét đánh giá lại
     */
}
