package org.logistic.util;

import lombok.Setter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class WriteLogUtil {
    BufferedWriter writer;
    String logFilePath;
    static WriteLogUtil writeLogUtil;

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

    private WriteLogUtil() {}

    public static WriteLogUtil getInstance() {
        if (writeLogUtil == null)
            writeLogUtil = new WriteLogUtil();
        return writeLogUtil;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
        initWriter();
    }

    // Khởi tạo writer và tạo thư mục nếu cần
    private void initWriter() {
        try {
            File file = new File(logFilePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // Tạo thư mục cha nếu chưa có
            }

            writer = new BufferedWriter(new FileWriter(file, false)); // append = true
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo Logger: " + e.getMessage());
        }
    }

    private void log(String level, String message) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(String.format("[%s] [%s] %s%n", timestamp, level, message));
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
        }
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void debug(String message) {
        log("DEBUG", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Lỗi khi đóng Logger: " + e.getMessage());
        }
    }
}