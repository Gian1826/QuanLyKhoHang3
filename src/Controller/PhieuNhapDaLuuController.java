package Controller;

import Model.PhieuNhapDaLuuDAO;
import View.PhieuNhapDaLuuPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PhieuNhapDaLuuController {
    private PhieuNhapDaLuuPanel view;
    private PhieuNhapDaLuuDAO dao;

    public PhieuNhapDaLuuController(PhieuNhapDaLuuPanel view) {
        this.view = view;
        this.dao = new PhieuNhapDaLuuDAO();
        initController();
        loadData(); // Tải dữ liệu ngay khi mở màn hình
    }

    private void initController() {
        // Sự kiện nút Tìm kiếm (Sau này bạn có thể bổ sung lấy text từ các bộ lọc)
        view.getBtnTimKiem().addActionListener(e -> loadData());

        // Sự kiện nút Hủy phiếu
        view.getBtnHuyPhieu().addActionListener(e -> thucHienHuyPhieu());

        // Sự kiện nút Xem chi tiết / Sửa
        view.getBtnXemChiTiet().addActionListener(e -> {
            int row = view.getTableDanhSach().getSelectedRow();
            if (row >= 0) {
                String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(view, "Hiển thị chi tiết cho phiếu: " + maPhieu);
                // Tại đây bạn có thể gọi một JDialog mới hiển thị danh sách ChiTietPhieuNhap
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để xem chi tiết!");
            }
        });
    }

    // Tải dữ liệu vào bảng
    private void loadData() {
        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        dao.layDanhSachPhieu(model);
    }

    // Xử lý logic hủy phiếu
    private void thucHienHuyPhieu() {
        int selectedRow = view.getTableDanhSach().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một phiếu nhập để hủy!");
            return;
        }

        // Cột 0 là Mã Phiếu theo thiết kế JTable của bạn
        String maPhieu = view.getTableDanhSach().getValueAt(selectedRow, 0).toString(); 

        int confirm = JOptionPane.showConfirmDialog(view, 
                "Bạn có chắc chắn muốn hủy phiếu " + maPhieu + " không?", 
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.huyPhieuNhap(maPhieu)) {
                JOptionPane.showMessageDialog(view, "Đã hủy phiếu thành công!");
                loadData(); // Cập nhật lại bảng sau khi hủy
            } else {
                JOptionPane.showMessageDialog(view, "Hủy phiếu thất bại!");
            }
        }
    }
}