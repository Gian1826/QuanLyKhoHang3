package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDaLuuDAO {

    // Cột PhieuNhap.TrangThai là tinyint. Quy ước (hãy kiểm tra lại với dữ liệu của bạn):
    public static final int TT_NHAP = 0;
    public static final int TT_DA_LUU = 1;
    public static final int TT_DA_HUY = 2;

    public static String tenTrangThai(int tt) {
        switch (tt) {
            case TT_NHAP:   return "Nháp";
            case TT_DA_LUU: return "Đã Lưu";
            case TT_DA_HUY: return "Đã Hủy";
            default:        return "Khác (" + tt + ")";
        }
    }

    public static int maTrangThai(String ten) {
        if ("Nháp".equals(ten)) return TT_NHAP;
        if ("Đã Hủy".equals(ten)) return TT_DA_HUY;
        return TT_DA_LUU;
    }

    public static class PhieuRow {
        public String ma, ncc, nguoiLap, trangThai, ghiChu;
        public java.sql.Date ngay;
        public double tong;
    }

    public static class ChiTietRow {
        public String maSP, tenSP, dvt;
        public int soLuong, daCat;
        public double donGia;
    }

    // ===== Tìm kiếm / lọc (tham số null = không lọc) =====
    public static List<PhieuRow> timKiem(java.sql.Date tu, java.sql.Date den,
                                         String ncc, String trangThai) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.MaPhieuNhap, p.NgayNhap, p.GhiChu, t.HoTen, t.TenDangNhap, ");
        sql.append("p.TrangThai, ");
        sql.append("ISNULL(SUM(c.SoLuong * c.DonGia), 0) AS TongTien ");
        sql.append("FROM PhieuNhap p ");
        sql.append("LEFT JOIN TaiKhoan t ON t.MaTaiKhoan = p.MaTaiKhoan ");
        sql.append("LEFT JOIN ChiTietPhieuNhap c ON c.MaPhieuNhap = p.MaPhieuNhap ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (tu != null) {
            sql.append("AND CAST(p.NgayNhap AS DATE) >= ? ");
            params.add(tu);
        }
        if (den != null) {
            sql.append("AND CAST(p.NgayNhap AS DATE) <= ? ");
            params.add(den);
        }
        if (ncc != null) {
            // Nhà cung cấp đang được lưu trong GhiChu dạng "NCC: ... | Khu: ..."
            sql.append("AND p.GhiChu LIKE ? ");
            params.add("%NCC: " + ncc + "%");
        }
        if (trangThai != null) {
            sql.append("AND p.TrangThai = ? ");
            params.add(maTrangThai(trangThai));
        }
        sql.append("GROUP BY p.MaPhieuNhap, p.NgayNhap, p.GhiChu, t.HoTen, t.TenDangNhap, p.TrangThai");
        sql.append(" ORDER BY p.NgayNhap DESC, p.MaPhieuNhap DESC");

        List<PhieuRow> ds = new ArrayList<>();
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object o = params.get(i);
                if (o instanceof java.sql.Date) ps.setDate(i + 1, (java.sql.Date) o);
                else if (o instanceof Integer) ps.setInt(i + 1, (Integer) o);
                else ps.setString(i + 1, String.valueOf(o));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuRow r = new PhieuRow();
                    r.ma = rs.getString("MaPhieuNhap");
                    r.ngay = rs.getDate("NgayNhap");
                    r.ghiChu = rs.getString("GhiChu");
                    r.ncc = tachNCC(r.ghiChu);
                    String hoTen = rs.getString("HoTen");
                    r.nguoiLap = (hoTen != null && !hoTen.isEmpty()) ? hoTen : rs.getString("TenDangNhap");
                    r.tong = rs.getDouble("TongTien");
                    r.trangThai = tenTrangThai(rs.getInt("TrangThai"));
                    ds.add(r);
                }
            }
        }
        return ds;
    }

    // ===== Chi tiết 1 phiếu =====
    public static List<ChiTietRow> layChiTiet(String maPhieu) throws SQLException {
        String sql = "SELECT c.MaSanPham, s.TenSanPham, s.DonViTinh, c.SoLuong, c.DonGia, c.SoLuongDaCat "
                + "FROM ChiTietPhieuNhap c LEFT JOIN SanPham s ON s.MaSanPham = c.MaSanPham "
                + "WHERE c.MaPhieuNhap = ? ORDER BY c.MaSanPham";
        List<ChiTietRow> ds = new ArrayList<>();
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietRow r = new ChiTietRow();
                    r.maSP = rs.getString("MaSanPham");
                    r.tenSP = rs.getString("TenSanPham");
                    r.dvt = rs.getString("DonViTinh");
                    r.soLuong = rs.getInt("SoLuong");
                    r.donGia = rs.getDouble("DonGia");
                    r.daCat = rs.getInt("SoLuongDaCat");
                    ds.add(r);
                }
            }
        }
        return ds;
    }

    // ===== Hủy phiếu (đổi trạng thái, không xóa dữ liệu) =====
    public static void huyPhieu(String maPhieu) throws SQLException {
        try (Connection conn = KetNoiDB.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM ChiTietPhieuNhap WHERE MaPhieuNhap = ? AND SoLuongDaCat > 0")) {
                ps.setString(1, maPhieu);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalStateException(
                                "Phiếu này đã có hàng được cất vào kho, không thể hủy.");
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PhieuNhap SET TrangThai = ? WHERE MaPhieuNhap = ? AND TrangThai <> ?")) {
                ps.setInt(1, TT_DA_HUY);
                ps.setString(2, maPhieu);
                ps.setInt(3, TT_DA_HUY);
                if (ps.executeUpdate() == 0) {
                    throw new IllegalStateException("Phiếu đã ở trạng thái Đã Hủy.");
                }
            }
        }
    }

    // ===== Sửa ghi chú =====
    public static boolean capNhatGhiChu(String maPhieu, String ghiChu) throws SQLException {
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE PhieuNhap SET GhiChu = ? WHERE MaPhieuNhap = ?")) {
            ps.setString(1, ghiChu);
            ps.setString(2, maPhieu);
            return ps.executeUpdate() > 0;
        }
    }

    // ===== Tiện ích: lấy tên NCC từ ghi chú "NCC: x | Khu: y | ..." =====
    public static String tachNCC(String ghiChu) {
        if (ghiChu != null && ghiChu.startsWith("NCC: ")) {
            String s = ghiChu.substring(5);
            int i = s.indexOf(" | ");
            return i >= 0 ? s.substring(0, i) : s;
        }
        return "";
    }
}