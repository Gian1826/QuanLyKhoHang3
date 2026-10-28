package Controller;

import Model.PhieuNhapDaLuuDAO;
import View.PhieuNhapDaLuuPanel;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PhieuNhapDaLuuController {

    private static final SimpleDateFormat DINH_DANG_NGAY = new SimpleDateFormat("dd/MM/yyyy");

    private final PhieuNhapDaLuuPanel view;
    private final JTable tbl;
    private DefaultTableModel model;
    private List<PhieuNhapDaLuuDAO.PhieuRow> dsHienTai = new ArrayList<>();

    public PhieuNhapDaLuuController(PhieuNhapDaLuuPanel view) {
        this.view = view;
        this.tbl = view.getTableDanhSach();

        khoiTaoBang();
        khoiTaoBoLoc();

        view.getBtnTimKiem().addActionListener(e -> taiDuLieu());
        view.getBtnXemChiTiet().addActionListener(e -> xemChiTiet());
        view.getBtnSua().addActionListener(e -> suaGhiChu());
        view.getBtnHuyPhieu().addActionListener(e -> huyPhieu());

        // Mỗi lần chuyển sang màn hình này thì nạp lại để thấy phiếu mới lưu
        view.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                taiDuLieu();
            }
        });

        taiDuLieu();
    }

    // ================= KHỞI TẠO =================
    private void khoiTaoBang() {
        model = new DefaultTableModel(
                new Object[]{"Mã Phiếu", "Ngày", "Nhà Cung Cấp", "Người lập", "Tổng tiền", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 4 ? Double.class : String.class;
            }
        };
        tbl.setModel(model);   // bỏ 4 dòng trống mặc định
        tbl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    private void khoiTaoBoLoc() {
        themTatCa(view.getCbNhaCungCap());
        themTatCa(view.getCbTrangThai());
    }

    private static void themTatCa(JComboBox<String> cbo) {
        cbo.insertItemAt("Tất cả", 0);
        cbo.setSelectedIndex(0);
    }

    // ================= TẢI DỮ LIỆU =================
    private void taiDuLieu() {
        java.sql.Date tu, den;
        try {
            tu = parseNgay(view.getTxtTuNgay().getText());
            den = parseNgay(view.getTxtDenNgay().getText());
        } catch (java.text.ParseException ex) {
            JOptionPane.showMessageDialog(view, "Ngày phải có dạng dd/MM/yyyy (ví dụ 28/10/2026)!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tu != null && den != null && tu.after(den)) {
            JOptionPane.showMessageDialog(view, "\"Từ ngày\" phải nhỏ hơn hoặc bằng \"Đến ngày\"!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ncc = view.getCbNhaCungCap().getSelectedIndex() <= 0
                ? null : (String) view.getCbNhaCungCap().getSelectedItem();
        String tt = view.getCbTrangThai().getSelectedIndex() <= 0
                ? null : (String) view.getCbTrangThai().getSelectedItem();

        try {
            dsHienTai = PhieuNhapDaLuuDAO.timKiem(tu, den, ncc, tt);
            model.setRowCount(0);
            for (PhieuNhapDaLuuDAO.PhieuRow r : dsHienTai) {
                model.addRow(new Object[]{
                    r.ma,
                    r.ngay == null ? "" : DINH_DANG_NGAY.format(r.ngay),
                    r.ncc,
                    r.nguoiLap,
                    r.tong,
                    r.trangThai
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Không tải được danh sách: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static java.sql.Date parseNgay(String s) throws java.text.ParseException {
        if (s == null || s.trim().isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        return new java.sql.Date(sdf.parse(s.trim()).getTime());
    }

    // ================= CHỌN DÒNG =================
    private PhieuNhapDaLuuDAO.PhieuRow phieuDangChon() {
        int r = tbl.getSelectedRow();
        if (r < 0 || r >= dsHienTai.size()) {
            JOptionPane.showMessageDialog(view, "Hãy chọn một phiếu trong danh sách!");
            return null;
        }
        return dsHienTai.get(r);
    }

    // ================= XEM CHI TIẾT =================
    private void xemChiTiet() {
        PhieuNhapDaLuuDAO.PhieuRow p = phieuDangChon();
        if (p == null) return;
        try {
            List<PhieuNhapDaLuuDAO.ChiTietRow> ds = PhieuNhapDaLuuDAO.layChiTiet(p.ma);
            DefaultTableModel m = new DefaultTableModel(
                    new Object[]{"Mã SP", "Tên Sản Phẩm", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền", "Đã cất"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            double tong = 0;
            for (PhieuNhapDaLuuDAO.ChiTietRow c : ds) {
                double tt = c.soLuong * c.donGia;
                tong += tt;
                m.addRow(new Object[]{c.maSP, c.tenSP, c.dvt, c.soLuong, c.donGia, tt, c.daCat});
            }
            JTable t = new JTable(m);
            JScrollPane sp = new JScrollPane(t);
            sp.setPreferredSize(new Dimension(700, 250));

            JOptionPane.showMessageDialog(view, sp,
                    "Phiếu " + p.ma + " - Tổng: " + new DecimalFormat("#,###").format(tong) + " đ",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Không xem được chi tiết: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= SỬA GHI CHÚ =================
    private void suaGhiChu() {
        PhieuNhapDaLuuDAO.PhieuRow p = phieuDangChon();
        if (p == null) return;
        if ("Đã Hủy".equals(p.trangThai)) {
            JOptionPane.showMessageDialog(view, "Phiếu đã hủy, không thể sửa!");
            return;
        }

        // Ghi chú lưu dạng "NCC: x | Khu: y | ghi chú người dùng"
        String gc = p.ghiChu == null ? "" : p.ghiChu;
        String tienTo = "";
        String ghiChuNguoiDung = gc;
        String[] parts = gc.split(" \\| ", 3);
        if (parts.length >= 2 && parts[0].startsWith("NCC: ") && parts[1].startsWith("Khu: ")) {
            tienTo = parts[0] + " | " + parts[1];
            ghiChuNguoiDung = parts.length == 3 ? parts[2] : "";
        }

        String nhap = JOptionPane.showInputDialog(view, "Ghi chú của phiếu " + p.ma + ":", ghiChuNguoiDung);
        if (nhap == null) return;   // bấm Cancel
        nhap = nhap.trim();

        String moi;
        if (tienTo.isEmpty()) {
            moi = nhap;
        } else {
            moi = nhap.isEmpty() ? tienTo : tienTo + " | " + nhap;
        }

        try {
            if (PhieuNhapDaLuuDAO.capNhatGhiChu(p.ma, moi)) {
                JOptionPane.showMessageDialog(view, "Đã cập nhật ghi chú!");
                taiDuLieu();
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= HỦY PHIẾU =================
    private void huyPhieu() {
        PhieuNhapDaLuuDAO.PhieuRow p = phieuDangChon();
        if (p == null) return;

        int ok = JOptionPane.showConfirmDialog(view, "Bạn chắc chắn muốn hủy phiếu " + p.ma + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            PhieuNhapDaLuuDAO.huyPhieu(p.ma);
            JOptionPane.showMessageDialog(view, "Đã hủy phiếu " + p.ma);
            taiDuLieu();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Không thể hủy", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}