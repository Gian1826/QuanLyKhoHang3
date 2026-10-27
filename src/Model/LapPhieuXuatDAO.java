package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class LapPhieuXuatDAO {

    // Lấy thông tin Sản phẩm và Tổng tồn kho dựa vào Mã SP
    public Object[] layThongTinSanPham(String maSP) {
        String sql = "SELECT s.TenSanPham, s.DonViTinh, s.GiaXuat, ISNULL(SUM(t.SoLuongTon), 0) AS TongTon " +
                     "FROM SanPham s " +
                     "LEFT JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
                     "WHERE s.MaSanPham = ? AND s.TrangThai = 1 " +
                     "GROUP BY s.TenSanPham, s.DonViTinh, s.GiaXuat";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("TenSanPham"),
                        rs.getString("DonViTinh"),
                        rs.getInt("TongTon"),
                        rs.getDouble("GiaXuat")
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lưu Phiếu xuất và Chi tiết phiếu xuất (Transaction)
    public boolean luuPhieuXuat(String maPhieu, String ngayXuat, String maKH, int maTaiKhoan, 
                                String maKhu, double tongTien, String ghiChu, int trangThai, 
                                DefaultTableModel chiTietModel) {
        
        String sqlPhieu = "INSERT INTO PhieuXuat (MaPhieuXuat, NgayXuat, MaKH, MaTaiKhoan, MaKhu, TongTien, GhiChu, TrangThai) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietPhieuXuat (MaPhieuXuat, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lưu thông tin bảng PhieuXuat
            try (PreparedStatement pstPhieu = conn.prepareStatement(sqlPhieu)) {
                pstPhieu.setString(1, maPhieu);
                pstPhieu.setString(2, ngayXuat); // Định dạng yyyy-MM-dd
                pstPhieu.setString(3, maKH);
                pstPhieu.setInt(4, maTaiKhoan);
                pstPhieu.setString(5, maKhu);
                pstPhieu.setDouble(6, tongTien);
                pstPhieu.setString(7, ghiChu);
                pstPhieu.setInt(8, trangThai);
                pstPhieu.executeUpdate();
            }

            // 2. Lưu thông tin bảng ChiTietPhieuXuat
            try (PreparedStatement pstChiTiet = conn.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                    Object maSPObj = chiTietModel.getValueAt(i, 1);
                    if (maSPObj == null || maSPObj.toString().trim().isEmpty()) continue;

                    String maSP = maSPObj.toString();
                    int soLuong = Integer.parseInt(chiTietModel.getValueAt(i, 5).toString());
                    double donGia = Double.parseDouble(chiTietModel.getValueAt(i, 6).toString());
                    
                    pstChiTiet.setString(1, maPhieu);
                    pstChiTiet.setString(2, maSP);
                    pstChiTiet.setInt(3, soLuong);
                    pstChiTiet.setDouble(4, donGia);
                    pstChiTiet.addBatch();
                }
                pstChiTiet.executeBatch();
            }

            conn.commit(); // Xác nhận Transaction
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}