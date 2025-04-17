package org.logistic.util;

import lombok.Getter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích ghi log cho ứng dụng
 */
public class WriteLogUtil {
    private final Map<String, BufferedWriter> writers = new HashMap<>();
    private String currentLogFilePath;
    private static WriteLogUtil instance;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Đường dẫn log cho các thuật toán khác nhau
     */
    @Getter
    public enum PathLog {
        SA("logs/SimulatedAnnealing.log"),
        SHO("logs/SpottedHyenaOptimizer.log"),
        ACO("logs/AntColonyOptimization.log"),
        GWO("logs/GreyWolfOptimizer.log");

        private final String path;

        PathLog(String path) {
            this.path = path;
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
        this.currentLogFilePath = logFilePath;
        if (!writers.containsKey(logFilePath)) {
            initWriter(logFilePath);
        }
    }

    /**
     * Khởi tạo writer và tạo thư mục nếu cần
     * 
     * @param logFilePath Đường dẫn file log
     */
    private void initWriter(String logFilePath) {
        try {
            File file = new File(logFilePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // Tạo thư mục cha nếu chưa có
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true)); // Tạm thời để là ghi đè
            writers.put(logFilePath, writer);
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo Logger cho " + logFilePath + ": " + e.getMessage());
        }
    }

    /**
     * Ghi log với mức độ và thông điệp được chỉ định
     * 
     * @param level Mức độ log (INFO, DEBUG, ERROR, WARN)
     * @param message Thông điệp cần ghi
     */
    private void log(String level, String message) {
        if (currentLogFilePath == null || !writers.containsKey(currentLogFilePath)) {
            System.err.println("Logger chưa được khởi tạo. Hãy gọi setLogFilePath trước.");
            return;
        }
        
        BufferedWriter writer = writers.get(currentLogFilePath);
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            writer.write(String.format("[%s] [%s] %s%n", timestamp, level, message));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi log vào " + currentLogFilePath + ": " + e.getMessage());
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
     * Đóng tất cả các writer và giải phóng tài nguyên
     */
    public void close() {
        for (Map.Entry<String, BufferedWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng Logger " + entry.getKey() + ": " + e.getMessage());
            }
        }
        writers.clear();
        currentLogFilePath = null;
    }
}