package Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class KetNoiDB {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static Connection conn = null;

    // Chạy một lần khi class được nạp: đọc cấu hình từ ketnoi.properties
    static {
        Properties p = new Properties();
        p.setProperty("server", "localhost");
        p.setProperty("port", "1433");
        p.setProperty("database", "QuanLyKho_DB");
        p.setProperty("user", "sa");
        p.setProperty("password", "123456");
        try (FileInputStream in = new FileInputStream("ketnoi.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.out.println("Khong doc duoc ketnoi.properties tai: "
                    + new File("ketnoi.properties").getAbsolutePath());
        }
        URL = "jdbc:jtds:sqlserver://" + p.getProperty("server").trim() + ":"
                + p.getProperty("port").trim() + "/" + p.getProperty("database").trim();
        USER = p.getProperty("user").trim();
        PASSWORD = p.getProperty("password").trim();
    }

    private KetNoiDB() {
    }

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Khong tim thay driver jTDS (jtds-1.3.1.jar).", e);
            }
        }
        return conn;
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Chạy file này (chuột phải > Run File) để kiểm tra kết nối
    public static void main(String[] args) {
        try {
            Connection c = getConnection();
            System.out.println("Ket noi thanh cong toi: " + c.getCatalog());
        } catch (SQLException e) {
            System.out.println("Ket noi that bai: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
}