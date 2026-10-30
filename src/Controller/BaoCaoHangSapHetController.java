package Controller;

import Model.BaoCaoHangSapHetDAO;
import View.BaoCaoHangSapHetPanel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class BaoCaoHangSapHetController {
    private final BaoCaoHangSapHetPanel view;
    private final BaoCaoHangSapHetDAO dao;

    public BaoCaoHangSapHetController(BaoCaoHangSapHetPanel view) {
        this.view = view;
        this.dao = new BaoCaoHangSapHetDAO();
        initController();   // không nạp bảng ở đây: chỉ hiện khi bấm Tìm kiếm
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
        for (String dm : dao.layDanhSachDanhMuc()) view.getCbDanhMuc().addItem(dm);

        view.getCbTrangThai().removeAllItems();
        view.getCbTrangThai().addItem("Tất cả");
        view.getCbTrangThai().addItem("Sắp hết");
        view.getCbTrangThai().addItem("Hết hàng");
    }

    private void loadData() {
        Object dm = view.getCbDanhMuc().getSelectedItem();
        Object tt = view.getCbTrangThai().getSelectedItem();
        String danhMuc = dm != null ? dm.toString() : "Tất cả";
        String trangThai = tt != null ? tt.toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
        try {
            int n = dao.layDuLieuHangSapHet(danhMuc, trangThai, model);
            if (n == 0) JOptionPane.showMessageDialog(view, "Không có hàng nào dưới mức tồn tối thiểu!");
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
            tbl.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("BÁO CÁO HÀNG SẮP HẾT - " + LocalDate.now()),
                    new MessageFormat("Trang {0}"));
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
        fc.setSelectedFile(new File("BaoCaoHangSapHet_" + LocalDate.now() + ".csv"));
        if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getParentFile(), f.getName() + ".csv");
        }
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            w.write('\uFEFF');   // BOM để Excel đọc đúng tiếng Việt
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

    private String csv(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}