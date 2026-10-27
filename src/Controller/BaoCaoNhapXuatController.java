package Controller;

import Model.BaoCaoNhapXuatDAO;
import View.BaoCaoNhapXuatPanel;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class BaoCaoNhapXuatController {
    private BaoCaoNhapXuatPanel view;
    private BaoCaoNhapXuatDAO dao;

    public BaoCaoNhapXuatController(BaoCaoNhapXuatPanel view) {
        this.view = view;
        this.dao = new BaoCaoNhapXuatDAO();
        initController();
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
    }

    private void loadData() {
        String tuNgay = view.getTxtTuNgay().getText().trim();
        String denNgay = view.getTxtDenNgay().getText().trim();
        String danhMuc = view.getCbDanhMuc().getSelectedItem() != null ? view.getCbDanhMuc().getSelectedItem().toString() : "Tất cả";

        if(tuNgay.isEmpty() || denNgay.isEmpty()){
             JOptionPane.showMessageDialog(view, "Vui lòng nhập Từ ngày và Đến ngày!");
             return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
        dao.layDuLieuBaoCao(tuNgay, denNgay, danhMuc, model);
    }
}