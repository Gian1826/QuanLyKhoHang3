package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BaoCaoHangSapHetDAO {

    // Lấy danh mục sản phẩm cho combobox bộ lọc
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

    // Truy vấn dữ liệu từ View vw_HangSapHet kết hợp bảng SanPham và DanhMuc
    public int layDuLieuHangSapHet(String danhMuc, String trangThaiLoc, DefaultTableModel model) throws Exception {
    StringBuilder sql = new StringBuilder(
        "SELECT h.MaSanPham, h.TenSanPham, h.Ton, h.ToiThieu, h.CanNhapThem, h.TrangThai " +
        "FROM vw_HangSapHet h " +
        "JOIN SanPham s ON h.MaSanPham = s.MaSanPham " +
        "JOIN DanhMuc d ON s.MaDanhMuc = d.MaDanhMuc " +
        "WHERE 1=1 ");

    boolean locDM = danhMuc != null && !danhMuc.equals("Tất cả");
    boolean locTT = trangThaiLoc != null && !trangThaiLoc.equals("Tất cả");
    if (locDM) sql.append(" AND d.TenDanhMuc = ? ");
    if (locTT) sql.append(" AND h.TrangThai = ? ");
    sql.append(" ORDER BY h.Ton");

    try (Connection conn = KetNoiDB.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql.toString())) {
        int i = 1;
        if (locDM) pst.setString(i++, danhMuc);
        if (locTT) pst.setString(i++, trangThaiLoc);

        try (ResultSet rs = pst.executeQuery()) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaSanPham"),
                    rs.getString("TenSanPham"),
                    rs.getInt("Ton"),
                    rs.getInt("ToiThieu"),
                    rs.getInt("CanNhapThem"),
                    rs.getString("TrangThai")
                });
            }
            return model.getRowCount();
        }
    }
}
}