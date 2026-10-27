package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class PhieuHoanTraDaLuuDAO {

    public void layDanhSachPhieuHoanTra(String tuNgay, String denNgay, String tuKhoa, String trangThai, DefaultTableModel model) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.MaPhieuHoanTra, p.NgayHoanTra, p.MaPhieuXuat, t.TenDangNhap, " +
            "(SELECT COUNT(MaSanPham) FROM ChiTietHoanTra WHERE MaPhieuHoanTra = p.MaPhieuHoanTra) AS SoMatHang, p.TrangThai " +
            "FROM PhieuHoanTra p " +
            "LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan " +
            "WHERE 1=1 "
        );

        if (tuNgay != null && !tuNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayHoanTra AS DATE) >= ? ");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            sql.append(" AND CAST(p.NgayHoanTra AS DATE) <= ? ");
        }
        if (tuKhoa != null && !tuKhoa.isEmpty()) {
            sql.append(" AND (p.MaPhieuHoanTra LIKE ? OR p.MaPhieuXuat LIKE ?) ");
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

        sql.append(" ORDER BY p.NgayHoanTra DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (tuNgay != null && !tuNgay.isEmpty()) pst.setString(paramIndex++, tuNgay);
            if (denNgay != null && !denNgay.isEmpty()) pst.setString(paramIndex++, denNgay);
            if (tuKhoa != null && !tuKhoa.isEmpty()) {
                pst.setString(paramIndex++, "%" + tuKhoa + "%");
                pst.setString(paramIndex++, "%" + tuKhoa + "%");
            }
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
                        rs.getString("MaPhieuHoanTra"),
                        rs.getDate("NgayHoanTra"),
                        rs.getString("MaPhieuXuat"),
                        rs.getString("TenDangNhap"),
                        rs.getInt("SoMatHang"),
                        tenTrangThai
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean huyPhieuHoanTra(String maPhieu) {
        String sql = "UPDATE PhieuHoanTra SET TrangThai = 2 WHERE MaPhieuHoanTra = ?";
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