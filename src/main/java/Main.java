import org.algorithm.Algorithm;
import org.algorithm.SpottedHyenaOptimizer;
import org.data.GenerateData;
import org.data.Result;
import org.model.Route;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        test();
    }

    /**
     * Dùng kiểm tra các thuật toán và cho gốc nhìn tổng quan về khởi chạy thật tế
     */
    public static void test() {
        // Create data
        GenerateData generateData = new GenerateData();
        List<Route> routes = generateData.generateSolution(); // q = 3 n = 5 width = 100

        // Run algorithm
        Algorithm algorithm = new SpottedHyenaOptimizer(generateData.getLocations(), routes);
        Result result = algorithm.optimizer();
        result.print();
    }

    /**
     * Dùng để khởi chạy và so sánh cá thuật toán chung
     */
}
