package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

/**
 * Mối liên kết giữa các điểm trong môi trường di chuyển của phương tiện
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@Setter
public class Route {
    int[] route; // Chỉ thứ tự điểm phương tiện đi qua
    int fitnessValue; // Đánh giá chất lượng tuyến đường
    ArrayList<Location> locations; // Điểm dừng trên tuyến đường
    /**
     * Ma trận đối xứng với đường chéo bằng 0,
     * nó cho biết từ điểm này tới điểm kia mất bao lâu
     */
    int[][] distances;
    int maxCapacity; // Sức chứa tối đa của phương tiện
    boolean valid; // Thỏa điều kiện?

    public Route(int[] route, ArrayList<Location> locations, int[][] distances, int maxCapacity) {
        this.route = route;
        this.maxCapacity = maxCapacity;
        this.locations = locations;
        this.distances = distances;
        this.fitnessValue = calculateFitness();
        this.valid = calculateValid();
    }

    /**
     * Phương thức tính toán giá trị độ thích nghi (fitness value) của tuyến đường.
     * Giá trị này dựa trên các yếu tố:
     * <p>
     * Tổng thời gian (time) di chuyển giữa các địa điểm.
     * Sức chứa (capacity) được cập nhật khi di chuyển qua mỗi điểm dừng.
     * Phạt (penalty) nếu thời gian vượt qua giới hạn cho phép (time > UTW) hoặc nếu sức chứa vượt quá giới hạn cho phép (capacity > maxCapacity).
     * <p>
     * Phương thức này tính toán tổng thời gian và các khoản phạt, rồi cộng lại để ra giá trị độ thích nghi.
     *
     * @return Số điểm đánh giá
     */
    public int calculateFitness() {
        int fitness = 0;
        int penalty = 0; // Điểm phạt
        int time = 0; // Tổng thời gian di chuyển
        int capacity = 0; // Tổng số lượng hàng phải nhận tại location

        for (int i = 0; i < route.length - 1; i++) {
            time += distances[route[i]][route[i + 1]]; // Xem thời gian là tổng khoảng cách
            capacity += locations.get(i).getLoad(); // Sức chứa cần nhận tại điểm i
            // Di chuyển ít hơn thời gian yêu cầu tối thiểu
            if (time < locations.get(route[i + 1]).getLTW()) {
                time = locations.get(route[i + 1]).getLTW();
            }
            // Di chuyển nhiều hơn thời gian có thể chờ tại điểm i
            if (time > locations.get(route[i + 1]).getUTW()) {
                // Tính ra số điểm phạt dựa trên mức độ trễ của phương tiện, tối thiểu là 100
                penalty += Math.max(100, 10 * (time - locations.get(route[i]).getUTW()));
            }
            // Vượt quá tải trọng
            if (capacity > maxCapacity) {
                // Tính ra số điểm phạt
                penalty += Math.max(100, 10 * (capacity - maxCapacity));
            }
        }
        // Điểm tối ưu sẽ là tổng (số điểm phạt + thời gian di chuyển)
        fitness = penalty + time;
        return fitness;
    }

    /**
     * Phương thức kiểm tra tính hợp lệ của tuyến đường. Nó kiểm tra các điều kiện sau:
     * <p>
     * Thời gian dịch vụ không vượt quá giới hạn cho phép (LTW và UTW).
     * Sức chứa của phương tiện không vượt quá maxCapacity.
     * Nếu tuyến đường hợp lệ, phương thức trả về true, ngược lại trả về false. Nó cũng đánh dấu các điểm dừng là đã được phục vụ (setServiced(true)) và kiểm tra các điều kiện khác (như thời gian chờ).
     *
     * @return Tính hợp lệ của tuyến đường
     */
    boolean calculateValid() {
        boolean valid = true; // Xem rằng ban đầu thỏa mọi điều kiện
        resetLocations(); // Đảm bảo bắt đầu có thể đi được
        int currentLoad = 0; // Tải trọng hiện tại
        int currentTime = 0; // Thời gian hiện tại
        // Tra cứu khoảng cách từ điểm dừng trước đến điểm dừng tiếp theo trong ma trận distances
        int previousIndex = 0;
        for (int i = 0; i < route.length; i++) {
            Location lo = locations.get(route[i]); // Lấy ra các thông tin tại điểm
            currentLoad += lo.getLoad(); // Tải trọng cần mang tại điểm này
            currentTime += distances[previousIndex][route[i]]; // Từ điểm trước đến điểm hiện tại mất bao lâu
            previousIndex = route[i];
            locations.get(route[i]).setServiced(true); // Đã đến
            /**
             * Có ý nghĩa rằng điểm tiếp theo trong tuyến đường cần được chuẩn bị để phục vụ
             * hoặc tiếp nhận hàng hóa sau khi điểm nhận hàng hoàn thành.
             */
            if (lo.isPickup()) // Nếu là điểm nhận hàng
                locations.get(route[i] + 1).setServiceable(true); // Đánh dấu điểm kế tiếp là có thể phục vụ
            // Tính tổng thời gian di chuyển
            currentTime += Math.max(0, lo.getLTW() - currentTime);
            // Điều kiện phải đảm bảo
            if (currentTime > lo.getUTW() || currentLoad > maxCapacity) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    /**
     * Đặt lại trạng thái chưa phục vụ cho các địa điểm
     */
    public void resetLocations() {
        for (int i = 1; i < locations.size(); i++)
            locations.get(i).resetServiced();
    }

}
