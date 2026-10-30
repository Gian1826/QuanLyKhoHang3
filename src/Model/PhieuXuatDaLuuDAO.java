package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PhieuXuatDaLuuDAO {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String loiCuoi = "";

    // Danh sách tên khách hàng cho combobox lọc
    public List<String> layDanhSachKhachHang() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT TenKH FROM KhachHang ORDER BY TenKH";
        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Tìm phiếu xuất theo bộ lọc. Tham số nào null thì bỏ qua. tuNgay/denNgay:
     * yyyy-MM-dd. trangThai: 0 nháp, 1 đã lưu, 2 đã hủy. Mỗi dòng: {maPhieu,
     * ngay (dd/MM/yyyy), tenKH, nguoiLap, tongTien, trangThai}
     */
    public List<Object[]> timKiem(String tuNgay, String denNgay, String tenKH, Integer trangThai) {
        List<Object[]> ds = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.MaPhieuXuat, p.NgayXuat, k.TenKH, t.TenDangNhap, p.TongTien, p.TrangThai "
                + "FROM PhieuXuat p "
                + "LEFT JOIN KhachHang k ON p.MaKH = k.MaKH "
                + "LEFT JOIN TaiKhoan t ON p.MaTaiKhoan = t.MaTaiKhoan "
                + "WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (tuNgay != null && !tuNgay.trim().isEmpty()) {
            sql.append("AND p.NgayXuat >= ? ");
            params.add(java.sql.Date.valueOf(tuNgay.trim()));
        }
        if (denNgay != null && !denNgay.trim().isEmpty()) {
            sql.append("AND p.NgayXuat <= ? ");
            params.add(java.sql.Date.valueOf(denNgay.trim()));
        }
        if (tenKH != null && !tenKH.trim().isEmpty()) {
            sql.append("AND k.TenKH = ? ");
            params.add(tenKH.trim());
        }
        if (trangThai != null) {
            sql.append("AND p.TrangThai = ? ");
            params.add(trangThai);
        }
        sql.append("ORDER BY p.NgayXuat DESC, p.MaPhieuXuat DESC");

        try (Connection conn = KetNoiDB.getConnection(); PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{
                        rs.getString("MaPhieuXuat"),
                        rs.getDate("NgayXuat").toLocalDate().format(DMY),
                        rs.getString("TenKH"),
                        rs.getString("TenDangNhap"),
                        rs.getDouble("TongTien"),
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

    // Hủy phiếu: chuyển TrangThai = 2 (chưa hủy thì mới hủy được)
    public boolean huyPhieu(String maPhieu) {
        String sql = "UPDATE PhieuXuat SET TrangThai = 2 WHERE MaPhieuXuat = ? AND TrangThai IN (0, 1)";
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