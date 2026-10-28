package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PhieuNhapDAO {

    /**
     * Đặt true nếu cột ThanhTien trong ChiTietPhieuNhap là cột tính toán (computed).
     * Khi đó DAO sẽ không INSERT cột này.
     */
    private static final boolean THANH_TIEN_LA_COT_TINH = true;

    /** Giá trị cột PhieuNhap.TrangThai (tinyint) cho phiếu vừa lưu. Xem PhieuNhapDaLuuDAO. */
    private static final int TRANG_THAI_DA_LUU = 1;

    // ===== Các lớp dữ liệu nhỏ =====
    public static class DongNhap {
        public String maSP;
        public int soLuong;
        public double donGia;

        public DongNhap(String maSP, int soLuong, double donGia) {
            this.maSP = maSP;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }
    }

    public static class SanPhamInfo {
        public String ten;
        public String donViTinh;
        public double giaNhap;
    }

    // ===== Tra cứu sản phẩm theo mã =====
    public static SanPhamInfo laySanPham(String maSP) {
        String sql = "SELECT TenSanPham, DonViTinh, GiaNhap FROM SanPham WHERE MaSanPham = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SanPhamInfo sp = new SanPhamInfo();
                    sp.ten = rs.getString("TenSanPham");
                    sp.donViTinh = rs.getString("DonViTinh");
                    sp.giaNhap = rs.getDouble("GiaNhap");
                    return sp;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== Sinh mã phiếu kế tiếp: PN-0001, PN-0002... =====
    public static String taoMaPhieuMoi() {
        try (Connection conn = KetNoiDB.getConnection()) {
            return taoMaPhieu(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return "PN-0001";
        }
    }

    private static String taoMaPhieu(Connection conn) throws SQLException {
        String sql = "SELECT MAX(MaPhieuNhap) FROM PhieuNhap WHERE MaPhieuNhap LIKE 'PN-%'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int so = 0;
            if (rs.next() && rs.getString(1) != null) {
                try {
                    so = Integer.parseInt(rs.getString(1).substring(3).trim());
                } catch (NumberFormatException ex) {
                    so = 0;
                }
            }
            return String.format("PN-%04d", so + 1);
        }
    }

    // ===== Lấy MaTaiKhoan (số) từ tên đăng nhập =====
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

    /**
     * Lưu phiếu nhập + chi tiết trong 1 transaction.
     * @return mã phiếu đã lưu
     * @throws Exception nếu có lỗi (đã rollback), controller sẽ hiện thông báo
     */
    public static String luuPhieuNhap(java.sql.Date ngayNhap, String tenDangNhap, String maKhu,
                                      String ghiChu, List<DongNhap> dsDong) throws Exception {
        if (dsDong == null || dsDong.isEmpty()) {
            throw new IllegalArgumentException("Phiếu chưa có dòng hàng nào!");
        }

        Connection conn = null;
        try {
            conn = KetNoiDB.getConnection();
            conn.setAutoCommit(false);

            String maPhieu = taoMaPhieu(conn);
            int maTaiKhoan = layMaTaiKhoan(conn, tenDangNhap);

            // 1. Bảng PhieuNhap
            double tongTien = 0;
            for (DongNhap d : dsDong) {
                tongTien += d.soLuong * d.donGia;
            }
            String sqlPhieu = "INSERT INTO PhieuNhap (MaPhieuNhap, NgayNhap, MaTaiKhoan, MaKhu, TongTien, GhiChu, TrangThai) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPhieu)) {
                ps.setString(1, maPhieu);
                ps.setDate(2, ngayNhap);
                ps.setInt(3, maTaiKhoan);
                ps.setString(4, maKhu);
                ps.setDouble(5, tongTien);
                ps.setString(6, ghiChu);
                ps.setInt(7, TRANG_THAI_DA_LUU);
                ps.executeUpdate();
            }

            // 2. Bảng ChiTietPhieuNhap
            String sqlCT = THANH_TIEN_LA_COT_TINH
                    ? "INSERT INTO ChiTietPhieuNhap (MaPhieuNhap, MaSanPham, SoLuong, DonGia, SoLuongDaCat) VALUES (?, ?, ?, ?, 0)"
                    : "INSERT INTO ChiTietPhieuNhap (MaPhieuNhap, MaSanPham, SoLuong, DonGia, ThanhTien, SoLuongDaCat) VALUES (?, ?, ?, ?, ?, 0)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                for (DongNhap d : dsDong) {
                    ps.setString(1, maPhieu);
                    ps.setString(2, d.maSP);
                    ps.setInt(3, d.soLuong);
                    ps.setDouble(4, d.donGia);
                    if (!THANH_TIEN_LA_COT_TINH) {
                        ps.setDouble(5, d.soLuong * d.donGia);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return maPhieu;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}