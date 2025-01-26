package org.algorithm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.sql.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConnectData {
     String url = "jdbc:mysql://localhost:3306/logistic";
     String username = "root";
     String password = "";
     Connection connection;

    // Phương thức mở kết nối
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Đối với MySQL
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("[ConnectData > connect()] Connect is open!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy driver JDBC", e);
            }
        }
    }

    // Phương thức đóng kết nối
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("[ConnectData > disconnect()] Connect is closed!");
        }
    }

    // Phương thức thực hiện câu truy vấn SELECT
    public ResultSet executeQuery(String query, Object... parameters) throws SQLException {
        PreparedStatement statement = prepareStatement(query, parameters);
        return statement.executeQuery();
    }

    // Phương thức thực hiện câu lệnh INSERT/UPDATE/DELETE
    public int executeUpdate(String query, Object... parameters) throws SQLException {
        PreparedStatement statement = prepareStatement(query, parameters);
        return statement.executeUpdate();
    }

    // Phương thức chuẩn bị câu lệnh PreparedStatement
    private PreparedStatement prepareStatement(String query, Object... parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement;
    }

    /**
     * Triển khai một class có khả năng connect xuống csdl để truy xuất dữ liệu
     */
}
