package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class PhieuKiemKeDAO {

    // Lấy danh sách các Khu để đưa vào JComboBox
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

    // Đổ danh sách hàng hóa đang tồn tại trong Khu được chọn lên bảng
    public void layDanhSachHangTrongKhu(String maKhu, DefaultTableModel model) {
        String sql = "SELECT t.MaSanPham, s.TenSanPham, t.MaViTri, t.SoLuongTon " +
                     "FROM TonKho t " +
                     "JOIN SanPham s ON t.MaSanPham = s.MaSanPham " +
                     "JOIN ViTri v ON t.MaViTri = v.MaViTri " +
                     "WHERE v.MaKhu = ?";
        try (Connection conn = KetNoiDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maKhu);
            try (ResultSet rs = pst.executeQuery()) {
                model.setRowCount(0);
                int stt = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        stt++,
                        rs.getString("MaSanPham"),
                        rs.getString("TenSanPham"),
                        rs.getString("MaViTri"),
                        rs.getInt("SoLuongTon"), // SL hệ thống
                        "", // SL thực tế (Để trống cho user nhập)
                        0   // Chênh lệch ban đầu = 0
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lưu Phiếu kiểm kê và cập nhật tồn kho nếu là Hoàn tất (Transaction)
    public boolean luuPhieuKiemKe(String maPhieu, String ngayKiemKe, String maKhu, int maTaiKhoan, 
                                  String ghiChu, int trangThai, DefaultTableModel model) {
        
        String sqlPhieu = "INSERT INTO PhieuKiemKe (MaPhieuKiemKe, NgayKiemKe, MaKhu, MaTaiKhoan, GhiChu, TrangThai) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlChiTiet = "INSERT INTO ChiTietKiemKe (MaPhieuKiemKe, MaSanPham, MaViTri, SoLuongHeThong, SoLuongThucTe) " +
                            "VALUES (?, ?, ?, ?, ?)";
        String sqlUpdTonKho = "UPDATE TonKho SET SoLuongTon = ? WHERE MaSanPham = ? AND MaViTri = ?";

        try (Connection conn = KetNoiDB.getConnection()) {
            conn.setAutoCommit(false); 

            // 1. Lưu thông tin PhieuKiemKe
            try (PreparedStatement pstPhieu = conn.prepareStatement(sqlPhieu)) {
                pstPhieu.setString(1, maPhieu);
                pstPhieu.setString(2, ngayKiemKe); // Định dạng: yyyy-MM-dd
                pstPhieu.setString(3, maKhu);
                pstPhieu.setInt(4, maTaiKhoan);
                pstPhieu.setString(5, ghiChu);
                pstPhieu.setInt(6, trangThai);
                pstPhieu.executeUpdate();
            }

            // 2. Lưu ChiTietKiemKe và Cập nhật Tồn kho (nếu hoàn tất)
            try (PreparedStatement pstChiTiet = conn.prepareStatement(sqlChiTiet);
                 PreparedStatement pstUpdTK = conn.prepareStatement(sqlUpdTonKho)) {
                 
                for (int i = 0; i < model.getRowCount(); i++) {
                    String maSP = model.getValueAt(i, 1).toString();
                    String viTri = model.getValueAt(i, 3).toString();
                    int slHeThong = Integer.parseInt(model.getValueAt(i, 4).toString());
                    
                    // Nếu user chưa nhập SL Thực tế, mặc định coi như bằng SL Hệ thống
                    int slThucTe = slHeThong; 
                    Object thucTeObj = model.getValueAt(i, 5);
                    if (thucTeObj != null && !thucTeObj.toString().trim().isEmpty()) {
                        slThucTe = Integer.parseInt(thucTeObj.toString().trim());
                    }

                    // Lưu chi tiết
                    pstChiTiet.setString(1, maPhieu);
                    pstChiTiet.setString(2, maSP);
                    pstChiTiet.setString(3, viTri);
                    pstChiTiet.setInt(4, slHeThong);
                    pstChiTiet.setInt(5, slThucTe);
                    pstChiTiet.addBatch();

                    // Nếu trạng thái = 1 (Hoàn tất kiểm kê), cập nhật lại bảng TonKho theo số lượng thực tế
                    if (trangThai == 1) {
                        pstUpdTK.setInt(1, slThucTe);
                        pstUpdTK.setString(2, maSP);
                        pstUpdTK.setString(3, viTri);
                        pstUpdTK.addBatch();
                    }
                }
                
                pstChiTiet.executeBatch();
                if (trangThai == 1) {
                    pstUpdTK.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}