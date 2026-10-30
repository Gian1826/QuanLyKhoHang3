package Model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class LichSuKiemKeDAO {

    public List<String> layDanhSachKhu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaKhu FROM Khu ORDER BY MaKhu";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(rs.getString("MaKhu"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public String layHoTen(String tenDangNhap) {
    try (Connection c = KetNoiDB.getConnection();
         PreparedStatement p = c.prepareStatement("SELECT HoTen FROM TaiKhoan WHERE TenDangNhap = ?")) {
        p.setString(1, tenDangNhap);
        try (ResultSet rs = p.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) return rs.getString(1).trim();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
    }
    /** tuNgay / denNgay có thể null (không lọc). */
    public void layDanhSachPhieuKiemKe(Date tuNgay, Date denNgay, String khu, String trangThai,
                                       DefaultTableModel model) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT p.MaPhieuKiemKe, p.NgayKiemKe, p.MaKhu, " +
            "COALESCE(NULLIF(LTRIM(RTRIM(t.HoTen)), N''), t.TenDangNhap) AS NguoiKiemKe, " +
            "(SELECT COUNT(*) FROM ChiTietKiemKe c WHERE c.MaPhieuKiemKe = p.MaPhieuKiemKe " +
            " AND c.SoLuongThucTe <> c.SoLuongHeThong) AS MatHangLech, p.TrangThai " +
            "FROM PhieuKiemKe p LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan WHERE 1=1 ");

        if (tuNgay != null)  sql.append(" AND CAST(p.NgayKiemKe AS DATE) >= ? ");
        if (denNgay != null) sql.append(" AND CAST(p.NgayKiemKe AS DATE) <= ? ");
        boolean locKhu = khu != null && !khu.equals("Tất cả");
        if (locKhu) sql.append(" AND p.MaKhu = ? ");

        Integer ttId = null;
        if ("Nháp".equals(trangThai)) ttId = 0;
        else if ("Hoàn tất".equals(trangThai)) ttId = 1;
        if (ttId != null) sql.append(" AND p.TrangThai = ? ");

        sql.append(" ORDER BY p.NgayKiemKe DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (tuNgay != null)  pst.setDate(i++, tuNgay);
            if (denNgay != null) pst.setDate(i++, denNgay);
            if (locKhu)          pst.setString(i++, khu);
            if (ttId != null)    pst.setInt(i++, ttId);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("MaPhieuKiemKe"),
                        rs.getDate("NgayKiemKe"),
                        rs.getString("MaKhu"),
                        rs.getString("NguoiKiemKe"),
                        rs.getInt("MatHangLech"),
                        rs.getInt("TrangThai") == 1 ? "Hoàn tất" : "Nháp"
                    });
                }
            }
        }
    }

    /** Chi tiết một phiếu: STT, Mã SP, Tên SP, Vị trí, SL hệ thống, SL thực tế, Chênh lệch. */
    public void layChiTietPhieu(String maPhieu, DefaultTableModel model) throws Exception {
        String sql = "SELECT c.MaSanPham, s.TenSanPham, c.MaViTri, c.SoLuongHeThong, c.SoLuongThucTe " +
                     "FROM ChiTietKiemKe c JOIN SanPham s ON c.MaSanPham = s.MaSanPham " +
                     "WHERE c.MaPhieuKiemKe = ? ORDER BY c.MaViTri, c.MaSanPham";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                int stt = 1;
                while (rs.next()) {
                    int ht = rs.getInt(4), tt = rs.getInt(5);
                    model.addRow(new Object[]{stt++, rs.getString(1), rs.getString(2),
                        rs.getString(3), ht, tt, tt - ht});
                }
            }
        }
    }

    public String layGhiChu(String maPhieu) throws Exception {
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                 "SELECT GhiChu FROM PhieuKiemKe WHERE MaPhieuKiemKe = ?")) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) return rs.getString(1);
            }
        }
        return "";
    }
}