package Controller;

import Model.PhieuNhapDAO;
import Model.PhienDangNhap;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Cột của bảng: 0 STT | 1 Mã SP | 2 Tên SP | 3 ĐVT | 4 Số lượng | 5 Đơn giá | 6 Thành tiền
 */
public class PhieuNhapController {

    private static final int COT_STT = 0, COT_MASP = 1, COT_TEN = 2, COT_DVT = 3,
                             COT_SL = 4, COT_GIA = 5, COT_TT = 6;

    private final javax.swing.JPanel view;
    private final JTextField txtMaPhieu, txtNgayNhap, txtGhiChu;
    private final JComboBox<?> cboNguoiLap, cboNhaCungCap, cboKhuNhan;
    private final JTable tbl;
    private final JLabel lblTongTien;

    private DefaultTableModel model;
    private boolean dangCapNhat = false;   // chống lặp vô hạn khi tự set giá trị ô

    // Constructor rút gọn: chỉ cần truyền panel (dùng trong Main)
    public PhieuNhapController(View.PhieuNhapPanel p) {
        this(p,
             p.getTxtMaPhieu(), p.getTxtNgayNhap(),
             p.getCbNguoiLap(), p.getCbNhaCungCap(), p.getCbKhuNhan(),
             p.getTxtGhiChu(), p.getTableChiTiet(), p.getLblTongTien(),
             p.getBtnThemDong(), p.getBtnXoaDong(), p.getBtnHuy(), p.getBtnLuuPhieu());
    }

    public PhieuNhapController(javax.swing.JPanel view,
                               JTextField txtMaPhieu, JTextField txtNgayNhap,
                               JComboBox<?> cboNguoiLap, JComboBox<?> cboNhaCungCap,
                               JComboBox<?> cboKhuNhan, JTextField txtGhiChu,
                               JTable tbl, JLabel lblTongTien,
                               JButton btnThemDong, JButton btnXoaDong,
                               JButton btnHuy, JButton btnLuuPhieu) {
        this.view = view;
        this.txtMaPhieu = txtMaPhieu;
        this.txtNgayNhap = txtNgayNhap;
        this.cboNguoiLap = cboNguoiLap;
        this.cboNhaCungCap = cboNhaCungCap;
        this.cboKhuNhan = cboKhuNhan;
        this.txtGhiChu = txtGhiChu;
        this.tbl = tbl;
        this.lblTongTien = lblTongTien;

        khoiTaoBang();
        khoiTaoForm();

        btnThemDong.addActionListener((ActionEvent e) -> themDong());
        btnXoaDong.addActionListener((ActionEvent e) -> xoaDong());
        btnHuy.addActionListener((ActionEvent e) -> lamMoi());
        btnLuuPhieu.addActionListener((ActionEvent e) -> luuPhieu());
    }

    // ================= KHỞI TẠO =================
    private void khoiTaoBang() {
        model = new DefaultTableModel(
                new Object[]{"STT", "Mã SP", "Tên Sản Phẩm", "ĐVT", "Số lượng", "Đơn giá", "Thành Tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == COT_MASP || col == COT_SL || col == COT_GIA;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == COT_STT || col == COT_SL) return Integer.class;
                if (col == COT_GIA || col == COT_TT) return Double.class;
                return String.class;
            }
        };
        tbl.setModel(model);   // model rỗng: hết 4 dòng trống mặc định

        model.addTableModelListener(e -> {
            if (dangCapNhat || e.getType() != TableModelEvent.UPDATE) return;
            int r = e.getFirstRow();
            int c = e.getColumn();
            if (r < 0 || r >= model.getRowCount()) return;
            if (c == COT_MASP) {
                napSanPham(r);
            } else if (c == COT_SL || c == COT_GIA) {
                tinhThanhTien(r);
            }
        });
    }

    private void khoiTaoForm() {
        txtMaPhieu.setText(PhieuNhapDAO.taoMaPhieuMoi());
        txtMaPhieu.setEditable(false);
        txtNgayNhap.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        // Người lập lấy từ phiên đăng nhập, không cho chọn
        String ten = PhienDangNhap.tenDangNhap;
        String hoTen = PhienDangNhap.hoTen;
        @SuppressWarnings("unchecked")
        JComboBox<Object> cbo = (JComboBox<Object>) cboNguoiLap;
        cbo.removeAllItems();
        cbo.addItem(hoTen != null && !hoTen.isEmpty() ? hoTen : (ten == null ? "(chưa đăng nhập)" : ten));
        cbo.setEnabled(false);

        capNhatTong();
    }

    // ================= XỬ LÝ BẢNG =================
    private void themDong() {
        dungSua();
        dangCapNhat = true;
        model.addRow(new Object[]{model.getRowCount() + 1, "", "", "", 1, 0.0, 0.0});
        dangCapNhat = false;
        int r = model.getRowCount() - 1;
        tbl.changeSelection(r, COT_MASP, false, false);
        tbl.editCellAt(r, COT_MASP);
        if (tbl.getEditorComponent() != null) tbl.getEditorComponent().requestFocus();
    }

    private void xoaDong() {
        dungSua();
        int r = tbl.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(view, "Hãy chọn dòng cần xóa!");
            return;
        }
        dangCapNhat = true;
        model.removeRow(r);
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, COT_STT);   // đánh lại STT
        }
        dangCapNhat = false;
        capNhatTong();
    }

    private void napSanPham(int r) {
        String ma = chuoi(model.getValueAt(r, COT_MASP)).trim();
        dangCapNhat = true;
        try {
            if (ma.isEmpty()) {
                model.setValueAt("", r, COT_TEN);
                model.setValueAt("", r, COT_DVT);
                return;
            }
            PhieuNhapDAO.SanPhamInfo sp = PhieuNhapDAO.laySanPham(ma);
            if (sp == null) {
                model.setValueAt("", r, COT_TEN);
                model.setValueAt("", r, COT_DVT);
                JOptionPane.showMessageDialog(view, "Không có sản phẩm mã: " + ma);
            } else {
                model.setValueAt(sp.ten, r, COT_TEN);
                model.setValueAt(sp.donViTinh, r, COT_DVT);
                model.setValueAt(sp.giaNhap, r, COT_GIA);
            }
        } finally {
            dangCapNhat = false;
        }
        tinhThanhTien(r);
    }

    private void tinhThanhTien(int r) {
        int sl = soNguyen(model.getValueAt(r, COT_SL));
        double gia = soThuc(model.getValueAt(r, COT_GIA));
        dangCapNhat = true;
        model.setValueAt(sl * gia, r, COT_TT);
        dangCapNhat = false;
        capNhatTong();
    }

    private void capNhatTong() {
        double tong = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            tong += soThuc(model.getValueAt(i, COT_TT));
        }
        lblTongTien.setText("Tổng tiền: " + new DecimalFormat("#,###").format(tong) + " đ");
    }

    // ================= LƯU / HỦY =================
    private void luuPhieu() {
        dungSua();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Phiếu chưa có dòng hàng nào!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (PhienDangNhap.tenDangNhap == null) {
            JOptionPane.showMessageDialog(view, "Chưa đăng nhập! Hãy chạy ứng dụng từ màn hình đăng nhập.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.sql.Date ngay;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            ngay = new java.sql.Date(sdf.parse(txtNgayNhap.getText().trim()).getTime());
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(view, "Ngày nhập phải có dạng dd/MM/yyyy!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PhieuNhapDAO.DongNhap> ds = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String ma = chuoi(model.getValueAt(i, COT_MASP)).trim();
            int sl = soNguyen(model.getValueAt(i, COT_SL));
            double gia = soThuc(model.getValueAt(i, COT_GIA));
            if (ma.isEmpty() || chuoi(model.getValueAt(i, COT_TEN)).isEmpty()) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": chưa có Mã SP hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (sl <= 0) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": số lượng phải lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (gia < 0) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": đơn giá không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ds.add(new PhieuNhapDAO.DongNhap(ma, sl, gia));
        }

        // Nhà cung cấp và khu nhận chưa có cột riêng nên ghép vào ghi chú
        String ghiChu = "NCC: " + cboNhaCungCap.getSelectedItem()
                + " | Khu: " + cboKhuNhan.getSelectedItem();
        String ghiChuNhap = txtGhiChu.getText().trim();
        if (!ghiChuNhap.isEmpty()) {
            ghiChu += " | " + ghiChuNhap;
        }

        try {
            // "Khu B" -> "B" (cột PhieuNhap.MaKhu là char(1))
            String khu = String.valueOf(cboKhuNhan.getSelectedItem()).trim();
            String maKhu = khu.isEmpty() ? null : khu.substring(khu.length() - 1).toUpperCase();
            String maPhieu = PhieuNhapDAO.luuPhieuNhap(ngay, PhienDangNhap.tenDangNhap, maKhu, ghiChu, ds);
            JOptionPane.showMessageDialog(view, "Đã lưu phiếu nhập " + maPhieu);
            lamMoi();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lưu phiếu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoi() {
        dungSua();
        dangCapNhat = true;
        model.setRowCount(0);
        dangCapNhat = false;
        txtGhiChu.setText("");
        txtMaPhieu.setText(PhieuNhapDAO.taoMaPhieuMoi());
        txtNgayNhap.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        capNhatTong();
    }

    // ================= TIỆN ÍCH =================
    private void dungSua() {
        if (tbl.isEditing()) {
            tbl.getCellEditor().stopCellEditing();
        }
    }

    private static String chuoi(Object o) {
        return o == null ? "" : o.toString();
    }

    private static int soNguyen(Object o) {
        try {
            return (int) Double.parseDouble(chuoi(o).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double soThuc(Object o) {
        try {
            return Double.parseDouble(chuoi(o).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}