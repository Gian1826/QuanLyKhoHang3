package View;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KetNoiDB {

    private static final String SERVER   = "localhost";
    private static final String PORT     = "1433";
    private static final String DATABASE = "QuanLyKho_DB";
    private static final String USER     = "sa";
    private static final String PASSWORD = "123456";

    private static final String URL =
            "jdbc:jtds:sqlserver://" + SERVER + ":" + PORT + "/" + DATABASE;

    private static Connection conn = null;

    private KetNoiDB() {
    }

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy driver JDBC jTDS. "
                        + "Hãy kiểm tra jtds-1.3.1.jar đã có trong Properties > Libraries chưa.", e);
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

    public static void main(String[] args) {
        try {
            Connection c = KetNoiDB.getConnection();
            if (c != null && !c.isClosed()) {
                System.out.println("Kết nối SQL Server thành công!");
                System.out.println("Database: " + c.getCatalog());
                System.out.println("Driver  : " + c.getMetaData().getDriverName()
                        + " " + c.getMetaData().getDriverVersion());
            }
        } catch (SQLException e) {
            System.out.println("Kết nối thất bại!");
            System.out.println("Lý do: " + e.getMessage());
            e.printStackTrace();
        } finally {
            KetNoiDB.closeConnection();
            System.out.println("Đã đóng kết nối.");
        }
    }
}