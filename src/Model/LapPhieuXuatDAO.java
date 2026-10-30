package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class LapPhieuXuatDAO {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Thông báo lỗi SQL gần nhất, để Controller hiện lên cho người dùng
    public String loiCuoi = "";

    // Lấy thông tin Sản phẩm và Tổng tồn kho dựa vào Mã SP
    public Object[] layThongTinSanPham(String maSP) {
        String sql = "SELECT s.TenSanPham, s.DonViTinh, s.GiaXuat, ISNULL(SUM(t.SoLuongTon), 0) AS TongTon " +
                     "FROM SanPham s " +
                     "LEFT JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
                     "WHERE s.MaSanPham = ? AND s.TrangThai = 1 " +
                     "GROUP BY s.TenSanPham, s.DonViTinh, s.GiaXuat";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("TenSanPham"),
                        rs.getString("DonViTinh"),
                        rs.getInt("TongTon"),
                        rs.getDouble("GiaXuat")
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy MaTaiKhoan (số) từ tên đăng nhập, giống PhieuNhapDAO
    private static int layMaTaiKhoan(Connection conn, String tenDangNhap) throws SQLException {
        String sql = "SELECT MaTaiKhoan FROM TaiKhoan WHERE TenDangNhap = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MaTaiKhoan");
                }
            }
        }
        throw new SQLException("Không tìm thấy tài khoản: " + tenDangNhap);
    }

    // Thử tenDangNhap trước, không có thì thử hoTen (phòng khi session chỉ lưu một trong hai)
    private static int layMaTaiKhoanHienTai(Connection conn) throws SQLException {
        String ten = PhienDangNhap.tenDangNhap;
        if (ten != null && !ten.trim().isEmpty()) {
            try {
                return layMaTaiKhoan(conn, ten.trim());
            } catch (SQLException ex) {
                if (PhienDangNhap.hoTen == null) throw ex;
            }
        }
        return layMaTaiKhoan(conn, String.valueOf(PhienDangNhap.hoTen).trim());
    }

    // Lưu Phiếu xuất và Chi tiết phiếu xuất (Transaction)
    // ngayXuat: yyyy-MM-dd (controller đã đổi sẵn)
    public boolean luuPhieuXuat(String maPhieu, String ngayXuat, String maKH,
                                String maKhu, double tongTien, String ghiChu, int trangThai,
                                DefaultTableModel chiTietModel) {

        String sqlPhieu = "INSERT INTO PhieuXuat (MaPhieuXuat, NgayXuat, MaKH, MaKhu, TongTien, GhiChu, TrangThai, MaTaiKhoan) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maTK = 1;
                try (PreparedStatement pst = conn.prepareStatement(sqlPhieu)) {
                    pst.setString(1, maPhieu);
                    pst.setTimestamp(2, taoThoiGian(ngayXuat));
                    pst.setString(3, maKH);
                    pst.setString(4, maKhu);
                    pst.setDouble(5, tongTien);
                    pst.setString(6, ghiChu);
                    pst.setInt(7, trangThai);
                    pst.setInt(8, maTK);
                    pst.executeUpdate();
                }
                luuChiTiet(conn, maPhieu, chiTietModel);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
            return false;
        }
    }

    // Mã phiếu kế tiếp: PN-0004 -> PN-0005
    public String layMaPhieuMoi() {
        String sql = "SELECT ISNULL(MAX(CAST(SUBSTRING(MaPhieuXuat, 4, 10) AS INT)), 0) " +
                     "FROM PhieuXuat WHERE MaPhieuXuat LIKE 'PX-[0-9]%'";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            int so = rs.next() ? rs.getInt(1) : 0;
            return String.format("PX-%04d", so + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "PX-0001";
        }
    }

    // Danh sách MaKhu cho combobox
    public List<String> layDanhSachKhu() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT MaKhu FROM Khu ORDER BY MaKhu";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) ds.add(rs.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Đổi chữ người dùng gõ (mã hoặc tên) thành MaKH; không thấy thì trả về null
    public String timMaKH(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String sql = "SELECT TOP 1 MaKH FROM KhachHang WHERE MaKH = ? OR TenKH = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, text.trim());
            pst.setString(2, text.trim());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Thông tin phiếu để nạp lại form khi sửa: {ngayXuat (dd/MM/yyyy), MaKH, MaKhu, ghiChu, trangThai}
    public Object[] layThongTinPhieu(String maPhieu) {
        String sql = "SELECT NgayXuat, MaKH, MaKhu, GhiChu, TrangThai FROM PhieuXuat WHERE MaPhieuXuat = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getTimestamp("NgayXuat").toLocalDateTime().toLocalDate().format(DMY),
                        rs.getString("MaKH"),
                        rs.getString("MaKhu"),
                        rs.getString("GhiChu"),
                        rs.getInt("TrangThai")
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Chi tiết phiếu: mỗi dòng {maSP, tenSP, dvt, ton, slXuat, donGia}
    public List<Object[]> layChiTietPhieu(String maPhieu) {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT c.MaSanPham, s.TenSanPham, s.DonViTinh, " +
                     "       ISNULL(SUM(t.SoLuongTon), 0) AS TongTon, c.SoLuong, c.DonGia " +
                     "FROM ChiTietPhieuXuat c " +
                     "JOIN SanPham s ON c.MaSanPham = s.MaSanPham " +
                     "LEFT JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
                     "WHERE c.MaPhieuXuat = ? " +
                     "GROUP BY c.MaSanPham, s.TenSanPham, s.DonViTinh, c.SoLuong, c.DonGia " +
                     "ORDER BY c.MaSanPham";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getString("DonViTinh"),
                        rs.getInt("TongTon"),
                        rs.getInt("SoLuong"),
                        rs.getDouble("DonGia")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    // Cập nhật phiếu nháp: sửa bảng phiếu, xóa chi tiết cũ, thêm chi tiết mới (Transaction)
    public boolean capNhatPhieuXuat(String maPhieu, String ngayXuat, String maKH,
                                    String maKhu, double tongTien, String ghiChu, int trangThai,
                                    DefaultTableModel chiTietModel) {
        String sqlPhieu = "UPDATE PhieuXuat SET NgayXuat = ?, MaKH = ?, MaKhu = ?, " +
                          "TongTien = ?, GhiChu = ?, TrangThai = ? " +
                          "WHERE MaPhieuXuat = ? AND TrangThai = 0";
        String sqlXoa = "DELETE FROM ChiTietPhieuXuat WHERE MaPhieuXuat = ?";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pst = conn.prepareStatement(sqlPhieu)) {
                    pst.setTimestamp(1, taoThoiGian(ngayXuat));
                    pst.setString(2, maKH);
                    pst.setString(3, maKhu);
                    pst.setDouble(4, tongTien);
                    pst.setString(5, ghiChu);
                    pst.setInt(6, trangThai);
                    pst.setString(7, maPhieu);
                    if (pst.executeUpdate() != 1) {
                        conn.rollback();
                        loiCuoi = "Không phải phiếu nháp hoặc phiếu không tồn tại";
                        return false;
                    }
                }
                try (PreparedStatement pst = conn.prepareStatement(sqlXoa)) {
                    pst.setString(1, maPhieu);
                    pst.executeUpdate();
                }
                luuChiTiet(conn, maPhieu, chiTietModel);
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            loiCuoi = e.getMessage();
            return false;
        }
    }

    // Ghi các dòng của bảng vào ChiTietPhieuXuat (dùng chung cho lưu mới và cập nhật)
    private void luuChiTiet(Connection conn, String maPhieu, DefaultTableModel model) throws Exception {
        String sql = "INSERT INTO ChiTietPhieuXuat (MaPhieuXuat, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            for (int i = 0; i < model.getRowCount(); i++) {
                Object maSPObj = model.getValueAt(i, 1);
                if (maSPObj == null || maSPObj.toString().trim().isEmpty()) continue;

                pst.setString(1, maPhieu);
                pst.setString(2, maSPObj.toString().trim());
                pst.setInt(3, Integer.parseInt(model.getValueAt(i, 5).toString().trim()));
                pst.setDouble(4, Double.parseDouble(model.getValueAt(i, 6).toString().trim()));
                pst.addBatch();
            }
            pst.executeBatch();
        }
    }
    private static java.sql.Timestamp taoThoiGian(String ngayXuat) {
    java.time.LocalDateTime ldt = java.time.LocalDate.parse(ngayXuat)
            .atTime(java.time.LocalTime.now().withNano(0));
    return java.sql.Timestamp.valueOf(ldt);
    }
}