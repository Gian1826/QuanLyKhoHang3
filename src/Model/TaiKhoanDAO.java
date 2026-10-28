package Model; 

import Model.KetNoiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaiKhoanDAO {
    /**
     * Cập nhật thông tin cá nhân dựa vào TenDangNhap
     */
    public static boolean capNhatThongTin(String tenDangNhap, String hoTen, String email, String soDienThoai) {
        // Đảm bảo đây là câu lệnh UPDATE chứ không phải INSERT
        String sql = "UPDATE TaiKhoan SET HoTen = ?, Email = ?, SoDienThoai = ? WHERE TenDangNhap = ?";
        
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, soDienThoai);
            ps.setString(4, tenDangNhap); // Điều kiện để biết cập nhật cho tài khoản nào
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean doiMatKhau(String tenDangNhap, String matKhauCu, String matKhauMoi) {
        // Câu lệnh SQL cập nhật mật khẩu với điều kiện khớp tên đăng nhập và mật khẩu cũ
        String sql = "UPDATE TaiKhoan SET MatKhau = ? WHERE TenDangNhap = ? AND MatKhau = ?";
        
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, matKhauMoi);
            ps.setString(2, tenDangNhap);
            ps.setString(3, matKhauCu);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;   
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
