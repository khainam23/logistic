package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.data.Result;
import org.model.Location;

import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Algorithm {
    int maxCapacity; // Trọng tải chung của các xe
    final int MAX_ITERATOR = 100; // Giới hạn số lần tìm giải pháp
    final int MAX_TIME_SOLUTION = 60000; // Giới hạn thời gian tìm giải pháp
    double[][] distances; // Khoảng các giữa các điểm
    List<Location> locations;
    final Random rd = new Random();
    double[] lowerBounds;
    double[] upperBounds;

    /**
     * CÀNG THẤP THÌ CÀNG TỐT
     * Tính toán giá trị tối ưu trên các tiêu chí:
     * + NV - Số lượng đội xe sử dụng
     * + TC - Tổng quảng đường đi được
     * + SD - Thời lượng của dịch vụ
     * + WT - Thời gian phải chờ của khách hàng
     * @return
     */
    // Các thuật toán kế thừa triển khai tại đây
    public abstract Result run();
}
