package Controller;

import Model.LichSuLayHangDAO;
import View.LichSuLayHangPanel;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class LichSuLayHangController {
    private LichSuLayHangPanel view;
    private LichSuLayHangDAO dao;

    public LichSuLayHangController(LichSuLayHangPanel view) {
        this.view = view;
        this.dao = new LichSuLayHangDAO();
        initController();
    }

    private void initController() {
        loadComboBoxNguoiLay();
        loadData(); // Tải dữ liệu ban đầu

        // Sự kiện nút Tìm kiếm
        view.getBtnTimKiem().addActionListener(e -> loadData());

        // Sự kiện nút Xem chi tiết
        view.getBtnXemChiTiet().addActionListener(e -> xemChiTietLenh());
    }

    private void loadComboBoxNguoiLay() {
        view.getCbNguoiLay().removeAllItems();
        view.getCbNguoiLay().addItem("Tất cả");
        List<String> dsNguoiLay = dao.layDanhSachNguoiLay();
        for (String ten : dsNguoiLay) {
            view.getCbNguoiLay().addItem(ten);
        }
    }

    private void loadData() {
        String tuNgay = view.getTxtTuNgay().getText().trim();
        String denNgay = view.getTxtDenNgay().getText().trim();
        String nguoiLay = view.getCbNguoiLay().getSelectedItem() != null ? view.getCbNguoiLay().getSelectedItem().toString() : "Tất cả";
        String trangThai = view.getCbTrangThai().getSelectedItem() != null ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";
        
        DefaultTableModel model = (DefaultTableModel) view.getTableLichSu().getModel();
        dao.timKiemLichSu(tuNgay, denNgay, nguoiLay, trangThai, model);
    }

    private void xemChiTietLenh() {
        int selectedRow = view.getTableLichSu().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng để xem chi tiết!");
            return;
        }
        
        String maLenh = view.getTableLichSu().getValueAt(selectedRow, 0).toString();
        JOptionPane.showMessageDialog(view, "Chức năng hiển thị chi tiết cho lệnh: " + maLenh + " (Mở JDialog mới tại đây)");
    }
}