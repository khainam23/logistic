package org.logistic.algorithm;

import org.logistic.data.ReadDataFromFile;
import org.logistic.model.Location;
import org.logistic.model.Route;
import org.logistic.util.CheckConditionUtil;
import org.logistic.util.FitnessUtil;

import java.util.Arrays;

/**
 * Test để kiểm tra tính chính xác của logic ràng buộc
 */
public class ValidationTest {

    public static void main(String[] args) {
        System.out.println("=== KIỂM TRA TÍNH CHÍNH XÁC LOGIC RÀNG BUỘC ===\n");

        // Đọc dữ liệu
        ReadDataFromFile dataReader = new ReadDataFromFile();
        dataReader.readProblemData("data/vrpspdtw_Liu_Tang_Yao/src/200_1.txt", ReadDataFromFile.ProblemType.VRPSPDTW_LIU_TANG_YAO);

        Location[] locations = dataReader.getLocations();
        int maxCapacity = dataReader.getMaxCapacity();

        if (locations == null) {
            System.out.println("Không đọc được dữ liệu");
            return;
        }

        System.out.println("Đã đọc " + locations.length + " locations");
        System.out.println("Max capacity: " + maxCapacity);
        
        // In thông tin một vài locations để debug
        System.out.println("\n--- Thông tin một vài locations ---");
        for (int i = 0; i < Math.min(5, locations.length); i++) {
            Location loc = locations[i];
            System.out.printf("Location %d: demand=%d, ltw=%d, utw=%d, serviceTime=%d, isPick=%s, isDeliver=%s\n",
                i, loc.getDemand(), loc.getLtw(), loc.getUtw(), loc.getServiceTime(), 
                loc.isPick(), loc.isDeliver());
        }

        CheckConditionUtil checkUtil = CheckConditionUtil.getInstance();
        FitnessUtil fitnessUtil = FitnessUtil.getInstance();

        // Đọc solution từ file
        dataReader.readSolution("data/vrpspdtw_Liu_Tang_Yao/solution/200_1.txt");
        Route[] solutionRoutes = dataReader.getRoutes();

        System.out.println("\n=== KIỂM TRA SOLUTION TỪ FILE ===");
        
        // Chỉ kiểm tra từng route trong solution từ file
        testSolutionRoutes(solutionRoutes, locations, maxCapacity, checkUtil);
        
        // So sánh với fitness calculation
        compareFitnessValidation(solutionRoutes, locations, maxCapacity, checkUtil, fitnessUtil);
    }

    /**
     * Test 1: Kiểm tra từng route trong solution
     */
    private static void testSolutionRoutes(Route[] routes, Location[] locations, int maxCapacity, CheckConditionUtil checkUtil) {
        System.out.println("\n--- Test 1: Kiểm tra solution routes ---");
        
        int validRoutes = 0;
        int invalidRoutes = 0;
        
        for (int i = 0; i < routes.length; i++) {
            Route route = routes[i];
            boolean isValid = checkUtil.isInsertionFeasible(route, locations, maxCapacity, 0);
            
            System.out.printf("Route %d: %s -> %s\n", 
                i + 1, 
                Arrays.toString(route.getIndLocations()), 
                isValid ? "VALID" : "INVALID"
            );
            
            if (isValid) {
                validRoutes++;
                // In chi tiết route hợp lệ
                printRouteDetails(route, locations, maxCapacity);
            } else {
                invalidRoutes++;
                // Phân tích lý do không hợp lệ
                analyzeInvalidRoute(route, locations, maxCapacity);
            }
        }
        
        System.out.printf("\nKết quả: %d routes hợp lệ, %d routes không hợp lệ\n", validRoutes, invalidRoutes);
    }



    /**
     * So sánh với fitness calculation
     */
    private static void compareFitnessValidation(Route[] routes, Location[] locations, int maxCapacity, 
                                               CheckConditionUtil checkUtil, FitnessUtil fitnessUtil) {
        System.out.println("\n--- So sánh với fitness calculation ---");
        
        double totalFitness = fitnessUtil.calculatorFitness(routes, locations);
        System.out.printf("Total fitness từ FitnessUtil: %.2f\n", totalFitness);
        
        // Kiểm tra xem có route nào không hợp lệ nhưng vẫn được tính fitness
        int validCount = 0;
        for (Route route : routes) {
            if (checkUtil.isInsertionFeasible(route, locations, maxCapacity, 0)) {
                validCount++;
            }
        }
        
        System.out.printf("Số routes hợp lệ theo CheckConditionUtil: %d/%d\n", validCount, routes.length);
        
        if (validCount == routes.length) {
            System.out.println("✅ Tất cả routes đều hợp lệ - Logic ràng buộc nhất quán");
        } else {
            System.out.println("⚠️ Có routes không hợp lệ - Cần kiểm tra logic");
        }
    }

    /**
     * In chi tiết route hợp lệ
     */
    private static void printRouteDetails(Route route, Location[] locations, int maxCapacity) {
        int[] indLocations = route.getIndLocations();
        int currentLoad = 0;
        int currentTime = 0;
        
        System.out.println("  Chi tiết route:");
        System.out.println("  Location | Load | Time | Ready | Due | Service");
        
        for (int i = 0; i < indLocations.length; i++) {
            Location loc = locations[indLocations[i]];
            
            // Tính load
            currentLoad += loc.getDemand();
            
            // Tính time
            if (currentTime < loc.getLtw()) {
                currentTime = loc.getLtw();
            }
            currentTime += loc.getServiceTime();
            
            System.out.printf("  %8d | %4d | %4d | %5d | %3d | %7d\n",
                indLocations[i], currentLoad, currentTime - loc.getServiceTime(), 
                loc.getLtw(), loc.getUtw(), loc.getServiceTime());
            
            // Thêm travel time đến location tiếp theo
            if (i < indLocations.length - 1) {
                Location nextLoc = locations[indLocations[i + 1]];
                currentTime += loc.distance(nextLoc);
            }
        }
        System.out.println();
    }

    /**
     * Phân tích lý do route không hợp lệ
     */
    private static void analyzeInvalidRoute(Route route, Location[] locations, int maxCapacity) {
        int[] indLocations = route.getIndLocations();
        int currentLoad = 0;
        int currentTime = 0;
        boolean hasError = false;
        
        System.out.println("  ❌ Phân tích lỗi:");
        System.out.println("    Step | Loc | Load | Time | Ready | Due | Service | Error");
        
        for (int i = 0; i < indLocations.length; i++) {
            if (indLocations[i] >= locations.length) {
                System.out.printf("    %4d | %3d | ---- | ---- | ----- | --- | ------- | INDEX OUT OF BOUNDS\n", 
                    i+1, indLocations[i]);
                hasError = true;
                continue;
            }
            
            Location loc = locations[indLocations[i]];
            String errorMsg = "";
            
            // Kiểm tra ràng buộc trọng tải
            if (loc.isDeliver()) {
                currentLoad -= loc.getDemandDeliver();
                if (currentLoad < 0) {
                    errorMsg += "NEGATIVE_LOAD ";
                    hasError = true;
                }
            }
            
            if (loc.isPick()) {
                currentLoad += loc.getDemandPick();
                if (currentLoad > maxCapacity) {
                    errorMsg += "OVER_CAPACITY ";
                    hasError = true;
                }
            }
            
            // Kiểm tra ràng buộc thời gian
            if (currentTime < loc.getLtw()) {
                currentTime = loc.getLtw();
            }
            
            if (currentTime > loc.getUtw()) {
                errorMsg += "TIME_VIOLATION ";
                hasError = true;
            }
            
            System.out.printf("    %4d | %3d | %4d | %4d | %5d | %3d | %7d | %s\n",
                i+1, indLocations[i], currentLoad, currentTime, 
                loc.getLtw(), loc.getUtw(), loc.getServiceTime(), errorMsg);
            
            currentTime += loc.getServiceTime();
            
            // Tính thời gian di chuyển đến location tiếp theo
            if (i < indLocations.length - 1) {
                if (indLocations[i + 1] < locations.length) {
                    Location nextLoc = locations[indLocations[i + 1]];
                    currentTime += loc.distance(nextLoc);
                }
            }
        }
        
        if (!hasError) {
            System.out.println("    ⚠️ Không phát hiện lỗi rõ ràng - có thể do logic kiểm tra khác");
        }
        System.out.println();
    }
}