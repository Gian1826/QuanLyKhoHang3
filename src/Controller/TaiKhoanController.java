package Controller;

import Model.TaiKhoanDAO;
import View.ThongTinTaiKhoanPanel;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaiKhoanController {
    
    private ThongTinTaiKhoanPanel view;

    public TaiKhoanController(ThongTinTaiKhoanPanel view) {
        this.view = view;
        initController();
    }

    private void initController() {
        view.getLuuThongTin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLyLuuThongTin();
            }
        });
        view.getDoiMatKhau().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLyDoiMatKhau();
            }
        });
    }

    // ==========================================
    // XỬ LÝ: LƯU THÔNG TIN CÁ NHÂN
    // ==========================================
    private void xuLyLuuThongTin() {
        try {
            String tenDangNhap = view.getTxtTenDangNhap().getText().trim();
            String hoTen       = view.getTxtHoTen().getText().trim();
            String email       = view.getTxtEmail().getText().trim();
            String soDienThoai = view.getSoDienThoai().getText().trim();

            if (hoTen.isEmpty() || email.isEmpty() || soDienThoai.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin cá nhân!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean thanhCong = TaiKhoanDAO.capNhatThongTin(tenDangNhap, hoTen, email, soDienThoai);

            if (thanhCong) {
                JOptionPane.showMessageDialog(view, "Cập nhật thông tin thành công!");
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật thông tin thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // XỬ LÝ: ĐỔI MẬT KHẨU
    // ==========================================
    private void xuLyDoiMatKhau() {
        try {
            String matKhauCu    = new String(view.getMatKhauHientai().getPassword());
            String matKhauMoi   = new String(view.getMatKhauMoi().getPassword());
            String xacNhanMoi   = new String(view.getXacNhanMatKhauMoi().getPassword());
            String tenDangNhap  = view.getTxtTenDangNhap().getText().trim();
            if (matKhauCu.isEmpty() || matKhauMoi.isEmpty() || xacNhanMoi.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng nhập đầy đủ thông tin mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!matKhauMoi.equals(xacNhanMoi)) {
                JOptionPane.showMessageDialog(view, "Mật khẩu mới và xác nhận mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean thanhCong = TaiKhoanDAO.doiMatKhau(tenDangNhap, matKhauCu, matKhauMoi);
            if (thanhCong) {
                JOptionPane.showMessageDialog(view, "Đổi mật khẩu thành công!");
                view.getMatKhauHientai().setText("");
                view.getMatKhauMoi().setText("");
                view.getXacNhanMatKhauMoi().setText("");
            } else {
                JOptionPane.showMessageDialog(view, "Đổi mật khẩu thất bại! Kiểm tra lại mật khẩu hiện tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}