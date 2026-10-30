package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class PhieuKiemKeDAO {

    public static class PhieuInfo {
        public Timestamp ngay;
        public String maKhu, ghiChu;
        public int trangThai;
    }

    public List<String> layDanhSachKhu() {
        List<String> list = new ArrayList<>();
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT MaKhu FROM Khu ORDER BY MaKhu");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) list.add(rs.getString("MaKhu"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int layMaTaiKhoan(String tenDangNhap) {
        try (Connection c = KetNoiDB.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT MaTaiKhoan FROM TaiKhoan WHERE TenDangNhap = ?")) {
            p.setString(1, tenDangNhap);
            ResultSet rs = p.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /** Hàng đang tồn trong khu (phiếu mới). */
    public void layDanhSachHangTrongKhu(String maKhu, DefaultTableModel model) throws Exception{
    String sql = "SELECT t.MaSanPham, s.TenSanPham, t.MaViTri, t.SoLuongTon " +
                 "FROM TonKho t JOIN SanPham s ON t.MaSanPham = s.MaSanPham " +
                 "ORDER BY t.MaViTri, t.MaSanPham";
    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
        model.setRowCount(0);
        int stt = 1;
        while (rs.next()) {
            model.addRow(new Object[]{stt++, rs.getString(1), rs.getString(2),
                rs.getString(3), rs.getInt(4), "", 0});
            }
        }
    }
    public void layToanBoHangTon(DefaultTableModel model) throws Exception {
    String sql = "SELECT t.MaSanPham, s.TenSanPham, t.MaViTri, t.SoLuongTon " +
                 "FROM TonKho t JOIN SanPham s ON t.MaSanPham = s.MaSanPham " +
                 "ORDER BY t.MaViTri, t.MaSanPham";
    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
        model.setRowCount(0);
        int stt = 1;
        while (rs.next()) {
            model.addRow(new Object[]{stt++, rs.getString(1), rs.getString(2),
                rs.getString(3), rs.getInt(4), "", 0});
            }
        }
    }
    /** Trả về null nếu chưa có phiếu mã này. */
    public PhieuInfo layPhieu(String maPhieu) throws Exception {
        String sql = "SELECT NgayKiemKe, MaKhu, GhiChu, TrangThai FROM PhieuKiemKe WHERE MaPhieuKiemKe = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                PhieuInfo p = new PhieuInfo();
                p.ngay = rs.getTimestamp(1);
                p.maKhu = rs.getString(2);
                p.ghiChu = rs.getString(3);
                p.trangThai = rs.getInt(4);
                return p;
            }
        }
    }

    /** Nạp chi tiết của phiếu đã lưu. */
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
    public List<String> layMaSanPhamTheoPhieu(String maPhieu) throws Exception {
    List<String> ds = new ArrayList<>();
    String sql = "SELECT MaSanPham FROM ChiTietPhieuNhap WHERE MaPhieuNhap = ? " +
                 "UNION SELECT MaSanPham FROM ChiTietPhieuXuat WHERE MaPhieuXuat = ?";
    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, maPhieu);
        pst.setString(2, maPhieu);
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) ds.add(rs.getString(1));
        }
    }
    return ds;
    }
    /**
     * Lưu phiếu (mới hoặc ghi đè phiếu nháp cùng mã) trong 1 transaction.
     * trangThai = 1: cập nhật TonKho theo số thực tế.
     */
    public void luuPhieuKiemKe(String maPhieu, Timestamp ngay, String maKhu, int maTaiKhoan,
                               String ghiChu, int trangThai, DefaultTableModel model) throws Exception {
        Connection conn = null;
        try {
            conn = KetNoiDB.getConnection();
            conn.setAutoCommit(false);

            PhieuInfo cu = null;
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT TrangThai FROM PhieuKiemKe WHERE MaPhieuKiemKe = ?")) {
                p.setString(1, maPhieu);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) { cu = new PhieuInfo(); cu.trangThai = rs.getInt(1); }
                }
            }
            if (cu != null && cu.trangThai == 1) {
                throw new Exception("Phiếu " + maPhieu + " đã hoàn tất, không thể sửa!");
            }

            if (cu == null) {
                try (PreparedStatement p = conn.prepareStatement(
                        "INSERT INTO PhieuKiemKe (MaPhieuKiemKe, NgayKiemKe, MaKhu, MaTaiKhoan, GhiChu, TrangThai) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {
                    p.setString(1, maPhieu);
                    p.setTimestamp(2, ngay);
                    p.setString(3, maKhu);
                    p.setInt(4, maTaiKhoan);
                    p.setString(5, ghiChu);
                    p.setInt(6, trangThai);
                    p.executeUpdate();
                }
            } else {   // ghi đè phiếu nháp
                try (PreparedStatement p = conn.prepareStatement(
                        "UPDATE PhieuKiemKe SET NgayKiemKe=?, MaKhu=?, MaTaiKhoan=?, GhiChu=?, TrangThai=? " +
                        "WHERE MaPhieuKiemKe=?")) {
                    p.setTimestamp(1, ngay);
                    p.setString(2, maKhu);
                    p.setInt(3, maTaiKhoan);
                    p.setString(4, ghiChu);
                    p.setInt(5, trangThai);
                    p.setString(6, maPhieu);
                    p.executeUpdate();
                }
                try (PreparedStatement p = conn.prepareStatement(
                        "DELETE FROM ChiTietKiemKe WHERE MaPhieuKiemKe = ?")) {
                    p.setString(1, maPhieu);
                    p.executeUpdate();
                }
            }

            try (PreparedStatement ct = conn.prepareStatement(
                    "INSERT INTO ChiTietKiemKe (MaPhieuKiemKe, MaSanPham, MaViTri, SoLuongHeThong, SoLuongThucTe) " +
                    "VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement tk = conn.prepareStatement(
                    "UPDATE TonKho SET SoLuongTon = ? WHERE MaSanPham = ? AND MaViTri = ?")) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    String maSP = model.getValueAt(i, 1).toString();
                    String viTri = model.getValueAt(i, 3).toString();
                    int ht = Integer.parseInt(model.getValueAt(i, 4).toString());
                    Object o = model.getValueAt(i, 5);
                    int tt = (o == null || o.toString().trim().isEmpty()) ? ht
                            : Integer.parseInt(o.toString().trim());

                    ct.setString(1, maPhieu);
                    ct.setString(2, maSP);
                    ct.setString(3, viTri);
                    ct.setInt(4, ht);
                    ct.setInt(5, tt);
                    ct.addBatch();

                    if (trangThai == 1) {
                        tk.setInt(1, tt);
                        tk.setString(2, maSP);
                        tk.setString(3, viTri);
                        tk.addBatch();
                    }
                }
                ct.executeBatch();
                if (trangThai == 1) tk.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignored) { }
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) { }
        }
    }
}