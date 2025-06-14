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
}