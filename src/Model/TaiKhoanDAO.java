package Model; 

import Model.KetNoiDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaiKhoanDAO {
    public static boolean ThemThongTin(String hoTen, String email, String soDienThoai) {
        String sql = "INSERT INTO TaiKhoan (HoTen, Email, SoDienThoai) VALUES (?, ?, ?)";
        
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            // Gán giá trị tương ứng cho các dấu hỏi chấm (?)
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, soDienThoai);
            
            // Thực thi câu lệnh Insert (trả về số dòng bị ảnh hưởng)
            int rowsAffected = ps.executeUpdate();
            
            // Nếu số dòng > 0 nghĩa là thêm thành công
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Thêm thất bại do lỗi SQL hoặc kết nối
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