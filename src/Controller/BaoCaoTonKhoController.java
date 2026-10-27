package Controller;

import Model.BaoCaoTonKhoDAO;
import View.BaoCaoTonKhoPanel;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class BaoCaoTonKhoController {
    private BaoCaoTonKhoPanel view;
    private BaoCaoTonKhoDAO dao;

    public BaoCaoTonKhoController(BaoCaoTonKhoPanel view) {
        this.view = view;
        this.dao = new BaoCaoTonKhoDAO();
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
        view.getCbDanhMuc().removeAllItems();
        view.getCbDanhMuc().addItem("Tất cả");
        List<String> listDM = dao.layDanhSachDanhMuc();
        for (String dm : listDM) {
            view.getCbDanhMuc().addItem(dm);
        }
        
        view.getCbKhu().removeAllItems();
        view.getCbKhu().addItem("Tất cả");
        List<String> listKhu = dao.layDanhSachKhu();
        for (String khu : listKhu) {
            view.getCbKhu().addItem(khu);
        }
    }

    private void loadData() {
        String danhMuc = view.getCbDanhMuc().getSelectedItem() != null ? view.getCbDanhMuc().getSelectedItem().toString() : "Tất cả";
        String khu = view.getCbKhu().getSelectedItem() != null ? view.getCbKhu().getSelectedItem().toString() : "Tất cả";
        String denNgay = view.getTxtDenNgay().getText().trim();

        DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
        dao.layDuLieuBaoCao(danhMuc, khu, denNgay, model);
    }
}