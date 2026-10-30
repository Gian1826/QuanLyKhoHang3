package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PhieuHoanTraDaLuuDAO {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String loiCuoi = "";

    /**
     * Tìm phiếu. Tham số null/rỗng thì bỏ qua. tuNgay/denNgay dạng yyyy-MM-dd.
     * trangThai: 0 nháp, 1 đã lưu, 2 đã hủy. Mỗi dòng: {maPhieu, ngay
     * dd/MM/yyyy, maPhieuXuat, nguoiLap, soMatHang, trangThai(int)}
     */
    public List<Object[]> timKiem(String tuNgay, String denNgay, String tuKhoa, Integer trangThai) {
        List<Object[]> ds = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT h.MaPhieuHoanTra, h.NgayHoanTra, h.MaPhieuXuat, t.TenDangNhap, "
                + "(SELECT COUNT(*) FROM ChiTietHoanTra c WHERE c.MaPhieuHoanTra = h.MaPhieuHoanTra) AS SoMatHang, "
                + "h.TrangThai "
                + "FROM PhieuHoanTra h "
                + "LEFT JOIN TaiKhoan t ON h.MaTaiKhoan = t.MaTaiKhoan "
                + "WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (tuNgay != null && !tuNgay.trim().isEmpty()) {
            sql.append("AND CAST(h.NgayHoanTra AS DATE) >= ? ");
            params.add(java.sql.Date.valueOf(tuNgay.trim()));
        }
        if (denNgay != null && !denNgay.trim().isEmpty()) {
            sql.append("AND CAST(h.NgayHoanTra AS DATE) <= ? ");
            params.add(java.sql.Date.valueOf(denNgay.trim()));
        }
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (h.MaPhieuHoanTra LIKE ? OR h.MaPhieuXuat LIKE ?) ");
            String kw = "%" + tuKhoa.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (trangThai != null) {
            sql.append("AND h.TrangThai = ? ");
            params.add(trangThai);
        }
        sql.append("ORDER BY h.NgayHoanTra DESC, h.MaPhieuHoanTra DESC");

        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("NgayHoanTra");
                    ds.add(new Object[]{
                        rs.getString("MaPhieuHoanTra"),
                        ts == null ? "" : ts.toLocalDateTime().toLocalDate().format(DMY),
                        rs.getString("MaPhieuXuat"),
                        rs.getString("TenDangNhap"),
                        rs.getInt("SoMatHang"),
                        rs.getInt("TrangThai")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
        }
        return ds;
    }

    // {maSanPham, tenSanPham, soLuongHoan, tinhTrang}
    public List<Object[]> layChiTietDaLuu(String maPhieu) {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT c.MaSanPham, s.TenSanPham, c.SoLuongHoan, c.TinhTrang "
                + "FROM ChiTietHoanTra c "
                + "LEFT JOIN SanPham s ON c.MaSanPham = s.MaSanPham "
                + "WHERE c.MaPhieuHoanTra = ?";
        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
        }
        return ds;
    }

    // Đầu phiếu cho nút Sửa: {ngay dd/MM/yyyy, maPhieuXuat, lyDo}
    public Object[] layPhieu(String maPhieu) {
        String sql = "SELECT NgayHoanTra, MaPhieuXuat, LyDo FROM PhieuHoanTra WHERE MaPhieuHoanTra = ?";
        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    String ngay = ts == null ? "" : ts.toLocalDateTime().toLocalDate().format(DMY);
                    return new Object[]{ngay, rs.getString(2), rs.getString(3)};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
        }
        return null;
    }

    // Hủy phiếu: TrangThai = 2 (chỉ hủy được nháp hoặc đã lưu)
    public boolean huyPhieu(String maPhieu) {
        String sql = "UPDATE PhieuHoanTra SET TrangThai = 2 WHERE MaPhieuHoanTra = ? AND TrangThai IN (0, 1)";
        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            return pst.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
            return false;
        }
    }
}