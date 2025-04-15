package org.logistic.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích ghi log cho ứng dụng
 */
public class WriteLogUtil {
    private BufferedWriter writer;
    private String logFilePath;
    private static WriteLogUtil instance;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Đường dẫn log cho các thuật toán khác nhau
     */
    public enum PathLog {
        SA("logs/SimulatedAnnealing.log"),
        SHO("logs/SpottedHyenaOptimizer.log"),
        ACO("logs/AntColonyOptimization.log"),
        GWO("logs/GreyWolfOptimizer.log");

        private final String path;

        PathLog(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * Constructor riêng tư để ngăn khởi tạo trực tiếp
     */
    private WriteLogUtil() {
        // Private constructor
    }

    /**
     * Lấy instance của WriteLogUtil (Singleton pattern)
     * 
     * @return Instance của WriteLogUtil
     */
    public static synchronized WriteLogUtil getInstance() {
        if (instance == null) {
            instance = new WriteLogUtil();
        }
        return instance;
    }

    /**
     * Thiết lập đường dẫn file log và khởi tạo writer
     * 
     * @param logFilePath Đường dẫn file log
     */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        initWriter();
    }

    /**
     * Khởi tạo writer và tạo thư mục nếu cần
     */
    private void initWriter() {
        try {
            File file = new File(logFilePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // Tạo thư mục cha nếu chưa có
            }

            writer = new BufferedWriter(new FileWriter(file, false));
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo Logger: " + e.getMessage());
        }
    }

    /**
     * Ghi log với mức độ và thông điệp được chỉ định
     * 
     * @param level Mức độ log (INFO, DEBUG, ERROR, WARN)
     * @param message Thông điệp cần ghi
     */
    private void log(String level, String message) {
        if (writer == null) {
            System.err.println("Logger chưa được khởi tạo. Hãy gọi setLogFilePath trước.");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            writer.write(String.format("[%s] [%s] %s%n", timestamp, level, message));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
        }
    }

    /**
     * Ghi log mức độ INFO
     * 
     * @param message Thông điệp cần ghi
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Ghi log mức độ DEBUG
     * 
     * @param message Thông điệp cần ghi
     */
    public void debug(String message) {
        log("DEBUG", message);
    }

    /**
     * Ghi log mức độ ERROR
     * 
     * @param message Thông điệp cần ghi
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * Ghi log mức độ WARN
     * 
     * @param message Thông điệp cần ghi
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Đóng writer và giải phóng tài nguyên
     */
    public void close() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng Logger: " + e.getMessage());
        }
    }
}