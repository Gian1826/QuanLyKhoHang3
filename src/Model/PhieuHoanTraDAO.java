package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class PhieuHoanTraDAO {

    // Lấy danh sách mã phiếu xuất để đưa vào JComboBox
    public List<String> layDanhSachPhieuXuat() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaPhieuXuat FROM PhieuXuat WHERE TrangThai = 1"; // Lấy các phiếu đã xuất thành công
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaPhieuXuat"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy chi tiết hàng hóa từ phiếu xuất gốc đưa lên JTable
    public void layChiTietPhieuXuat(String maPhieuXuat, DefaultTableModel model) {
        String sql = "SELECT c.MaSanPham, s.TenSanPham, c.SoLuong " +
                     "FROM ChiTietPhieuXuat c " +
                     "JOIN SanPham s ON c.MaSanPham = s.MaSanPham " +
                     "WHERE c.MaPhieuXuat = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieuXuat);
            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                int stt = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        stt++,
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getInt("SoLuong"), // SL Đề Xuất (Tối đa có thể trả)
                        0, // SL Hoàn trả mặc định là 0 (User sẽ nhập)
                        "Nguyên vẹn" // Tình trạng mặc định
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lưu thông tin PhieuHoanTra và ChiTietHoanTra (Sử dụng Transaction)
    public boolean luuPhieuHoanTra(String maPhieu, String ngayHoanTra, String maPhieuXuat, 
                                   int maTaiKhoan, String lyDo, int trangThai, 
                                   DefaultTableModel model) {
        String sqlPhieu = "INSERT INTO PhieuHoanTra (MaPhieuHoanTra, NgayHoanTra, MaPhieuXuat, MaTaiKhoan, LyDo, TrangThai) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietHoanTra (MaPhieuHoanTra, MaSanPham, SoLuongHoan, TinhTrang) " +
                            "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false); 
            
            // 1. Lưu PhieuHoanTra
            try (PreparedStatement pstPhieu = conn.prepareStatement(sqlPhieu)) {
                pstPhieu.setString(1, maPhieu);
                pstPhieu.setString(2, ngayHoanTra);
                pstPhieu.setString(3, maPhieuXuat);
                pstPhieu.setInt(4, maTaiKhoan);
                pstPhieu.setString(5, lyDo);
                pstPhieu.setInt(6, trangThai);
                pstPhieu.executeUpdate();
            }

            // 2. Lưu ChiTietHoanTra
            try (PreparedStatement pstChiTiet = conn.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    int slHoan = Integer.parseInt(model.getValueAt(i, 4).toString());
                    if (slHoan <= 0) continue; // Chỉ lưu mặt hàng có hoàn trả

                    String maSP = model.getValueAt(i, 1).toString();
                    String tinhTrang = model.getValueAt(i, 5).toString();

                    pstChiTiet.setString(1, maPhieu);
                    pstChiTiet.setString(2, maSP);
                    pstChiTiet.setInt(3, slHoan);
                    pstChiTiet.setString(4, tinhTrang);
                    pstChiTiet.addBatch();
                }
                pstChiTiet.executeBatch();
            }

            conn.commit(); 
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}