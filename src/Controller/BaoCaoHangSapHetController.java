package Controller;

import Model.BaoCaoHangSapHetDAO;
import View.BaoCaoHangSapHetPanel; // Đổi tên class View theo thực tế của bạn
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class BaoCaoHangSapHetController {
    private BaoCaoHangSapHetPanel view;
    private BaoCaoHangSapHetDAO dao;

    public BaoCaoHangSapHetController(BaoCaoHangSapHetPanel view) {
        this.view = view;
        this.dao = new BaoCaoHangSapHetDAO();
        initController();
        loadData();
    }

    private void initController() {
        loadComboBoxes();

        view.getBtnTimKiem().addActionListener(e -> loadData());
        
        view.getBtnInBaoCao().addActionListener(e -> {
            JOptionPane.showMessageDialog(view, "Chức năng In báo cáo đang phát triển!");
        });
        
        view.getBtnXuatExcel().addActionListener(e -> {
            JOptionPane.showMessageDialog(view, "Chức năng Xuất Excel đang phát triển!");
        });
    }

    private void loadComboBoxes() {
        // Load danh mục
        view.getCbDanhMuc().removeAllItems();
        view.getCbDanhMuc().addItem("Tất cả");
        List<String> listDM = dao.layDanhSachDanhMuc();
        for (String dm : listDM) {
            view.getCbDanhMuc().addItem(dm);
        }

        // Load trạng thái cảnh báo dựa trên giá trị trả về của View SQL
        view.getCbTrangThai().removeAllItems();
        view.getCbTrangThai().addItem("Tất cả");
        view.getCbTrangThai().addItem("Sắp hết");
        view.getCbTrangThai().addItem("Hết hàng");
    }

    private void loadData() {
        String danhMuc = view.getCbDanhMuc().getSelectedItem() != null ? view.getCbDanhMuc().getSelectedItem().toString() : "Tất cả";
        String trangThai = view.getCbTrangThai().getSelectedItem() != null ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
        dao.layDuLieuHangSapHet(danhMuc, trangThai, model);
    }
}