package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class CatVaoViTriDAO {

    // Lấy danh sách phiếu nhập đã lưu nhưng chưa cất hết hàng
    public List<String> layDanhSachPhieuChoCat() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT c.MaPhieuNhap FROM ChiTietPhieuNhap c " +
                     "JOIN PhieuNhap p ON c.MaPhieuNhap = p.MaPhieuNhap " +
                     "WHERE p.TrangThai = 1 AND c.SoLuong > c.SoLuongDaCat";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaPhieuNhap"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public String layKhuCuaPhieu(String maPhieu) {
    String sql = "SELECT MaKhu FROM PhieuNhap WHERE MaPhieuNhap = ?";   // sửa tên cột nếu khác
    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, maPhieu);
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
    }
    // Đổ dữ liệu các sản phẩm cần cất vào JTable
    public void layChiTietPhieu(String maPhieu, DefaultTableModel model) {
        String sql = "SELECT c.MaSanPham, s.TenSanPham, (c.SoLuong - c.SoLuongDaCat) AS SLCanCat, c.ViTriDeXuat " +
                     "FROM ChiTietPhieuNhap c JOIN SanPham s ON c.MaSanPham = s.MaSanPham " +
                     "WHERE c.MaPhieuNhap = ? AND c.SoLuong > c.SoLuongDaCat";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                int stt = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        stt++,
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getInt("SLCanCat"),
                        rs.getString("ViTriDeXuat") != null ? rs.getString("ViTriDeXuat") : "",
                        0 // Cột "Số lượng đã cất" mặc định bằng 0 để user tự nhập
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Thực hiện lưu Transaction (Cập nhật phiếu nhập & cộng dồn Tồn kho)
    public boolean xacNhanCatHang(String maPhieu, DefaultTableModel model) {
        String sqlUpdChiTiet = "UPDATE ChiTietPhieuNhap SET SoLuongDaCat = SoLuongDaCat + ? WHERE MaPhieuNhap = ? AND MaSanPham = ?";
        String sqlCheckTonKho = "SELECT SoLuongTon FROM TonKho WHERE MaSanPham = ? AND MaViTri = ?";
        String sqlUpdTonKho = "UPDATE TonKho SET SoLuongTon = SoLuongTon + ? WHERE MaSanPham = ? AND MaViTri = ?";
        String sqlInsTonKho = "INSERT INTO TonKho (MaSanPham, MaViTri, SoLuongTon) VALUES (?, ?, ?)";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement pstCT = conn.prepareStatement(sqlUpdChiTiet);
                 PreparedStatement pstCheckTK = conn.prepareStatement(sqlCheckTonKho);
                 PreparedStatement pstUpdTK = conn.prepareStatement(sqlUpdTonKho);
                 PreparedStatement pstInsTK = conn.prepareStatement(sqlInsTonKho)) {

                for (int i = 0; i < model.getRowCount(); i++) {
                    int slCat = Integer.parseInt(model.getValueAt(i, 5).toString());
                    if (slCat <= 0) continue; 

                    String maSP = model.getValueAt(i, 1).toString();
                    String viTri = model.getValueAt(i, 4).toString();

                    // 1. Cập nhật Số lượng đã cất vào Chi Tiết Phiếu
                    pstCT.setInt(1, slCat);
                    pstCT.setString(2, maPhieu);
                    pstCT.setString(3, maSP);
                    pstCT.executeUpdate();

                    // 2. Cập nhật bảng Tồn Kho
                    pstCheckTK.setString(1, maSP);
                    pstCheckTK.setString(2, viTri);
                    try (ResultSet rs = pstCheckTK.executeQuery()) {
                        if (rs.next()) {
                            pstUpdTK.setInt(1, slCat);
                            pstUpdTK.setString(2, maSP);
                            pstUpdTK.setString(3, viTri);
                            pstUpdTK.executeUpdate();
                        } else {
                            pstInsTK.setString(1, maSP);
                            pstInsTK.setString(2, viTri);
                            pstInsTK.setInt(3, slCat);
                            pstInsTK.executeUpdate();
                        }
                    }
                }
                conn.commit(); 
                return true;
            } catch (Exception e) {
                conn.rollback(); 
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}