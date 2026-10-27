package Controller;

import Model.DanhSachLayHangDAO;
import View.DanhSachLayHangPanel;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class DanhSachLayHangController {
    private DanhSachLayHangPanel view;
    private DanhSachLayHangDAO dao;

    public DanhSachLayHangController(DanhSachLayHangPanel view) {
        this.view = view;
        this.dao = new DanhSachLayHangDAO();
        initController();
        loadData(); // Tải toàn bộ dữ liệu ban đầu
    }

    private void initController() {
        // Thiết lập sự kiện nút Tìm kiếm
        view.getBtnTimKiem().addActionListener(e -> loadData());

        // Sự kiện nút Bắt đầu lấy (Chuyển trạng thái sang Đang lấy)
        view.getBtnBatDauLay().addActionListener(e -> thucHienBatDauLay());

        // Sự kiện nút Xác nhận đã lấy (Chuyển trạng thái sang Hoàn thành)
        view.getBtnXacNhanDaLay().addActionListener(e -> thucHienXacNhanLay());
    }

    private void loadData() {
        String ngay = view.getTxtNgay().getText().trim();
        String khu = view.getCbKhu().getSelectedItem() != null ? view.getCbKhu().getSelectedItem().toString() : "Tất cả";
        String trangThai = view.getCbTrangThai().getSelectedItem() != null ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";
        
        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        dao.timKiemDanhSachLayHang(ngay, khu, trangThai, model);
    }

    private void thucHienBatDauLay() {
        int selectedRow = view.getTableDanhSach().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng lệnh để bắt đầu!");
            return;
        }

        String maLenh = view.getTableDanhSach().getValueAt(selectedRow, 0).toString();
        String trangThaiHienTai = view.getTableDanhSach().getValueAt(selectedRow, 5).toString();

        if (!trangThaiHienTai.equals("Chờ lấy")) {
            JOptionPane.showMessageDialog(view, "Chỉ có thể bắt đầu các lệnh đang ở trạng thái 'Chờ lấy'!");
            return;
        }

        int maNguoiLay = 1; // Giả sử ID tài khoản đăng nhập là 1 (Cập nhật theo class PhienDangNhap của bạn)
        if (dao.capNhatTrangThaiLenh(maLenh, 1, maNguoiLay)) {
            JOptionPane.showMessageDialog(view, "Đã bắt đầu lấy hàng cho lệnh: " + maLenh);
            loadData();
        } else {
            JOptionPane.showMessageDialog(view, "Có lỗi xảy ra khi cập nhật!");
        }
    }

    private void thucHienXacNhanLay() {
        int selectedRow = view.getTableDanhSach().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng lệnh để xác nhận!");
            return;
        }

        String maLenh = view.getTableDanhSach().getValueAt(selectedRow, 0).toString();
        String trangThaiHienTai = view.getTableDanhSach().getValueAt(selectedRow, 5).toString();

        if (!trangThaiHienTai.equals("Đang lấy")) {
            JOptionPane.showMessageDialog(view, "Chỉ có thể xác nhận các lệnh đang ở trạng thái 'Đang lấy'!");
            return;
        }

        int maNguoiLay = 1; // Tương tự như trên
        int confirm = JOptionPane.showConfirmDialog(view, "Xác nhận đã lấy đủ hàng cho lệnh " + maLenh + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.capNhatTrangThaiLenh(maLenh, 2, maNguoiLay)) {
                JOptionPane.showMessageDialog(view, "Đã hoàn thành lệnh: " + maLenh);
                loadData();
            } else {
                JOptionPane.showMessageDialog(view, "Có lỗi xảy ra khi cập nhật!");
            }
        }
    }
}