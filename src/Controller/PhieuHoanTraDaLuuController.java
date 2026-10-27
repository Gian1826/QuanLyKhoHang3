package Controller;

import Model.PhieuHoanTraDaLuuDAO;
import View.PhieuHoanTraDaLuuPanel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PhieuHoanTraDaLuuController {
    private PhieuHoanTraDaLuuPanel view;
    private PhieuHoanTraDaLuuDAO dao;

    public PhieuHoanTraDaLuuController(PhieuHoanTraDaLuuPanel view) {
        this.view = view;
        this.dao = new PhieuHoanTraDaLuuDAO();
        initController();
        loadData();
    }

    private void initController() {
        view.getBtnTimKiem().addActionListener(e -> loadData());
        view.getBtnHuyPhieu().addActionListener(e -> thucHienHuyPhieu());
        
        view.getBtnXemChiTiet().addActionListener(e -> {
             int row = view.getTableDanhSach().getSelectedRow();
             if (row >= 0) {
                 String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                 JOptionPane.showMessageDialog(view, "Xem chi tiết phiếu hoàn trả: " + maPhieu);
             } else {
                 JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để xem chi tiết!");
             }
        });
        
         view.getBtnInPhieu().addActionListener(e -> {
             int row = view.getTableDanhSach().getSelectedRow();
             if (row >= 0) {
                 String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                 JOptionPane.showMessageDialog(view, "In phiếu hoàn trả: " + maPhieu);
             } else {
                 JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để in!");
             }
        });
        
        view.getBtnSua().addActionListener(e -> {
             int row = view.getTableDanhSach().getSelectedRow();
             if (row >= 0) {
                 String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                 JOptionPane.showMessageDialog(view, "Chỉnh sửa phiếu hoàn trả: " + maPhieu);
             } else {
                 JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để sửa!");
             }
        });
    }

    private void loadData() {
        String tuNgay = view.getTxtTuNgay().getText().trim();
        String denNgay = view.getTxtDenNgay().getText().trim();
        String tuKhoa = view.getTxtTuKhoa().getText().trim();
        String trangThai = view.getCbTrangThai().getSelectedItem() != null ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        dao.layDanhSachPhieuHoanTra(tuNgay, denNgay, tuKhoa, trangThai, model);
    }

    private void thucHienHuyPhieu() {
        int selectedRow = view.getTableDanhSach().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một phiếu hoàn trả để hủy!");
            return;
        }

        String maPhieu = view.getTableDanhSach().getValueAt(selectedRow, 0).toString();
        String trangThai = view.getTableDanhSach().getValueAt(selectedRow, 5).toString();
        
        if (trangThai.equals("Đã hủy")) {
             JOptionPane.showMessageDialog(view, "Phiếu này đã bị hủy!");
             return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, 
                "Bạn có chắc chắn muốn hủy phiếu " + maPhieu + " không?", 
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.huyPhieuHoanTra(maPhieu)) {
                JOptionPane.showMessageDialog(view, "Đã hủy phiếu thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(view, "Hủy phiếu thất bại!");
            }
        }
    }
}