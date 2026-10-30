package Controller;

import Model.BaoCaoNhapXuatDAO;
import View.BaoCaoNhapXuatPanel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class BaoCaoNhapXuatController {
    private final BaoCaoNhapXuatPanel view;
    private final BaoCaoNhapXuatDAO dao;

    public BaoCaoNhapXuatController(BaoCaoNhapXuatPanel view) {
        this.view = view;
        this.dao = new BaoCaoNhapXuatDAO();
        initController();   // không nạp bảng ở đây: chỉ lọc khi bấm Tìm kiếm
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
    }

    /** Nhận d/M/yyyy hoặc yyyy-M-d, trả null nếu sai. */
    private LocalDate docNgay(String s) {
        DateTimeFormatter[] fs = {DateTimeFormatter.ofPattern("d/M/uuuu"),
                                  DateTimeFormatter.ofPattern("uuuu-M-d")};
        for (DateTimeFormatter f : fs) {
            try { return LocalDate.parse(s.trim(), f); } catch (Exception ignored) { }
        }
        return null;
    }

    private void loadData() {
        LocalDate tu = docNgay(view.getTxtTuNgay().getText());
        LocalDate den = docNgay(view.getTxtDenNgay().getText());
        if (tu == null || den == null) {
            JOptionPane.showMessageDialog(view, "Nhập Từ ngày và Đến ngày dạng dd/MM/yyyy!");
            return;
        }
        if (den.isBefore(tu)) {
            JOptionPane.showMessageDialog(view, "Đến ngày phải sau hoặc bằng Từ ngày!");
            return;
        }
        Object dmSel = view.getCbDanhMuc().getSelectedItem();
        String danhMuc = dmSel != null ? dmSel.toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableBaoCao().getModel();
        try {
            int n = dao.layDuLieuBaoCao(
                    java.sql.Timestamp.valueOf(tu.atStartOfDay()),
                    java.sql.Timestamp.valueOf(den.plusDays(1).atStartOfDay()),
                    danhMuc, model);
            if (n == 0) JOptionPane.showMessageDialog(view, "Không có dữ liệu phù hợp!");
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
            String ky = view.getTxtTuNgay().getText().trim() + " - " + view.getTxtDenNgay().getText().trim();
            tbl.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("BÁO CÁO NHẬP XUẤT TỒN (" + ky + ")"),
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
        fc.setSelectedFile(new File("BaoCaoNhapXuat_" + LocalDate.now() + ".csv"));
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