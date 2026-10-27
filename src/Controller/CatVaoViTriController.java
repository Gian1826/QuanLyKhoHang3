package Controller;

import Model.CatVaoViTriDAO;
import View.CatVaoViTriPanel; // Tên class View thực tế của bạn
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class CatVaoViTriController {
    private CatVaoViTriPanel view;
    private CatVaoViTriDAO dao;

    public CatVaoViTriController(CatVaoViTriPanel view) {
        this.view = view;
        this.dao = new CatVaoViTriDAO();
        initController();
    }

    private void initController() {
        loadComboBoxPhieuNhap();

        // Bắt sự kiện chọn mã phiếu nhập -> Đổ dữ liệu chi tiết ra bảng
        view.getCbPhieuNhap().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String maPhieu = e.getItem().toString();
                if (!maPhieu.contains("Chọn phiếu nhập")) {
                    DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
                    dao.layChiTietPhieu(maPhieu, model);
                } else {
                    ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
                }
            }
        });

        // Bắt sự kiện Xác nhận chuyển hàng
        view.getBtnXacNhan().addActionListener(e -> thucHienCatHang());
        
        // Bắt sự kiện Hủy
        view.getBtnHuy().addActionListener(e -> {
            view.getCbPhieuNhap().setSelectedIndex(0);
            ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
        });
    }

    private void loadComboBoxPhieuNhap() {
        view.getCbPhieuNhap().removeAllItems();
        view.getCbPhieuNhap().addItem("— Chọn phiếu nhập —");
        List<String> dsPhieu = dao.layDanhSachPhieuChoCat();
        for (String phieu : dsPhieu) {
            view.getCbPhieuNhap().addItem(phieu);
        }
    }

    private void thucHienCatHang() {
        if (view.getCbPhieuNhap().getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu nhập!");
            return;
        }

        String maPhieu = view.getCbPhieuNhap().getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Phiếu này không có dữ liệu cất hàng!");
            return;
        }

        // Validate dữ liệu từ bảng (kiểm tra Số lượng và Vị trí nhập tay)
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                int slCanCat = Integer.parseInt(model.getValueAt(i, 3).toString());
                int slDaCat = Integer.parseInt(model.getValueAt(i, 5).toString());
                String viTri = model.getValueAt(i, 4).toString().trim();
                
                if (slDaCat > slCanCat || slDaCat < 0) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng cất (" + slDaCat + ") không hợp lệ!");
                    return;
                }
                if (slDaCat > 0 && viTri.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Vui lòng nhập vị trí cất hàng!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng phải là số nguyên!");
                return;
            }
        }

        // Thực thi cập nhật SQL
        if (dao.xacNhanCatHang(maPhieu, model)) {
            JOptionPane.showMessageDialog(view, "Xác nhận chuyển hàng thành công!");
            dao.layChiTietPhieu(maPhieu, model); 
            // Nếu đã cất hết hàng, load lại combobox để loại bỏ mã phiếu đó
            if (model.getRowCount() == 0) {
                loadComboBoxPhieuNhap();
            }
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu dữ liệu. Vui lòng thử lại!");
        }
    }
}