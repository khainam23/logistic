package org.logistic.util;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.logistic.model.Location;
import org.logistic.model.Route;

/**
 * Tiện ích tính toán giá trị fitness cho các giải pháp
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FitnessUtil {
    static FitnessUtil instance;
    int[] tempWeights;
    FitnessStrategy fitnessStrategy;

    private FitnessUtil() {
        this.tempWeights = new int[4];
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
     * Tính giá trị fitness của giải pháp dựa trên các tuyến đường và vị trí
     *
     * @param routes    Mảng các tuyến đường
     * @param locations Mảng các vị trí
     * @return Giá trị fitness (càng thấp càng tốt)
     */
    public double calculatorFitness(Route[] routes, Location[] locations) {
        AtomicInteger totalDistances = new AtomicInteger(0);
        AtomicInteger totalServiceTime = new AtomicInteger(0);
        AtomicInteger totalWaitingTime = new AtomicInteger(0);
        AtomicInteger numberVehicle = new AtomicInteger(0);

        Arrays.stream(routes).parallel().forEach(route -> {
            int[] indLocs = route.getIndLocations();

            if (indLocs.length > 0) {
                numberVehicle.incrementAndGet();

                for (int j = 0; j < indLocs.length - 1; j++) {
                    Location currLoc = locations[indLocs[j]];
                    Location nextLoc = locations[indLocs[j + 1]];

                    // Tính khoảng cách
                    totalDistances.addAndGet(currLoc.distance(nextLoc));

                    // Tính thời gian phục vụ
                    totalServiceTime.addAndGet(nextLoc.totalServiceTime());

                    // Tính thời gian chờ của khách hàng
                    int waitingTime = nextLoc.getLtw() - currLoc.totalServiceTime() - currLoc.distance(nextLoc);
                    if (waitingTime > 0) {
                        totalWaitingTime.addAndGet(waitingTime);
                    }
                }

                // Thêm khoảng cách về kho
                totalDistances.addAndGet(
                        locations[indLocs[indLocs.length - 1]].distance(locations[0]));
            }
        });

        tempWeights[0] = numberVehicle.get();
        tempWeights[1] = totalDistances.get();
        tempWeights[2] = totalServiceTime.get();
        tempWeights[3] = totalWaitingTime.get();

        // Sử dụng strategy để tính fitness
        return fitnessStrategy.calculateFitness(
                numberVehicle.get(),
                totalDistances.get(),
                totalServiceTime.get(),
                totalWaitingTime.get()
        );
    }

    /**
     * Lấy các giá trị weights tạm thời
     * 
     * @return mảng [numberVehicle, totalDistances, totalServiceTime,
     *         totalWaitingTime]
     */
    public int[] getTempWeights() {
        return tempWeights.clone();
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

/**
 * Interface định nghĩa strategy tính fitness
 */
interface FitnessStrategy {
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

/**
 * Strategy mặc định để tính fitness
 */
class DefaultFitnessStrategy implements FitnessStrategy {
    private final double alpha = 1.0;
    private final double beta = 1.0;
    private final double gamma = 1.0;
    private final double delta = 1.0;

    @Override
    public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
        return alpha * totalDistances + beta * totalServiceTime +
                gamma * totalWaitingTime + delta * numberVehicle;
    }
}

/**
 * Strategy có thể cấu hình để tính fitness
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ConfigurableFitnessStrategy implements FitnessStrategy {
    double alpha;
    double beta;
    double gamma;
    double delta;
    boolean useDistance;
    boolean useServiceTime;
    boolean useWaitingTime;
    boolean useVehicleCount;

    @Builder
    public ConfigurableFitnessStrategy(
            double alpha, double beta, double gamma, double delta,
            boolean useDistance, boolean useServiceTime, boolean useWaitingTime, boolean useVehicleCount) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.delta = delta;
        this.useDistance = useDistance;
        this.useServiceTime = useServiceTime;
        this.useWaitingTime = useWaitingTime;
        this.useVehicleCount = useVehicleCount;
    }

    @Override
    public double calculateFitness(int numberVehicle, int totalDistances, int totalServiceTime, int totalWaitingTime) {
        double fitness = 0.0;
        
        if (useDistance) {
            fitness += alpha * totalDistances;
        }
        
        if (useServiceTime) {
            fitness += beta * totalServiceTime;
        }
        
        if (useWaitingTime) {
            fitness += gamma * totalWaitingTime;
        }
        
        if (useVehicleCount) {
            fitness += delta * numberVehicle;
        }
        
        return fitness;
    }
}

/**
 * Builder để tạo và cấu hình FitnessStrategy
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
class FitnessStrategyBuilder {
    double alpha = 1.0;
    double beta = 1.0;
    double gamma = 1.0;
    double delta = 1.0;
    boolean useDistance = true;
    boolean useServiceTime = true;
    boolean useWaitingTime = true;
    boolean useVehicleCount = true;

    public FitnessStrategyBuilder withAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public FitnessStrategyBuilder withBeta(double beta) {
        this.beta = beta;
        return this;
    }

    public FitnessStrategyBuilder withGamma(double gamma) {
        this.gamma = gamma;
        return this;
    }

    public FitnessStrategyBuilder withDelta(double delta) {
        this.delta = delta;
        return this;
    }

    public FitnessStrategyBuilder useDistance(boolean use) {
        this.useDistance = use;
        return this;
    }

    public FitnessStrategyBuilder useServiceTime(boolean use) {
        this.useServiceTime = use;
        return this;
    }

    public FitnessStrategyBuilder useWaitingTime(boolean use) {
        this.useWaitingTime = use;
        return this;
    }

    public FitnessStrategyBuilder useVehicleCount(boolean use) {
        this.useVehicleCount = use;
        return this;
    }

    public FitnessStrategy build() {
        return new ConfigurableFitnessStrategy(
                alpha, beta, gamma, delta,
                useDistance, useServiceTime, useWaitingTime, useVehicleCount
        );
    }
}