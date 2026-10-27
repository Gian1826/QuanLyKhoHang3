package Model;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PhieuNhapDAO {
    
    // Hàm xử lý logic lưu Database
    public boolean luuPhieuNhap(String maPhieu, String ngayNhap, String nguoiLap, 
                                String nhaCungCap, String khuNhan, String ghiChu, DefaultTableModel model) {
        Connection conn = null;
        try {
            conn = KetNoiDB.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Lưu thông tin chung vào bảng PhieuNhap
            String sqlPhieu = "INSERT INTO PhieuNhap (MaPhieuNhap, NgayNhap, MaTaiKhoan, GhiChu) VALUES (?, ?, ?, ?)";
            PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu);
            psPhieu.setString(1, maPhieu);
            // Ép kiểu chuỗi ngày nhập từ giao diện (dd/MM/yyyy) sang java.sql.Date cho Database
            try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            java.util.Date parsedDate = sdf.parse(ngayNhap);
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
    
            // Dùng setDate thay vì setString
            psPhieu.setDate(2, sqlDate); 
            } catch (java.text.ParseException ex) {
            ex.printStackTrace();
            System.out.println("Lỗi: Ngày nhập không đúng định dạng dd/MM/yyyy");
            return false; // Ngừng lưu nếu ngày sai định dạng
        }
            psPhieu.setString(3, nguoiLap);
            psPhieu.setString(4, ghiChu);
            psPhieu.executeUpdate();

            // 2. Lưu từng dòng chi tiết vào bảng ChiTietPhieuNhap
            String sqlChiTiet = "INSERT INTO ChiTietPhieuNhap (MaPhieuNhap, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
            PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet);
            
            for (int i = 0; i < model.getRowCount(); i++) {
                String maSP = model.getValueAt(i, 1).toString();
                int soLuong = Integer.parseInt(model.getValueAt(i, 4).toString());
                double donGia = Double.parseDouble(model.getValueAt(i, 5).toString());
                
                psChiTiet.setString(1, maPhieu);
                psChiTiet.setString(2, maSP);
                psChiTiet.setInt(3, soLuong);
                psChiTiet.setDouble(4, donGia);
                psChiTiet.addBatch();
            }
            psChiTiet.executeBatch();
            
            conn.commit(); // Xác nhận transaction
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // Hoàn tác nếu có lỗi
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            // Đóng kết nối
        } 
    }
}