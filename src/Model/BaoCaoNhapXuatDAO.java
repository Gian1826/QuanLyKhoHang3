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

    public void layDuLieuBaoCao(String tuNgay, String denNgay, String danhMuc, DefaultTableModel model) {
        // Lưu ý: Logic tính toán tồn đầu kỳ, nhập, xuất, tồn cuối kỳ thực tế phức tạp hơn.
        // Đây là một truy vấn ví dụ để hiển thị dữ liệu lên bảng.
        StringBuilder sql = new StringBuilder(
            "SELECT s.MaSanPham, s.TenSanPham, " +
            "0 AS TonDauKy, " + // Cần logic SQL phức tạp hơn để tính chính xác
            "ISNULL((SELECT SUM(SoLuong) FROM ChiTietPhieuNhap cpn JOIN PhieuNhap pn ON cpn.MaPhieuNhap = pn.MaPhieuNhap WHERE cpn.MaSanPham = s.MaSanPham AND pn.TrangThai = 1 AND pn.NgayNhap BETWEEN ? AND ?), 0) AS Nhap, " +
            "ISNULL((SELECT SUM(SoLuong) FROM ChiTietPhieuXuat cpx JOIN PhieuXuat px ON cpx.MaPhieuXuat = px.MaPhieuXuat WHERE cpx.MaSanPham = s.MaSanPham AND px.TrangThai = 1 AND px.NgayXuat BETWEEN ? AND ?), 0) AS Xuat, " +
            "ISNULL(SUM(t.SoLuongTon), 0) AS TonCuoiKy " + // Cần logic SQL phức tạp hơn để tính chính xác
            "FROM SanPham s " +
            "JOIN DanhMuc d ON s.MaDanhMuc = d.MaDanhMuc " +
            "LEFT JOIN TonKho t ON s.MaSanPham = t.MaSanPham " +
            "WHERE s.TrangThai = 1 "
        );

        if (danhMuc != null && !danhMuc.equals("Tất cả")) {
            sql.append(" AND d.TenDanhMuc = ? ");
        }

        sql.append(" GROUP BY s.MaSanPham, s.TenSanPham ");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            // set tham số cho subquery Nhập
            pst.setString(paramIndex++, tuNgay);
            pst.setString(paramIndex++, denNgay);
            // set tham số cho subquery Xuất
            pst.setString(paramIndex++, tuNgay);
            pst.setString(paramIndex++, denNgay);
            
            if (danhMuc != null && !danhMuc.equals("Tất cả")) pst.setString(paramIndex++, danhMuc);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getInt("TonDauKy"),
                        rs.getInt("Nhap"),
                        rs.getInt("Xuat"),
                        rs.getInt("TonCuoiKy")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}