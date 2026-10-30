package Controller;

import Model.LichSuLayHangDAO;
import View.LichSuLayHangPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LichSuLayHangController {
    private LichSuLayHangPanel view;
    private LichSuLayHangDAO dao;

    public LichSuLayHangController(LichSuLayHangPanel view) {
        this.view = view;
        this.dao = new LichSuLayHangDAO();
        initController();
        view.getTableLichSu().setDefaultEditor(Object.class, null);
        view.getTableLichSu().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void initController() {
        loadComboBoxNguoiLay();
        loadData(); // Tải dữ liệu ban đầu

        view.getBtnTimKiem().addActionListener(e -> loadData());
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
        JTable tbl = view.getTableLichSu();
        int selectedRow = tbl.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng để xem chi tiết!");
            return;
        }

        String maLenh    = tbl.getValueAt(selectedRow, 0).toString();
        String thoiGian  = tbl.getValueAt(selectedRow, 1).toString();
        String nguoiLay  = tbl.getValueAt(selectedRow, 2).toString();
        String trangThai = tbl.getValueAt(selectedRow, 4).toString();

        // --- Tạo dialog ---
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(view),
                "Chi tiết lệnh " + maLenh, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));

        // Thông tin chung
        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 5, 8));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Thông tin chung"));
        pnlInfo.add(new JLabel("Mã lệnh:"));    pnlInfo.add(new JLabel(maLenh));
        pnlInfo.add(new JLabel("Thời gian:"));  pnlInfo.add(new JLabel(thoiGian));
        pnlInfo.add(new JLabel("Người lấy:"));  pnlInfo.add(new JLabel(nguoiLay));
        pnlInfo.add(new JLabel("Trạng thái:")); pnlInfo.add(new JLabel(trangThai));

        // Bảng mặt hàng
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Mã SP", "Tên sản phẩm", "Vị trí", "Cần lấy", "Đã lấy"}, 0);
        JTable tblChiTiet = new JTable(model);
        tblChiTiet.setDefaultEditor(Object.class, null);
        tblChiTiet.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(tblChiTiet);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách mặt hàng"));

        dao.layChiTietLenh(maLenh, model);   // truy vấn CSDL nằm ở DAO

        // Nút đóng
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dialog.dispose());
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.add(btnDong);

        dialog.add(pnlInfo, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        ((JComponent) dialog.getContentPane())
                .setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dialog.setSize(650, 420);
        dialog.setLocationRelativeTo(view);
        dialog.setVisible(true);
    }
}