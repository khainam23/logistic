package org.logistic.model;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@Data
public class Location {
    Point point;
    double serviceTimePick;
    double serviceTimeDeliver;
    double demandPick;
    double demandDeliver;
    double ltw; // low time window (ready time)
    double utw; // upper time window (due time)
    boolean isPick;
    boolean isDeliver;

    /**
     * Lấy tổng thời gian phục vụ
     * @return Tổng thời gian phục vụ
     */
    public double totalServiceTime() {
        return serviceTimeDeliver + serviceTimePick;
    }

    /**
     * Tính khoảng cách đến một địa điểm khác
     * @param oLocation Địa điểm khác
     * @return Khoảng cách
     */
    public double distance(Location oLocation) {
        return this.point.distanceTo(oLocation.point);
    }
    
    /**
     * Lấy tọa độ X
     * @return Tọa độ X
     */
    public double getX() {
        return point.getX();
    }
    
    /**
     * Lấy tọa độ Y
     * @return Tọa độ Y
     */
    public double getY() {
        return point.getY();
    }
    
    /**
     * Lấy nhu cầu (demand)
     * @return Nhu cầu
     */
    public double getDemand() {
        return isPick ? demandPick : demandDeliver;
    }
    
    /**
     * Lấy thời gian sẵn sàng (ready time)
     * @return Thời gian sẵn sàng
     */
    public double getReadyTime() {
        return ltw;
    }
    
    /**
     * Lấy thời gian hạn chót (due time)
     * @return Thời gian hạn chót
     */
    public double getDueTime() {
        return utw;
    }
    
    /**
     * Lấy thời gian phục vụ
     * @return Thời gian phục vụ
     */
    public double getServiceTime() {
        // Nếu location vừa là pickup vừa là delivery, tính cả hai
        if (isPick && isDeliver) {
            return serviceTimePick + serviceTimeDeliver;
        }
        // Nếu chỉ là pickup hoặc chỉ là delivery
        return isPick ? serviceTimePick : serviceTimeDeliver;
    }
}
