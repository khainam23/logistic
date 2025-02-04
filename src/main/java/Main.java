import org.data.GenerateData;

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
        generateData.generateLocations(20, 1000);
//        generateData.printData();

        int vehicle = 5;


    }

    /**
     * Dùng để khởi chạy và so sánh cá thuật toán chung
     */
}
