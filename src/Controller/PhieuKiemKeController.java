package Controller;

import Model.PhienDangNhap;
import Model.PhieuKiemKeDAO;
import View.PhieuKiemKePanel;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.util.HashSet;
import java.util.Set;

public class PhieuKiemKeController {
    private static final String CHON_KHU = "— Chọn khu —";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private Set<String> spLoc = null;   // sản phẩm của phiếu nhập đang lọc (null = không lọc)
    private final PhieuKiemKePanel view;
    private final PhieuKiemKeDAO dao;
    private final DefaultTableModel model;
    private final List<Object[]> dongGoc = new ArrayList<>();  // toàn bộ dòng chưa lọc
    private boolean dangNap = false;       // chặn listener khi code tự set giá trị
    private boolean dangCapNhat = false;   // chặn lặp khi tự set cột chênh lệch
    private String maPhieuDangMo = "";     // phiếu nháp đang nạp từ DB
    private boolean daHoanTat = false;

    public PhieuKiemKeController(PhieuKiemKePanel view) {
        this.view = view;
        this.dao = new PhieuKiemKeDAO();

        // Chỉ cho sửa cột "SL thực tế" (cột 5)
        model = new DefaultTableModel(
                new Object[]{"STT", "Mã SP", "Tên sản phẩm", "Vị trí", "SL hệ thống", "SL thực tế", "Chênh lệch"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5 && !daHoanTat; }
        };
        view.getTableKetQua().setModel(model);

        view.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && view.isShowing()) {
                capNhatNguoiKiemKe();
            }
        });
        capNhatNguoiKiemKe();
        initController();
    }

    private boolean daDangNhap() {
        String ten = PhienDangNhap.tenDangNhap;
        return ten != null && !ten.isBlank() && !ten.equalsIgnoreCase("guest");
    }

    /** Điền họ tên người đăng nhập và khóa ô. */
    private void capNhatNguoiKiemKe() {
        String hoTen = PhienDangNhap.hoTen;
        String hienThi = (hoTen != null && !hoTen.isEmpty()) ? hoTen
                : (PhienDangNhap.tenDangNhap != null ? PhienDangNhap.tenDangNhap : "");
        view.getTxtNguoiKiemKe().setText(daDangNhap() ? hienThi : "");
        view.getTxtNguoiKiemKe().setEditable(false);
    }

    private void initController() {
        loadComboBoxKhu();
        view.getTxtNgayKiemKe().setText(LocalDate.now().format(FMT));
        napTatCa();   // vào màn hình là có sẵn toàn bộ bảng

        // Gõ mã phiếu -> nếu trùng phiếu đã lưu thì nạp, chờ 300ms sau khi ngừng gõ
        Timer timerMa = new Timer(300, e -> napPhieu());
        timerMa.setRepeats(false);
        view.getTxtMaPhieu().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { timerMa.restart(); }
            @Override public void removeUpdate(DocumentEvent e)  { timerMa.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { timerMa.restart(); }
        });

        // Chọn khu -> lọc bảng
        view.getCbKhuKiemKe().addItemListener(e -> {
            if (dangNap || e.getStateChange() != ItemEvent.SELECTED) return;
            locTheoKhu(e.getItem().toString());
        });

        // Nhập SL thực tế -> tự tính chênh lệch
        model.addTableModelListener(e -> {
            if (dangCapNhat || dangNap) return;
            int r = e.getFirstRow();
            if (r < 0 || r >= model.getRowCount() || e.getColumn() != 5) return;
            dangCapNhat = true;
            try {
                Object tt = model.getValueAt(r, 5);
                if (tt == null || tt.toString().trim().isEmpty()) {
                    model.setValueAt(0, r, 6);
                } else {
                    int ht = Integer.parseInt(model.getValueAt(r, 4).toString());
                    model.setValueAt(Integer.parseInt(tt.toString().trim()) - ht, r, 6);
                }
            } catch (NumberFormatException ignored) {
                // đang gõ dở hoặc nhập chữ: kiểm tra lại lúc bấm lưu
            } finally {
                dangCapNhat = false;
            }
            capNhatSoMatHangLech();
        });

        view.getBtnHuy().addActionListener(e -> resetForm());
        view.getBtnLuuNhap().addActionListener(e -> thucHienLuu(0));
        view.getBtnHoanTat().addActionListener(e -> thucHienLuu(1));
    }

    private void loadComboBoxKhu() {
        view.getCbKhuKiemKe().removeAllItems();
        view.getCbKhuKiemKe().addItem(CHON_KHU);
        for (String khu : dao.layDanhSachKhu()) view.getCbKhuKiemKe().addItem(khu);
    }

    /** Hiện toàn bộ hàng tồn trong kho (mặc định khi chưa mở phiếu nào). */
    private void napTatCa() {
        dangNap = true;
        try {
            dao.layToanBoHangTon(model);
            chepBangVaoDongGoc();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi tải hàng tồn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            dangNap = false;
        }
        capNhatSoMatHangLech();
    }

    /** Gọi khi mã phiếu thay đổi: chỉ nạp nếu mã trùng một phiếu đã lưu. */
    private void napPhieu() {
    String ma = view.getTxtMaPhieu().getText().trim();
    if (!ma.isEmpty() && ma.equals(maPhieuDangMo)) return;

    try {
        PhieuKiemKeDAO.PhieuInfo p = ma.isEmpty() ? null : dao.layPhieu(ma);

        // 1) Trùng phiếu kiểm kê đã lưu -> nạp phiếu đó
        if (p != null) {
            dangNap = true;
            try {
                spLoc = null;
                maPhieuDangMo = ma;
                daHoanTat = p.trangThai == 1;
                if (p.ngay != null)
                    view.getTxtNgayKiemKe().setText(p.ngay.toLocalDateTime().toLocalDate().format(FMT));
                view.getTxtGhiChu().setText(p.ghiChu == null ? "" : p.ghiChu);
                view.getCbKhuKiemKe().setSelectedItem(p.maKhu);
                dao.layChiTietPhieu(ma, model);
                chepBangVaoDongGoc();
            } finally {
                dangNap = false;
            }
            capNhatSoMatHangLech();
            capNhatNut();
            if (daHoanTat)
                JOptionPane.showMessageDialog(view, "Phiếu " + ma + " đã hoàn tất, chỉ xem, không sửa được.");
            return;
        }

        // 2) Đang mở phiếu kiểm kê khác -> về bảng đầy đủ trước
        if (!maPhieuDangMo.isEmpty()) resetBangVaTrangThai();

        // 3) Trùng phiếu nhập -> lọc theo sản phẩm của phiếu; mã lạ/rỗng -> bỏ lọc
        Set<String> sp = new HashSet<>();
    if (!ma.isEmpty())
        for (String s : dao.layMaSanPhamTheoPhieu(ma)) sp.add(s.toUpperCase());

    String hoa = ma.toUpperCase();
    boolean maLoc = hoa.startsWith("PN") || hoa.startsWith("PX");
    if (!sp.isEmpty()) spLoc = sp;              // tìm thấy phiếu -> lọc
    else if (maLoc)    spLoc = new HashSet<>(); // mã PN/PX không tồn tại -> bảng trống
    else               spLoc = null;            // mã khác/rỗng -> bảng đầy đủ

        Object k = view.getCbKhuKiemKe().getSelectedItem();
        locTheoKhu(k == null ? CHON_KHU : k.toString());
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(view, "Lỗi tải phiếu: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

    private void capNhatNut() {
        boolean ok = daDangNhap() && !daHoanTat;
        view.getBtnLuuNhap().setEnabled(ok);
        view.getBtnHoanTat().setEnabled(ok);
    }

    /** Quay về trạng thái mặc định: không phiếu nào mở, bảng đầy đủ. */
    private void resetBangVaTrangThai() {
        dangNap = true;
        try {
            maPhieuDangMo = "";
            daHoanTat = false;
            view.getCbKhuKiemKe().setSelectedIndex(0);
        } finally {
            dangNap = false;
        }
        napTatCa();
        capNhatNut();
    }

    private void capNhatSoMatHangLech() {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object o = model.getValueAt(i, 6);
            if (o != null) {
                try { if (Integer.parseInt(o.toString()) != 0) count++; }
                catch (NumberFormatException ignored) { }
            }
        }
        view.getLblSoMatHangLech().setText("Số mặt hàng lệch: " + count);
    }

    /** Nút Hủy: xóa trắng form, bảng về đầy đủ. */
    private void resetForm() {
        if (view.getTableKetQua().isEditing()) view.getTableKetQua().getCellEditor().cancelCellEditing();
        dangNap = true;
        try {
            view.getTxtMaPhieu().setText("");
            view.getTxtNgayKiemKe().setText(LocalDate.now().format(FMT));
            view.getTxtGhiChu().setText("");
            view.getCbKhuKiemKe().setSelectedIndex(0);
            maPhieuDangMo = "";
            daHoanTat = false;
        } finally {
            dangNap = false;
        }
        napTatCa();
        capNhatNut();
    }

    /** Lưu lại SL thực tế/chênh lệch đang hiển thị vào dongGoc (trước khi đổi bộ lọc). */
    private void luuBangVaoDongGoc() {
        for (int i = 0; i < model.getRowCount(); i++) {
            Object maSP = model.getValueAt(i, 1), viTri = model.getValueAt(i, 3);
            for (Object[] g : dongGoc) {
                if (g[1].equals(maSP) && g[3].equals(viTri)) {
                    g[5] = model.getValueAt(i, 5);
                    g[6] = model.getValueAt(i, 6);
                    break;
                }
            }
        }
    }

    /** Hiển thị các dòng của dongGoc thuộc khu đã chọn (CHON_KHU = hiện tất cả). */
    private void locTheoKhu(String maKhu) {
    luuBangVaoDongGoc();
    dangNap = true;
    try {
        model.setRowCount(0);
        int stt = 1;
        for (Object[] g : dongGoc) {
            String viTri = String.valueOf(g[3]);
            boolean khuOk = maKhu.equals(CHON_KHU) || viTri.startsWith(maKhu + "-");
            boolean spOk = spLoc == null || spLoc.contains(String.valueOf(g[1]).toUpperCase());
            if (khuOk && spOk) {
                model.addRow(new Object[]{stt++, g[1], g[2], g[3], g[4], g[5], g[6]});
            }
        }
    } finally {
        dangNap = false;
    }
    capNhatSoMatHangLech();
    }

    /** Chép toàn bộ bảng hiện tại vào dongGoc. */
    private void chepBangVaoDongGoc() {
        dongGoc.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] r = new Object[7];
            for (int c = 0; c < 7; c++) r[c] = model.getValueAt(i, c);
            dongGoc.add(r);
        }
    }

    /** trangThai: 0 = Lưu nháp, 1 = Hoàn tất kiểm kê. */
    private void thucHienLuu(int trangThai) {
        if (!daDangNhap()) {
            JOptionPane.showMessageDialog(view, "Vui lòng đăng nhập để thực hiện chức năng này!");
            return;
        }
        if (view.getTableKetQua().isEditing()) view.getTableKetQua().getCellEditor().stopCellEditing();

        String maPhieu = view.getTxtMaPhieu().getText().trim();
        String ghiChu = view.getTxtGhiChu().getText().trim();
        if (ghiChu.equals("Nhập ghi chú")) ghiChu = "";   // bỏ chữ gợi ý nếu còn

        if (maPhieu.isEmpty() || view.getCbKhuKiemKe().getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã phiếu và chọn Khu!");
            return;
        }
        java.sql.Timestamp ngay;
        try {
            LocalDate ld = LocalDate.parse(view.getTxtNgayKiemKe().getText().trim(),
                    FMT.withResolverStyle(java.time.format.ResolverStyle.STRICT));
            ngay = java.sql.Timestamp.valueOf(ld.atTime(java.time.LocalTime.now().withNano(0)));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Ngày kiểm kê phải có dạng dd/MM/yyyy!");
            return;
        }
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Khu này không có hàng hóa để kiểm kê!");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            Object o = model.getValueAt(i, 5);
            boolean trong = o == null || o.toString().trim().isEmpty();
            if (trong) {
                if (trangThai == 1) {
                    JOptionPane.showMessageDialog(view,
                            "Dòng " + (i + 1) + ": vui lòng nhập SL thực tế trước khi hoàn tất!");
                    return;
                }
                continue;
            }
            try {
                if (Integer.parseInt(o.toString().trim()) < 0) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": SL thực tế không được âm!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": SL thực tế phải là số nguyên!");
                return;
            }
        }

        int maTaiKhoan = dao.layMaTaiKhoan(PhienDangNhap.tenDangNhap);
        if (maTaiKhoan == -1) {
            JOptionPane.showMessageDialog(view, "Không tìm thấy tài khoản đang đăng nhập!");
            return;
        }

        String hoi = trangThai == 1
                ? "Hoàn tất kiểm kê sẽ cập nhật tồn kho theo số thực tế.\nBạn có chắc chắn?"
                : "Bạn có muốn lưu nháp phiếu này?";
        if (JOptionPane.showConfirmDialog(view, hoi, "Xác nhận", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;

        try {
            String maKhu = view.getCbKhuKiemKe().getSelectedItem().toString();
            dao.luuPhieuKiemKe(maPhieu, ngay, maKhu, maTaiKhoan, ghiChu, trangThai, model);
            JOptionPane.showMessageDialog(view, trangThai == 1
                    ? "Đã hoàn tất kiểm kê và cập nhật tồn kho!" : "Lưu nháp thành công!");
            resetForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}