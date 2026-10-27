package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class LichSuKiemKeDAO {

    public List<String> layDanhSachKhu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaKhu FROM Khu";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaKhu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void layDanhSachPhieuKiemKe(String tuNgay, String denNgay, String khu, String trangThai, DefaultTableModel model) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.MaPhieuKiemKe, p.NgayKiemKe, p.MaKhu, t.TenDangNhap, " +
            "(SELECT COUNT(MaSanPham) FROM ChiTietKiemKe WHERE MaPhieuKiemKe = p.MaPhieuKiemKe AND (SoLuongThucTe - SoLuongHeThong) <> 0) AS MatHangLech, p.TrangThai " +
            "FROM PhieuKiemKe p " +
            "LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan " +
            "WHERE 1=1 "
        );

        if (tuNgay != null && !tuNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayKiemKe AS DATE) >= ? ");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayKiemKe AS DATE) <= ? ");
        }
        if (khu != null && !khu.equals("Tất cả")) {
            sql.append(" AND p.MaKhu = ? ");
        }

        Integer ttId = null;
        if (trangThai != null) {
            if (trangThai.equals("Nháp")) ttId = 0;
            else if (trangThai.equals("Hoàn tất")) ttId = 1;
        }

        if (ttId != null) {
            sql.append(" AND p.TrangThai = ? ");
        }

        sql.append(" ORDER BY p.NgayKiemKe DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (tuNgay != null && !tuNgay.isEmpty()) pst.setString(paramIndex++, tuNgay);
            if (denNgay != null && !denNgay.isEmpty()) pst.setString(paramIndex++, denNgay);
            if (khu != null && !khu.equals("Tất cả")) pst.setString(paramIndex++, khu);
            if (ttId != null) pst.setInt(paramIndex++, ttId);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    String tenTrangThai = "";
                    switch (rs.getInt("TrangThai")) {
                        case 0: tenTrangThai = "Nháp"; break;
                        case 1: tenTrangThai = "Hoàn tất"; break;
                    }

                    model.addRow(new Object[]{
                        rs.getString("MaPhieuKiemKe"),
                        rs.getDate("NgayKiemKe"),
                        rs.getString("MaKhu"),
                        rs.getString("TenDangNhap"),
                        rs.getInt("MatHangLech"),
                        tenTrangThai
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}