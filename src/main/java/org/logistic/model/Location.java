package org.logistic.model;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@Data
public class Location {
    Point point;
    int serviceTimePick;
    int serviceTimeDeliver;
    int demandPick;
    int demandDeliver;
    int ltw; // low time window (ready time)
    int utw; // upper time window (due time)
    boolean isPick;
    boolean isDeliver;

    /**
     * Lấy tổng thời gian phục vụ
     * @return Tổng thời gian phục vụ
     */
    public int totalServiceTime() {
        return serviceTimeDeliver + serviceTimePick;
    }

    /**
     * Tính khoảng cách đến một địa điểm khác
     * @param oLocation Địa điểm khác
     * @return Khoảng cách
     */
    public int distance(Location oLocation) {
        return this.point.distanceTo(oLocation.point);
    }
    
    /**
     * Lấy tọa độ X
     * @return Tọa độ X
     */
    public float getX() {
        return point.getX();
    }
    
    /**
     * Lấy tọa độ Y
     * @return Tọa độ Y
     */
    public float getY() {
        return point.getY();
    }
    
    /**
     * Lấy nhu cầu (demand)
     * @return Nhu cầu
     */
    public int getDemand() {
        return isPick ? demandPick : demandDeliver;
    }
    
    /**
     * Lấy thời gian sẵn sàng (ready time)
     * @return Thời gian sẵn sàng
     */
    public int getReadyTime() {
        return ltw;
    }
    
    /**
     * Lấy thời gian hạn chót (due time)
     * @return Thời gian hạn chót
     */
    public int getDueTime() {
        return utw;
    }
    
    /**
     * Lấy thời gian phục vụ
     * @return Thời gian phục vụ
     */
    public int getServiceTime() {
        return isPick ? serviceTimePick : serviceTimeDeliver;
    }
}
