package Controller;

import Model.LichSuKiemKeDAO;
import View.LichSuKiemKePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class LichSuKiemKeController {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final LichSuKiemKePanel view;
    private final LichSuKiemKeDAO dao;

    public LichSuKiemKeController(LichSuKiemKePanel view) {
        this.view = view;
        this.dao = new LichSuKiemKeDAO();
        // Mới vào: bảng để trống, chỉ hiện sau khi bấm Tìm kiếm
        ((DefaultTableModel) view.getTableDanhSach().getModel()).setRowCount(0);
        initController();
    }

    private void initController() {
        loadComboBoxKhu();
        view.getBtnTimKiem().addActionListener(e -> timKiem());
        view.getBtnXemChiTiet().addActionListener(e -> xemChiTiet());
        view.getBtnInPhieu().addActionListener(e -> inPhieuDangChon());
    }

    private void loadComboBoxKhu() {
        view.getCbKhu().removeAllItems();
        view.getCbKhu().addItem("Tất cả");
        List<String> listKhu = dao.layDanhSachKhu();
        for (String khu : listKhu) view.getCbKhu().addItem(khu);
    }

    /** Rỗng -> null; sai định dạng -> ném DateTimeParseException. */
    private java.sql.Date parseNgay(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        return java.sql.Date.valueOf(LocalDate.parse(s, FMT));
    }

    private void timKiem() {
        java.sql.Date tu, den;
        try {
            tu = parseNgay(view.getTxtTuNgay().getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Từ ngày phải có dạng dd/MM/yyyy (ví dụ 01/10/2026)!");
            return;
        }
        try {
            den = parseNgay(view.getTxtDenNgay().getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Đến ngày phải có dạng dd/MM/yyyy (ví dụ 30/10/2026)!");
            return;
        }
        if (tu != null && den != null && tu.after(den)) {
            JOptionPane.showMessageDialog(view, "Từ ngày không được sau Đến ngày!");
            return;
        }

        String khu = view.getCbKhu().getSelectedItem() != null
                ? view.getCbKhu().getSelectedItem().toString() : "Tất cả";
        String trangThai = view.getCbTrangThai().getSelectedItem() != null
                ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";

        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        try {
            dao.layDanhSachPhieuKiemKe(tu, den, khu, trangThai, model);
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(view, "Không tìm thấy phiếu kiểm kê nào.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tìm kiếm: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Dòng đang chọn: [mã, ngày, khu, người kiểm kê, mặt hàng lệch, kết quả] =====
    private String[] dongDangChon(String thongBaoChuaChon) {
        int r = view.getTableDanhSach().getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(view, thongBaoChuaChon);
            return null;
        }
        String[] s = new String[6];
        for (int i = 0; i < 6; i++) {
            Object o = view.getTableDanhSach().getValueAt(r, i);
            s[i] = o == null ? "" : o.toString();
        }
        return s;
    }

    /** Bảng chi tiết (không cho sửa), dòng lệch tô đỏ. */
    private JTable taoBangChiTiet(String maPhieu) throws Exception {
        DefaultTableModel m = new DefaultTableModel(
                new Object[]{"STT", "Mã SP", "Tên sản phẩm", "Vị trí", "SL hệ thống", "SL thực tế", "Chênh lệch"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        dao.layChiTietPhieu(maPhieu, m);
        JTable t = new JTable(m);
        t.setRowHeight(24);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                if (!sel) {
                    int cl = Integer.parseInt(tb.getValueAt(row, 6).toString());
                    c.setForeground(cl != 0 ? Color.RED : Color.BLACK);
                }
                return c;
            }
        });
        return t;
    }

    private void xemChiTiet() {
        String[] s = dongDangChon("Vui lòng chọn phiếu để xem chi tiết!");
        if (s == null) return;
        try {
            JTable bang = taoBangChiTiet(s[0]);
            String ghiChu = dao.layGhiChu(s[0]);

            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(view),
                    "Chi tiết phiếu kiểm kê " + s[0], JDialog.ModalityType.APPLICATION_MODAL);

            JLabel info = new JLabel("<html><b>Mã phiếu:</b> " + s[0]
                    + " &nbsp;&nbsp; <b>Ngày:</b> " + s[1]
                    + " &nbsp;&nbsp; <b>Khu:</b> " + s[2]
                    + " &nbsp;&nbsp; <b>Người kiểm kê:</b> " + s[3]
                    + "<br><b>Trạng thái:</b> " + s[5]
                    + " &nbsp;&nbsp; <b>Mặt hàng lệch:</b> " + s[4]
                    + (ghiChu.isEmpty() ? "" : "<br><b>Ghi chú:</b> " + ghiChu) + "</html>");
            info.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            JButton btnIn = new JButton("In phiếu");
            JButton btnDong = new JButton("Đóng");
            btnIn.addActionListener(e -> inBang(bang, s));
            btnDong.addActionListener(e -> dlg.dispose());
            JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            nut.add(btnIn);
            nut.add(btnDong);

            dlg.setLayout(new BorderLayout());
            dlg.add(info, BorderLayout.NORTH);
            dlg.add(new JScrollPane(bang), BorderLayout.CENTER);
            dlg.add(nut, BorderLayout.SOUTH);
            dlg.setSize(800, 450);
            dlg.setLocationRelativeTo(view);
            dlg.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải chi tiết: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inPhieuDangChon() {
        String[] s = dongDangChon("Vui lòng chọn phiếu để in!");
        if (s == null) return;
        try {
            inBang(taoBangChiTiet(s[0]), s);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải phiếu để in: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Mở hộp thoại in. Chọn "Microsoft Print to PDF" nếu muốn lưu ra file. */
    private void inBang(JTable bang, String[] s) {
        MessageFormat dau = new MessageFormat(esc("PHIẾU KIỂM KÊ " + s[0]
                + " | Khu " + s[2] + " | Ngày " + s[1] + " | Người kiểm kê: " + s[3]));
        MessageFormat cuoi = new MessageFormat("Trang {0}");
        try {
            bang.print(JTable.PrintMode.FIT_WIDTH, dau, cuoi);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(view, "Không in được: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** MessageFormat coi ' và { là ký tự đặc biệt. */
    private String esc(String t) {
        return t.replace("'", "''").replace("{", "'{'").replace("}", "'}'");
    }
}