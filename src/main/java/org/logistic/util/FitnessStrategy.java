package org.logistic.util;

/**
 * Interface định nghĩa strategy tính fitness
 */
public interface FitnessStrategy {
    /**
     * Tính giá trị fitness
     * 
     * @param numberVehicle    Số lượng phương tiện
     * @param totalDistances   Tổng khoảng cách
     * @param totalServiceTime Tổng thời gian phục vụ
     * @param totalWaitingTime Tổng thời gian chờ
     * @return Giá trị fitness
     */
    double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime);
    
    /**
     * Kiểm tra xem có cần tính khoảng cách không
     * 
     * @return true nếu cần tính
     */
    default boolean needsDistance() {
        return true;
    }
    
    /**
     * Kiểm tra xem có cần tính thời gian phục vụ không
     * 
     * @return true nếu cần tính
     */
    default boolean needsServiceTime() {
        return true;
    }
    
    /**
     * Kiểm tra xem có cần tính thời gian chờ không
     * 
     * @return true nếu cần tính
     */
    default boolean needsWaitingTime() {
        return true;
    }
    
    /**
     * Kiểm tra xem có cần tính số phương tiện không
     * 
     * @return true nếu cần tính
     */
    default boolean needsVehicleCount() {
        return true;
    }
}