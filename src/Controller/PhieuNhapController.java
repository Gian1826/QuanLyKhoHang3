package Controller;

import Model.PhieuNhapDAO;
import View.PhieuNhapPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PhieuNhapController {
    private PhieuNhapPanel view;
    private PhieuNhapDAO dao;

    public PhieuNhapController(PhieuNhapPanel view) {
        this.view = view;
        this.dao = new PhieuNhapDAO();
        initController();
    }

    private void initController() {
        // Sự kiện thêm dòng
        view.getBtnThemDong().addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            // Cột: STT, Mã SP, Tên SP, ĐVT, Số lượng, Đơn giá, Thành tiền
            model.addRow(new Object[]{model.getRowCount() + 1, "", "", "", 1, 0.0, 0.0});
        });

        // Sự kiện xóa dòng
        view.getBtnXoaDong().addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            int selectedRow = view.getTableChiTiet().getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
                // Cập nhật lại STT sau khi xóa
                for (int i = 0; i < model.getRowCount(); i++) {
                    model.setValueAt(i + 1, i, 0);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng để xóa!");
            }
        });

        // Sự kiện lưu phiếu
        view.getBtnLuuPhieu().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thucHienLuuPhieu();
            }
        });
    }

    private void thucHienLuuPhieu() {
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        if (maPhieu.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập mã phiếu!");
            return;
        }

        // Lấy dữ liệu thực tế từ các field trên giao diện
        String ngayNhap = view.getTxtNgayNhap().getText().trim();
        String nguoiLap = view.getCbNguoiLap().getSelectedItem().toString();
        String ghiChu = view.getTxtGhiChu().getText().trim();
        
        // Lưu ý: ComboBox đang chứa Tên (VD: "Khu A"), nếu DB cần Mã (VD: "A") thì bạn cần cắt chuỗi hoặc dùng Map để quy đổi.
        String nhaCungCap = view.getCbNhaCungCap().getSelectedItem().toString();
        String khuNhan = view.getCbKhuNhan().getSelectedItem().toString();

        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng thêm chi tiết sản phẩm vào phiếu!");
            return;
        }

        // Truyền dữ liệu xuống DAO để lưu vào CSDL
        boolean isSuccess = dao.luuPhieuNhap(maPhieu, ngayNhap, nguoiLap, nhaCungCap, khuNhan, ghiChu, model);
        
        if (isSuccess) {
            JOptionPane.showMessageDialog(view, "Lưu phiếu nhập thành công!");
            // Reset form sau khi lưu thành công (tùy chọn)
            // model.setRowCount(0); 
        } else {
            JOptionPane.showMessageDialog(view, "Lưu thất bại. Vui lòng kiểm tra lại dữ liệu hoặc kết nối!");
        }
    }
}