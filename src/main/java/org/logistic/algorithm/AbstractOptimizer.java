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
    
    /**
     * Khởi tạo optimizer với WriteLogUtil
     * 
     * @param writeLogUtil Tiện ích ghi log
     */
    public AbstractOptimizer(WriteLogUtil writeLogUtil) {
        this.writeLogUtil = writeLogUtil;
    }
    
    /**
     * Áp dụng toán tử hoán đổi (swap) cho một tuyến đường
     * 
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applySwapOperator(Route route) {
        int[] way = route.getIndLocations();
        if (way.length < 2) {
            return; // Không thể hoán đổi nếu chỉ có 1 phần tử hoặc ít hơn
        }
        
        // Chọn hai vị trí ngẫu nhiên khác nhau
        int pos1 = random.nextInt(way.length);
        int pos2;
        do {
            pos2 = random.nextInt(way.length);
        } while (pos1 == pos2);
        
        // Hoán đổi hai điểm
        int temp = way[pos1];
        way[pos1] = way[pos2];
        way[pos2] = temp;
        
        // Đảm bảo giá trị không vượt quá giới hạn
        validateLocationIndices(way);
    }
    
    /**
     * Áp dụng toán tử chèn (insert) cho một tuyến đường
     * 
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applyInsertOperator(Route route) {
        int[] way = route.getIndLocations();
        if (way.length < 2) {
            return; // Không thể chèn nếu chỉ có 1 phần tử hoặc ít hơn
        }
        
        // Chọn vị trí nguồn và đích
        int pos = random.nextInt(way.length);
        int insertPos = random.nextInt(way.length);
        
        // Thực hiện chèn
        int posVal = way[Math.max(insertPos, pos)];
        for (int i = Math.min(insertPos, pos); i <= Math.max(insertPos, pos); i++) {
            int tempVal = way[i];
            way[i] = posVal;
            posVal = tempVal;
        }
        
        // Đảm bảo giá trị không vượt quá giới hạn
        validateLocationIndices(way);
    }
    
    /**
     * Áp dụng toán tử đảo ngược (reverse) cho một tuyến đường
     * 
     * @param route Tuyến đường cần áp dụng toán tử
     */
    protected void applyReverseOperator(Route route) {
        int[] way = route.getIndLocations();
        if (way.length < 2) {
            return; // Không thể đảo ngược nếu chỉ có 1 phần tử hoặc ít hơn
        }
        
        // Chọn hai vị trí ngẫu nhiên
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
        
        // Đảm bảo giá trị không vượt quá giới hạn
        validateLocationIndices(way);
    }

    protected void applyPdShift(Route[] routes) {

    }

    protected void applyPdExchange(Route[] routes) {

    }
    
    /**
     * Áp dụng toán tử ngẫu nhiên cho khám phá
     * 
     * @param route Tuyến đường cần áp dụng toán tử
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
     * 
     * @param fitnessUtil Tiện ích tính fitness
     * @param checkConditionUtil Tiện ích kiểm tra điều kiện
     * @param locations Mảng các vị trí
     * @param currentTarget Trọng tải hiện tại
     */
    protected void setupParameters(FitnessUtil fitnessUtil, CheckConditionUtil checkConditionUtil, 
                                 Location[] locations, int currentTarget) {
        this.fitnessUtil = fitnessUtil;
        this.checkConditionUtil = checkConditionUtil;
        this.locations = locations;
        this.currentTarget = currentTarget;
    }
    
    /**
     * Đảm bảo các chỉ số vị trí không vượt quá giới hạn
     * 
     * @param indices Mảng các chỉ số cần kiểm tra
     */
    private void validateLocationIndices(int[] indices) {
        if (locations == null) {
            return;
        }
        
        int maxLocationIndex = locations.length - 1;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] > maxLocationIndex) {
                indices[i] = maxLocationIndex;
            }
        }
    }
}