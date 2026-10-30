package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class DanhSachLayHangDAO {
    public int layMaTaiKhoan(String tenDangNhap) {
    try (Connection c = KetNoiDB.getConnection();
         PreparedStatement p = c.prepareStatement("SELECT MaTaiKhoan FROM TaiKhoan WHERE TenDangNhap = ?")) {
        p.setString(1, tenDangNhap);
        ResultSet rs = p.executeQuery();
        return rs.next() ? rs.getInt(1) : -1;
    } catch (Exception e) { return -1; }
}
    // Lấy danh sách lệnh lấy hàng (kết hợp với chi tiết lệnh và sản phẩm)
    public void timKiemDanhSachLayHang(String ngay, String khu, String trangThai, DefaultTableModel model) {
        // Build câu SQL linh hoạt dựa trên bộ lọc
        StringBuilder sql = new StringBuilder(
            "SELECT l.MaLenh, l.MaPhieuXuat, s.TenSanPham, c.MaViTri, c.SoLuongCanLay, l.TrangThai " +
            "FROM LenhLayHang l " +
            "JOIN ChiTietLayHang c ON l.MaLenh = c.MaLenh " +
            "JOIN SanPham s ON c.MaSanPham = s.MaSanPham " +
            "JOIN PhieuXuat p ON l.MaPhieuXuat = p.MaPhieuXuat " +
            "WHERE 1=1 "
        );

        // Map trạng thái từ text sang ID (0 = Chờ lấy, 1 = Đang lấy, 2 = Hoàn thành, 3 = Đã hủy)
        Integer ttId = null;
        if (trangThai.equals("Chờ lấy")) ttId = 0;
        else if (trangThai.equals("Đang lấy")) ttId = 1;
        else if (trangThai.equals("Hoàn thành")) ttId = 2;
        else if (trangThai.equals("Đã hủy")) ttId = 3; 

        if (ngay != null && !ngay.isEmpty()) {
            sql.append(" AND CAST(l.NgayTao AS DATE) = ? ");
        }
        if (khu != null && !khu.equals("Tất cả")) {
            sql.append(" AND p.MaKhu = ? ");
        }
        if (ttId != null) {
            sql.append(" AND l.TrangThai = ? ");
        }

        sql.append(" ORDER BY l.NgayTao DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (ngay != null && !ngay.isEmpty()) pst.setString(paramIndex++, ngay);
            if (khu != null && !khu.equals("Tất cả")) pst.setString(paramIndex++, khu);
            if (ttId != null) pst.setInt(paramIndex++, ttId);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0); // Xóa dữ liệu cũ
                while (rs.next()) {
                    String tenTrangThai = "";
                    switch (rs.getInt("TrangThai")) {
                        case 0: tenTrangThai = "Chờ lấy"; break;
                        case 1: tenTrangThai = "Đang lấy"; break;
                        case 2: tenTrangThai = "Hoàn thành"; break;
                        case 3: tenTrangThai = "Đã hủy"; break;
                    }

                    model.addRow(new Object[]{
                        rs.getString("MaLenh"),
                        rs.getString("MaPhieuXuat"),
                        rs.getString("TenSanPham"),
                        rs.getString("MaViTri"),
                        rs.getInt("SoLuongCanLay"),
                        tenTrangThai
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Cập nhật trạng thái lệnh lấy hàng (0 -> 1 hoặc 1 -> 2)
    public boolean capNhatTrangThaiLenh(String maLenh, int trangThaiMoi, int maNguoiLay) {
        String sql = "UPDATE LenhLayHang SET TrangThai = ?, MaNguoiLay = ? ";
        if (trangThaiMoi == 2) {
            sql += ", ThoiGianHoanThanh = GETDATE() ";
        }
        sql += "WHERE MaLenh = ?";

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, trangThaiMoi);
            pst.setInt(2, maNguoiLay);
            pst.setString(3, maLenh);
            
            // Nếu chuyển sang Hoàn thành, tự động cập nhật Số lượng đã lấy = Số lượng cần lấy
            if (trangThaiMoi == 2) {
                String sqlChiTiet = "UPDATE ChiTietLayHang SET SoLuongDaLay = SoLuongCanLay WHERE MaLenh = ?";
                try (PreparedStatement pstCT = conn.prepareStatement(sqlChiTiet)) {
                    pstCT.setString(1, maLenh);
                    pstCT.executeUpdate();
                }
            }
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}