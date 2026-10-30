package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class PhieuHoanTraDAO {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String loiCuoi = "";

    // ================= LẬP PHIẾU =================
    // Phiếu xuất đã lưu (TrangThai = 1) để chọn làm phiếu gốc
    public List<String> layDanhSachPhieuXuat() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT MaPhieuXuat FROM PhieuXuat WHERE TrangThai = 1 ORDER BY MaPhieuXuat DESC";
        try (Connection conn = KetNoiDB.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
        }
        return ds;
    }

    // Nạp hàng của phiếu xuất: STT, Mã SP, Tên SP, SL đề xuất, SL hoàn trả (0), Tình trạng
    public void layChiTietPhieuXuat(String maPX, DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT c.MaSanPham, s.TenSanPham, c.SoLuong FROM ChiTietPhieuXuat c "
                + "LEFT JOIN SanPham s ON c.MaSanPham = s.MaSanPham WHERE c.MaPhieuXuat = ?";
        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPX);
            try (ResultSet rs = pst.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{stt++, rs.getString(1), rs.getString(2),
                        rs.getInt(3), 0, "Nguyên vẹn"});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
        }
    }

    // Lưu phiếu (thêm mới, hoặc cập nhật nếu mã đã có và đang là nháp) - dùng Transaction
    public boolean luuPhieuHoanTra(String ma, String ngayStr, String maPX, int maTaiKhoan,
            String lyDo, int trangThai, DefaultTableModel model) {
        Connection conn = null;
        try {
            conn = KetNoiDB.getConnection();
            conn.setAutoCommit(false);

            java.sql.Date ngay = java.sql.Date.valueOf(LocalDate.parse(ngayStr, DMY));

            boolean tonTai = false;
            try (PreparedStatement p = conn.prepareStatement(
                    "SELECT TrangThai FROM PhieuHoanTra WHERE MaPhieuHoanTra = ?")) {
                p.setString(1, ma);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        tonTai = true;
                        if (rs.getInt(1) != 0) {
                            throw new Exception("Mã phiếu đã tồn tại và không phải phiếu nháp");
                        }
                    }
                }
            }

            if (tonTai) {
                try (PreparedStatement p = conn.prepareStatement(
                        "UPDATE PhieuHoanTra SET NgayHoanTra=?, MaPhieuXuat=?, LyDo=?, TrangThai=? "
                        + "WHERE MaPhieuHoanTra=?")) {
                    p.setDate(1, ngay);
                    p.setString(2, maPX);
                    p.setString(3, lyDo);
                    p.setInt(4, trangThai);
                    p.setString(5, ma);
                    p.executeUpdate();
                }
                try (PreparedStatement p = conn.prepareStatement(
                        "DELETE FROM ChiTietHoanTra WHERE MaPhieuHoanTra=?")) {
                    p.setString(1, ma);
                    p.executeUpdate();
                }
            } else {
                try (PreparedStatement p = conn.prepareStatement(
                        "INSERT INTO PhieuHoanTra(MaPhieuHoanTra, NgayHoanTra, MaPhieuXuat, MaTaiKhoan, LyDo, TrangThai) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
                    p.setString(1, ma);
                    p.setDate(2, ngay);
                    p.setString(3, maPX);
                    p.setInt(4, maTaiKhoan);
                    p.setString(5, lyDo);
                    p.setInt(6, trangThai);
                    p.executeUpdate();
                }
            }

            int soDong = 0;
            try (PreparedStatement p = conn.prepareStatement(
                    "INSERT INTO ChiTietHoanTra(MaPhieuHoanTra, MaSanPham, SoLuongHoan, TinhTrang) VALUES (?,?,?,?)")) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object o = model.getValueAt(i, 4);
                    String s = o == null ? "" : o.toString().trim();
                    if (s.isEmpty()) {
                        continue;
                    }
                    int sl = Integer.parseInt(s);
                    if (sl <= 0) {
                        continue;   // chỉ lưu dòng có hoàn trả
                    }
                    p.setString(1, ma);
                    p.setString(2, model.getValueAt(i, 1).toString());
                    p.setInt(3, sl);
                    p.setString(4, String.valueOf(model.getValueAt(i, 5)));
                    p.addBatch();
                    soDong++;
                }
                if (soDong == 0) {
                    throw new Exception("Chưa có mặt hàng nào có số lượng hoàn trả > 0");
                }
                p.executeBatch();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignore) {
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    // ================= PHIẾU ĐÃ LƯU =================
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

        try (Connection conn = KetNoiDB.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql.toString())) {
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
    public String taoMaMoi() {
    int max = 0;
    String sql = "SELECT MaPhieuHoanTra FROM PhieuHoanTra WHERE MaPhieuHoanTra LIKE 'HT-%'";
    try (Connection conn = KetNoiDB.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
            try {
                int so = Integer.parseInt(rs.getString(1).substring(3).trim());
                if (so > max) {
                    max = so;
                }
            } catch (Exception ignore) {
                // bỏ qua mã không đúng định dạng
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        loiCuoi = e.getMessage();
    }
    return String.format("HT-%04d", max + 1);
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