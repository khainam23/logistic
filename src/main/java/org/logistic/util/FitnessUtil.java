package org.logistic.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.model.Solution;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tiện ích tính toán giá trị fitness cho các giải pháp
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FitnessUtil {
    static FitnessUtil instance;
    // Sử dụng ThreadLocal để mỗi thread có bản sao riêng của tempWeights
    private static final ThreadLocal<int[]> tempWeights = ThreadLocal.withInitial(() -> new int[4]);
    FitnessStrategy fitnessStrategy;
    boolean parallelMode = true; // Mặc định là song song

    private FitnessUtil() {
        this.fitnessStrategy = new DefaultFitnessStrategy();
    }

    /**
     * Lấy instance của FitnessUtil (Singleton pattern)
     * 
     * @return Instance của FitnessUtil
     */
    public static synchronized FitnessUtil getInstance() {
        if (instance == null) {
            instance = new FitnessUtil();
        }
        return instance;
    }

    /**
     * Thiết lập strategy tính fitness
     * 
     * @param fitnessStrategy Strategy mới để tính fitness
     */
    public void setFitnessStrategy(FitnessStrategy fitnessStrategy) {
        this.fitnessStrategy = fitnessStrategy;
    }

    /**
     * Lấy strategy tính fitness hiện tại
     * 
     * @return Strategy hiện tại
     */
    public FitnessStrategy getFitnessStrategy() {
        return this.fitnessStrategy;
    }

    /**
     * Thiết lập chế độ song song
     * 
     * @param parallelMode true để bật song song, false để tắt
     */
    public void setParallelMode(boolean parallelMode) {
        this.parallelMode = parallelMode;
    }

    /**
     * Tính giá trị fitness của giải pháp dựa trên các tuyến đường và vị trí
     * Sử dụng chế độ song song hiện tại được thiết lập
     *
     * @param routes    Mảng các tuyến đường
     * @param locations Mảng các vị trí
     * @return Giá trị fitness (càng thấp càng tốt)
     */
    public double calculatorFitness(Route[] routes, Location[] locations) {
        return calculatorFitness(routes, locations, this.parallelMode);
    }

    /**
     * Tính giá trị fitness của giải pháp dựa trên các tuyến đường và vị trí
     *
     * @param routes    Mảng các tuyến đường
     * @param locations Mảng các vị trí
     * @param parallel  Có sử dụng xử lý song song hay không
     * @return Giá trị fitness (càng thấp càng tốt)
     */
    public double calculatorFitness(Route[] routes, Location[] locations, boolean parallel) {
        if (parallel) {
            return calculatorFitnessParallel(routes, locations);
        } else {
            return calculatorFitnessSequential(routes, locations);
        }
    }

    /**
     * Tính giá trị fitness song song (sử dụng stream parallel)
     */
    private double calculatorFitnessParallel(Route[] routes, Location[] locations) {
        AtomicInteger totalDistances = new AtomicInteger(0);
        AtomicInteger totalServiceTime = new AtomicInteger(0);
        AtomicInteger totalWaitingTime = new AtomicInteger(0);
        AtomicInteger numberVehicle = new AtomicInteger(0);

        Arrays.stream(routes).parallel().forEach(route -> {
            int[] indLocs = route.getIndLocations();

            if (indLocs != null && indLocs.length > 0) {
                numberVehicle.incrementAndGet();
                
                // Thời gian hiện tại bắt đầu từ 0 (xuất phát từ kho)
                int currentTime = 0;

                for (int j = 0; j < indLocs.length; j++) {
                    Location currLoc = locations[indLocs[j]];
                    
                    // Nếu không phải điểm đầu tiên, tính thời gian di chuyển từ điểm trước
                    if (j > 0) {
                        Location prevLoc = locations[indLocs[j - 1]];
                        currentTime += prevLoc.distance(currLoc);
                        
                        // Tính khoảng cách
                        totalDistances.addAndGet(prevLoc.distance(currLoc));
                    } else {
                        // Khoảng cách từ kho đến điểm đầu tiên
                        currentTime += locations[0].distance(currLoc);
                        totalDistances.addAndGet(locations[0].distance(currLoc));
                    }
                    
                    // Tính thời gian chờ nếu đến sớm hơn time window
                    int waitingTime = Math.max(0, currLoc.getLtw() - currentTime);
                    totalWaitingTime.addAndGet(waitingTime);
                    
                    // Cập nhật thời gian hiện tại (thời gian bắt đầu phục vụ)
                    currentTime = Math.max(currentTime, currLoc.getLtw());
                    
                    // Thêm thời gian phục vụ (chỉ tính thời gian phục vụ tương ứng với hoạt động)
                    currentTime += currLoc.getServiceTime();
                    totalServiceTime.addAndGet(currLoc.getServiceTime());
                }

                // Thêm khoảng cách về kho
                totalDistances.addAndGet(
                        locations[indLocs[indLocs.length - 1]].distance(locations[0]));
            }
        });

        int[] weights = tempWeights.get();
        weights[0] = numberVehicle.get();
        weights[1] = totalDistances.get();
        weights[2] = totalServiceTime.get();
        weights[3] = totalWaitingTime.get();

        // Sử dụng strategy để tính fitness
        return fitnessStrategy.calculateFitness(
                numberVehicle.get(),
                totalDistances.get(),
                totalServiceTime.get(),
                totalWaitingTime.get()
        );
    }

    /**
     * Tính giá trị fitness tuần tự (không sử dụng stream parallel)
     */
    private double calculatorFitnessSequential(Route[] routes, Location[] locations) {
        int totalDistances = 0;
        int totalServiceTime = 0;
        int totalWaitingTime = 0;
        int numberVehicle = 0;

        for (Route route : routes) {
            int[] indLocs = route.getIndLocations();

            if (indLocs != null && indLocs.length > 0) {
                numberVehicle++;
                
                // Thời gian hiện tại bắt đầu từ 0 (xuất phát từ kho)
                int currentTime = 0;

                for (int j = 0; j < indLocs.length; j++) {
                    Location currLoc = locations[indLocs[j]];
                    
                    // Nếu không phải điểm đầu tiên, tính thời gian di chuyển từ điểm trước
                    if (j > 0) {
                        Location prevLoc = locations[indLocs[j - 1]];
                        currentTime += prevLoc.distance(currLoc);
                        
                        // Tính khoảng cách
                        totalDistances += prevLoc.distance(currLoc);
                    } else {
                        // Khoảng cách từ kho đến điểm đầu tiên
                        currentTime += locations[0].distance(currLoc);
                        totalDistances += locations[0].distance(currLoc);
                    }
                    
                    // Tính thời gian chờ nếu đến sớm hơn time window
                    int waitingTime = Math.max(0, currLoc.getLtw() - currentTime);
                    totalWaitingTime += waitingTime;
                    
                    // Cập nhật thời gian hiện tại (thời gian bắt đầu phục vụ)
                    currentTime = Math.max(currentTime, currLoc.getLtw());
                    
                    // Thêm thời gian phục vụ (chỉ tính thời gian phục vụ tương ứng với hoạt động)
                    currentTime += currLoc.getServiceTime();
                    totalServiceTime += currLoc.getServiceTime();
                }

                // Thêm khoảng cách về kho
                totalDistances += locations[indLocs[indLocs.length - 1]].distance(locations[0]);
            }
        }

        int[] weights = tempWeights.get();
        weights[0] = numberVehicle;
        weights[1] = totalDistances;
        weights[2] = totalServiceTime;
        weights[3] = totalWaitingTime;

        // Sử dụng strategy để tính fitness
        return fitnessStrategy.calculateFitness(
                numberVehicle,
                totalDistances,
                totalServiceTime,
                totalWaitingTime
        );
    }

    /**
     * Lấy các giá trị weights tạm thời
     * 
     * @return mảng [numberVehicle, totalDistances, totalServiceTime,
     *         totalWaitingTime]
     */
    public int[] getTempWeights() {
        return tempWeights.get().clone();
    }

    /**
     * Tính toán các thành phần weights từ Solution
     * 
     * @param solution Solution cần tính toán
     * @param locations Mảng các địa điểm
     * @return mảng [numberVehicle, totalDistances, totalServiceTime, totalWaitingTime]
     */
    public int[] calculateWeightsFromSolution(Solution solution, Location[] locations) {
        if (solution == null || solution.getRoutes() == null) {
            return new int[]{0, 0, 0, 0};
        }

        int numberVehicle = 0;
        int totalDistances = 0;
        int totalServiceTime = 0;
        int totalWaitingTime = 0;

        Route[] routes = solution.getRoutes();
        
        for (Route route : routes) {
            if (route != null && route.getIndLocations() != null && route.getIndLocations().length > 0) {
                numberVehicle++;
                int[] indLocs = route.getIndLocations();
                
                // Thời gian hiện tại bắt đầu từ 0 (xuất phát từ kho)
                int currentTime = 0;

                for (int i = 0; i < indLocs.length; i++) {
                    Location currLoc = locations[indLocs[i]];
                    
                    // Nếu không phải điểm đầu tiên, tính thời gian di chuyển từ điểm trước
                    if (i > 0) {
                        Location prevLoc = locations[indLocs[i - 1]];
                        currentTime += prevLoc.distance(currLoc);
                        
                        // Tính khoảng cách
                        totalDistances += prevLoc.distance(currLoc);
                    } else {
                        // Khoảng cách từ kho đến điểm đầu tiên
                        currentTime += locations[0].distance(currLoc);
                        totalDistances += locations[0].distance(currLoc);
                    }
                    
                    // Tính thời gian chờ nếu đến sớm hơn time window
                    int waitingTime = Math.max(0, currLoc.getLtw() - currentTime);
                    totalWaitingTime += waitingTime;
                    
                    // Cập nhật thời gian hiện tại (thời gian bắt đầu phục vụ)
                    currentTime = Math.max(currentTime, currLoc.getLtw());
                    
                    // Thêm thời gian phục vụ (chỉ tính thời gian phục vụ tương ứng với hoạt động)
                    currentTime += currLoc.getServiceTime();
                    totalServiceTime += currLoc.getServiceTime();
                }

                // Thêm khoảng cách về kho
                totalDistances += locations[indLocs[indLocs.length - 1]].distance(locations[0]);
            }
        }

        return new int[]{numberVehicle, totalDistances, totalServiceTime, totalWaitingTime};
    }

    /**
     * Dọn dẹp ThreadLocal để tránh memory leak
     * Nên được gọi khi thread kết thúc hoặc không còn sử dụng FitnessUtil
     */
    public static void cleanupThreadLocal() {
        tempWeights.remove();
    }

    /**
     * Tạo builder để cấu hình strategy tính fitness
     * 
     * @return FitnessStrategyBuilder
     */
    public static FitnessStrategyBuilder createStrategyBuilder() {
        return new FitnessStrategyBuilder();
    }
}