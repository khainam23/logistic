package org.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.model.Route;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Result {
    String name; // Tên thuật toán
    List<Route> finalSolution; // Giải pháp cuối cùng của thuật toán cho ra
    double fitness; // Điểm của thuật toán này
    double firstTimeSolution; // Thời gian tìm ra lời giải đầu tiên

    public void print() {
        System.out.println("*".repeat(20));
        System.out.println("Algorithm: " + name);
        System.out.println("Final Solution:");
        finalSolution.forEach(Route::print);
        System.out.println("Fitness: " + fitness);
    }
    /**
     * Lưu trữ các thông tin để sau này xem xét đánh giá lại
     */
}
