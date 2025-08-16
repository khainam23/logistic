package org.logistic.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Class đại diện cho thông tin khoảng cách và thời gian giữa hai điểm
 * Sử dụng trong định dạng Liu Tang Yao DISTANCETIME_SECTION
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DistanceTime {
    
    /** Điểm bắt đầu */
    int fromNode;
    
    /** Điểm đến */
    int toNode;
    
    /** Tổng khoảng cách di chuyển */
    double distance;
    
    /** Tổng thời gian di chuyển */
    double travelTime;
    
    @Override
    public String toString() {
        return String.format("DistanceTime{from=%d, to=%d, distance=%.2f, time=%.2f}", 
                fromNode, toNode, distance, travelTime);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DistanceTime that = (DistanceTime) obj;
        return fromNode == that.fromNode && toNode == that.toNode;
    }
    
    @Override
    public int hashCode() {
        return fromNode * 31 + toNode;
    }
}