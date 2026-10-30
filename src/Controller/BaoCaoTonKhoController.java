package Controller;

import Model.BaoCaoTonKhoDAO;
import View.BaoCaoTonKhoPanel;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import javax.swing.JFileChooser;
import javax.swing.JTable;

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
        
        view.getBtnInBaoCao().addActionListener(e -> inBaoCao());
        view.getBtnXuatExcel().addActionListener(e -> xuatExcel());
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
    String danhMuc = view.getCbDanhMuc().getSelectedItem() != null
            ? view.getCbDanhMuc().getSelectedItem().toString() : "Tất cả";
    String khu = view.getCbKhu().getSelectedItem() != null
            ? view.getCbKhu().getSelectedItem().toString() : "Tất cả";
    String denNgay = view.getTxtDenNgay().getText().trim();

    DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
    try {
        int soDong = dao.layDuLieuBaoCao(danhMuc, khu, denNgay, model);
        if (soDong == 0) {
            JOptionPane.showMessageDialog(view, "Không có dữ liệu phù hợp!");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(view, "Lỗi truy vấn: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
    private void inBaoCao() {
    JTable tbl = view.getTableBaoCao();
    if (tbl.getRowCount() == 0) {
        JOptionPane.showMessageDialog(view, "Chưa có dữ liệu để in! Hãy bấm Tìm kiếm trước.");
        return;
    }
    try {
        MessageFormat tieuDe = new MessageFormat("BÁO CÁO TỒN KHO - " + LocalDate.now());
        MessageFormat chanTrang = new MessageFormat("Trang {0}");
        // Hiện hộp thoại chọn máy in của hệ thống
        tbl.print(JTable.PrintMode.FIT_WIDTH, tieuDe, chanTrang);
    } catch (java.awt.print.PrinterException ex) {
        JOptionPane.showMessageDialog(view, "Không in được: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

private void xuatExcel() {
    JTable tbl = view.getTableBaoCao();
    if (tbl.getRowCount() == 0) {
        JOptionPane.showMessageDialog(view, "Chưa có dữ liệu để xuất! Hãy bấm Tìm kiếm trước.");
        return;
    }

    JFileChooser fc = new JFileChooser();
    fc.setSelectedFile(new File("BaoCaoTonKho_" + LocalDate.now() + ".csv"));
    if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;

    File f = fc.getSelectedFile();
    if (!f.getName().toLowerCase().endsWith(".csv")) {
        f = new File(f.getParentFile(), f.getName() + ".csv");
    }

    try (BufferedWriter w = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
        w.write('\uFEFF');          // BOM để Excel đọc đúng tiếng Việt
                // ép Excel tách cột bằng dấu phẩy

        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < tbl.getColumnCount(); c++) {
            if (c > 0) sb.append(',');
            sb.append(csv(tbl.getColumnName(c)));
        }
        w.write(sb.append('\n').toString());

        for (int r = 0; r < tbl.getRowCount(); r++) {
            sb.setLength(0);
            for (int c = 0; c < tbl.getColumnCount(); c++) {
                if (c > 0) sb.append(',');
                Object v = tbl.getValueAt(r, c);
                sb.append(csv(v == null ? "" : v.toString()));
            }
            w.write(sb.append('\n').toString());
        }
        JOptionPane.showMessageDialog(view, "Đã xuất file:\n" + f.getAbsolutePath());
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(view, "Không xuất được file: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

/** Bọc ô trong dấu nháy kép để các số dạng "50,000" không bị tách cột. */
private String csv(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
}
}