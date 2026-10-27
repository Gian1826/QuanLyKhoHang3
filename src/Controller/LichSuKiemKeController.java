package Controller;

import Model.LichSuKiemKeDAO;
import View.LichSuKiemKePanel;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class LichSuKiemKeController {
    private LichSuKiemKePanel view;
    private LichSuKiemKeDAO dao;

    public LichSuKiemKeController(LichSuKiemKePanel view) {
        this.view = view;
        this.dao = new LichSuKiemKeDAO();
        initController();
        loadData();
    }

    private void initController() {
        loadComboBoxKhu();

        view.getBtnTimKiem().addActionListener(e -> loadData());
        
        view.getBtnXemChiTiet().addActionListener(e -> {
             int row = view.getTableDanhSach().getSelectedRow();
             if (row >= 0) {
                 String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                 JOptionPane.showMessageDialog(view, "Xem chi tiết phiếu kiểm kê: " + maPhieu);
             } else {
                 JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để xem chi tiết!");
             }
        });
        
         view.getBtnInPhieu().addActionListener(e -> {
             int row = view.getTableDanhSach().getSelectedRow();
             if (row >= 0) {
                 String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                 JOptionPane.showMessageDialog(view, "In phiếu kiểm kê: " + maPhieu);
             } else {
                 JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để in!");
             }
        });
    }

    private void loadComboBoxKhu() {
        view.getCbKhu().removeAllItems();
        view.getCbKhu().addItem("Tất cả");
        List<String> listKhu = dao.layDanhSachKhu();
        for (String khu : listKhu) {
            view.getCbKhu().addItem(khu);
        }
    }

    private void loadData() {
        String tuNgay = view.getTxtTuNgay().getText().trim();
        String denNgay = view.getTxtDenNgay().getText().trim();
        String khu = view.getCbKhu().getSelectedItem() != null ? view.getCbKhu().getSelectedItem().toString() : "Tất cả";
        String trangThai = view.getCbTrangThai().getSelectedItem() != null ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        dao.layDanhSachPhieuKiemKe(tuNgay, denNgay, khu, trangThai, model);
    }
}