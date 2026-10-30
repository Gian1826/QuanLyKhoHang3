package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BaoCaoTonKhoDAO {

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
    
    public List<String> layDanhSachKhu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaKhu FROM Khu";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("MaKhu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int layDuLieuBaoCao(String danhMuc, String khu, String denNgay, DefaultTableModel model) throws Exception {
    StringBuilder sql = new StringBuilder(
        "SELECT s.MaSanPham, s.TenSanPham, d.TenDanhMuc, s.DonViTinh, " +
        "COALESCE(SUM(t.SoLuongTon), 0) AS Ton, s.GiaNhap, " +
        "COALESCE(SUM(t.SoLuongTon), 0) * s.GiaNhap AS GiaTriTon " +
        "FROM SanPham s " +
        "JOIN DanhMuc d ON s.MaDanhMuc = d.MaDanhMuc " +
        "LEFT JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
        "LEFT JOIN ViTri v ON t.MaViTri = v.MaViTri " +
        "WHERE 1=1 "
    );

    if (danhMuc != null && !danhMuc.equals("Tất cả")) {
        sql.append(" AND d.TenDanhMuc = ? ");
    }
    if (khu != null && !khu.equals("Tất cả")) {
        sql.append(" AND v.MaKhu = ? ");
    }
    sql.append(" GROUP BY s.MaSanPham, s.TenSanPham, d.TenDanhMuc, s.DonViTinh, s.GiaNhap ");
    sql.append(" ORDER BY s.MaSanPham");

    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql.toString())) {

        int paramIndex = 1;
        if (danhMuc != null && !danhMuc.equals("Tất cả")) pst.setString(paramIndex++, danhMuc);
        if (khu != null && !khu.equals("Tất cả")) pst.setString(paramIndex++, khu);

        try (ResultSet rs = pst.executeQuery()) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaSanPham"),
                    rs.getString("TenSanPham"),
                    rs.getString("TenDanhMuc"),
                    rs.getString("DonViTinh"),
                    rs.getInt("Ton"),
                    String.format("%,.0f", rs.getDouble("GiaNhap")),
                    String.format("%,.0f", rs.getDouble("GiaTriTon"))
                });
            }
            return model.getRowCount();
        }
    }
}
}