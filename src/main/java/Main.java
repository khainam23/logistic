import org.data.GenerateData;
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


    }

    /**
     * Dùng để khởi chạy và so sánh cá thuật toán chung
     */
}
