package Controller;

import Model.CatVaoViTriDAO;
import Model.PhienDangNhap;
import View.CatVaoViTriPanel;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class CatVaoViTriController {
    private final CatVaoViTriPanel view;
    private final CatVaoViTriDAO dao;
    private String maPhieuDangMo = "";
    private String khuCuaPhieu = "";   // khu nhận của phiếu đang mở (vd "A")// phiếu đang hiển thị
    private final List<Object[]> dongGoc = new ArrayList<>();   // toàn bộ dòng của phiếu (chưa lọc)
    private final List<Object[]> dangHien = new ArrayList<>();  // các dòng đang hiện (theo đúng thứ tự trong bảng)

    public CatVaoViTriController(CatVaoViTriPanel view) {
        this.view = view;
        this.dao = new CatVaoViTriDAO();
        view.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && view.isShowing()) {
                capNhatTrangThaiDangNhap();
            }
        });
        capNhatTrangThaiDangNhap();
        initController();
        view.getCbKhu().addActionListener(e -> {
    locTheoKhu();
    String khu = maKhuDangChon();
    if (!maPhieuDangMo.isEmpty() && khu != null && !khu.equalsIgnoreCase(khuCuaPhieu)) {
        JOptionPane.showMessageDialog(view,
                "Phiếu " + maPhieuDangMo + " được nhận vào khu " + khuCuaPhieu
                + ", không phải khu " + khu + "!");
    }
});
    }

    private boolean daDangNhap() {
        String ten = PhienDangNhap.tenDangNhap;
        return ten != null && !ten.isBlank() && !ten.equalsIgnoreCase("guest");
    }

    /** Chưa đăng nhập: khóa nút. Đã đăng nhập: mở nút, điền và khóa ô Người thực hiện. */
    private void capNhatTrangThaiDangNhap() {
        boolean ok = daDangNhap();
        view.getBtnXacNhan().setEnabled(ok);
        view.getBtnHuy().setEnabled(ok);

        String hoTen = PhienDangNhap.hoTen;
        String tenHienThi = (hoTen != null && !hoTen.isEmpty()) ? hoTen : PhienDangNhap.tenDangNhap;
        view.getTxtNguoiThucHien().setText(ok ? tenHienThi : "");
        view.getTxtNguoiThucHien().setEditable(false);
    }

    private void initController() {
        // Gõ mã phiếu: đúng mới nạp
        view.getCbPhieuNhap().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { napPhieu(); }
            @Override public void removeUpdate(DocumentEvent e)  { napPhieu(); }
            @Override public void changedUpdate(DocumentEvent e) { napPhieu(); }
        });

        // Đổi khu: lọc lại bảng (tên getter có thể khác, sửa cho đúng panel của bạn)
        view.getCbKhu().addActionListener(e -> locTheoKhu());
        view.getBtnXacNhan().addActionListener(e -> thucHienCatHang());
        view.getBtnHuy().addActionListener(e -> lamTrang());
    }

    private DefaultTableModel model() {
        return (DefaultTableModel) view.getTableChiTiet().getModel();
    }

    private void lamTrang() {
        if (view.getTableChiTiet().isEditing()) view.getTableChiTiet().getCellEditor().cancelCellEditing();
        maPhieuDangMo = "";
        dongGoc.clear();
        dangHien.clear();
        view.getCbPhieuNhap().setText("");
        model().setRowCount(0);
    }

    /** Lấy mã khu từ mục đang chọn ("Khu B" -> "B"). null = không lọc (mục "Tất cả"). */
    private String maKhuDangChon() {
        if (view.getCbKhu().getSelectedIndex() <= 0) return null;   // mục đầu "— Chọn khu —" = hiện tất cả
            Object o = view.getCbKhu().getSelectedItem();
        if (o == null) return null;
            String s = o.toString().trim();
            String low = s.toLowerCase();
        if (s.isEmpty() || low.contains("tất cả") || low.contains("chọn khu")) return null;
            int i = s.lastIndexOf(' ');
            return (i >= 0 ? s.substring(i + 1) : s).toUpperCase();
    }

    /** Chép số lượng/vị trí người dùng đã nhập ở các dòng đang hiện về dongGoc. */
    private void luuBangVaoDongGoc() {
        if (view.getTableChiTiet().isEditing()) view.getTableChiTiet().getCellEditor().stopCellEditing();
            DefaultTableModel m = model();
        for (int i = 0; i < m.getRowCount() && i < dangHien.size(); i++) {
            Object[] g = dangHien.get(i);
            for (int c = 0; c < g.length; c++) g[c] = m.getValueAt(i, c);
        }
    }

    /** Hiện các dòng của phiếu thuộc khu đang chọn (cột 4 = vị trí, dạng "B-11"). */
    private void locTheoKhu() {
    luuBangVaoDongGoc();
    DefaultTableModel m = model();
    m.setRowCount(0);
    dangHien.clear();
    if (maPhieuDangMo.isEmpty()) return;

    String khu = maKhuDangChon();
    if (khu == null || !khu.equalsIgnoreCase(khuCuaPhieu)) return;   // sai khu -> bảng trống

    int stt = 1;
    for (Object[] g : dongGoc) {
        g[0] = stt++;
        m.addRow(g);
        dangHien.add(g);
        }
    }

    /** Nạp bảng từ DB vào dongGoc rồi lọc theo khu. */
    private void nap(String maChuan) {
        String k = dao.layKhuCuaPhieu(maChuan);
        khuCuaPhieu = k == null ? "" : k.trim();
        DefaultTableModel m = model();
        dao.layChiTietPhieu(maChuan, m);
        dongGoc.clear();
        for (int i = 0; i < m.getRowCount(); i++) {
            Object[] r = new Object[m.getColumnCount()];
            for (int c = 0; c < r.length; c++) r[c] = m.getValueAt(i, c);
            dongGoc.add(r);
        }
        dangHien.clear();
        for (int i = 0; i < m.getRowCount(); i++) dangHien.add(dongGoc.get(i)); // khớp tạm để luuBang không lệch
        locTheoKhu();
    }

    /** Mỗi khi ô mã phiếu đổi: khớp đúng phiếu thì nạp, không khớp thì xóa bảng. */
    private void napPhieu() {
        String ma = view.getCbPhieuNhap().getText().trim();

        String maChuan = null;
        if (!ma.isEmpty()) {
            for (String p : dao.layDanhSachPhieuChoCat()) {
                if (p.equalsIgnoreCase(ma)) { maChuan = p; break; }
            }
        }

        if (maChuan == null) {
            maPhieuDangMo = "";
            dongGoc.clear();
            dangHien.clear();
            model().setRowCount(0);
            return;
        }
        if (maChuan.equals(maPhieuDangMo)) return;

        maPhieuDangMo = maChuan;
        nap(maChuan);
    }

    private void thucHienCatHang() {
        if (!daDangNhap()) {
            JOptionPane.showMessageDialog(view, "Vui lòng đăng nhập để thực hiện chức năng này!");
            return;
        }
        if (view.getTableChiTiet().isEditing()) view.getTableChiTiet().getCellEditor().stopCellEditing();

        if (maPhieuDangMo.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập mã phiếu nhập hợp lệ!");
            return;
        }

        DefaultTableModel model = model();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Khu này không có dữ liệu cất hàng!");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                int slCanCat = Integer.parseInt(String.valueOf(model.getValueAt(i, 3)).trim());
                int slDaCat  = Integer.parseInt(String.valueOf(model.getValueAt(i, 5)).trim());
                Object vt = model.getValueAt(i, 4);
                String viTri = vt == null ? "" : vt.toString().trim();

                if (slDaCat > slCanCat || slDaCat < 0) {
                    JOptionPane.showMessageDialog(view,
                            "Dòng " + (i + 1) + ": Số lượng cất (" + slDaCat + ") không hợp lệ!");
                    return;
                }
                if (slDaCat > 0 && viTri.isEmpty()) {
                    JOptionPane.showMessageDialog(view,
                            "Dòng " + (i + 1) + ": Vui lòng nhập vị trí cất hàng!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view,
                        "Dòng " + (i + 1) + ": Số lượng phải là số nguyên!");
                return;
            }
        }

        String ma = maPhieuDangMo;
        if (dao.xacNhanCatHang(ma, model)) {   // chỉ gửi các dòng đang hiện (đúng khu)
            JOptionPane.showMessageDialog(view, "Xác nhận chuyển hàng thành công!");
            nap(ma);                            // nạp lại phần còn chưa cất, giữ bộ lọc khu
            if (dongGoc.isEmpty()) lamTrang();  // cất hết cả phiếu -> xóa trắng
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu dữ liệu. Vui lòng thử lại!");
        }
    }
}