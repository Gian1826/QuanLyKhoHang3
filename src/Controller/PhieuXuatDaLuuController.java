package Controller;

import Model.PhieuXuatDaLuuDAO;
import View.PhieuXuatDaLuuPanel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class PhieuXuatDaLuuController {

    private PhieuXuatDaLuuPanel view;
    private PhieuXuatDaLuuDAO dao;

    public PhieuXuatDaLuuController(PhieuXuatDaLuuPanel view) {
        this.view = view;
        this.dao = new PhieuXuatDaLuuDAO();
        initController();
        view.getCbKhachHang().removeAllItems();
        view.getCbKhachHang().addItem("Tất cả");
        for (String ten : dao.layDanhSachKhachHang()) {
            view.getCbKhachHang().addItem(ten);
        }

        view.getCbTrangThai().removeAllItems();
        for (String s : new String[]{"Tất cả", "Nháp", "Đã lưu", "Đã hủy"}) {
            view.getCbTrangThai().addItem(s);
        }
        loadData();
    }

    private void initController() {
        view.getBtnTimKiem().addActionListener(e -> loadData());
        view.getBtnHuyPhieu().addActionListener(e -> thucHienHuyPhieu());

        view.getBtnXemChiTiet().addActionListener(e -> {
            int row = view.getTableDanhSach().getSelectedRow();
            if (row >= 0) {
                String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(view, "Xem chi tiết phiếu: " + maPhieu);
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để xem chi tiết!");
            }
        });

        view.getBtnInPhieu().addActionListener(e -> {
            int row = view.getTableDanhSach().getSelectedRow();
            if (row >= 0) {
                String maPhieu = view.getTableDanhSach().getValueAt(row, 0).toString();
                JOptionPane.showMessageDialog(view, "In phiếu: " + maPhieu);
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn phiếu để in!");
            }
        });
    }

    private void loadData() {
        String tuNgay = view.getTxtTuNgay().getText().trim();
        String denNgay = view.getTxtDenNgay().getText().trim();

        Object kh = view.getCbKhachHang().getSelectedItem();
        String khachHang = (kh != null && !"Tất cả".equals(kh.toString())) ? kh.toString() : null;

        Integer trangThai = null;
        int idx = view.getCbTrangThai().getSelectedIndex();
        if (idx > 0) {
            trangThai = idx - 1;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        model.setRowCount(0);
        for (Object[] r : dao.timKiem(tuNgay, denNgay, khachHang, trangThai)) {
            int tt = (Integer) r[5];
            r[5] = tt == 0 ? "Nháp" : tt == 1 ? "Đã lưu" : "Đã hủy";
            model.addRow(r);
        }
    }

    private void thucHienHuyPhieu() {
        int selectedRow = view.getTableDanhSach().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một phiếu xuất để hủy!");
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
            if (dao.huyPhieu(maPhieu)) {
                JOptionPane.showMessageDialog(view, "Đã hủy phiếu thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(view, "Hủy phiếu thất bại!");
            }
        }
    }
}