package org.logistic.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;
import org.logistic.util.WriteLogUtil;

import java.util.Random;

/**
 * Lớp trừu tượng cung cấp các phương thức chung cho các thuật toán tối ưu hóa
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractOptimizer implements Optimizer {
    
    // Các tham số chung
    final Random random = new Random();
    final WriteLogUtil writeLogUtil;
    
    // Các tham số được thiết lập trong quá trình chạy
    Location[] locations;
    FitnessUtil fitnessUtil;
    CheckConditionUtil checkConditionUtil;
    int currentTarget;
    
    public AbstractOptimizer(WriteLogUtil writeLogUtil) {
        this.writeLogUtil = writeLogUtil;
    }
    
    /**
     * Áp dụng toán tử hoán đổi (swap) cho một tuyến đường
     */
    protected void applySwapOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên
        int[] way = route.getIndLocations();
        if (way.length < 2) return; // Không thể hoán đổi nếu chỉ có 1 phần tử hoặc ít hơn
        
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);
        
        // Đảm bảo pos1 khác pos2
        while (pos1 == pos2) {
            pos2 = random.nextInt(way.length);
        }
        
        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;
        
        // Đảm bảo giá trị không vượt quá giới hạn
        if (locations != null) {
            int maxLocationIndex = locations.length - 1;
            if (way[pos1] > maxLocationIndex) way[pos1] = maxLocationIndex;
            if (way[pos2] > maxLocationIndex) way[pos2] = maxLocationIndex;
        }
    }
    
    /**
     * Áp dụng toán tử chèn (insert) cho một tuyến đường
     */
    protected void applyInsertOperator(Route route) {
        // Chọn một điểm để di chuyển
        int[] way = route.getIndLocations();
        if (way.length < 2) return; // Không thể chèn nếu chỉ có 1 phần tử hoặc ít hơn
        
        int pos = random.nextInt(way.length);
        
        // Chọn vị trí mới để chèn điểm
        int insertPos = random.nextInt(way.length);
        int posVal = way[Math.max(insertPos, pos)];
        
        for (int i = Math.min(insertPos, pos); i <= Math.max(insertPos, pos); i++) {
            int tempVal = way[i];
            way[i] = posVal;
            posVal = tempVal;
        }
        
        // Đảm bảo tất cả các giá trị không vượt quá giới hạn
        if (locations != null) {
            int maxLocationIndex = locations.length - 1;
            for (int i = 0; i < way.length; i++) {
                if (way[i] > maxLocationIndex) way[i] = maxLocationIndex;
            }
        }
    }
    
    /**
     * Áp dụng toán tử đảo ngược (reverse) cho một tuyến đường
     */
    protected void applyReverseOperator(Route route) {
        // Chọn hai vị trí ngẫu nhiên
        int[] way = route.getIndLocations();
        if (way.length < 2) return; // Không thể đảo ngược nếu chỉ có 1 phần tử hoặc ít hơn
        
        int pos1 = random.nextInt(way.length);
        int pos2 = random.nextInt(way.length);
        
        // Đảm bảo pos1 < pos2
        if (pos1 > pos2) {
            int temp = pos1;
            pos1 = pos2;
            pos2 = temp;
        }
        
        // Đảo ngược đoạn từ pos1 đến pos2
        while (pos1 < pos2) {
            int temp = way[pos1];
            way[pos1] = way[pos2];
            way[pos2] = temp;
            pos1++;
            pos2--;
        }
        
        // Đảm bảo tất cả các giá trị không vượt quá giới hạn
        if (locations != null) {
            int maxLocationIndex = locations.length - 1;
            for (int i = 0; i < way.length; i++) {
                if (way[i] > maxLocationIndex) way[i] = maxLocationIndex;
            }
        }
    }
    
    /**
     * Áp dụng toán tử ngẫu nhiên cho khám phá
     */
    protected void applyRandomOperation(Route route) {
        int operator = random.nextInt(3);
        switch (operator) {
            case 0 -> applySwapOperator(route);
            case 1 -> applyInsertOperator(route);
            case 2 -> applyReverseOperator(route);
        }
    }
    
    /**
     * Thiết lập các tham số chung cho thuật toán
     */
    protected void setupParameters(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, 
                                 Location[] locations, int currentTarget) {
        this.fitnessUtil = fitnessUtil;
        this.checkConditionUtil = checkConditionUtil;
        this.locations = locations;
        this.currentTarget = currentTarget;
    }
}