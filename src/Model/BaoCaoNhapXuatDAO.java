package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BaoCaoNhapXuatDAO {

    public List<String> layDanhSachDanhMuc() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT TenDanhMuc FROM DanhMuc";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("TenDanhMuc"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int layDuLieuBaoCao(java.sql.Timestamp tu, java.sql.Timestamp denCong1,
                           String danhMuc, DefaultTableModel model) throws Exception {
    StringBuilder sql = new StringBuilder(
        "SELECT s.MaSanPham, s.TenSanPham, " +
        "ISNULL((SELECT SUM(c.SoLuong) FROM ChiTietPhieuNhap c JOIN PhieuNhap p ON c.MaPhieuNhap = p.MaPhieuNhap " +
        "        WHERE c.MaSanPham = s.MaSanPham AND p.TrangThai = 1 AND p.NgayNhap >= ? AND p.NgayNhap < ?), 0) AS Nhap, " +
        "ISNULL((SELECT SUM(c.SoLuong) FROM ChiTietPhieuXuat c JOIN PhieuXuat p ON c.MaPhieuXuat = p.MaPhieuXuat " +
        "        WHERE c.MaSanPham = s.MaSanPham AND p.TrangThai = 1 AND p.NgayXuat >= ? AND p.NgayXuat < ?), 0) AS Xuat, " +
        "ISNULL((SELECT SUM(c.SoLuong) FROM ChiTietPhieuNhap c JOIN PhieuNhap p ON c.MaPhieuNhap = p.MaPhieuNhap " +
        "        WHERE c.MaSanPham = s.MaSanPham AND p.TrangThai = 1 AND p.NgayNhap >= ?), 0) AS NhapSau, " +
        "ISNULL((SELECT SUM(c.SoLuong) FROM ChiTietPhieuXuat c JOIN PhieuXuat p ON c.MaPhieuXuat = p.MaPhieuXuat " +
        "        WHERE c.MaSanPham = s.MaSanPham AND p.TrangThai = 1 AND p.NgayXuat >= ?), 0) AS XuatSau, " +
        "ISNULL((SELECT SUM(t.SoLuongTon) FROM TonKho t WHERE t.MaSanPham = s.MaSanPham), 0) AS TonHienTai " +
        "FROM SanPham s JOIN DanhMuc d ON s.MaDanhMuc = d.MaDanhMuc " +
        "WHERE s.TrangThai = 1 ");

    boolean loc = danhMuc != null && !danhMuc.equals("Tất cả");
    if (loc) sql.append(" AND d.TenDanhMuc = ? ");
    sql.append(" ORDER BY s.MaSanPham");

    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql.toString())) {
        int i = 1;
        pst.setTimestamp(i++, tu);       pst.setTimestamp(i++, denCong1);   // nhập trong kỳ
        pst.setTimestamp(i++, tu);       pst.setTimestamp(i++, denCong1);   // xuất trong kỳ
        pst.setTimestamp(i++, denCong1);                                    // nhập sau kỳ
        pst.setTimestamp(i++, denCong1);                                    // xuất sau kỳ
        if (loc) pst.setString(i++, danhMuc);

        try (ResultSet rs = pst.executeQuery()) {
            model.setRowCount(0);
            while (rs.next()) {
                int nhap = rs.getInt("Nhap"), xuat = rs.getInt("Xuat");
                int cuoi = rs.getInt("TonHienTai") - rs.getInt("NhapSau") + rs.getInt("XuatSau");
                int dau = cuoi - nhap + xuat;
                model.addRow(new Object[]{rs.getString("MaSanPham"), rs.getString("TenSanPham"),
                    dau, nhap, xuat, cuoi});
            }
            return model.getRowCount();
        }
    }
}
}