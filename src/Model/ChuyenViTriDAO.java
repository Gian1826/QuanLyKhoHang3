package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class ChuyenViTriDAO {

    // Lấy danh sách các vị trí đang chứa sản phẩm (Vị trí nguồn)
    public List<String> layViTriNguonCuaSanPham(String maSP) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaViTri FROM TonKho WHERE MaSanPham = ? AND SoLuongTon > 0";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaViTri"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy tất cả các vị trí trong kho (Vị trí đích)
    public List<String> layTatCaViTri() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaViTri FROM ViTri";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaViTri"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tìm kiếm thông tin sản phẩm và đưa lên JTable (khi người dùng nhập mã SP)
    public void timKiemSanPham(String maSP, String viTriNguon, DefaultTableModel model) {
        String sql = "SELECT s.MaSanPham, s.TenSanPham, t.MaViTri, t.SoLuongTon " +
                     "FROM SanPham s JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
                     "WHERE s.MaSanPham = ? AND t.MaViTri = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maSP);
            pst.setString(2, viTriNguon);
            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                if (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getString("MaViTri"),
                        rs.getInt("SoLuongTon"),
                        0 // Cột số lượng chuyển mặc định là 0
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Thực hiện Transaction chuyển hàng
    public boolean thucHienChuyenViTri(String maSP, String viTriNguon, String viTriDich, int soLuongChuyen, String lyDo, int maTaiKhoan) {
        String sqlTruNguon = "UPDATE TonKho SET SoLuongTon = SoLuongTon - ? WHERE MaSanPham = ? AND MaViTri = ?";
        String sqlCheckDich = "SELECT SoLuongTon FROM TonKho WHERE MaSanPham = ? AND MaViTri = ?";
        String sqlCongDich = "UPDATE TonKho SET SoLuongTon = SoLuongTon + ? WHERE MaSanPham = ? AND MaViTri = ?";
        String sqlInsertDich = "INSERT INTO TonKho (MaSanPham, MaViTri, SoLuongTon) VALUES (?, ?, ?)";
        String sqlLichSu = "INSERT INTO LichSuChuyenViTri (MaSanPham, ViTriNguon, ViTriDich, SoLuong, LyDo, MaTaiKhoan) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Trừ số lượng ở vị trí nguồn
            try (PreparedStatement pstTru = conn.prepareStatement(sqlTruNguon)) {
                pstTru.setInt(1, soLuongChuyen);
                pstTru.setString(2, maSP);
                pstTru.setString(3, viTriNguon);
                if (pstTru.executeUpdate() == 0) throw new Exception("Không trừ được tồn kho nguồn.");
            }

            // 2. Kiểm tra và cộng số lượng ở vị trí đích
            try (PreparedStatement pstCheck = conn.prepareStatement(sqlCheckDich)) {
                pstCheck.setString(1, maSP);
                pstCheck.setString(2, viTriDich);
                try (ResultSet rs = pstCheck.executeQuery()) {
                    if (rs.next()) {
                        // Nếu đã có dòng tồn kho ở vị trí đích -> UPDATE
                        try (PreparedStatement pstCong = conn.prepareStatement(sqlCongDich)) {
                            pstCong.setInt(1, soLuongChuyen);
                            pstCong.setString(2, maSP);
                            pstCong.setString(3, viTriDich);
                            pstCong.executeUpdate();
                        }
                    } else {
                        // Nếu chưa có -> INSERT
                        try (PreparedStatement pstInsert = conn.prepareStatement(sqlInsertDich)) {
                            pstInsert.setString(1, maSP);
                            pstInsert.setString(2, viTriDich);
                            pstInsert.setInt(3, soLuongChuyen);
                            pstInsert.executeUpdate();
                        }
                    }
                }
            }

            // 3. Ghi log vào LichSuChuyenViTri
            try (PreparedStatement pstLog = conn.prepareStatement(sqlLichSu)) {
                pstLog.setString(1, maSP);
                pstLog.setString(2, viTriNguon);
                pstLog.setString(3, viTriDich);
                pstLog.setInt(4, soLuongChuyen);
                pstLog.setString(5, lyDo);
                pstLog.setInt(6, maTaiKhoan); 
                pstLog.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}