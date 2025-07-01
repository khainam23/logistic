/**
 * File test để kiểm tra tính năng bật/tắt song song
 * Thay đổi parallelEnabled = false trong Main.java để test chế độ tuần tự
 */
public class TestParallel {
    public static void main(String[] args) {
        System.out.println("Để test tính năng bật/tắt song song:");
        System.out.println("1. Mở file src/main/java/org/logistic/Main.java");
        System.out.println("2. Tìm dòng: boolean parallelEnabled = true;");
        System.out.println("3. Thay đổi thành: boolean parallelEnabled = false;");
        System.out.println("4. Chạy lại ứng dụng để thấy sự khác biệt");
        System.out.println("");
        System.out.println("Khi parallelEnabled = true:");
        System.out.println("- Các thuật toán chạy song song");
        System.out.println("- Tính fitness sử dụng stream.parallel()");
        System.out.println("");
        System.out.println("Khi parallelEnabled = false:");
        System.out.println("- Các thuật toán chạy tuần tự");
        System.out.println("- Tính fitness sử dụng vòng lặp for thông thường");
    }
}