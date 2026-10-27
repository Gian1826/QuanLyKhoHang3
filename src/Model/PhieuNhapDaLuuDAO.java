package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
// Import class KetNoiDB của bạn

public class PhieuNhapDaLuuDAO {

    // Hàm lấy danh sách phiếu nhập đổ vào bảng
    public void layDanhSachPhieu(DefaultTableModel model) {
        // Kết hợp 3 bảng PhieuNhap, NhaCungCap, TaiKhoan để lấy tên thay vì lấy mã
        String sql = "SELECT p.MaPhieuNhap, p.NgayNhap, n.TenNCC, t.TenDangNhap, p.TongTien " +
                     "FROM PhieuNhap p " +
                     "LEFT JOIN NhaCungCap n ON p.MaNCC = n.MaNCC " +
                     "LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan " +
                     "WHERE p.TrangThai != 2 " + // Không hiển thị phiếu đã hủy (Tùy chỉnh nếu cần)
                     "ORDER BY p.NgayNhap DESC";

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            model.setRowCount(0); // Xóa dữ liệu cũ trên bảng
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaPhieuNhap"),
                    rs.getDate("NgayNhap"),
                    rs.getString("TenNCC"),
                    rs.getString("TenDangNhap"),
                    String.format("%,.0f", rs.getDouble("TongTien")) // Format tiền tệ
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm cập nhật trạng thái phiếu thành 2 (Đã hủy)
    public boolean huyPhieuNhap(String maPhieu) {
        String sql = "UPDATE PhieuNhap SET TrangThai = 2 WHERE MaPhieuNhap = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}