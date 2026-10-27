package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class PhieuXuatDaLuuDAO {

    public void layDanhSachPhieuXuat(String tuNgay, String denNgay, String khachHang, String trangThai, DefaultTableModel model) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.MaPhieuXuat, p.NgayXuat, p.MaKH, t.TenDangNhap, p.TongTien, p.TrangThai " +
            "FROM PhieuXuat p " +
            "LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan " +
            "WHERE 1=1 "
        );

        if (tuNgay != null && !tuNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayXuat AS DATE) >= ? ");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayXuat AS DATE) <= ? ");
        }
        if (khachHang != null && !khachHang.equals("Tất cả")) {
            sql.append(" AND p.MaKH = ? ");
        }

        Integer ttId = null;
        if (trangThai != null) {
            if (trangThai.equals("Nháp")) ttId = 0;
            else if (trangThai.equals("Đã lưu")) ttId = 1;
            else if (trangThai.equals("Đã hủy")) ttId = 2;
        }

        if (ttId != null) {
            sql.append(" AND p.TrangThai = ? ");
        }

        sql.append(" ORDER BY p.NgayXuat DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (tuNgay != null && !tuNgay.isEmpty()) pst.setString(paramIndex++, tuNgay);
            if (denNgay != null && !denNgay.isEmpty()) pst.setString(paramIndex++, denNgay);
            if (khachHang != null && !khachHang.equals("Tất cả")) pst.setString(paramIndex++, khachHang);
            if (ttId != null) pst.setInt(paramIndex++, ttId);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    String tenTrangThai = "";
                    switch (rs.getInt("TrangThai")) {
                        case 0: tenTrangThai = "Nháp"; break;
                        case 1: tenTrangThai = "Đã lưu"; break;
                        case 2: tenTrangThai = "Đã hủy"; break;
                    }

                    model.addRow(new Object[]{
                        rs.getString("MaPhieuXuat"),
                        rs.getDate("NgayXuat"),
                        rs.getString("MaKH"),
                        rs.getString("TenDangNhap"),
                        String.format("%,.0f", rs.getDouble("TongTien")),
                        tenTrangThai
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean huyPhieuXuat(String maPhieu) {
        String sql = "UPDATE PhieuXuat SET TrangThai = 2 WHERE MaPhieuXuat = ?";
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
