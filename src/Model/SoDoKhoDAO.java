package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SoDoKhoDAO {

    // Lấy danh sách vị trí và tổng số lượng hàng đang tồn ở vị trí đó
    // Trả về List chứa các mảng Object: [MaViTri, MaKhu, SucChua, TongTon]
    public List<Object[]> layDanhSachViTri(String maKhu) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT v.MaViTri, v.MaKhu, v.SucChua, ISNULL(SUM(t.SoLuongTon), 0) AS TongTon " +
                     "FROM ViTri v " +
                     "LEFT JOIN TonKho t ON v.MaViTri = t.MaViTri " +
                     "WHERE (v.MaKhu = ? OR ? = 'Tất cả') " +
                     "GROUP BY v.MaViTri, v.MaKhu, v.SucChua " +
                     "ORDER BY v.MaViTri";

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maKhu);
            pst.setString(2, maKhu);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("MaViTri"),
                        rs.getString("MaKhu"),
                        rs.getInt("SucChua"),
                        rs.getInt("TongTon")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy chi tiết sản phẩm tại một vị trí cụ thể
    public String layChiTietSanPhamTaiViTri(String maViTri) {
        String sql = "SELECT s.TenSanPham, t.SoLuongTon " +
                     "FROM TonKho t JOIN SanPham s ON t.MaSanPham = s.MaSanPham " +
                     "WHERE t.MaViTri = ? AND t.SoLuongTon > 0";
        StringBuilder sb = new StringBuilder();
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maViTri);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    sb.append(rs.getString("TenSanPham"))
                      .append(" (").append(rs.getInt("SoLuongTon")).append(")\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.length() > 0 ? sb.toString().trim() : "Trống";
    }
}