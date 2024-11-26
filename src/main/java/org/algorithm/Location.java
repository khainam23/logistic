package org.algorithm;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Địa điểm giao, nhận hàng hóa
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Location {
    final boolean pickup; // Điểm lấy hàng
    // Tọa độ
    final int x;
    final int y;
    final int load; // Khối lượng hàng hóa (+: Lấy, -: Nhận)
    int LTW;// Thời gian thấp nhất có thể thực hiện tại điểm này
    int UTW;// Thời gian cao nhất có thể thực hiện tại điểm này
    boolean serviced; // Điểm đã được phục vụ chưa?
    boolean serviceable; // Địa điểm có thể phục vụ hay không?
    public Location(boolean pickup, int x, int y, int load) {
        this.pickup = pickup;
        this.x = x;
        this.y = y;
        this.load = load;
        this.serviced = false; // Mặc định là chưa
        this.serviceable = pickup; // Tùy thuộc vào có là điểm nhận hàng
    }
    // Đặt lại tình trạng của địa điểm là chưa phục vụ
    public void resetServiced() {
        serviced = false;
        serviceable = pickup;
    }
    @Override
    public String toString() {
        return String.format("%8s %11s %6s %6s %6s %10s %13s", pickup, "(" + x + ", " + y + "):", load, LTW, UTW, serviced, serviceable);
    }
}
