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
    public void layDuLieuHangSapHet(String danhMuc, String trangThaiLoc, DefaultTableModel model) {
        StringBuilder sql = new StringBuilder(
            "SELECT h.MaSanPham, h.TenSanPham, h.Ton, h.ToiThieu, h.CanNhapThem, h.TrangThai " +
            "FROM vw_HangSapHet h " +
            "JOIN SanPham s ON h.MaSanPham = s.MaSanPham " +
            "JOIN DanhMuc d ON s.MaDanhMuc = d.MaDanhMuc " +
            "WHERE 1=1 "
        );

        if (danhMuc != null && !danhMuc.equals("Tất cả")) {
            sql.append(" AND d.TenDanhMuc = ? ");
        }
        if (trangThaiLoc != null && !trangThaiLoc.equals("Tất cả")) {
            sql.append(" AND h.TrangThai = ? ");
        }

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (danhMuc != null && !danhMuc.equals("Tất cả")) {
                pst.setString(paramIndex++, danhMuc);
            }
            if (trangThaiLoc != null && !trangThaiLoc.equals("Tất cả")) {
                pst.setString(paramIndex++, trangThaiLoc); // "Hết hàng" hoặc "Sắp hết"
            }

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}