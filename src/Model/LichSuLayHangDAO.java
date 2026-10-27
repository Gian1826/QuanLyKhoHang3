package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class LichSuLayHangDAO {

    // Lấy danh sách nhân viên đã từng lấy hàng để đổ vào JComboBox
    public List<String> layDanhSachNguoiLay() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT t.TenDangNhap " +
                     "FROM LenhLayHang l JOIN TaiKhoan t ON l.MaNguoiLay = t.MaTaiKhoan";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("TenDangNhap"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Truy vấn dữ liệu lịch sử dựa trên bộ lọc
    public void timKiemLichSu(String tuNgay, String denNgay, String nguoiLay, String trangThai, DefaultTableModel model) {
        StringBuilder sql = new StringBuilder(
            "SELECT l.MaLenh, l.NgayTao, t.TenDangNhap, " +
            "(SELECT COUNT(MaSanPham) FROM ChiTietLayHang WHERE MaLenh = l.MaLenh) AS SoLuongMatHang, " +
            "l.TrangThai " +
            "FROM LenhLayHang l " +
            "LEFT JOIN TaiKhoan t ON l.MaNguoiLay = t.MaTaiKhoan " +
            "WHERE 1=1 "
        );

        if (tuNgay != null && !tuNgay.isEmpty()) {
            sql.append(" AND CAST(l.NgayTao AS DATE) >= ? ");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            sql.append(" AND CAST(l.NgayTao AS DATE) <= ? ");
        }
        if (nguoiLay != null && !nguoiLay.equals("Tất cả")) {
            sql.append(" AND t.TenDangNhap = ? ");
        }
        
        Integer ttId = null;
        if (trangThai.equals("Chờ lấy")) ttId = 0;
        else if (trangThai.equals("Đang lấy")) ttId = 1;
        else if (trangThai.equals("Hoàn thành")) ttId = 2;
        else if (trangThai.equals("Đã hủy")) ttId = 3;

        if (ttId != null) {
            sql.append(" AND l.TrangThai = ? ");
        }

        sql.append(" ORDER BY l.NgayTao DESC");

        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (tuNgay != null && !tuNgay.isEmpty()) pst.setString(paramIndex++, tuNgay);
            if (denNgay != null && !denNgay.isEmpty()) pst.setString(paramIndex++, denNgay);
            if (nguoiLay != null && !nguoiLay.equals("Tất cả")) pst.setString(paramIndex++, nguoiLay);
            if (ttId != null) pst.setInt(paramIndex++, ttId);

            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0); 
                while (rs.next()) {
                    String tenTrangThai = "";
                    switch (rs.getInt("TrangThai")) {
                        case 0: tenTrangThai = "Chờ lấy"; break;
                        case 1: tenTrangThai = "Đang lấy"; break;
                        case 2: tenTrangThai = "Hoàn thành"; break;
                        case 3: tenTrangThai = "Đã hủy"; break;
                    }

                    model.addRow(new Object[]{
                        rs.getString("MaLenh"),
                        rs.getTimestamp("NgayTao"), // Hoặc ThoiGianHoanThanh
                        rs.getString("TenDangNhap") != null ? rs.getString("TenDangNhap") : "Chưa phân công",
                        rs.getInt("SoLuongMatHang"),
                        tenTrangThai
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}